package MasterSlaveArchitecture;

import Components.*;
import java.util.List;

public class SlaveCore extends Thread {
    private final int coreId; // Unique ID for this SlaveCore
    private boolean isBusy;
    private List<Parser.Instruction> assignedProcess;
    private Memory memory;
    private MasterCore master;
    private int processId; // Add processId to track the process

    public SlaveCore(int coreId, Memory memory, MasterCore master) {
        this.coreId = coreId;
        this.memory = memory;
        this.master = master;
        this.isBusy = false;
    }

    public synchronized boolean isBusy() {
        return isBusy;
    }

    public boolean isAvailable() {
        return !isBusy;
    }

    public synchronized void assignProcess(List<Parser.Instruction> process, int processId) {
        this.assignedProcess = process;
        this.processId = processId; // Set the processId when assigning a process
        this.isBusy = true;
        notify();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this) {
                while (!isBusy) {
                    try {
                        wait(); // Wait for a process to be assigned
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Execute the assigned process
            for (Parser.Instruction instruction : assignedProcess) {
                try {
                    switch (instruction.getType()) {
                        case ASSIGN:
                            // Evaluate the expression and assign the result to the variable
                            String variableName = instruction.getOperands().get(0).toLowerCase();
                            double result = Parser.evaluateExpression(instruction.getOperands(), memory, processId);
                            memory.assign(processId, variableName, result);
                            break;

                        case PRINT:
                            // Print the value of the variable
                            String printVar = instruction.getOperands().get(0).toLowerCase();
                            System.out.println("Core " + coreId + ": " + printVar + " = " + memory.get(processId, printVar));
                            break;

                        default:
                            System.err.println("Unknown instruction type: " + instruction.getType());
                    }
                } catch (Exception e) {
                    System.err.println("Error executing instruction: " + instruction + " in Core " + coreId);
                }
            }

            // Notify MasterCore that this core is idle
            synchronized (this) {
                isBusy = false;
                master.notifyIdleCore(coreId);
            }
        }
    }
}