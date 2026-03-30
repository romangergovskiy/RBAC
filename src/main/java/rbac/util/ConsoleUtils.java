package rbac.util;

import java.util.List;
import java.util.Scanner;

public final class ConsoleUtils {

    private ConsoleUtils() {}

    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message);
            String line = scanner.nextLine();
            String v = ValidationUtils.normalizeString(line);
            if (!required && v.isEmpty()) return "";
            if (required && v.isEmpty()) {
                System.out.println("Value required.");
                continue;
            }
            return v;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message);
            String line = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(line);
                if (n >= min && n <= max) return n;
                System.out.println("Enter number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (yes/no): ");
            String line = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(line) || "yes".equals(line)) return true;
            if ("n".equals(line) || "no".equals(line)) return false;
            System.out.println("Type yes or no.");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) throw new IllegalArgumentException("options empty");
        System.out.println(message);
        for (int i = 0; i < options.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + options.get(i));
        }
        int n = promptInt(scanner, "Choice (1-" + options.size() + "): ", 1, options.size());
        return options.get(n - 1);
    }
}
