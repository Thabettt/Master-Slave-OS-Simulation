package MasterSlaveArchitecture;

import Components.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MasterCore extends Thread {
    private final ReadyQueue readyQueue; // Shared Ready Queue
    private final List<SlaveCore> slaveCores; // List of Slave Cores

    public MasterCore(ReadyQueue readyQueue, List<SlaveCore> slaveCores) {
        this.readyQueue = readyQueue;
        this.slaveCores = slaveCores;
    }

    /**
     * Run the MasterCore to manage process scheduling and delegation.
     */
    @Override
    public void run() {
        while (true) {
            synchronized (readyQueue) {
                // Sort ReadyQueue by Burst Time (SJF Scheduling)
                List<ProcessControlBlock> sortedQueue = getSortedQueueByBurstTime();
                readyQueue.displayQueue();

                // Assign processes to available slave cores
                for (ProcessControlBlock pcb : sortedQueue) {
                    SlaveCore availableCore = getAvailableSlaveCore();
                    if (availableCore != null) {
                        availableCore.assignProcess(pcb.getInstructions(), pcb.getProcessId());
                        readyQueue.dequeue(); // Remove from ReadyQueue
                        System.out.println("[MasterCore] Process " + pcb.getProcessId() + " assigned to " + availableCore.getName());
                    }
                }
            }

            // Simulate a scheduling interval
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Retrieves the sorted queue by burst time.
     *
     * @return List of sorted PCBs.
     */
    private List<ProcessControlBlock> getSortedQueueByBurstTime() {
        List<ProcessControlBlock> sortedQueue = new ArrayList<>();
        synchronized (readyQueue) {
            while (!readyQueue.isEmpty()) {
                sortedQueue.add(readyQueue.dequeue());
            }
            sortedQueue.sort(Comparator.comparingInt(ProcessControlBlock::getBurstTime));
            for (ProcessControlBlock pcb : sortedQueue) {
                readyQueue.enqueue(pcb);
            }
        }
        return sortedQueue;
    }

    /**
     * Finds an available slave core.
     *
     * @return An available SlaveCore or null if none are available.
     */
    private SlaveCore getAvailableSlaveCore() {
        for (SlaveCore slave : slaveCores) {
            if (slave.isAvailable()) {
                return slave;
            }
        }
        return null; // No cores are available
    }

    public void notifyIdleCore(int coreId) {
        synchronized (slaveCores.get(coreId)) {
            slaveCores.get(coreId).notify();
        }
    }
}