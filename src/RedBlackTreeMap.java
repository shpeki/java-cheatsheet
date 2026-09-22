import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * <h2>RedBlackTreeMap</h2>
 *
 * <p>A from-scratch, educational re-implementation of the core of
 * {@link java.util.TreeMap}, backed by a <b>red-black tree</b> — the exact same
 * underlying data structure {@code java.util.TreeMap} itself uses internally
 * (see the JDK source: {@code java.util.TreeMap} keeps its entries in a
 * red-black tree since Java 1.2, based on the algorithm in Cormen, Leiserson,
 * Rivest and Stein's <i>Introduction to Algorithms</i> — usually called
 * "CLRS").</p>
 *
 * <h3>Why not just a plain Binary Search Tree (BST)?</h3>
 * <p>A plain BST gives you {@code O(log n)} search/insert/delete <b>only if
 * the tree happens to stay balanced</b>. If you insert already-sorted data
 * (1, 2, 3, 4, 5, ...) into a plain BST, every new node becomes the right
 * child of the previous one — the "tree" degenerates into a linked list, and
 * every operation becomes {@code O(n)}. A red-black tree fixes this by
 * enforcing extra rules on every insert/delete that keep the tree
 * approximately balanced <b>no matter what order keys arrive in</b>,
 * guaranteeing {@code O(log n)} for every operation, always.</p>
 *
 * <h3>The five red-black properties</h3>
 * <p>Every node is colored either RED or BLACK. A tree is a valid red-black
 * tree if and only if all five of these hold:</p>
 * <ol>
 *   <li><b>Every node is either red or black.</b> (Trivially true — it's a boolean.)</li>
 *   <li><b>The root is always black.</b></li>
 *   <li><b>Every leaf (the sentinel {@code NIL} node, see below) is black.</b></li>
 *   <li><b>If a node is red, both its children are black.</b>
 *       (Equivalently: no two red nodes can appear in a row on any path —
 *       "no red-red violations".)</li>
 *   <li><b>Every path from a given node down to any of its descendant leaves
 *       (the {@code NIL} sentinels) contains the same number of black nodes.</b>
 *       This count is called the node's <i>black-height</i>.</li>
 * </ol>
 *
 * <p>Property 5 is the one that actually delivers the balance guarantee: it
 * forces the longest possible root-to-leaf path (alternating red/black) to be
 * at most twice as long as the shortest possible path (all black), which is
 * what caps the tree height at {@code O(log n)} and therefore every
 * operation's running time at {@code O(log n)} as well.</p>
 *
 * <h3>The sentinel {@code NIL} node trick</h3>
 * <p>Instead of using Java {@code null} to represent "no child" / "no
 * parent", this implementation (like CLRS, and like the real JDK) uses a
 * single shared sentinel node, {@link #NIL}, colored BLACK, to stand in for
 * every leaf and for the parent of the root. This removes almost every
 * {@code null}-check from the rotation and fix-up code below — every node,
 * including "empty" ones, has a real {@code .color}, {@code .left},
 * {@code .right} and {@code .parent} you can safely read. It also lets the
 * deletion fix-up algorithm temporarily "park" a real position in
 * {@code NIL.parent} while it walks back up the tree — a classic, if slightly
 * mind-bending, trick explained in detail at {@link #deleteFixup}.</p>
 *
 * <h3>Complexity summary</h3>
 * <table border="1" cellpadding="4" summary="complexity">
 *   <tr><th>Operation</th><th>Time</th><th>Why</th></tr>
 *   <tr><td>{@link #get}</td><td>O(log n)</td><td>simple BST descent, bounded by tree height</td></tr>
 *   <tr><td>{@link #put}</td><td>O(log n)</td><td>BST insert (O(log n)) + fix-up walk (O(log n))</td></tr>
 *   <tr><td>{@link #remove}</td><td>O(log n)</td><td>BST delete (O(log n)) + fix-up walk (O(log n))</td></tr>
 *   <tr><td>{@link #firstKey} / {@link #lastKey}</td><td>O(log n)</td><td>walk all the way left / right</td></tr>
 *   <tr><td>{@link #ceilingKey} / {@link #floorKey} / {@link #higherKey} / {@link #lowerKey}</td><td>O(log n)</td><td>single root-to-leaf descent, remembering the best candidate seen</td></tr>
 *   <tr><td>{@link #keysInOrder}</td><td>O(n)</td><td>must visit every node once</td></tr>
 * </table>
 *
 * <p>This class intentionally implements only a useful subset of
 * {@code java.util.TreeMap}'s API (enough to demonstrate — and let you reuse —
 * every interesting operation: search, insert, delete, and the
 * "nearest key" navigation methods that make a red-black tree perfect for
 * things like a consistent-hashing ring). It is <b>not</b> thread-safe, and
 * (also like {@code java.util.TreeMap}) does not accept a {@code null} key,
 * since keys must be mutually comparable via {@link Comparable#compareTo}.</p>
 *
 * @param <K> the key type; must implement {@link Comparable}
 * @param <V> the value type
 */
public class RedBlackTreeMap<K extends Comparable<K>, V> {

    /** Node color constant. */
    private static final boolean RED = true;

    /** Node color constant. */
    private static final boolean BLACK = false;

    /**
     * A single node in the tree. Package-private (well, private-inner-class)
     * because callers only ever interact with keys and values, never nodes
     * directly — exactly like {@code java.util.TreeMap.Entry}.
     */
    private final class Node {
        K key;
        V value;
        Node left;
        Node right;
        Node parent;
        boolean color;

        Node(K key, V value, boolean color) {
            this.key = key;
            this.value = value;
            this.color = color;
            // Freshly created nodes point at the sentinel until linked into
            // the tree properly — never at Java's `null`.
            this.left = NIL;
            this.right = NIL;
            this.parent = NIL;
        }
    }

    /**
     * The shared sentinel node standing in for every "empty" leaf and for
     * the root's parent. Always BLACK (property 3 requires every leaf to be
     * black, and NIL represents all the leaves at once). See the class
     * Javadoc section "The sentinel NIL node trick" for why this exists.
     */
    private final Node NIL = new Node(null, null, BLACK);

    /** The tree's root. Starts out pointing at the sentinel (empty tree). */
    private Node root = NIL;

    /** Number of real (non-sentinel) key/value entries currently stored. */
    private int size = 0;

    // ------------------------------------------------------------------
    // Public query API
    // ------------------------------------------------------------------

    /** @return the number of key/value mappings currently in this map. */
    public int size() {
        return size;
    }

    /** @return {@code true} if this map contains no key/value mappings. */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Looks up the value associated with {@code key}.
     *
     * @param key the key to search for; must not be {@code null}
     * @return the associated value, or {@code null} if the key is not present
     */
    public V get(K key) {
        Node node = findNode(key);
        return node == NIL ? null : node.value;
    }

    /**
     * @param key the key to test for
     * @return {@code true} if this map has a mapping for {@code key}
     */
    public boolean containsKey(K key) {
        return findNode(key) != NIL;
    }

    /**
     * Plain iterative BST search — ignores color entirely. Color only
     * matters for keeping the tree balanced during {@link #put} and
     * {@link #remove}; a lookup is just "go left if smaller, right if
     * bigger, done if equal", exactly like in an unbalanced BST.
     *
     * @return the matching node, or the shared {@link #NIL} sentinel if
     *         {@code key} is not present (never Java {@code null})
     */
    private Node findNode(K key) {
        Node current = root;
        while (current != NIL) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return current;
            }
            current = (cmp < 0) ? current.left : current.right;
        }
        return NIL;
    }

    // ------------------------------------------------------------------
    // Navigation: the "nearest key" operations that make this structure
    // useful for things like a consistent-hashing ring (find the key at or
    // after a given hash, wrapping around at the end).
    // ------------------------------------------------------------------

    /**
     * @return the smallest key in the map
     * @throws NoSuchElementException if the map is empty
     */
    public K firstKey() {
        if (root == NIL) {
            throw new NoSuchElementException("map is empty");
        }
        return minimum(root).key;
    }

    /**
     * @return the largest key in the map
     * @throws NoSuchElementException if the map is empty
     */
    public K lastKey() {
        if (root == NIL) {
            throw new NoSuchElementException("map is empty");
        }
        return maximum(root).key;
    }

    /**
     * Finds the smallest key that is <b>greater than or equal to</b>
     * {@code key} (the "ceiling"). This is exactly the operation a
     * consistent-hashing ring needs: "which server is at or after this hash
     * position, walking clockwise?"
     *
     * <p>How it works: walk down from the root exactly like a normal search,
     * but every time we step <i>left</i> (because the current node's key is
     * {@code >= key}), that node becomes our best candidate so far, since
     * everything in its right subtree — where we're walking away from — is
     * even bigger. If we ever land exactly on {@code key}, that's the best
     * possible ceiling, so we can return immediately.</p>
     *
     * @param key the key to search from
     * @return the ceiling key, or {@code null} if every key in the map is
     *         smaller than {@code key}
     */
    public K ceilingKey(K key) {
        Node current = root;
        Node best = NIL;
        while (current != NIL) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return current.key;
            } else if (cmp < 0) {
                best = current;          // current.key > key: a candidate
                current = current.left;  // look for something even closer
            } else {
                current = current.right; // current.key < key: not good enough
            }
        }
        return best == NIL ? null : best.key;
    }

    /**
     * Finds the largest key that is <b>less than or equal to</b> {@code key}
     * (the "floor") — the mirror image of {@link #ceilingKey}.
     *
     * @param key the key to search from
     * @return the floor key, or {@code null} if every key in the map is
     *         bigger than {@code key}
     */
    public K floorKey(K key) {
        Node current = root;
        Node best = NIL;
        while (current != NIL) {
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                return current.key;
            } else if (cmp > 0) {
                best = current;           // current.key < key: a candidate
                current = current.right;  // look for something even closer
            } else {
                current = current.left;   // current.key > key: not good enough
            }
        }
        return best == NIL ? null : best.key;
    }

    /**
     * Finds the smallest key <b>strictly greater than</b> {@code key}.
     * Same walk as {@link #ceilingKey}, except landing exactly on
     * {@code key} does not count — we must keep looking to its right.
     *
     * @param key the key to search from
     * @return the next-higher key, or {@code null} if none exists
     */
    public K higherKey(K key) {
        Node current = root;
        Node best = NIL;
        while (current != NIL) {
            int cmp = key.compareTo(current.key);
            if (cmp < 0) {
                best = current;
                current = current.left;
            } else {
                // cmp >= 0: current.key <= key, so it (and its left subtree,
                // all <= current.key) can never be the answer — go right.
                current = current.right;
            }
        }
        return best == NIL ? null : best.key;
    }

    /**
     * Finds the largest key <b>strictly less than</b> {@code key} — the
     * mirror image of {@link #higherKey}.
     *
     * @param key the key to search from
     * @return the next-lower key, or {@code null} if none exists
     */
    public K lowerKey(K key) {
        Node current = root;
        Node best = NIL;
        while (current != NIL) {
            int cmp = key.compareTo(current.key);
            if (cmp > 0) {
                best = current;
                current = current.right;
            } else {
                current = current.left;
            }
        }
        return best == NIL ? null : best.key;
    }

    /** @return the node holding the smallest key in the subtree rooted at {@code x} */
    private Node minimum(Node x) {
        while (x.left != NIL) {
            x = x.left;
        }
        return x;
    }

    /** @return the node holding the largest key in the subtree rooted at {@code x} */
    private Node maximum(Node x) {
        while (x.right != NIL) {
            x = x.right;
        }
        return x;
    }

    /**
     * Returns every key in ascending order. Backed by a simple in-order
     * traversal (left subtree, then this node, then right subtree), which
     * visits a BST's keys in sorted order by definition — that invariant is
     * what makes a binary <b>search</b> tree "sorted" at all.
     *
     * @return a new, independent list of all keys, ascending
     */
    public List<K> keysInOrder() {
        List<K> result = new ArrayList<>(size);
        inorderCollect(root, result);
        return result;
    }

    private void inorderCollect(Node node, List<K> out) {
        if (node == NIL) {
            return;
        }
        inorderCollect(node.left, out);
        out.add(node.key);
        inorderCollect(node.right, out);
    }

    // ------------------------------------------------------------------
    // Insertion
    // ------------------------------------------------------------------

    /**
     * Associates {@code value} with {@code key}. If the key is already
     * present, its value is replaced.
     *
     * <p>This is a two-phase operation, exactly like the CLRS textbook
     * algorithm:</p>
     * <ol>
     *   <li><b>Ordinary BST insert:</b> walk down from the root comparing
     *       keys, and hang the new node off whichever {@code NIL} leaf we
     *       fall off of. The new node is colored RED.</li>
     *   <li><b>Fix-up ({@link #insertFixup}):</b> coloring the new node RED
     *       preserves property 5 (black-height) automatically — we haven't
     *       added any black nodes — but it might just have created a
     *       red-red violation (property 4) if its parent was also RED. The
     *       fix-up walks back up the tree resolving that violation, using
     *       only O(log n) rotations/recolorings, until the whole tree is
     *       valid again.</li>
     * </ol>
     *
     * @param key   the key; must not be {@code null}
     * @param value the value to associate with it
     * @return the previous value associated with {@code key}, or
     *         {@code null} if it was not previously present
     */
    public V put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("RedBlackTreeMap does not permit null keys");
        }

        Node parent = NIL;
        Node current = root;
        while (current != NIL) {
            parent = current;
            int cmp = key.compareTo(current.key);
            if (cmp == 0) {
                // Key already exists: just swap the value, no structural
                // change needed, so no fix-up needed either.
                V old = current.value;
                current.value = value;
                return old;
            } else if (cmp < 0) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        Node newNode = new Node(key, value, RED);
        newNode.parent = parent;
        if (parent == NIL) {
            root = newNode;               // tree was empty
        } else if (key.compareTo(parent.key) < 0) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        size++;
        insertFixup(newNode);
        return null;
    }

    /**
     * Restores the red-black properties after a plain BST insert placed a
     * new RED node {@code z}. Only property 4 (no red node has a red child)
     * can possibly be broken at this point, and only if {@code z}'s parent
     * is also RED — so the loop only runs while that's true.
     *
     * <p>At the top of every loop iteration, {@code z} is RED and the only
     * possible violation is between {@code z} and {@code z.parent}. There
     * are two symmetric halves (parent is a left child / parent is a right
     * child); only the "parent is a left child" half is commented in detail
     * below, the other half is its mirror image.</p>
     *
     * <p>Within that half, everything hinges on the color of {@code z}'s
     * <b>uncle</b> (z's parent's sibling):</p>
     * <ul>
     *   <li><b>Case 1 — uncle is RED:</b> we can simply recolor: flip
     *       parent and uncle to BLACK and grandparent to RED. This pushes
     *       the "extra red" problem up two levels (z becomes its own
     *       grandparent for the next loop check) without changing any
     *       black-heights. No rotation needed.</li>
     *   <li><b>Case 2 — uncle is BLACK, z is a "zig-zag" (right child of a
     *       left child):</b> a single left-rotation on the parent turns
     *       this into Case 3, in the same iteration.</li>
     *   <li><b>Case 3 — uncle is BLACK, z is a "straight line" (left child
     *       of a left child):</b> recolor parent and grandparent, then a
     *       single right-rotation on the grandparent. This fixes the
     *       violation completely and terminates the loop (the new subtree
     *       root is BLACK).</li>
     * </ul>
     *
     * @param z the freshly inserted RED node
     */
    private void insertFixup(Node z) {
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                Node uncle = z.parent.parent.right;
                if (uncle.color == RED) {
                    // Case 1: recolor and move the problem up the tree.
                    z.parent.color = BLACK;
                    uncle.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.right) {
                        // Case 2: rotate to turn the zig-zag into a
                        // straight line, then fall through to Case 3.
                        z = z.parent;
                        leftRotate(z);
                    }
                    // Case 3: recolor + single rotation, done.
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    rightRotate(z.parent.parent);
                }
            } else {
                // Mirror image of the block above, swapping left <-> right.
                Node uncle = z.parent.parent.left;
                if (uncle.color == RED) {
                    z.parent.color = BLACK;
                    uncle.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rightRotate(z);
                    }
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    leftRotate(z.parent.parent);
                }
            }
        }
        // Property 2 (root is black) can be violated if z bubbled all the
        // way up, or if z itself was the root. Always safe to reassert.
        root.color = BLACK;
    }

    // ------------------------------------------------------------------
    // Deletion — the trickiest part of a red-black tree by far.
    // ------------------------------------------------------------------

    /**
     * Removes the mapping for {@code key}, if present.
     *
     * <p>Deletion in a BST is already fiddly (a node with two children can't
     * simply be unlinked — you have to splice in its in-order successor),
     * and a red-black tree adds a second layer on top: removing or moving a
     * BLACK node can violate property 5 (equal black-height on every path),
     * which — unlike the insert case — cannot always be fixed by looking
     * only at the deleted node's immediate neighbors. That's what
     * {@link #deleteFixup} is for.</p>
     *
     * <p>High-level shape of the algorithm (again following CLRS):</p>
     * <ol>
     *   <li>Find the node {@code z} to delete. If it has at most one child,
     *       it can be spliced out directly.</li>
     *   <li>If it has two children, we don't move {@code z}'s key/value
     *       around directly — instead we find its in-order <b>successor</b>
     *       {@code y} (the smallest key in {@code z}'s right subtree, which
     *       is guaranteed to have at most one child), physically move
     *       {@code y} into {@code z}'s position, and then it's really
     *       {@code y} (in its original spot) that gets spliced out.</li>
     *   <li>Whichever node actually gets spliced out, remember its original
     *       color. If that color was BLACK, a black node just disappeared
     *       from some paths, so {@link #deleteFixup} is called on the node
     *       that took its place to restore property 5. If it was RED,
     *       nothing needed fixing — removing a red node can't change any
     *       black-height.</li>
     * </ol>
     *
     * @param key the key to remove
     * @return the value that was associated with {@code key}, or
     *         {@code null} if it was not present
     */
    public V remove(K key) {
        Node z = findNode(key);
        if (z == NIL) {
            return null; // nothing to remove
        }
        V removedValue = z.value;

        Node y = z;                    // y = the node that will actually be spliced out
        boolean yOriginalColor = y.color;
        Node x;                        // x = the node that moves into y's original position
                                        // (may be the NIL sentinel!)

        if (z.left == NIL) {
            x = z.right;
            transplant(z, z.right);
        } else if (z.right == NIL) {
            x = z.left;
            transplant(z, z.left);
        } else {
            // Two children: splice out z's in-order successor instead.
            y = minimum(z.right);
            yOriginalColor = y.color;
            x = y.right;

            if (y.parent == z) {
                // x might be NIL here, and NIL's .parent field is about to
                // be (ab)used by deleteFixup as scratch space to remember
                // "where x logically sits" — see deleteFixup's Javadoc.
                x.parent = y;
            } else {
                transplant(y, y.right);
                y.right = z.right;
                y.right.parent = y;
            }
            transplant(z, y);
            y.left = z.left;
            y.left.parent = y;
            y.color = z.color; // y now fully takes over z's position/color
        }

        size--;

        if (yOriginalColor == BLACK) {
            // A black node left the tree (or moved) — property 5 may now be
            // violated along the paths that used to go through it.
            deleteFixup(x);
        }

        return removedValue;
    }

    /**
     * Replaces the subtree rooted at {@code u} with the subtree rooted at
     * {@code v} in {@code u}'s parent's eyes — a small helper used twice
     * during deletion. Does <b>not</b> touch {@code v}'s children; the
     * caller is responsible for re-parenting those separately if needed
     * (see the two-children branch of {@link #remove}).
     */
    private void transplant(Node u, Node v) {
        if (u.parent == NIL) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    /**
     * Restores the red-black properties after a BLACK node has effectively
     * been removed from the path through {@code x} (x is the node that took
     * the removed node's place, and it is carrying an "extra" unit of
     * black-ness that needs to be resolved — CLRS calls this "doubly
     * black").
     *
     * <p><b>The NIL-parent trick:</b> {@code x} is very often the shared
     * {@link #NIL} sentinel itself (e.g. deleting a leaf). We still need to
     * know x's parent and which side it hangs on to reason about its
     * sibling — so {@link #remove} and {@link #transplant} make sure
     * {@code NIL.parent} is left pointing at the correct place before this
     * method runs. That is safe <i>only</i> because this method finishes
     * (and stops relying on {@code NIL.parent}) before any other tree
     * operation runs — never re-enter this method concurrently or leave it
     * half-finished.</p>
     *
     * <p>Only the "x is a left child" half is commented in detail; the
     * other half mirrors it with left/right swapped. Let {@code w} be
     * {@code x}'s sibling. Because property 5 held before the deletion,
     * {@code w} cannot be NIL when we reach here (its subtree must have had
     * at least the black-height that the missing black node needs to
     * "borrow"). Four cases, tried in order:</p>
     * <ul>
     *   <li><b>Case 1 — w is RED:</b> w can't be a final answer (we need a
     *       BLACK sibling to safely recolor into), so rotate it out of the
     *       way and recolor, turning this into one of cases 2-4.</li>
     *   <li><b>Case 2 — w is BLACK with two BLACK children:</b> we can
     *       remove a black unit from w's side by recoloring w RED — this
     *       fixes the local imbalance but pushes the "extra black" problem
     *       up to {@code x.parent}, so the loop continues from there.</li>
     *   <li><b>Case 3 — w is BLACK, w.left is RED, w.right is BLACK:</b> a
     *       rotation on w converts this into Case 4 in the same iteration.</li>
     *   <li><b>Case 4 — w is BLACK, w.right is RED:</b> a rotation on
     *       {@code x.parent} plus recoloring fixes everything in one shot
     *       and terminates the loop.</li>
     * </ul>
     *
     * @param x the node (possibly {@link #NIL}) that inherited the
     *          "extra black" and needs the tree rebalanced around it
     */
    private void deleteFixup(Node x) {
        while (x != root && x.color == BLACK) {
            if (x == x.parent.left) {
                Node w = x.parent.right; // x's sibling
                if (w.color == RED) {
                    // Case 1
                    w.color = BLACK;
                    x.parent.color = RED;
                    leftRotate(x.parent);
                    w = x.parent.right;
                }
                if (w.left.color == BLACK && w.right.color == BLACK) {
                    // Case 2
                    w.color = RED;
                    x = x.parent; // push the problem up one level
                } else {
                    if (w.right.color == BLACK) {
                        // Case 3
                        w.left.color = BLACK;
                        w.color = RED;
                        rightRotate(w);
                        w = x.parent.right;
                    }
                    // Case 4
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.right.color = BLACK;
                    leftRotate(x.parent);
                    x = root; // done: terminates the loop
                }
            } else {
                // Mirror image, swapping left <-> right throughout.
                Node w = x.parent.left;
                if (w.color == RED) {
                    w.color = BLACK;
                    x.parent.color = RED;
                    rightRotate(x.parent);
                    w = x.parent.left;
                }
                if (w.right.color == BLACK && w.left.color == BLACK) {
                    w.color = RED;
                    x = x.parent;
                } else {
                    if (w.left.color == BLACK) {
                        w.right.color = BLACK;
                        w.color = RED;
                        leftRotate(w);
                        w = x.parent.left;
                    }
                    w.color = x.parent.color;
                    x.parent.color = BLACK;
                    w.left.color = BLACK;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        // x is either the root (always black, property 2) or was already
        // RED (a "red-and-black" node just becomes plain black — this is
        // how case 2 can terminate the loop early: it exits the while
        // condition as soon as x turns out to be RED, and this line makes
        // it BLACK).
        x.color = BLACK;
    }

    // ------------------------------------------------------------------
    // Rotations — the two primitive, O(1) operations every fix-up above is
    // built from. A rotation changes local structure (who is whose child)
    // WITHOUT changing the in-order sequence of keys — the BST property is
    // preserved automatically, only the shape/height changes.
    // ------------------------------------------------------------------

    /**
     * Left-rotates around {@code x}. Before: {@code x} has a right child
     * {@code y}. After: {@code y} takes {@code x}'s place, {@code x}
     * becomes {@code y}'s left child, and {@code y}'s old left subtree
     * (whose keys are all between {@code x} and {@code y}) becomes
     * {@code x}'s new right subtree. Runs in O(1) — only a handful of
     * pointers change.
     *
     * <pre>
     *      x                    y
     *     / \                  / \
     *    a   y     ---&gt;      x   c
     *       / \              / \
     *      b   c            a   b
     * </pre>
     */
    private void leftRotate(Node x) {
        Node y = x.right;
        x.right = y.left;
        if (y.left != NIL) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == NIL) {
            root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    /**
     * Right-rotates around {@code x} — the exact mirror image of
     * {@link #leftRotate}.
     *
     * <pre>
     *        x                y
     *       / \              / \
     *      y   c   ---&gt;    a   x
     *     / \                  / \
     *    a   b                b   c
     * </pre>
     */
    private void rightRotate(Node x) {
        Node y = x.left;
        x.left = y.right;
        if (y.right != NIL) {
            y.right.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == NIL) {
            root = y;
        } else if (x == x.parent.right) {
            x.parent.right = y;
        } else {
            x.parent.left = y;
        }
        y.right = x;
        x.parent = y;
    }

    // ------------------------------------------------------------------
    // Self-check helper (not part of the "public API" you'd ship, but very
    // useful for convincing yourself — and anyone reading this class — that
    // the five properties really do hold after a sequence of operations).
    // Used by the stress test in main() below.
    // ------------------------------------------------------------------

    /**
     * Validates all five red-black properties plus basic BST ordering, by
     * walking the whole tree once. Throws {@link IllegalStateException}
     * naming the exact violation if anything is wrong. O(n).
     *
     * <p>This method exists purely for learning/testing purposes — it is
     * exactly the kind of check you'd write once, run against thousands of
     * random insertions and deletions, and then delete before shipping,
     * confident the algorithm above is implemented correctly.</p>
     */
    void validateInvariants() {
        if (root == NIL) {
            return; // empty tree trivially satisfies every property
        }
        if (root.color != BLACK) {
            throw new IllegalStateException("Property 2 violated: root is not black");
        }
        validateNode(root);
    }

    /** @return the black-height of the subtree rooted at {@code node} */
    private int validateNode(Node node) {
        if (node == NIL) {
            return 0; // NIL counts as black, contributing 0 extra black nodes... actually 1 black leaf
        }
        if (node.color == RED) {
            if (node.left.color == RED || node.right.color == RED) {
                throw new IllegalStateException("Property 4 violated: red node " + node.key + " has a red child");
            }
        }
        if (node.left != NIL) {
            if (node.left.key.compareTo(node.key) >= 0) {
                throw new IllegalStateException("BST property violated at " + node.key);
            }
            if (node.left.parent != node) {
                throw new IllegalStateException("Parent pointer broken at " + node.left.key);
            }
        }
        if (node.right != NIL) {
            if (node.right.key.compareTo(node.key) <= 0) {
                throw new IllegalStateException("BST property violated at " + node.key);
            }
            if (node.right.parent != node) {
                throw new IllegalStateException("Parent pointer broken at " + node.right.key);
            }
        }
        int leftBlackHeight = validateNode(node.left);
        int rightBlackHeight = validateNode(node.right);
        if (leftBlackHeight != rightBlackHeight) {
            throw new IllegalStateException("Property 5 violated at " + node.key
                    + ": left black-height=" + leftBlackHeight + " right black-height=" + rightBlackHeight);
        }
        return leftBlackHeight + (node.color == BLACK ? 1 : 0);
    }

    // ------------------------------------------------------------------
    // Demo + randomized stress test against java.util.TreeMap as a
    // known-correct reference implementation.
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        basicUsageDemo();
        randomizedStressTest();
    }

    private static void basicUsageDemo() {
        System.out.println("=== Basic usage ===");
        RedBlackTreeMap<Integer, String> map = new RedBlackTreeMap<>();
        int[] keys = {50, 30, 70, 20, 40, 60, 80, 10};
        for (int k : keys) {
            map.put(k, "value-" + k);
        }

        System.out.println("Sorted keys: " + map.keysInOrder());
        System.out.println("get(40)     = " + map.get(40));
        System.out.println("get(999)    = " + map.get(999));
        System.out.println("firstKey()  = " + map.firstKey());
        System.out.println("lastKey()   = " + map.lastKey());
        System.out.println("ceilingKey(45) = " + map.ceilingKey(45)); // -> 50
        System.out.println("floorKey(45)   = " + map.floorKey(45));   // -> 40
        System.out.println("higherKey(40)  = " + map.higherKey(40));  // -> 50
        System.out.println("lowerKey(40)   = " + map.lowerKey(40));   // -> 30

        map.remove(30);
        System.out.println("After remove(30): " + map.keysInOrder());
        map.validateInvariants();
        System.out.println("Red-black invariants hold after basic demo.\n");
    }

    private static void randomizedStressTest() {
        System.out.println("=== Randomized stress test vs java.util.TreeMap ===");
        Random random = new Random(42); // fixed seed: reproducible run
        RedBlackTreeMap<Integer, String> mine = new RedBlackTreeMap<>();
        java.util.TreeMap<Integer, String> reference = new java.util.TreeMap<>();

        int operations = 200_000;
        int keySpace = 5_000;

        for (int i = 0; i < operations; i++) {
            int key = random.nextInt(keySpace);
            int op = random.nextInt(4);

            switch (op) {
                case 0:
                case 1: // bias insert slightly more likely than delete
                    mine.put(key, "v" + key);
                    reference.put(key, "v" + key);
                    break;
                case 2:
                    mine.remove(key);
                    reference.remove(key);
                    break;
                default:
                    Integer expectedCeiling = reference.ceilingKey(key);
                    Integer actualCeiling = mine.ceilingKey(key);
                    if (!java.util.Objects.equals(expectedCeiling, actualCeiling)) {
                        throw new AssertionError("ceilingKey mismatch for key=" + key
                                + " expected=" + expectedCeiling + " actual=" + actualCeiling);
                    }
                    Integer expectedFloor = reference.floorKey(key);
                    Integer actualFloor = mine.floorKey(key);
                    if (!java.util.Objects.equals(expectedFloor, actualFloor)) {
                        throw new AssertionError("floorKey mismatch for key=" + key
                                + " expected=" + expectedFloor + " actual=" + actualFloor);
                    }
                    break;
            }

            // Periodically do a full structural check: same size, same
            // sorted key sequence, and the red-black invariants still hold.
            if (i % 5_000 == 0) {
                if (mine.size() != reference.size()) {
                    throw new AssertionError("size mismatch at i=" + i
                            + " mine=" + mine.size() + " reference=" + reference.size());
                }
                List<Integer> mineKeys = mine.keysInOrder();
                List<Integer> refKeys = new ArrayList<>(reference.keySet());
                if (!mineKeys.equals(refKeys)) {
                    throw new AssertionError("key sequence mismatch at i=" + i);
                }
                mine.validateInvariants();
            }
        }

        mine.validateInvariants();
        System.out.println("Completed " + operations + " randomized put/remove/ceiling/floor operations.");
        System.out.println("Final size: " + mine.size() + " (matches java.util.TreeMap: " + reference.size() + ")");
        System.out.println("All red-black invariants held throughout. PASS.");
    }
}
