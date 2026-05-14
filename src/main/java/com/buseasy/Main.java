package com.buseasy;

import java.util.TimeZone;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.buseasy.controller.AuthController;
import com.buseasy.view.MainFrame;

/**
 * Application entry point.
 *
 * All Swing work must run on the Event Dispatch Thread (EDT).
 * The startup sequence is:
 *   1. Create MainFrame
 *   2. Wire AuthController (which needs MainFrame to already exist)
 *   3. Ask AuthController to start — it checks for a saved session
 *      and either auto-logs in or shows the login screen
 *   4. Make the frame visible
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startApplication);
    }

    private static void startApplication() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        setSystemLookAndFeel();

        MainFrame frame = new MainFrame();

        AuthController authController = new AuthController(frame);
        frame.wireAuthController(authController);

        frame.setVisible(true);
        authController.startApp();
    }

    /** Uses the OS-native look & feel so the app looks natural on any platform. */
    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                 | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Fall back to Java's default look & feel — not a fatal error
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }
    }
}
