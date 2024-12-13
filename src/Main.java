// Main.java
import java.util.*;
import Components.*;
import MasterSlaveArchitecture.*;

public class Main {
    public static void main(String[] args) {
        List<String> filePaths = List.of("src/SourceFiles/Program_1.txt",
                "src/SourceFiles/Program_2.txt", "src/SourceFiles/Program_3.txt");
        runSimulation(filePaths);
    }

    private static void runSimulation(List<String> filePaths) {
        ReadyQueue readyQueue = new ReadyQueue();
        Set<String> addedFilePaths = new HashSet<>();
        int processId = 1;
        int memoryStart = 0;

        for (String filePath : filePaths) {
            if (!addedFilePaths.contains(filePath)) {
                Map<Integer, List<Parser.Instruction>> programs = Parser.parsePrograms(List.of(filePath));
                List<Parser.Instruction> instructions = programs.get(1);

                // Display parsed instructions
                System.out.println("Parsed Instructions from " + filePath + ":");
                for (Parser.Instruction instruction : instructions) {
                    System.out.println(instruction);
                }
                System.out.println();

                // Create a process for the file
                int burstTime = instructions.size();
                int memoryEnd = memoryStart + 99; // Assign memory range for the process
                ProcessControlBlock process = new ProcessControlBlock(processId++, memoryStart, memoryEnd, burstTime, 0, instructions);
                readyQueue.enqueue(process);
                addedFilePaths.add(filePath);
                // Update memoryStart for the next process
                memoryStart = memoryEnd + 1;
            }
        }

        // Initialize cores
        int numCores = 2; // Simulate a master core with 2 slave cores
        ProcessControlBlock[] cores = new ProcessControlBlock[numCores];

        // Memory state tracking (optional)
        List<String> memoryLog = new ArrayList<>();

        // Initialize memory, scanner, and assignedVariables
        Memory memory = new Memory();
        Scanner scanner = new Scanner(System.in);
        Map<Integer, Set<String>> assignedVariablesMap = new HashMap<>();

        System.out.println("\n=== Simulation Start ===\n");

        // Simulation loop (clock cycles)
        int clockCycle = 0;
        while (!readyQueue.isEmpty() || !allCoresIdle(cores)) {
            System.out.println("Clock Cycle: " + clockCycle + "\n");

            // Assign processes to idle cores using SJF
            for (int i = 0; i < numCores; i++) {
                if (cores[i] == null || cores[i].isCompleted()) {
                    // Find the shortest job in the ReadyQueue
                    ProcessControlBlock shortestJob = getShortestJob(readyQueue);
                    if (shortestJob != null) {
                        cores[i] = shortestJob;
                        cores[i].setState(ProcessControlBlock.STATE_RUNNING);
                        System.out.println("Core " + i + ": Assigned Process with processID " + shortestJob.getProcessId() + "\n");
                    }
                }
            }

            // Execute processes on each core
            for (int i = 0; i < numCores; i++) {
                if (cores[i] != null) {
                    Parser.Instruction instruction = cores[i].getNextInstruction();
                    if (instruction != null) {
                        System.out.println("Core " + i + ": Executing instruction: " + instruction);
                        executeInstruction(cores[i].getProcessId(), instruction, memory, scanner, assignedVariablesMap);
                    }
                    cores[i].reduceBurstTime(1); // Execute for 1 time unit
                    if (cores[i].isCompleted()) {
                        cores[i].setState(ProcessControlBlock.STATE_TERMINATED);
                        System.out.println("Core " + i + ": Process " + cores[i].getProcessId() + " completed.");
                        memoryLog.add("Process " + cores[i].getProcessId() + " memory released (" +
                                cores[i].getMemoryStart() + "-" + cores[i].getMemoryEnd() + ")");
                        memory.release(cores[i].getProcessId()); // Release memory for the process
                        cores[i] = null; // Free the core
                        System.out.println(); // Add extra newline after each process completion block
                    } else {
                        System.out.println("Core " + i + ": Executing Process " + cores[i].getProcessId() +
                                " (Remaining Time: " + cores[i].getBurstTime() + ")\n");
                    }
                }
            }

            // Display Ready Queue
            readyQueue.displayQueue();
            System.out.println();

            // Display memory log
            displayMemoryState(cores);
            System.out.println();

            // Increment clock cycle
            clockCycle++;
            System.out.println();
        }

        System.out.println("\n=== Simulation Complete ===");
    }

   private static void executeInstruction(int processId, Parser.Instruction instruction, Memory memory, Scanner scanner, Map<Integer, Set<String>> assignedVariablesMap) {
    Set<String> assignedVariables = assignedVariablesMap.computeIfAbsent(processId, k -> new HashSet<>());
    if (instruction.getType() == Parser.InstructionType.ASSIGN) {
        List<String> operands = instruction.getOperands();
        if (operands.size() == 2 && operands.get(1).equalsIgnoreCase("input")) {
            String variable = operands.get(0);
            double value = getValidInput(scanner, variable);
            memory.assign(processId, variable, value);
            assignedVariables.add(variable);
        } else if (operands.size() == 4) {
            String variable = operands.get(0);
            try {
                double result = Parser.evaluateExpression(operands, memory, processId);
                memory.assign(processId, variable, result);
                System.out.println("Memory Update: " + variable + " = " + result);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    } else if (instruction.getType() == Parser.InstructionType.PRINT) {
        List<String> operands = instruction.getOperands();
        if (operands.size() == 1) {
            String variable = operands.get(0);
            System.out.println("Printed value of " + variable + ": " + memory.get(processId, variable));
        }
    }
}

    private static double getValidInput(Scanner scanner, String variable) {
        while (true) {
            System.out.print("Enter value for " + variable + ": ");
            try {
                double value = scanner.nextDouble();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number.\n");
                scanner.next(); // Clear the invalid input
            } catch (NoSuchElementException e) {
                System.out.println("Input is too large. Please enter a smaller number.\n");
                scanner.next(); // Clear the invalid input
            }
        }
    }

    private static ProcessControlBlock getShortestJob(ReadyQueue readyQueue) {
        List<ProcessControlBlock> tempList = new ArrayList<>();
        while (!readyQueue.isEmpty()) {
            tempList.add(readyQueue.dequeue());
        }
        tempList.sort(Comparator.comparingInt(ProcessControlBlock::getBurstTime));
        ProcessControlBlock shortestJob = tempList.isEmpty() ? null : tempList.remove(0);
        for (ProcessControlBlock pcb : tempList) {
            readyQueue.enqueue(pcb);
        }
        return shortestJob;
    }

    private static boolean allCoresIdle(ProcessControlBlock[] cores) {
        for (ProcessControlBlock core : cores) {
            if (core != null) return false;
        }
        return true;
    }

    private static void displayMemoryState(ProcessControlBlock[] cores) {
        System.out.println("Memory State:");
        boolean memoryOccupied = false;
        for (ProcessControlBlock pcb : cores) {
            if (pcb != null) {
                System.out.println("Process " + pcb.getProcessId() + " memory occupied (" +
                        pcb.getMemoryStart() + " - " + pcb.getMemoryEnd() + ")");
                memoryOccupied = true;
            }
        }
        if (!memoryOccupied) {
            System.out.println("No memory actions yet.");
        }
    }
}