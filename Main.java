import java.util.LinkedList;
import java.util.Queue;

public class Main {

    static class Process {
        String name;
        int arrivalTime;
        int burstTime;
        int remainingTime;
        int completionTime;
        int waitingTime;
        int turnaroundTime;

        Process(String name, int arrivalTime, int burstTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }
    }

    public static void runRoundRobin(Process[] processes, int quantum) {
        Queue<Process> queue = new LinkedList<>();
        int currentTime = 0;
        int index = 0;
        int n = processes.length;

        // Sort by arrival time so we can add processes as they "arrive"
        java.util.Arrays.sort(processes, (a, b) -> a.arrivalTime - b.arrivalTime);

        // Seed the queue with any processes that have already arrived at time 0
        while (index < n && processes[index].arrivalTime <= currentTime) {
            queue.add(processes[index]);
            index++;
        }

        while (!queue.isEmpty()) {
            Process p = queue.poll();

            int execTime = Math.min(quantum, p.remainingTime);
            currentTime += execTime;
            p.remainingTime -= execTime;

            // Add any processes that arrived during this time slice
            while (index < n && processes[index].arrivalTime <= currentTime) {
                queue.add(processes[index]);
                index++;
            }

            if (p.remainingTime > 0) {
                queue.add(p); // not finished, back of the line
            } else {
                p.completionTime = currentTime;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
            }
        }
    }

    public static void main(String[] args) {
        Process[] processes = {
            new Process("P1", 0, 10),
            new Process("P2", 1, 5),
            new Process("P3", 2, 8)
        };
        int quantum = 3;

        runRoundRobin(processes, quantum);

        System.out.printf("%-6s%-10s%-10s%-12s%-10s%-10s%n",
                "Proc", "Arrival", "Burst", "Completion", "Waiting", "Turnaround");
        for (Process p : processes) {
            System.out.printf("%-6s%-10d%-10d%-12d%-10d%-10d%n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.completionTime, p.waitingTime, p.turnaroundTime);
        }
    }
}
