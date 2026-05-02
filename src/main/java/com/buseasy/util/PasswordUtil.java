package com.buseasy.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 password hashing utility.
 * All password storage and comparison must go through this class.
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Hashes a raw password using SHA-256.
     *
     * @param rawPassword the plain-text password
     * @return lowercase hex string of the hash (64 characters)
     */
    public static String hash(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes());
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Checks whether a raw password matches a stored hash.
     *
     * @param rawPassword   plain-text input from the user
     * @param storedHash    the hash stored in the database
     * @return true if they match
     */
    public static boolean matches(String rawPassword, String storedHash) {
        return hash(rawPassword).equals(storedHash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
