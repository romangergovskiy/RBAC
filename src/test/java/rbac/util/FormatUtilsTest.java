package rbac.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class FormatUtilsTest {

    @Test
    void formatTableHasBorders() {
        String t = FormatUtils.formatTable(new String[] { "A", "B" }, List.of(new String[][] { { "1", "2" } }));
        assertTrue(t.contains("+"));
        assertTrue(t.contains("|"));
    }

    @Test
    void truncateAndPad() {
        assertTrue(FormatUtils.truncate("abcdef", 5).endsWith("..."));
        assertEquals("ab   ", FormatUtils.padRight("ab", 5));
        assertEquals("   ab", FormatUtils.padLeft("ab", 5));
    }
}
