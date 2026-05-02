package com.buseasy.view.home;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.buseasy.model.BusSchedule;
import com.buseasy.util.DateUtil;
import com.buseasy.view.UiTheme;

/**
 * Modal dialog shown when more than one bus shares the same departure time.
 * The user picks one bus, and the chosen BusSchedule is returned.
 */
public class BusPickerDialog extends JDialog {

    private BusSchedule chosenSchedule = null;

    private BusPickerDialog(Window owner, List<BusSchedule> schedules) {
        super(owner, "Choose a Bus", ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout(8, 8));
        setMinimumSize(new Dimension(420, 300));
        getContentPane().setBackground(UiTheme.PAPER);

        JLabel title = new JLabel("Multiple buses at this time — choose one:", SwingConstants.CENTER);
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 0, 16));

        JPanel busListPanel = new JPanel();
        busListPanel.setLayout(new BoxLayout(busListPanel, BoxLayout.Y_AXIS));
        busListPanel.setOpaque(true);
        busListPanel.setBackground(UiTheme.PAPER);

        for (BusSchedule schedule : schedules) {
            JPanel busRow = buildBusRow(schedule);
            busListPanel.add(busRow);
            busListPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        JButton cancelButton = new JButton("Cancel");
        UiTheme.styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());

        JScrollPane scrollPane = new JScrollPane(busListPanel);
        UiTheme.styleScrollPane(scrollPane);

        add(title,                        BorderLayout.NORTH);
        add(scrollPane,                   BorderLayout.CENTER);
        add(cancelButton,                  BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildBusRow(BusSchedule schedule) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        UiTheme.styleSurface(row);
        row.setBorder(UiTheme.createCardBorder());
        String info = schedule.getBus().getBusNumber()
            + "  |  " + schedule.getRoute().toString()
            + "  |  Departs: " + DateUtil.formatTime(schedule.getDepartureTime())
            + "  |  Seats: " + schedule.getAvailableSeats();

        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(UiTheme.BODY);
        infoLabel.setForeground(UiTheme.TEXT_SECONDARY);
        row.add(infoLabel, BorderLayout.CENTER);

        JButton pickButton = new JButton("Pick this bus");
        UiTheme.stylePrimaryButton(pickButton);
        pickButton.addActionListener(e -> {
            chosenSchedule = schedule;
            dispose();
        });
        row.add(pickButton, BorderLayout.EAST);
        return row;
    }

    /**
     * Opens the dialog and returns the bus the user chose,
     * or null if they cancelled.
     */
    public static BusSchedule show(Window owner, List<BusSchedule> schedules) {
        BusPickerDialog dialog = new BusPickerDialog(owner, schedules);
        dialog.setVisible(true);
        return dialog.chosenSchedule;
    }
}
