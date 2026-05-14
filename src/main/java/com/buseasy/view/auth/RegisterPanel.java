package com.buseasy.view.auth;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.buseasy.controller.AuthController;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.UiTheme;

/**
 * Registration screen — shown when the user clicks "No account? Register".
 */
public class RegisterPanel extends JPanel {

    private final JTextField     usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField     fullNameField = new JTextField(20);
    private final JTextField     emailField    = new JTextField(20);
    private final JTextField     phoneField    = new JTextField(20);
    private final JCheckBox      militaryBox   = new JCheckBox(LanguageManager.text("Military personnel"));
    private final JLabel         errorLabel    = new JLabel(" ");

    private AuthController authController;

    public RegisterPanel() {
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
        gbc.insets    = new Insets(8, 8, 8, 8);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        gbc.gridy     = 0;
        gbc.weightx   = 1;

        JLabel eyebrow = UiTheme.createEyebrow(LanguageManager.text("FIRST TRIP, ZERO FRICTION"));
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(eyebrow, gbc);

        gbc.gridy++;
        JLabel title = new JLabel(LanguageManager.text("Create your BusEasy account"), SwingConstants.CENTER);
        title.setFont(UiTheme.DISPLAY);
        title.setForeground(UiTheme.TEXT);
        card.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel(LanguageManager.text("Save your profile once, then book routes and manage tickets from one place."), SwingConstants.CENTER);
        subtitle.setFont(UiTheme.BODY);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);
        card.add(subtitle, gbc);

        gbc.insets = new Insets(18, 8, 8, 8);
        addRow(card, gbc, LanguageManager.text("Username *"),  usernameField);
        addRow(card, gbc, LanguageManager.text("Password *"),  passwordField);
        addRow(card, gbc, LanguageManager.text("Full Name *"), fullNameField);
        addRow(card, gbc, LanguageManager.text("Email *"),     emailField);
        addRow(card, gbc, LanguageManager.text("Phone"),       phoneField);

        gbc.gridy++;
        gbc.gridwidth = 2;
        UiTheme.styleCheckBox(militaryBox);
        card.add(militaryBox, gbc);

        JButton registerButton = new JButton(LanguageManager.text("Register"));
        UiTheme.stylePrimaryButton(registerButton);
        gbc.gridy++;
        gbc.insets = new Insets(18, 8, 8, 8);
        card.add(registerButton, gbc);

        JButton backButton = new JButton(LanguageManager.text("Back to login"));
        UiTheme.styleLinkButton(backButton);
        gbc.gridy++;
        gbc.insets = new Insets(0, 8, 8, 8);
        card.add(backButton, gbc);

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

        registerButton.addActionListener(e -> onRegisterClicked());
        backButton.addActionListener(e -> onBackClicked());
    }

    /** Helper that adds a label + field row to the grid. */
    private void addRow(JPanel card, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx   = 0;
        gbc.gridx     = 0;
        card.add(createFieldLabel(labelText), gbc);
        gbc.gridx   = 1;
        gbc.weightx = 1;
        styleField(field);
        card.add(field, gbc);
    }

    private void onRegisterClicked() {
        authController.handleRegister(
            usernameField.getText().trim(),
            new String(passwordField.getPassword()),
            fullNameField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            militaryBox.isSelected(),
            this
        );
    }

    private void onBackClicked() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window instanceof com.buseasy.view.MainFrame frame) {
            frame.showLogin();
        }
    }

    /** Shows a red error message below the form. */
    public void showError(String message) {
        errorLabel.setText(message);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.META);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }

    private void styleField(JComponent field) {
        if (field instanceof JTextComponent textComponent) {
            UiTheme.styleTextInput(textComponent);
        }
    }
}
