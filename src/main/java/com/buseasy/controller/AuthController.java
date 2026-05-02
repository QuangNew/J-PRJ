package com.buseasy.controller;

import javax.swing.JOptionPane;

import com.buseasy.model.User;
import com.buseasy.service.AuthService;
import com.buseasy.util.SessionManager;
import com.buseasy.view.MainFrame;
import com.buseasy.view.auth.LoginPanel;
import com.buseasy.view.auth.RegisterPanel;

/**
 * Handles login, register, auto-login, and logout flows.
 * Owns the transition between the auth screens and the main app.
 */
public class AuthController {

    private final AuthService authService = new AuthService();
    private final MainFrame   mainFrame;

    public AuthController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * Called on app startup. If a valid session token exists, loads the user
     * and navigates directly to the home screen. Otherwise shows the login panel.
     */
    public void startApp() {
        int savedUserId = SessionManager.loadSession();
        if (savedUserId != -1) {
            User user = authService.loadUserById(savedUserId);
            if (user != null) {
                mainFrame.showMainTabs(user);
                return;
            }
        }
        mainFrame.showLogin();
    }

    /** Called when the user submits the login form. */
    public void handleLogin(String username, String password, LoginPanel panel) {
        try {
            User user = authService.login(username, password);
            SessionManager.saveSession(user.getId());
            mainFrame.showMainTabs(user);
        } catch (IllegalArgumentException e) {
            panel.showError(e.getMessage());
        } catch (RuntimeException e) {
            panel.showError("Connection error. Is XAMPP running?");
            System.err.println(e.getMessage());
        }
    }

    /** Called when the user submits the registration form. */
    public void handleRegister(String username, String password,
                                String fullName, String email,
                                String phone, boolean isMilitary,
                                RegisterPanel panel) {
        try {
            User user = authService.register(username, password, fullName, email, phone, isMilitary);
            SessionManager.saveSession(user.getId());
            mainFrame.showMainTabs(user);
        } catch (IllegalArgumentException e) {
            panel.showError(e.getMessage());
        } catch (RuntimeException e) {
            panel.showError("Connection error. Is XAMPP running?");
            System.err.println(e.getMessage());
        }
    }

    /** Clears the session and returns to the login screen. */
    public void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            mainFrame, "Are you sure you want to log out?",
            "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            SessionManager.clearSession();
            mainFrame.showLogin();
        }
    }
}
