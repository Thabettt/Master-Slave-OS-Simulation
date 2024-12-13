package Components;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class Memory {
    // Memory to store variables and their values for each process
    private Map<Integer, ConcurrentHashMap<String, Double>> processMemory;

    public Memory() {
        // Initialize the process-specific memory
        processMemory = new ConcurrentHashMap<>();
    }

    /**
     * Assigns a value to a variable in memory for a specific process.
     *
     * @param processId the process ID
     * @param variable the variable name
     * @param value the value to assign
     */
    public synchronized void assign(int processId, String variable, double value) {
        processMemory.computeIfAbsent(processId, k -> new ConcurrentHashMap<>()).put(variable, value);
        System.out.println("Memory Update for Process " + processId + ": " + variable + " = " + value);
    }

    /**
     * Retrieves the value of a variable from memory for a specific process.
     *
     * @param processId the process ID
     * @param variable the variable name
     * @return the value of the variable, or null if the variable does not exist
     */
    public Double get(int processId, String variable) {
        Map<String, Double> memory = processMemory.get(processId);
        if (memory != null) {
            return memory.get(variable);
        }
        return null;
    }

    /**
     * Releases the memory for a specific process.
     *
     * @param processId the process ID
     */
    public synchronized void release(int processId) {
        processMemory.remove(processId);
        System.out.println("Memory released for Process " + processId);
    }

    /**
     * Displays the current state of the memory for all processes.
     */
    public synchronized void displayMemoryState() {
        System.out.println("Current Memory State:");
        for (Map.Entry<Integer, ConcurrentHashMap<String, Double>> entry : processMemory.entrySet()) {
            int processId = entry.getKey();
            ConcurrentHashMap<String, Double> memory = entry.getValue();
            System.out.println("Process " + processId + ":");
            for (String key : memory.keySet()) {
                System.out.println("  " + key + " = " + memory.get(key));
            }
        }
    }

    public boolean containsKey(int processId, String variable) {
        Map<String, Double> memory = processMemory.get(processId);
        return memory != null && memory.containsKey(variable);
    }
}