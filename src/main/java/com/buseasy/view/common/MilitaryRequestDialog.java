package com.buseasy.view.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.buseasy.util.LanguageManager;
import com.buseasy.view.UiTheme;

public class MilitaryRequestDialog extends JDialog {

    private boolean submitted;
    private final JTextField serviceNumberField = new JTextField(24);
    private final JTextField unitNameField = new JTextField(24);
    private final JTextArea noteArea = new JTextArea(4, 24);
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);

    private MilitaryRequestDialog(Window owner) {
        super(owner, LanguageManager.text("military.form.title"), ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UiTheme.PAPER);
        buildUI();
        pack();
        setMinimumSize(new Dimension(460, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    public static MilitaryRequestForm show(Window owner) {
        MilitaryRequestDialog dialog = new MilitaryRequestDialog(owner);
        dialog.setVisible(true);
        if (!dialog.submitted) {
            return null;
        }
        return new MilitaryRequestForm(
            dialog.serviceNumberField.getText().trim(),
            dialog.unitNameField.getText().trim(),
            dialog.noteArea.getText().trim()
        );
    }

    private void buildUI() {
        JPanel card = new JPanel(new GridBagLayout());
        UiTheme.styleSurface(card);
        card.setBorder(UiTheme.createCardBorder());

        UiTheme.styleTextInput(serviceNumberField);
        UiTheme.styleTextInput(unitNameField);
        noteArea.setFont(UiTheme.BODY);
        noteArea.setForeground(UiTheme.TEXT);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(UiTheme.createRoundedBorder(UiTheme.BORDER, 8, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        JLabel title = new JLabel(LanguageManager.text("military.form.title"), SwingConstants.CENTER);
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        card.add(title, gbc);

        addRow(card, gbc, LanguageManager.text("military.service.no"), serviceNumberField);
        addRow(card, gbc, LanguageManager.text("military.unit"), unitNameField);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(createLabel(LanguageManager.text("military.note")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(new JScrollPane(noteArea), gbc);

        errorLabel.setFont(UiTheme.BODY);
        errorLabel.setForeground(UiTheme.ERROR);
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        card.add(errorLabel, gbc);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton cancelButton = new JButton("Cancel");
        JButton submitButton = new JButton(LanguageManager.text("military.submit"));
        UiTheme.styleSecondaryButton(cancelButton);
        UiTheme.stylePrimaryButton(submitButton);
        buttons.add(cancelButton);
        buttons.add(submitButton);
        gbc.gridy++;
        card.add(buttons, gbc);

        cancelButton.addActionListener(e -> dispose());
        submitButton.addActionListener(e -> submit());

        add(card, BorderLayout.CENTER);
    }

    private void addRow(JPanel card, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        card.add(createLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        card.add(field, gbc);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.META);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }

    private void submit() {
        if (serviceNumberField.getText().trim().isBlank() || unitNameField.getText().trim().isBlank()) {
            errorLabel.setText("Service number and unit are required.");
            return;
        }
        submitted = true;
        dispose();
    }

    public record MilitaryRequestForm(String serviceNumber, String unitName, String note) {
    }
}
