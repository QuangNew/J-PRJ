package com.buseasy.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

/**
 * Shared visual tokens and styling helpers for the BusEasy Swing UI.
 * Inspired by VoltAgent's Uber DESIGN.md: bold monochrome surfaces,
 * pill-shaped actions, dense cards, and an 8px spacing rhythm.
 */
public final class UiTheme {

    public static final Color INK = new Color(17, 17, 17);
    public static final Color PAPER = new Color(244, 244, 242);
    public static final Color SURFACE = Color.WHITE;
    public static final Color SUBTLE = new Color(239, 239, 239);
    public static final Color HOVER = new Color(226, 226, 226);
    public static final Color TEXT = INK;
    public static final Color TEXT_SECONDARY = new Color(75, 75, 75);
    public static final Color TEXT_MUTED = new Color(140, 140, 140);
    public static final Color BORDER = new Color(223, 223, 223);
    public static final Color SUCCESS = new Color(19, 105, 60);
    public static final Color ERROR = new Color(176, 41, 41);

    public static final Font DISPLAY = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font SECTION_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font CAPTION = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font META = new Font("Segoe UI", Font.BOLD, 12);

    private UiTheme() {
    }

    public static void stylePage(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(PAPER);
    }

    public static void styleSurface(JComponent component) {
        component.setOpaque(true);
        component.setBackground(SURFACE);
        component.setForeground(TEXT);
    }

    public static void stylePrimaryButton(AbstractButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(INK);
        button.setForeground(SURFACE);
        button.setFont(HEADING);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
    }

    public static void styleSecondaryButton(AbstractButton button) {
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(SURFACE);
        button.setForeground(TEXT);
        button.setFont(BODY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(createRoundedBorder(BORDER, 12, 18));
    }

    public static void styleLinkButton(AbstractButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(TEXT);
        button.setFont(BODY);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
    }

    public static void styleTextInput(JTextComponent field) {
        field.setFont(BODY);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(SURFACE);
        field.setBorder(createRoundedBorder(BORDER, 12, 12));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 42));
    }

    public static void styleCheckBox(JCheckBox box) {
        box.setOpaque(false);
        box.setFont(BODY);
        box.setForeground(TEXT_SECONDARY);
        box.setFocusPainted(false);
    }

    public static void styleSpinner(JSpinner spinner) {
        Dimension size = new Dimension(88, 42);
        spinner.setFont(BODY);
        spinner.setForeground(TEXT);
        spinner.setBackground(SURFACE);
        spinner.setPreferredSize(size);
        spinner.setMinimumSize(size);
        spinner.setBorder(createRoundedBorder(BORDER, 7, 8));

        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JFormattedTextField field = defaultEditor.getTextField();
            field.setFont(HEADING);
            field.setForeground(TEXT);
            field.setBackground(SURFACE);
            field.setHorizontalAlignment(SwingConstants.CENTER);
            field.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        }
    }

    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PAPER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    }

    public static void styleTabs(JTabbedPane tabs) {
        tabs.setFont(HEADING);
        tabs.setBackground(SURFACE);
        tabs.setForeground(TEXT);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
    }

    public static void styleStatusLabel(JLabel label) {
        label.setOpaque(true);
        label.setFont(CAPTION);
        label.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        label.setBackground(SURFACE);
        label.setForeground(TEXT_SECONDARY);
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        );
    }

    public static Border createRoundedBorder(Color borderColor, int vertical, int horizontal) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderColor, 1, true),
            BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal)
        );
    }

    public static JLabel createEyebrow(String text) {
        JLabel label = new JLabel(text);
        label.setFont(META);
        label.setForeground(TEXT_MUTED);
        return label;
    }
}
