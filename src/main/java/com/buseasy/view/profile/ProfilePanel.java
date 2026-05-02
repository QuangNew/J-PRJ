package com.buseasy.view.profile;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.buseasy.controller.ProfileController;
import com.buseasy.model.User;
import com.buseasy.view.UiTheme;

/**
 * Tab 3 — Profile.
 * Displays and allows editing of the logged-in user's profile fields.
 */
public class ProfilePanel extends JPanel {

    private ProfileController profileController;

    private final JTextField fullNameField = new JTextField(30);
    private final JTextField emailField    = new JTextField(30);
    private final JTextField phoneField    = new JTextField(30);
    private final JCheckBox  militaryBox   = new JCheckBox("Military personnel");
    private final JLabel     errorLabel    = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel     successLabel  = new JLabel(" ", SwingConstants.CENTER);

    // Username is shown read-only — it cannot be changed
    private final JLabel usernameValue = new JLabel();

    public ProfilePanel() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UiTheme.PAPER);
        buildUI();
    }

    public void setProfileController(ProfileController controller) {
        this.profileController = controller;
    }

    /**
     * Populates all fields with data from the given user object.
     */
    public void renderUser(User user) {
        usernameValue.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        militaryBox.setSelected(user.isMilitary());
        errorLabel.setText(" ");
        successLabel.setText(" ");
    }

    public void showError(String message) {
        errorLabel.setText(message);
        successLabel.setText(" ");
    }

    public void showSuccess(String message) {
        successLabel.setText(message);
        errorLabel.setText(" ");
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        UiTheme.styleSurface(card);
        card.setBorder(UiTheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets    = new Insets(10, 16, 10, 16);
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        gbc.gridy     = 0;
        gbc.weightx   = 1;

        JLabel eyebrow = UiTheme.createEyebrow("PROFILE SETTINGS");
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(eyebrow, gbc);

        gbc.gridy++;
        JLabel title = new JLabel("My Profile", SwingConstants.CENTER);
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        card.add(title, gbc);

        gbc.gridy++;
        card.add(new JSeparator(), gbc);

        styleField(fullNameField);
        styleField(emailField);
        styleField(phoneField);

        addReadOnlyRow(card, gbc, "Username:", usernameValue);
        addEditableRow(card, gbc, "Full Name:", fullNameField);
        addEditableRow(card, gbc, "Email:",     emailField);
        addEditableRow(card, gbc, "Phone:",     phoneField);

        gbc.gridy++;
        gbc.gridwidth = 2;
        UiTheme.styleCheckBox(militaryBox);
        card.add(militaryBox, gbc);

        JButton saveButton = new JButton("Save Changes");
        UiTheme.stylePrimaryButton(saveButton);
        saveButton.addActionListener(e -> onSaveClicked());
        gbc.gridy++;
        gbc.insets = new Insets(22, 16, 10, 16);
        card.add(saveButton, gbc);

        errorLabel.setForeground(UiTheme.ERROR);
        errorLabel.setFont(UiTheme.BODY);
        gbc.gridy++;
        gbc.insets = new Insets(10, 16, 10, 16);
        card.add(errorLabel, gbc);

        successLabel.setForeground(UiTheme.SUCCESS);
        successLabel.setFont(UiTheme.BODY);
        gbc.gridy++;
        card.add(successLabel, gbc);

        // Wrap card in a scrollable container so content is always reachable
        JPanel scrollContent = new JPanel(new GridBagLayout());
        scrollContent.setOpaque(true);
        scrollContent.setBackground(UiTheme.PAPER);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx   = 0;
        outer.gridy   = 0;
        outer.weightx = 1;
        outer.weighty = 0;
        outer.fill    = GridBagConstraints.HORIZONTAL;
        outer.anchor  = GridBagConstraints.NORTH;
        outer.insets  = new Insets(40, 80, 40, 80);
        scrollContent.add(card, outer);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        UiTheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addReadOnlyRow(JPanel card, GridBagConstraints gbc, String labelText, JLabel valueLabel) {
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx   = 0;
        gbc.gridx     = 0;
        card.add(createFieldLabel(labelText), gbc);
        gbc.gridx   = 1;
        gbc.weightx = 1;
        valueLabel.setFont(UiTheme.BODY);
        valueLabel.setForeground(UiTheme.TEXT);
        card.add(valueLabel, gbc);
    }

    private void addEditableRow(JPanel card, GridBagConstraints gbc, String labelText, JTextField field) {
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx   = 0;
        gbc.gridx     = 0;
        card.add(createFieldLabel(labelText), gbc);
        gbc.gridx   = 1;
        gbc.weightx = 1;
        card.add(field, gbc);
    }

    private void onSaveClicked() {
        if (profileController == null) return;
        profileController.saveProfile(
            fullNameField.getText().trim(),
            emailField.getText().trim(),
            phoneField.getText().trim(),
            militaryBox.isSelected()
        );
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.META);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }

    private void styleField(JTextField field) {
        UiTheme.styleTextInput(field);
    }
}
