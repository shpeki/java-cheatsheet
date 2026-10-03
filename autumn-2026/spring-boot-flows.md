# Spring Boot Internals: Startup Flow and Request Flow

Two flows, step by step: what happens between `SpringApplication.run(...)` and "ready to serve", and what happens between a TCP connection and your controller's response.

**Version baseline (checked 2026-10-03):** Spring Boot **4.1.1** on Spring Framework **7.0.x**; Boot 4 needs Java 17+, Jakarta EE 11 and a Servlet 6.1 baseline. Class names below are the ones in those docs, not Boot 2/3.

**Evidence tags**
- ✅ confirmed against the live reference docs or Javadoc listed in [Sources](#sources).
- ⚠️ from my knowledge of the source code or from secondary articles; I could not confirm it in the reference docs. Verify before asserting it in an interview.

---

## 1. Container startup flow

### 1.0 Overview

```
main()
 └─ SpringApplication.run(App.class, args)
     ├─ A. Bootstrap: build SpringApplication, load listeners/initializers   (ApplicationStartingEvent)
     ├─ B. Prepare Environment: properties, profiles, config files           (ApplicationEnvironmentPreparedEvent)
     ├─ C. Create ApplicationContext, run initializers, load bean defs       (ApplicationContextInitializedEvent, ApplicationPreparedEvent)
     ├─ D. context.refresh()  ← the Spring container proper, 12 steps (§1.4)
     │     ├─ BeanFactoryPostProcessors  → component scan + auto-configuration
     │     ├─ BeanPostProcessors registered
     │     ├─ onRefresh()                → web server object created
     │     ├─ singletons instantiated    → DI, proxies, @PostConstruct
     │     └─ finishRefresh()            → lifecycle start → Tomcat listens   (ContextRefreshedEvent, WebServerInitializedEvent)
     ├─ E. ApplicationStartedEvent → liveness CORRECT
     ├─ F. ApplicationRunner / CommandLineRunner
     └─ G. ApplicationReadyEvent → readiness ACCEPTING_TRAFFIC
```

✅ Event order (reference docs, "SpringApplication"):

| # | Event | When |
|---|---|---|
| 1 | `ApplicationStartingEvent` | Start of the run, before any processing except registering listeners and initializers |
| 2 | `ApplicationEnvironmentPreparedEvent` | `Environment` known, before the context is created |
| 3 | `ApplicationContextInitializedEvent` | Context prepared and `ApplicationContextInitializer`s called, before any bean definitions are loaded |
| 4 | `ApplicationPreparedEvent` | Just before refresh starts, after bean definitions are loaded |
| — | `WebServerInitializedEvent`, `ContextRefreshedEvent` | During refresh (published between 4 and 5) |
| 5 | `ApplicationStartedEvent` | After the context is refreshed, before runners are called |
| 6 | `AvailabilityChangeEvent` (`LivenessState.CORRECT`) | Right after 5: the app is "live" |
| 7 | `ApplicationReadyEvent` | After all `ApplicationRunner`/`CommandLineRunner` beans have run |
| 8 | `AvailabilityChangeEvent` (`ReadinessState.ACCEPTING_TRAFFIC`) | Right after 7: ready for traffic |
| — | `ApplicationFailedEvent` | Any exception during startup |

Interview-grade consequence: **readiness is only true after your runners finish**, so a slow `CommandLineRunner` delays the Kubernetes readiness probe flipping to UP. Liveness is true as soon as the context has refreshed.

### 1.1 Phase A: bootstrap (before any container exists)

1. `main` calls `SpringApplication.run(Application.class, args)`. The primary source class (here `@SpringBootApplication`) becomes the first configuration class. ✅ (`@SpringBootApplication` is the recommended opt-in to auto-configuration.)
2. The `SpringApplication` constructor deduces the application type (servlet, reactive or none) from the classpath. ⚠️
3. It loads, via `META-INF/spring.factories`, the `ApplicationContextInitializer`s and `ApplicationListener`s that must exist before a context does. ✅ The docs say listeners registered this way are picked up "regardless of the way the application is created", with the key `org.springframework.context.ApplicationListener`. Note that **auto-configuration moved out of `spring.factories`** into a separate imports file (§1.3), but `spring.factories` still exists for these early hooks.
4. `SpringApplicationRunListener`s are created; the built-in one forwards each phase as the application events above (`EventPublishingRunListener`). ⚠️
5. `ApplicationStartingEvent` fires. Shutdown hook registration happens for the context so JVM exit closes it gracefully. ✅ (`@PreDestroy`, `DisposableBean` run at shutdown.)

### 1.2 Phase B: Environment

- A `ConfigurableEnvironment` is created and property sources are assembled. ✅ events say the `Environment` is known at step 2.
- Property sources are ordered by precedence (command-line args beat environment variables beat `application.properties`, and so on); profiles are activated from `spring.profiles.active`. ⚠️ Exact precedence list: see the "Externalized Configuration" page, which I did not fetch.
- `EnvironmentPostProcessor`s run here (they load `application.yml`, add property sources). ✅ Boot 4 moved `EnvironmentPostProcessor` to `org.springframework.boot` (was `org.springframework.boot.env`) and `BootstrapRegistry` to `org.springframework.boot.bootstrap`.
- The banner prints (`spring.main.banner-mode`). ✅
- `ApplicationEnvironmentPreparedEvent` fires. Anything that needs config before beans exist (logging setup, config-server clients) hooks in here.

### 1.3 Phase C: create the context and load the first definitions

1. An `ApplicationContext` is created for the deduced type: for servlet apps it is a `ServletWebServerApplicationContext`. ✅ (class exists, its `onRefresh` creates the web server; §1.5)
2. `ApplicationContextInitializer`s run. → `ApplicationContextInitializedEvent` ✅
3. Your main class is registered as a bean definition. `ApplicationPreparedEvent` fires just before refresh. ✅

**What `@SpringBootApplication` is:** a meta-annotation of three things ⚠️ (standard knowledge; the auto-configuration page confirms `@EnableAutoConfiguration` and `@SpringBootApplication` as the opt-in):
- `@SpringBootConfiguration`: a `@Configuration`.
- `@ComponentScan`: scans the package of the main class and below.
- `@EnableAutoConfiguration`: imports the auto-configuration candidates.

### 1.4 Phase D: `refresh()` in 12 steps

✅ The steps and notes below are from the `AbstractApplicationContext` Javadoc.

| # | Step | What it does | Why it matters |
|---|---|---|---|
| 1 | `prepareRefresh()` | Sets startup date and active flag, initialises property sources | |
| 2 | `obtainFreshBeanFactory()` | Subclass refreshes the internal bean factory | Boot's context uses a `DefaultListableBeanFactory` ⚠️ |
| 3 | `prepareBeanFactory()` | Class loader, standard post-processors | |
| 4 | `postProcessBeanFactory()` | Subclass hook; "no post-processors have run… no beans instantiated yet" | |
| 5 | `invokeBeanFactoryPostProcessors()` | Instantiates and invokes all `BeanFactoryPostProcessor`s, in order. **Before singleton instantiation** | **Component scan and auto-configuration happen here** (see below) |
| 6 | `registerBeanPostProcessors()` | Instantiates and registers all `BeanPostProcessor`s. **Before any application bean** | This is what later wraps beans in proxies |
| 7 | `initMessageSource()` | i18n | |
| 8 | `initApplicationEventMulticaster()` | Uses `SimpleApplicationEventMulticaster` if you define none | |
| 9 | `onRefresh()` | Template hook, "before instantiation of singletons" | **Servlet web server is created here** (§1.5) |
| 10 | `registerListeners()` | Adds `ApplicationListener` beans | |
| 11 | `finishBeanFactoryInitialization()` | Instantiates all remaining (non-lazy) singletons | **All your `@Service`, `@Repository`, `@Controller` beans are built here** |
| 12 | `finishRefresh()` | `LifecycleProcessor.onRefresh()`, publishes `ContextRefreshedEvent` | **`SmartLifecycle` beans start, web server starts accepting connections** (§1.5) |

#### Step 5 in detail: where your beans and the auto-configuration come from

- The key `BeanFactoryPostProcessor` is `ConfigurationClassPostProcessor`. It parses `@Configuration` classes: processes `@ComponentScan` (finds `@Component`, `@Service`, `@Repository`, `@Controller`), `@Import`, `@Bean` methods. ⚠️
- `@EnableAutoConfiguration` imports auto-configurations through a deferred import selector, so they are processed **after** your own configuration. That is why "your bean wins" works: user definitions are registered first, then auto-configuration backs off via `@ConditionalOnMissingBean`. ⚠️ (the mechanism name is from the source; the back-away behaviour is ✅: "if you add your own `DataSource` bean, the default embedded database support backs away.")
- ✅ **Discovery file:** auto-configuration classes are listed in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (one class per line, `#` comments, `$` for nested classes). They are annotated `@AutoConfiguration` (meta-annotated with `@Configuration`) and **must not be component-scanned**.
- ✅ **Ordering:** `@AutoConfiguration(before = ..., after = ...)`, or `@AutoConfigureBefore`, `@AutoConfigureAfter`, `@AutoConfigureOrder`.
- ✅ **Conditions:** `@ConditionalOnClass`, `@ConditionalOnMissingClass`, `@ConditionalOnBean`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`, `@ConditionalOnResource`, `@ConditionalOnWebApplication`. Conditions are evaluated at definition time, before any bean exists.
- ✅ **Debugging:** run with `--debug` to get the conditions report (what matched, what did not, and why). Exclude with `@SpringBootApplication(exclude = ...)`, `excludeName`, or `spring.autoconfigure.exclude`.
- ✅ Example of the chain for web: adding `spring-boot-starter-webmvc` (Boot 4 rename of `spring-boot-starter-web`) puts Spring MVC and Tomcat on the classpath → `@ConditionalOnWebApplication` and `@ConditionalOnClass` conditions match → `WebMvcAutoConfiguration` registers the MVC infrastructure (§2.2).

#### Step 11 in detail: lifecycle of one singleton bean

For each non-lazy singleton, the `BeanFactory` does, in this order:

```
1. Instantiate           (constructor; constructor injection resolves dependencies recursively)
2. Populate properties   (field/setter injection: @Autowired, @Value)
3. Aware callbacks       (BeanNameAware, ApplicationContextAware...)
        "after population of normal bean properties but before an initialization callback"  ✅
4. BeanPostProcessor.postProcessBeforeInitialization()
5. @PostConstruct
6. InitializingBean.afterPropertiesSet()
7. custom init-method (@Bean(initMethod=...))
        5 → 6 → 7 is the documented order when the method names differ  ✅
8. BeanPostProcessor.postProcessAfterInitialization()   ← AOP proxies are created here ⚠️
        → the container stores the PROXY, not the raw object, as the bean
... context running ...
9. On shutdown: @PreDestroy → DisposableBean.destroy() → custom destroy-method  ✅ (same order)
```

**Proxying (step 8)** ✅ details from the AOP docs:
- Interface present → JDK dynamic proxy; no interface → CGLIB subclass proxy. Boot "may, depending on configuration properties, enable class-based proxies by default".
- CGLIB cannot advise `final` classes, `final` methods or `private` methods.
- **Self-invocation (`this.method()`) bypasses the proxy**, so advice (including `@Transactional`) does not run. Fixes: refactor, inject the bean into itself, `AopContext.currentProxy()` (discouraged), or AspectJ weaving.
- Spring 7 adds `@Proxyable(INTERFACES | TARGET_CLASS)` for per-bean control.

**Circular dependencies and ordering** ⚠️: constructor injection cycles fail at startup; Boot disallows circular references by default (`spring.main.allow-circular-references`). Verify the default in the Boot docs before quoting.

**Lazy init** ✅: `spring.main.lazy-initialization=true` or `SpringApplication.setLazyInitialization(true)`; beans are created on first use (faster start, problems appear later). `@Lazy(false)` opts a bean out.

### 1.5 The embedded web server (Tomcat) in the flow

Two-phase start. ⚠️ (from secondary articles and source knowledge; the reference docs confirm Tomcat and Jetty are supported and that the server listens on 8080 by default, not the internals)

```
refresh() step 9  onRefresh()      → ServletWebServerApplicationContext.createWebServer():
                                       - finds the ServletWebServerFactory bean (TomcatServletWebServerFactory)
                                       - builds the Tomcat object, registers ServletContextInitializer beans
                                         (DispatcherServlet, Filter beans, Servlet listeners)
                                       - Tomcat is created but NOT listening yet
refresh() step 11 singletons       → all your beans exist, DispatcherServlet bean initialised
refresh() step 12 finishRefresh()  → LifecycleProcessor starts SmartLifecycle beans by phase
                                       - WebServerStartStopLifecycle (phase Integer.MAX_VALUE - 1) → webServer.start()
                                       - Tomcat now listens on port 8080
                                       - WebServerInitializedEvent, then ContextRefreshedEvent
```

Why this matters: **no traffic can arrive before your beans are built**, because the port is opened last.

✅ Facts from the Boot web docs:
- Any `Servlet`, `Filter` or servlet `*Listener` that is a Spring bean is registered with the embedded container automatically. One servlet maps to `/`; filters map to `/*`.
- For full control use `ServletRegistrationBean`, `FilterRegistrationBean`, `ServletListenerRegistrationBean` (or `@ServletRegistration` / `@FilterRegistration`).
- Filter order: `@Order` / `Ordered`, or `FilterRegistrationBean.setOrder(int)`. Do not put a body-reading filter at `Ordered.HIGHEST_PRECEDENCE`. `logging.level.web=debug` prints the filter order.
- Embedded containers do **not** run `ServletContainerInitializer` or `WebApplicationInitializer`; use `ServletContextInitializer` beans.

### 1.6 Phases E–G: after the context is up

1. `ApplicationStartedEvent`, then liveness = `CORRECT`. ✅
2. `ApplicationRunner` (parsed `ApplicationArguments`, preferred) and `CommandLineRunner` (raw `String[]`) run, ordered with `@Order`. ✅
3. `ApplicationReadyEvent`, then readiness = `ACCEPTING_TRAFFIC`. ✅
4. If anything threw: `ApplicationFailedEvent`, the context is closed, the process exits non-zero. ✅

### 1.7 Shutdown (the mirror image)

✅ The JVM shutdown hook closes the context; `@PreDestroy` / `DisposableBean` run; `ExitCodeGenerator` beans can set the exit code. With virtual threads enabled (`spring.threads.virtual.enabled=true`, Java 21+), they are daemon threads, so set `spring.main.keep-alive=true` if only `@Scheduled` work would otherwise keep the JVM alive. ⚠️ Graceful web-server shutdown (`server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase`): not fetched, verify.

### 1.8 Measuring startup

✅ `ApplicationStartup` / `StartupStep` track the sequence; Boot ships `BufferingApplicationStartup`:

```java
SpringApplication app = new SpringApplication(MyApplication.class);
app.setApplicationStartup(new BufferingApplicationStartup(2048));
app.run(args);
```

Drain it through the actuator `startup` endpoint ⚠️ (I did not fetch the Actuator page).

---

## 2. HTTP request flow (Spring MVC, servlet stack)

### 2.0 Overview

```
Client ──TCP──► Tomcat Connector (acceptor → poller → worker thread)         ⚠️ Tomcat internals
                    │
                    ▼
        Servlet FilterChain  (ordered filters: characterEncoding, requestContext,
        │                      DelegatingFilterProxy → Spring Security FilterChainProxy, your Filters)
                    ▼
            DispatcherServlet.service() → doDispatch()
                    │
   ┌────────────────┼──────────────────────────────────────────────┐
   │ 1 bind WebApplicationContext, LocaleResolver                  │
   │ 2 multipart check (MultipartResolver)                         │
   │ 3 HandlerMapping → HandlerExecutionChain (handler + interceptors)
   │ 4 interceptors.preHandle()                                    │
   │ 5 HandlerAdapter.handle() → argument resolvers, message       │
   │   converters, @Valid → YOUR CONTROLLER METHOD                  │
   │       └─ service proxy → @Transactional → repository → DB      │
   │ 6 return value handling (@ResponseBody → HttpMessageConverter) │
   │ 7 interceptors.postHandle()                                   │
   │ 8 view rendering (only if a model/view was returned)           │
   │ 9 exceptions → HandlerExceptionResolver chain                  │
   │10 interceptors.afterCompletion()                              │
   └────────────────────────────────────────────────────────────────┘
                    ▼
        response travels back out through the filters
```

### 2.1 Before Spring: connector and filters

- ⚠️ Tomcat accepts the connection on its connector, a worker thread from the pool (`server.tomcat.threads.max`) handles the request, and the Servlet container builds `HttpServletRequest` / `HttpServletResponse`. With `spring.threads.virtual.enabled=true` ✅ Boot runs request handling on virtual threads (Java 21+).
- The request goes through the **servlet `FilterChain`**, in order. ✅ Filter beans are registered automatically and ordered by `@Order`/`Ordered`/`FilterRegistrationBean.setOrder`.
- **Spring Security** sits here ✅ (Security architecture docs):

```
Servlet FilterChain
  → DelegatingFilterProxy        bridge from the container's filter lifecycle to a Spring bean (looks the bean up lazily)
    → FilterChainProxy           Spring Security's single entry point; applies HttpFirewall, clears SecurityContext
      → SecurityFilterChain      the FIRST chain whose RequestMatcher matches is used
        → DisableEncodeUrlFilter
        → WebAsyncManagerIntegrationFilter
        → SecurityContextHolderFilter     loads SecurityContext (e.g. from session)
        → HeaderWriterFilter
        → CsrfFilter
        → LogoutFilter
        → UsernamePasswordAuthenticationFilter
        → BasicAuthenticationFilter
        → RequestCacheAwareFilter
        → SecurityContextHolderAwareRequestFilter
        → AnonymousAuthenticationFilter
        → ExceptionTranslationFilter     AuthenticationException → login/401, AccessDeniedException → 403
        → AuthorizationFilter            authorizeHttpRequests rules decided here
  → DispatcherServlet
```

(That is the default order for the docs' example with csrf, httpBasic, formLogin; a JWT resource-server chain swaps in `BearerTokenAuthenticationFilter` ⚠️.)

Consequences worth saying out loud:
- Security runs **before** `DispatcherServlet`, so a request rejected by Security never reaches `@ControllerAdvice` (its exceptions are handled by `ExceptionTranslationFilter`, not by your advice).
- `@PreAuthorize` is a different layer: method security via an AOP proxy (§2.5), after the controller is selected.

### 2.2 `DispatcherServlet`: what Boot wired up for it

The `DispatcherServlet` is one servlet bean, registered by Boot, mapped to `/`. ⚠️ (`DispatcherServletRegistrationBean`, a Boot class, was not in the docs I fetched). It owns a `WebApplicationContext`, which in Boot is simply the application context. ✅ (the framework docs describe the root/child hierarchy; Boot uses a single context.)

At init it detects the **special beans** ✅ and falls back to defaults if you define none:

| Bean type | Role | Typical implementation |
|---|---|---|
| `HandlerMapping` | request → handler + interceptors | `RequestMappingHandlerMapping` (`@RequestMapping`), `SimpleUrlHandlerMapping` |
| `HandlerAdapter` | invokes the handler regardless of its type | the one for annotated methods |
| `HandlerExceptionResolver` | maps exceptions to responses/views | `@ExceptionHandler` support, `ResponseStatusException` etc. ⚠️ |
| `ViewResolver` | view name → `View` | `ContentNegotiatingViewResolver`, `BeanNameViewResolver` (from `WebMvcAutoConfiguration`) ✅ |
| `LocaleResolver` | locale/time zone | |
| `MultipartResolver` | multipart parsing | |
| `FlashMapManager` | attributes across redirects | |

`WebMvcAutoConfiguration` ✅ provides: view resolvers, static resource serving (incl. WebJars), `Converter`/`GenericConverter`/`Formatter` registration, `HttpMessageConverters`, `MessageCodesResolver`, static `index.html`, `ConfigurableWebBindingInitializer`. **Do not add `@EnableWebMvc`** with Boot; customise with a `WebMvcConfigurer`.

At startup `RequestMappingHandlerMapping` scans every controller bean, builds a map of (path pattern, method, params, consumes, produces) → handler method. ⚠️ That is why a duplicate mapping fails at startup, not at request time.

### 2.3 Inside `doDispatch`: the documented sequence

✅ From the framework docs ("Processing sequence"):

1. The `WebApplicationContext` is bound as a request attribute (`DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE`).
2. The locale resolver is bound to the request.
3. If a `MultipartResolver` is configured and the request has multiparts, it is wrapped in `MultipartHttpServletRequest`.
4. A handler is searched for. If found, the execution chain (preprocessors, postprocessors, controller) runs to prepare a model. For annotated controllers the response may be rendered inside the `HandlerAdapter` instead of returning a view.
5. If a model is returned, the view is rendered. If not (a pre/post-processor already fulfilled the request), no view.
6. `HandlerExceptionResolver` beans resolve exceptions thrown during processing.

Interceptor callback order ⚠️ (standard `HandlerInterceptor` contract, not in the page I fetched): `preHandle` (in registration order; return `false` stops the chain) → handler → `postHandle` (reverse order) → view render → `afterCompletion` (reverse order, always runs for interceptors whose `preHandle` returned `true`, even after an exception).

### 2.4 Calling your controller method

For an annotated method, the `HandlerAdapter` builds the call ✅:

1. **Arguments** are resolved by `HandlerMethodArgumentResolver`s: `@PathVariable`, `@RequestParam`, `@RequestHeader`, `@CookieValue`, `@RequestBody`, `HttpEntity<B>`, `@RequestPart`, `@ModelAttribute`, `Principal`, `HttpServletRequest`… A simple type with no annotation is treated as `@RequestParam`; anything else as `@ModelAttribute`.
2. **`@RequestBody`**: body converted by an `HttpMessageConverter`, chosen by `Content-Type` and the target type.
3. **Conversion/formatting**: built-in converters, registered `Converter` beans, `@DateTimeFormat`, `@NumberFormat`.
4. **Validation**: `@Valid` triggers Bean Validation; a `BindingResult` placed right after the argument captures errors, otherwise a validation exception is thrown (handled in §2.6).
5. The method is invoked. Return values go through `HandlerMethodReturnValueHandler`s: `@ResponseBody`/`@RestController` → `HttpMessageConverter`; `ResponseEntity<T>` controls status, headers, body; `String` = view name; `void` = implicit view name.

**Jackson in Boot 4 / Framework 7** ✅: Jackson 3 is the preferred library (`com.fasterxml.jackson` → `tools.jackson`). The JSON converter is `JacksonJsonHttpMessageConverter`; `MappingJackson2HttpMessageConverter` is **deprecated since 7.0**. Customise converters with `ServerHttpMessageConvertersCustomizer` / `ClientHttpMessageConvertersCustomizer` beans (any `HttpMessageConverter` bean is also added). Boot 4 renames: `Jackson2ObjectMapperBuilderCustomizer` → `JsonMapperBuilderCustomizer`; properties under `spring.jackson.json.read` / `.write`.

### 2.5 Below the controller: service, transaction, repository

Your controller usually calls a `@Service`. What it holds is a **proxy** (created in §1.4 step 8):

```
controller ──► CGLIB/JDK proxy ──► TransactionInterceptor ──► real service ──► repository (Spring Data proxy) ──► DB
                  (also: @PreAuthorize, @Cacheable, @Async, @Retryable… interceptors, ordered)
```

✅ `@Transactional` facts:
- Applied by the AOP proxy; the interceptor is `TransactionInterceptor`. `@EnableTransactionManagement` activates it (Boot auto-configures it ⚠️).
- Since 6.0: public methods always intercepted; protected/package-visible with class-based proxies; interface proxies only public interface methods; private never.
- **Self-invocation does not open a transaction.**
- Default **propagation = `REQUIRED`**; default isolation `DEFAULT`; read-write; **rollback only on `RuntimeException` and `Error`, not checked exceptions** (6.2 lets you change the global default via `@EnableTransactionManagement(rollbackOn = ...)`; per method `rollbackFor`/`noRollbackFor`).

⚠️ Not in the docs I fetched, but standard and worth knowing: with Spring Data JPA, `open-in-view` (`spring.jpa.open-in-view`) keeps the `EntityManager` open for the whole request, which is why lazy loading can "work" in the controller and why it is a common N+1/connection-holding trap. Verify the default value (`true` with a startup warning) in the Boot docs.

### 2.6 Errors and the response

✅ Resolution when something throws:
- `HandlerExceptionResolver`s try to map the exception: `@ExceptionHandler` methods (local, or global in a `@ControllerAdvice`), `ResponseStatusException` ⚠️.
- If nothing resolves it, the exception propagates to the container, which forwards to **`/error`**. Boot maps `/error` globally: JSON for machine clients, the "whitelabel" HTML page for browsers. Customise with `@ControllerAdvice` (extend `ResponseEntityExceptionHandler`), an `ErrorViewResolver`, an `ErrorController`, or extend `BasicErrorController` ✅.
- `spring.mvc.problemdetails.enabled=true` ✅ switches to RFC 9457 `application/problem+json`.
- Filters that must see the error dispatch need `DispatcherType.ERROR` on their registration ✅.
- Custom static error pages: `public/error/404.html`, templates `templates/error/5xx.ftlh` ✅.

### 2.7 The response travels back

1. `@ResponseBody` / `ResponseEntity` is written by a converter (§2.4), and the status line and headers go out. ⚠️ (If the response is committed, later exceptions can no longer change the status. A good "why did my error handler not work" answer.)
2. `postHandle`/`afterCompletion` interceptors run (§2.3).
3. Control unwinds back through the filter chain (post-`chain.doFilter` code, e.g. response headers/logging), then Tomcat flushes to the client. Security's `FilterChainProxy` clears the `SecurityContext` ✅ on the way out.

### 2.8 Async and virtual threads (variations)

- ⚠️ Returning `Callable`, `DeferredResult`, `CompletableFuture` or `SseEmitter` releases the container thread; the result is re-dispatched. `WebAsyncManagerIntegrationFilter` ✅ (in the Security chain) carries the `SecurityContext` across that boundary.
- ✅ `spring.threads.virtual.enabled=true` (Java 21+): request threads become virtual. See `conga-gaps.md` §7.1.1 for pinning and the JEP 491 change in Java 24.
- WebFlux (reactive stack) is a different pipeline (`DispatcherHandler`, `WebFilter`); not covered here. ⚠️

---

## 3. One request, end to end (worked example)

`POST /orders` with a JSON body and `Authorization: Bearer ...`, controller `OrderController.create(@Valid @RequestBody OrderRequest r)` → `OrderService.place(...)` (`@Transactional`) → `OrderRepository.save(...)`.

| # | Step | Component |
|---|---|---|
| 1 | Connection accepted, worker (or virtual) thread assigned | Tomcat ⚠️ |
| 2 | Encoding/context filters | servlet filters |
| 3 | `DelegatingFilterProxy` → `FilterChainProxy` picks `SecurityFilterChain` for `/orders` | Spring Security ✅ |
| 4 | Token validated, `SecurityContext` populated; `AuthorizationFilter` checks rules | Security ✅ (bearer filter ⚠️) |
| 5 | `DispatcherServlet.doDispatch` | MVC ✅ |
| 6 | `RequestMappingHandlerMapping` finds `OrderController#create` + interceptors | ✅ |
| 7 | `preHandle` on interceptors | ⚠️ |
| 8 | `@RequestBody`: `JacksonJsonHttpMessageConverter` reads JSON → `OrderRequest` | ✅ |
| 9 | `@Valid`: Bean Validation; on failure an exception goes to the exception resolvers (400) | ✅ |
| 10 | Controller method runs, calls `orderService` (a proxy) | |
| 11 | `TransactionInterceptor` begins a transaction (`REQUIRED`) | ✅ |
| 12 | `OrderService.place` runs, `orderRepository.save` → JPA/JDBC → DB | |
| 13 | Method returns → commit; `RuntimeException` → rollback, checked exception → commit by default | ✅ |
| 14 | Return value `ResponseEntity<Order>` → converter serialises JSON, sets status/headers | ✅ |
| 15 | `postHandle`, `afterCompletion` | ⚠️ |
| 16 | Unwind through Security (context cleared) and servlet filters; Tomcat sends the response | ✅ / ⚠️ |

---

## 4. Likely interview questions with short answers

1. **What happens when you call `SpringApplication.run`?** Phases A–G in §1.0: environment, context creation, `refresh()` (scan + auto-config in BFPPs, BPPs, singletons, then start the web server last), runners, ready.
2. **Where does auto-configuration come from?** The `AutoConfiguration.imports` file in each starter's jar, imported by `@EnableAutoConfiguration`, filtered by `@Conditional…`, processed after user config, so user beans win.
3. **How does Boot know to start Tomcat?** Classpath + `@ConditionalOnWebApplication` → a `ServletWebServerFactory` bean; `onRefresh` creates it, `finishRefresh` starts it (⚠️ internals).
4. **`BeanFactoryPostProcessor` vs `BeanPostProcessor`?** The first edits bean *definitions* before any singleton exists (step 5); the second wraps bean *instances* around init (step 6 registers, step 11 applies). Proxies come from the second kind.
5. **Order of `@PostConstruct`, `afterPropertiesSet`, `init-method`?** In that order ✅.
6. **Why does `@Transactional` not work on a private method / on `this.foo()`?** Proxy-based AOP: private not intercepted, self-invocation bypasses the proxy ✅.
7. **What is the difference between a filter and an interceptor?** Filter = servlet spec, runs before `DispatcherServlet`, sees every request (Security lives here). Interceptor = Spring MVC, runs after handler mapping, knows the handler method.
8. **Why can `@ControllerAdvice` not handle a 401?** Security filters run before `DispatcherServlet`; `ExceptionTranslationFilter` handles it ✅.
9. **What does readiness vs liveness mean in the startup events?** Liveness at `ApplicationStartedEvent`, readiness after runners ✅.
10. **How would you find what slows startup?** `--debug` conditions report, `BufferingApplicationStartup` + actuator, lazy init as a trade-off, AOT/native as an option ✅/⚠️.

---

## 5. Gaps in this document (to close before the interview)

- Boot **AOT/GraalVM** startup path (processing at build time instead of `refresh()`): not covered.
- **Externalized configuration** precedence list and `@ConfigurationProperties` binding: referenced, not fetched.
- **Graceful shutdown** properties and **Actuator** `startup`/health groups: not fetched.
- **Spring Data JPA** repository proxy creation and `open-in-view`: only mentioned.
- **WebFlux** pipeline: out of scope here.
- Anything tagged ⚠️ above: confirm in the Boot/Framework reference or by stepping through `SpringApplication.run` and `AbstractApplicationContext.refresh` in a debugger (best exercise: put breakpoints on `refresh()`, `createWebServer`, `DispatcherServlet.doDispatch`, `TransactionInterceptor.invoke`).

---

## Sources

All fetched 2026-10-03. Anything not backed by one of these is tagged ⚠️ in the text.

- Spring Boot reference, SpringApplication (events, availability, runners, lazy init, shutdown, startup tracking, spring.factories listeners): https://docs.spring.io/spring-boot/reference/features/spring-application.html
- Spring Boot reference, Auto-configuration (opt-in, back-away, exclusion, `--debug`): https://docs.spring.io/spring-boot/reference/using/auto-configuration.html
- Spring Boot reference, Creating your own auto-configuration (`AutoConfiguration.imports`, `@AutoConfiguration`, ordering, conditions): https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html
- Spring Boot reference, Servlet web applications (filters, error handling, `WebMvcAutoConfiguration`, converters): https://docs.spring.io/spring-boot/reference/web/servlet.html
- Spring Boot 4.0 Migration Guide (modular starters, Jackson 3, package moves, baselines): https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
- Spring Framework `AbstractApplicationContext` Javadoc (`refresh()` steps): https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/support/AbstractApplicationContext.html
- Spring Framework reference, bean lifecycle callbacks: https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html
- Spring Framework reference, AOP proxying mechanisms: https://docs.spring.io/spring-framework/reference/core/aop/proxying.html
- Spring Framework reference, `@Transactional`: https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html
- Spring Framework reference, DispatcherServlet processing sequence: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/sequence.html
- Spring Framework reference, special bean types: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/special-bean-types.html
- Spring Framework reference, controller method arguments and return values: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/arguments.html
- Spring Framework reference, context hierarchy: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/context-hierarchy.html
- Spring Security reference, servlet architecture (`DelegatingFilterProxy`, `FilterChainProxy`, filter order): https://docs.spring.io/spring-security/reference/servlet/architecture.html
- Spring blog, Introducing Jackson 3 support in Spring: https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring/
- `JacksonJsonHttpMessageConverter` Javadoc: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/converter/json/JacksonJsonHttpMessageConverter.html
- Release status (Boot 4.1.x, Framework 7.0.x): https://github.com/spring-projects/spring-boot/releases and https://spring.io/projects/release-highlights/
- Secondary, used only for the ⚠️ embedded-server two-phase start (not fetched in full, search summary only): https://dev.to/silver_dev/spring-boot-under-the-hood-part-7-from-embedded-tomcat-to-your-restcontroller-33kk
