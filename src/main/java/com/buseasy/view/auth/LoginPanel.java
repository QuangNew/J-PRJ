package com.buseasy.view.auth;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.buseasy.controller.AuthController;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.UiTheme;

/**
 * Login screen — shown on first launch or after logout.
 */
public class LoginPanel extends JPanel {

    private final JTextField     usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel         errorLabel    = new JLabel(" ");

    private AuthController authController;

    public LoginPanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UiTheme.PAPER);
        buildUI();
    }

    public void setAuthController(AuthController controller) {
        this.authController = controller;
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        UiTheme.styleSurface(card);
        card.setBorder(UiTheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(8, 8, 8, 8);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridx   = 0;
        gbc.gridy   = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        JLabel eyebrow = UiTheme.createEyebrow(LanguageManager.text("URBAN MOBILITY PLATFORM"));
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(eyebrow, gbc);

        gbc.gridy++;
        JLabel title = new JLabel(LanguageManager.text("Book your next route in seconds"), SwingConstants.CENTER);
        title.setFont(UiTheme.DISPLAY);
        title.setForeground(UiTheme.TEXT);
        card.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel(LanguageManager.text("Sign in to browse routes, manage tickets, and check out faster."), SwingConstants.CENTER);
        subtitle.setFont(UiTheme.BODY);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);
        card.add(subtitle, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(20, 8, 8, 8);
        gbc.gridwidth = 1;
        gbc.weightx   = 0;
        card.add(createFieldLabel(LanguageManager.text("Username")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        UiTheme.styleTextInput(usernameField);
        card.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 0;
        card.add(createFieldLabel(LanguageManager.text("Password")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        UiTheme.styleTextInput(passwordField);
        card.add(passwordField, gbc);

        JButton loginButton = new JButton(LanguageManager.text("Login"));
        UiTheme.stylePrimaryButton(loginButton);
        gbc.gridx    = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(18, 8, 8, 8);
        card.add(loginButton, gbc);

        JButton registerButton = new JButton(LanguageManager.text("Create an account"));
        UiTheme.styleLinkButton(registerButton);
        gbc.gridy++;
        gbc.insets = new Insets(0, 8, 8, 8);
        card.add(registerButton, gbc);

        errorLabel.setForeground(UiTheme.ERROR);
        errorLabel.setFont(UiTheme.BODY);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        card.add(errorLabel, gbc);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.insets = new Insets(32, 32, 32, 32);
        outer.anchor = GridBagConstraints.CENTER;

        JPanel scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setOpaque(true);
        scrollContent.setBackground(UiTheme.PAPER);
        scrollContent.add(card, outer);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        UiTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        loginButton.addActionListener(e -> onLoginClicked());
        registerButton.addActionListener(e -> onRegisterClicked());
        passwordField.addActionListener(e -> onLoginClicked());
    }

    private void onLoginClicked() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        authController.handleLogin(username, password, this);
    }

    private void onRegisterClicked() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof com.buseasy.view.MainFrame frame) {
            frame.showRegister();
        }
    }

    /** Shows a red error message below the form. */
    public void showError(String message) {
        errorLabel.setText(message);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(UiTheme.META);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }
}
