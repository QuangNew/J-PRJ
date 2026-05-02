package com.buseasy.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Persists the logged-in userId to a local file so the user
 * does not need to log in again every time the app starts.
 *
 * The token file is stored next to the running JAR.
 * It contains only the userId integer as plain text.
 */
public class SessionManager {

    private static final String TOKEN_FILE = "session.token";
    private static final int NO_SESSION    = -1;

    /** Saves the userId so the app can auto-login next time. */
    public static void saveSession(int userId) {
        try (FileWriter writer = new FileWriter(TOKEN_FILE)) {
            writer.write(String.valueOf(userId));
        } catch (IOException e) {
            System.err.println("Warning: could not save session — " + e.getMessage());
        }
    }

    /**
     * Reads the saved userId.
     *
     * @return the userId if a session exists, or -1 if none.
     */
    public static int loadSession() {
        File file = new File(TOKEN_FILE);
        if (!file.exists()) {
            return NO_SESSION;
        }
        try {
            String content = Files.readString(file.toPath()).trim();
            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            return NO_SESSION;
        }
    }

    /** Deletes the session file — used during logout. */
    public static void clearSession() {
        File file = new File(TOKEN_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}
