package com.buseasy.view.home;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.buseasy.controller.HomeController;
import com.buseasy.model.BusSchedule;
import com.buseasy.util.DateUtil;
import com.buseasy.view.UiTheme;

/**
 * Shows the list of departure times for a selected day.
 * If more than one bus shares a departure time, the row shows
 * "N buses available" and expands to a BusPickerDialog on click.
 * A single-bus row opens the AddToCartDialog directly.
 */
public class TimelinePanel extends JPanel {

    private HomeController homeController;

    private final JLabel titleLabel   = new JLabel("", SwingConstants.CENTER);
    private final JPanel listPanel    = new JPanel();

    public TimelinePanel() {
        setLayout(new BorderLayout(4, 8));
        setOpaque(false);

        titleLabel.setFont(UiTheme.SECTION_TITLE);
        titleLabel.setForeground(UiTheme.TEXT);

        JButton backButton = new JButton("< Back to Calendar");
        UiTheme.styleSecondaryButton(backButton);
        backButton.addActionListener(e -> {
            Container parent = getParent();
            if (parent instanceof JPanel homePanel) {
                ((CardLayout) homePanel.getLayout()).show(homePanel, "CALENDAR");
            }
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 12, 0));
        topBar.add(backButton,  BorderLayout.WEST);
        topBar.add(titleLabel,  BorderLayout.CENTER);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(true);
        listPanel.setBackground(UiTheme.PAPER);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        UiTheme.styleScrollPane(scrollPane);

        add(topBar,                          BorderLayout.NORTH);
        add(scrollPane,                      BorderLayout.CENTER);
    }

    public void setHomeController(HomeController controller) {
        this.homeController = controller;
    }

    /**
     * Renders schedule rows for the given date.
     * Groups schedules that share the same departure-hour:minute together.
     */
    public void renderSchedules(LocalDate date, List<BusSchedule> schedules) {
        titleLabel.setText("Schedules on " + DateUtil.formatDate(date));
        listPanel.removeAll();

        if (schedules.isEmpty()) {
            listPanel.add(createEmptyState("No departures scheduled for this day."));
        } else {
            // Group by departure time string (HH:mm)
            Map<String, List<BusSchedule>> byTime = groupByDepartureTime(schedules);
            for (Map.Entry<String, List<BusSchedule>> entry : byTime.entrySet()) {
                listPanel.add(buildScheduleRow(entry.getKey(), entry.getValue()));
                listPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private Map<String, List<BusSchedule>> groupByDepartureTime(List<BusSchedule> schedules) {
        // LinkedHashMap keeps insertion order so times display chronologically
        Map<String, List<BusSchedule>> map = new LinkedHashMap<>();
        for (BusSchedule s : schedules) {
            String time = DateUtil.formatTime(s.getDepartureTime());
            map.computeIfAbsent(time, k -> new ArrayList<>()).add(s);
        }
        return map;
    }

    /** Builds a single row for a departure time. */
    private JPanel buildScheduleRow(String time, List<BusSchedule> busesAtThisTime) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        UiTheme.styleSurface(row);
        row.setBorder(UiTheme.createCardBorder());

        JLabel timeLabel = new JLabel(time);
        timeLabel.setFont(UiTheme.SECTION_TITLE);
        timeLabel.setForeground(UiTheme.TEXT);

        JLabel infoLabel;
        if (busesAtThisTime.size() == 1) {
            BusSchedule s = busesAtThisTime.get(0);
            infoLabel = new JLabel(s.getBus().getBusNumber()
                + " — " + s.getRoute().toString()
                + "   |   " + s.getAvailableSeats() + " seats left");
        } else {
            infoLabel = new JLabel(busesAtThisTime.size() + " buses available — click to choose");
        }
        infoLabel.setFont(UiTheme.BODY);
        infoLabel.setForeground(UiTheme.TEXT_SECONDARY);

        JButton selectButton = new JButton("Select >");
        UiTheme.stylePrimaryButton(selectButton);
        selectButton.addActionListener(e -> onSelectClicked(busesAtThisTime));

        row.add(timeLabel,    BorderLayout.WEST);
        row.add(infoLabel,    BorderLayout.CENTER);
        row.add(selectButton, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    /**
     * If one bus: open AddToCartDialog immediately.
     * If many buses: show BusPickerDialog first, then AddToCartDialog.
     */
    private void onSelectClicked(List<BusSchedule> buses) {
        BusSchedule chosen;
        if (buses.size() == 1) {
            chosen = buses.get(0);
        } else {
            chosen = BusPickerDialog.show(SwingUtilities.getWindowAncestor(this), buses);
            if (chosen == null) return; // user cancelled
        }
        AddToCartDialog.show(SwingUtilities.getWindowAncestor(this), chosen, homeController);
    }

    private JPanel createEmptyState(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        UiTheme.styleSurface(panel);
        panel.setBorder(UiTheme.createCardBorder());

        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(UiTheme.BODY);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        panel.add(label, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height + 40));
        return panel;
    }
}
