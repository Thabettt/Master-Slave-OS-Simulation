package Components;

import java.util.LinkedList;
import java.util.Queue;

public class ReadyQueue {
    // Queue to store processes
    private final Queue<ProcessControlBlock> queue;

    public ReadyQueue() {
        // Initialize the queue
        queue = new LinkedList<>();
    }

    /**
     * Adds a process to the ready queue.
     *
     * @param pcb the process control block representing the process
     */
    public synchronized void enqueue(ProcessControlBlock pcb) {
        queue.offer(pcb);
        System.out.println("[ReadyQueue] Process added: " + pcb + "\n");
        notifyAll(); // Notify any waiting threads that a process is available
    }

    /**
     * Retrieves and removes the next process from the ready queue.
     * If the queue is empty, this method will wait until a process is available.
     *
     * @return the next process control block
     */
    public synchronized ProcessControlBlock dequeue() {
        while (queue.isEmpty()) {
            try {
                System.out.println("[ReadyQueue] Queue is empty. Waiting for processes...");
                wait(); // Wait until a process is added
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ReadyQueue] Thread interrupted while waiting.");
                return null; // Return null if interrupted
            }
        }
        ProcessControlBlock pcb = queue.poll();
        System.out.println("[ReadyQueue] Process removed: " + pcb + "\n");
        return pcb;
    }

    /**
     * Checks if the ready queue is empty.
     *
     * @return true if the queue is empty, false otherwise
     */
    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Displays the current state of the ready queue.
     */
    public synchronized void displayQueue() {
        System.out.println("[ReadyQueue] Current queue state:");
        if (queue.isEmpty()) {
            System.out.println("[ReadyQueue] The queue is empty.");
        } else {
            for (ProcessControlBlock pcb : queue) {
                System.out.println("  " + pcb);
            }
        }
    }

    public String peek() {
        return queue.peek().toString();
    }
}
