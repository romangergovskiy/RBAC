package rbac.command;

import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import rbac.RBACSystem;

public class CommandParser {
    private final Map<String, Command> commands = new TreeMap<>();
    private final Map<String, String> commandDescriptions = new TreeMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name, command);
        commandDescriptions.put(name, description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command cmd = commands.get(commandName);
        if (cmd == null) {
            System.out.println("Unknown command: " + commandName + ". Type 'help' for list.");
            return;
        }
        cmd.execute(scanner, system);
    }

    public void printHelp() {
        System.out.println("=== Available commands ===");
        for (var e : commandDescriptions.entrySet()) {
            System.out.printf("  %-25s %s%n", e.getKey(), e.getValue());
        }
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.isBlank()) return;
        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        executeCommand(commandName, scanner, system);
    }
}
