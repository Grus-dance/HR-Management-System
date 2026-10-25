package org.example.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    @Test
    void hashShouldVerify() {
        String encoded = PasswordHasher.hash("abc12345");
        assertTrue(PasswordHasher.matches("abc12345", encoded));
        assertFalse(PasswordHasher.matches("abc12346", encoded));
    }

    @Test
    void hashShouldUseIndependentSalt() {
        String first = PasswordHasher.hash("123456");
        String second = PasswordHasher.hash("123456");
        assertNotEquals(first, second);
        assertTrue(first.startsWith("pbkdf2$120000$"));
    }

    @Test
    void malformedHashShouldNotMatch() {
        assertFalse(PasswordHasher.matches("123456", null));
        assertFalse(PasswordHasher.matches("123456", "plain-text"));
        assertFalse(PasswordHasher.matches(null, PasswordHasher.hash("123456")));
    }
}
