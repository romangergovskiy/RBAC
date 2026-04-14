package rbac;

import java.util.Scanner;

import rbac.command.CommandParser;
import rbac.command.CommandRegistry;

public class Main {
    public static void main(String[] args) {
        var system = new RBACSystem();
        system.initialize();

        var parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        System.out.println("RBAC System. Type 'help' for commands, 'exit' to quit.");

        try (var scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();
                parser.parseAndExecute(line, scanner, system);
            }
        } finally {
            system.shutdown();
        }
    }
}
