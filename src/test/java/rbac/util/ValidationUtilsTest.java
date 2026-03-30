package rbac.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidationUtilsTest {

    @Test
    void usernameAndEmail() {
        assertTrue(ValidationUtils.isValidUsername("user_123"));
        assertFalse(ValidationUtils.isValidUsername("ab"));
        assertTrue(ValidationUtils.isValidEmail("a@b.co"));
        assertFalse(ValidationUtils.isValidEmail("bad"));
    }

    @Test
    void dateFormats() {
        assertTrue(ValidationUtils.isValidDate("2026-01-15"));
        assertTrue(ValidationUtils.isValidDate("2026-01-15 12:30"));
        assertFalse(ValidationUtils.isValidDate("01-15-2026"));
    }

    @Test
    void requireNonEmptyThrows() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonEmpty("", "f"));
    }

    @Test
    void normalizeString() {
        assertEquals("a b", ValidationUtils.normalizeString("  a   b  "));
    }
}
