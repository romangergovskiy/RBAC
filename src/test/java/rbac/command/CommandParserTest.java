package rbac.command;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import rbac.RBACSystem;

class CommandParserTest {

    @Test
    void unknownCommandShowsMessage() {
        var parser = new CommandParser();
        var sys = new RBACSystem();
        var out = new ByteArrayOutputStream();
        var origOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            parser.parseAndExecute("foo", new Scanner(""), sys);
            assertTrue(out.toString().contains("Unknown command"));
        } finally {
            System.setOut(origOut);
        }
    }

    @Test
    void helpPrintsCommands() {
        var parser = new CommandParser();
        CommandRegistry.registerAll(parser);
        var sys = new RBACSystem();
        var out = new ByteArrayOutputStream();
        var origOut = System.out;
        System.setOut(new PrintStream(out));
        try {
            parser.executeCommand("help", new Scanner(""), sys);
            assertTrue(out.toString().contains("help"));
            assertTrue(out.toString().contains("user-list"));
        } finally {
            System.setOut(origOut);
        }
    }
}
