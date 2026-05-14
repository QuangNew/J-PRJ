package com.buseasy.view.cart;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import com.buseasy.model.Ticket;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.UiTheme;

public class ReminderSetupDialog extends JDialog {

    private final JComboBox<ReminderOption> optionBox = new JComboBox<>(new ReminderOption[] {
        new ReminderOption(LanguageManager.text("No reminder"), null),
        new ReminderOption(LanguageManager.text("15 minutes before"), 15),
        new ReminderOption(LanguageManager.text("30 minutes before"), 30),
        new ReminderOption(LanguageManager.text("1 hour before"), 60),
        new ReminderOption(LanguageManager.text("2 hours before"), 120),
        new ReminderOption(LanguageManager.text("1 day before"), 1440),
        new ReminderOption(LanguageManager.text("2 days before"), 2880)
    });

    private Integer selectedOffset;

    private ReminderSetupDialog(Window owner, List<Ticket> tickets) {
        super(owner, LanguageManager.text("Set Reminder"), ModalityType.APPLICATION_MODAL);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(UiTheme.PAPER);
        setMinimumSize(new Dimension(520, 420));
        buildUi(tickets);
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUi(List<Ticket> tickets) {
        JPanel card = new JPanel(new GridBagLayout());
        UiTheme.styleSurface(card);
        card.setBorder(UiTheme.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel eyebrow = UiTheme.createEyebrow(LanguageManager.text("BOOKING COMPLETE"));
        eyebrow.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(eyebrow, gbc);

        gbc.gridy++;
        JLabel title = new JLabel(LanguageManager.text("Choose a reminder for this checkout"), SwingConstants.CENTER);
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        card.add(title, gbc);

        gbc.gridy++;
        JLabel hint = new JLabel(LanguageManager.text("This reminder will apply to all tickets bought just now."), SwingConstants.CENTER);
        hint.setFont(UiTheme.BODY);
        hint.setForeground(UiTheme.TEXT_SECONDARY);
        card.add(hint, gbc);

        gbc.gridy++;
        card.add(new JSeparator(), gbc);

        gbc.gridy++;
        JLabel summaryTitle = new JLabel(LanguageManager.text("Purchased tickets"));
        summaryTitle.setFont(UiTheme.META);
        summaryTitle.setForeground(UiTheme.TEXT_MUTED);
        card.add(summaryTitle, gbc);

        gbc.gridy++;
        JLabel summary = new JLabel(buildSummaryHtml(tickets));
        summary.setFont(UiTheme.BODY);
        summary.setForeground(UiTheme.TEXT);
        card.add(summary, gbc);

        gbc.gridy++;
        JLabel optionLabel = new JLabel(LanguageManager.text("Reminder time"));
        optionLabel.setFont(UiTheme.META);
        optionLabel.setForeground(UiTheme.TEXT_MUTED);
        card.add(optionLabel, gbc);

        gbc.gridy++;
        optionBox.setFont(UiTheme.BODY);
        card.add(optionBox, gbc);

        gbc.gridy++;
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton skipButton = new JButton(LanguageManager.text("Skip"));
        JButton saveButton = new JButton(LanguageManager.text("Save Reminder"));
        UiTheme.styleSecondaryButton(skipButton);
        UiTheme.stylePrimaryButton(saveButton);
        buttons.add(skipButton);
        buttons.add(saveButton);
        card.add(buttons, gbc);

        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        outer.insets = new Insets(16, 16, 16, 16);
        outer.fill = GridBagConstraints.HORIZONTAL;
        outer.weightx = 1;
        add(card, outer);

        skipButton.addActionListener(e -> {
            selectedOffset = null;
            dispose();
        });
        saveButton.addActionListener(e -> {
            ReminderOption option = (ReminderOption) optionBox.getSelectedItem();
            selectedOffset = option == null ? null : option.minutes();
            dispose();
        });
    }

    private String buildSummaryHtml(List<Ticket> tickets) {
        StringBuilder html = new StringBuilder("<html>");
        for (Ticket ticket : tickets) {
            html.append("• ")
                .append(ticket.getSchedule().getRoute())
                .append(" — ")
                .append(DateUtil.formatDateTime(ticket.getSchedule().getDepartureTime()))
                .append("<br>");
        }
        html.append("</html>");
        return html.toString();
    }

    public static Integer show(Window owner, List<Ticket> tickets) {
        ReminderSetupDialog dialog = new ReminderSetupDialog(owner, tickets);
        dialog.setVisible(true);
        return dialog.selectedOffset;
    }

    private static class ReminderOption {
        private final String label;
        private final Integer minutes;

        private ReminderOption(String label, Integer minutes) {
            this.label = label;
            this.minutes = minutes;
        }

        public Integer minutes() {
            return minutes;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
