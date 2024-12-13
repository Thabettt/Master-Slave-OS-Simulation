package Components;

import java.util.List;

public class ProcessControlBlock {
    private final int processId; // Unique ID for the process (immutable)
    private int programCounter; // Tracks the current instruction
    private final int memoryStart; // Start address in memory
    private final int memoryEnd; // End address in memory
    private String state; // Process state (READY, RUNNING, TERMINATED)
    private int priority; // Priority of the process (optional, for future use)
    private int burstTime; // Remaining CPU burst time
    private final int arrivalTime; // Time at which the process arrived
    private List<Parser.Instruction> instructions; // List of instructions for the process

    // Constants for process states
    public static final String STATE_READY = "READY";
    public static final String STATE_RUNNING = "RUNNING";
    public static final String STATE_TERMINATED = "TERMINATED";

    /**
     * Constructor to initialize a new PCB.
     *
     * @param processId    the unique ID of the process
     * @param memoryStart  the start address of the process's memory
     * @param memoryEnd    the end address of the process's memory
     * @param burstTime    the CPU burst time for the process
     * @param arrivalTime  the arrival time of the process
     * @param instructions the list of instructions for the process
     * @throws IllegalArgumentException if invalid values are provided
     */
    public ProcessControlBlock(int processId, int memoryStart, int memoryEnd, int burstTime, int arrivalTime, List<Parser.Instruction> instructions) {
        if (memoryStart >= memoryEnd) {
            throw new IllegalArgumentException("Memory start address must be less than memory end address.");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be greater than 0.");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time cannot be negative.");
        }

        this.processId = processId;
        this.memoryStart = memoryStart;
        this.memoryEnd = memoryEnd;
        this.burstTime = burstTime;
        this.arrivalTime = arrivalTime;
        this.programCounter = 0; // Start at the first instruction
        this.state = STATE_READY; // Initial state is READY
        this.priority = 0; // Default priority (can be adjusted later)
        this.instructions = instructions; // Initialize the instructions field
    }

    // Getter methods
    public int getProcessId() {
        return processId;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public int getMemoryStart() {
        return memoryStart;
    }

    public int getMemoryEnd() {
        return memoryEnd;
    }

    public String getState() {
        return state;
    }

    public int getPriority() {
        return priority;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public List<Parser.Instruction> getInstructions() {
        return instructions;
    }

    // Setter methods
    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public void setState(String state) {
        if (!state.equals(STATE_READY) && !state.equals(STATE_RUNNING) && !state.equals(STATE_TERMINATED)) {
            throw new IllegalArgumentException("Invalid state: " + state);
        }
        this.state = state;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setBurstTime(int burstTime) {
        if (burstTime < 0) {
            throw new IllegalArgumentException("Burst time cannot be negative.");
        }
        this.burstTime = burstTime;
    }

    public void setInstructions(List<Parser.Instruction> instructions) {
        this.instructions = instructions;
    }

    // Utility methods
    public void incrementProgramCounter() {
        this.programCounter++;
    }

    public void reduceBurstTime(int time) {
        if (time < 0) {
            throw new IllegalArgumentException("Time reduction cannot be negative.");
        }
        this.burstTime = Math.max(0, this.burstTime - time);
    }

    public boolean isCompleted() {
        return burstTime == 0;
    }

    public Parser.Instruction getNextInstruction() {
        if (programCounter < instructions.size()) {
            return instructions.get(programCounter++);
        }
        return null;
    }

    @Override
    public String toString() {
        return "PCB{" +
                "processId=" + processId +
                ", programCounter=" + programCounter +
                ", memoryStart=" + memoryStart +
                ", memoryEnd=" + memoryEnd +
                ", state='" + state + '\'' +
                ", priority=" + priority +
                ", burstTime=" + burstTime +
                ", arrivalTime=" + arrivalTime +
                ", instructions=" + instructions +
                '}';
    }

    public boolean hasMoreInstructions() {
        return programCounter < instructions.size();
    }
}