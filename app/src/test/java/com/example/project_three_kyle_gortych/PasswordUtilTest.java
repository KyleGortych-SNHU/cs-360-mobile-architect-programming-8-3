package com.example.project_three_kyle_gortych;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.project_three_kyle_gortych.util.PasswordUtil;

import org.junit.Test;

/**
 * Local JVM unit tests for {@link PasswordUtil}. These do not require an
 * Android device because bcrypt is pure Java. Run with
 * {@code ./gradlew test} or from Android Studio's test runner.
 */
public class PasswordUtilTest {

    @Test
    public void hash_producesNonEmptyStringDifferentFromInput() {
        String hash = PasswordUtil.hash("correct horse battery staple");
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertNotEquals("correct horse battery staple", hash);
    }

    @Test
    public void verify_returnsTrueForMatchingPassword() {
        String hash = PasswordUtil.hash("letmein");
        assertTrue(PasswordUtil.verify("letmein", hash));
    }

    @Test
    public void verify_returnsFalseForWrongPassword() {
        String hash = PasswordUtil.hash("letmein");
        assertFalse(PasswordUtil.verify("letmeout", hash));
    }

    @Test
    public void verify_isCaseSensitive() {
        String hash = PasswordUtil.hash("Password1");
        assertFalse(PasswordUtil.verify("password1", hash));
    }

    @Test
    public void hash_producesDifferentOutputsForSameInput() {
        // bcrypt includes a random salt, so two hashes of the same password
        // must not be identical – and both must still verify.
        String h1 = PasswordUtil.hash("same-input");
        String h2 = PasswordUtil.hash("same-input");
        assertNotEquals(h1, h2);
        assertTrue(PasswordUtil.verify("same-input", h1));
        assertTrue(PasswordUtil.verify("same-input", h2));
    }

    @Test
    public void verify_handlesNullSafely() {
        String hash = PasswordUtil.hash("x");
        assertFalse(PasswordUtil.verify(null, hash));
        assertFalse(PasswordUtil.verify("x", null));
        assertFalse(PasswordUtil.verify(null, null));
    }

    @Test
    public void verify_returnsFalseForMalformedHash() {
        // Not a bcrypt string at all – should be treated as a mismatch
        // rather than crashing.
        assertFalse(PasswordUtil.verify("anything", "not-a-real-hash"));
    }
}