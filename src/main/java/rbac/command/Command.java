package rbac.command;

import java.util.Scanner;

import rbac.RBACSystem;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system);
}
