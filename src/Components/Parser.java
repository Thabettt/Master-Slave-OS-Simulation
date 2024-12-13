package Components;

import java.io.*;
import java.util.*;

public class Parser {

    // Instruction Types Enum
    public enum InstructionType {
        ASSIGN, PRINT
    }

    // Represents a parsed instruction
    public static class Instruction {
        private InstructionType type;
        private List<String> operands;

        public Instruction(InstructionType type, List<String> operands) {
            this.type = type;
            this.operands = operands;
        }

        public InstructionType getType() {
            return type;
        }

        public List<String> getOperands() {
            return operands;
        }

        @Override
        public String toString() {
            return type + " " + operands;
        }
    }

    // Parses all programs from a list of file paths
    public static Map<Integer, List<Instruction>> parsePrograms(List<String> filePaths) {
        Map<Integer, List<Instruction>> programs = new HashMap<>();
        int processId = 1;

        for (String filePath : filePaths) {
            try {
                List<Instruction> instructions = parseProgramFile(filePath);
                programs.put(processId, instructions);
                processId++;
            } catch (IOException e) {
                System.err.println("Error reading file: " + filePath);
            }
        }
        return programs;
    }

    // Parses a single program file
    public static List<Instruction> parseProgramFile(String filePath) throws IOException {
        List<Instruction> instructions = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("//")) { // Skip empty lines and comments
                Instruction instruction = parseInstruction(line);
                if (instruction != null) {
                    instructions.add(instruction);
                }
            }
        }
        reader.close();
        return instructions;
    }

    // Parses a single line into an Instruction
    private static Instruction parseInstruction(String line) {
        String[] parts = line.split(" ");
        if (parts.length < 2) return null;

        String command = parts[0].toUpperCase();
        List<String> operands = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));

        switch (command) {
            case "ASSIGN":
                if (operands.size() == 4) {
                    String operation = operands.get(1).toLowerCase();
                    if (!isValidOperation(operation)) {
                        System.err.println("Invalid operation: " + operation);
                        return null;
                    }
                }
                return new Instruction(InstructionType.ASSIGN, operands);
            case "PRINT":
                return new Instruction(InstructionType.PRINT, operands);
            default:
                System.err.println("Unknown command: " + command);
                return null;
        }
    }

    // Checks if the operation is valid
    private static boolean isValidOperation(String operation) {
        return operation.equals("add") || operation.equals("multiply") || operation.equals("divide") || operation.equals("subtract");
    }

    // Evaluates an expression
    public static double evaluateExpression(List<String> operands, Memory memory, int processId) {
    if (operands.size() == 4) {
        String operation = operands.get(1).toLowerCase();
        String var1 = operands.get(2).toLowerCase();
        String var2 = operands.get(3).toLowerCase();

        Double operand1 = memory.get(processId, var1);
        Double operand2 = memory.get(processId, var2);

        if (operand1 == null || operand2 == null) {
            throw new IllegalArgumentException("Variable(s) " + var1 + " or " + var2 + " do not exist in memory.");
        }

        switch (operation) {
            case "add":
                return operand1 + operand2;
            case "multiply":
                return operand1 * operand2;
            case "divide":
                return operand1 / operand2;
            case "subtract":
                return operand1 - operand2;
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }
    throw new IllegalArgumentException("Invalid expression: " + operands);
}
}