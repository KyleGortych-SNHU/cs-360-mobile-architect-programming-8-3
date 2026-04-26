package com.example.project_three_kyle_gortych.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Thin wrapper around the bcrypt library that hides the library's
 * {@code char[]} / {@code byte[]} conversions behind two {@code String}
 * methods. bcrypt automatically generates a random salt for every hash,
 * so hashing the same password twice yields two different strings – both
 * still verify correctly.
 *
 * <p>Cost factor 12 is a sensible default on modern phones: each hash takes
 * roughly 100–300 ms, which is slow enough to resist brute-force attacks
 * but fast enough to run on the login button click without blocking the UI
 * thread for long (we still push the work onto a background executor).
 */
public final class PasswordUtil {

    /** bcrypt work factor; 2^COST rounds per hash. */
    private static final int COST = 12;

    private PasswordUtil() { /* no instances */ }

    /**
     * Produces a salted bcrypt hash of {@code password}. The resulting
     * 60-character string embeds the algorithm, cost and salt, so the
     * database row stores everything needed to verify later.
     */
    public static String hash(String password) {
        if (password == null) {
            throw new IllegalArgumentException("password must not be null");
        }
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    /**
     * Returns {@code true} iff {@code password} matches {@code hash}. Null
     * inputs return {@code false} so callers can safely chain this against
     * a DAO lookup that might itself return null.
     */
    public static boolean verify(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        try {
            BCrypt.Result result = BCrypt.verifyer()
                    .verify(password.toCharArray(), hash);
            return result.verified;
        } catch (IllegalArgumentException e) {
            // Malformed hash – treat as a mismatch rather than crashing.
            return false;
        }
    }
}