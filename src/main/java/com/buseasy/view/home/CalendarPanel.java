package com.buseasy.view.home;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import com.buseasy.controller.HomeController;
import com.buseasy.view.UiTheme;

/**
 * Draws a monthly calendar grid.
 * Each day cell shows how many bus schedules exist that day.
 * Clicking a day that has buses triggers HomeController.onDaySelected().
 */
public class CalendarPanel extends JPanel {

    private HomeController homeController;

    private YearMonth       currentMonth;
    private final JLabel    monthLabel   = new JLabel("", SwingConstants.CENTER);
    private final JPanel    gridPanel    = new JPanel();

    public CalendarPanel() {
        setLayout(new BorderLayout(4, 4));
        setOpaque(false);
        add(buildNavigationBar(), BorderLayout.NORTH);

        gridPanel.setLayout(new GridLayout(0, 7, 4, 4));
        gridPanel.setOpaque(false);
        JPanel wrapper = new JPanel(new BorderLayout());
        UiTheme.styleSurface(wrapper);
        wrapper.setBorder(UiTheme.createCardBorder());
        wrapper.add(buildDayHeaderRow(), BorderLayout.NORTH);
        wrapper.add(gridPanel,            BorderLayout.CENTER);

        JScrollPane calScroll = new JScrollPane(wrapper);
        UiTheme.styleScrollPane(calScroll);
        add(calScroll, BorderLayout.CENTER);
    }

    public void setHomeController(HomeController controller) {
        this.homeController = controller;
    }

    /**
     * Renders the calendar for the given month, annotating each day
     * with the number of available bus schedules.
     *
     * @param month        the month to display
     * @param busCountByDay map of { day-of-month → schedule count }
     */
    public void renderMonth(YearMonth month, Map<Integer, Integer> busCountByDay) {
        this.currentMonth = month;
        monthLabel.setText(month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                           + " " + month.getYear());
        gridPanel.removeAll();

        LocalDate firstDay = month.atDay(1);
        int startOffset = firstDay.getDayOfWeek().getValue() % 7; // Sun=0 Mon=1 ...

        // Fill empty cells before the first day
        for (int i = 0; i < startOffset; i++) {
            gridPanel.add(new JLabel(""));
        }

        // Create one cell per day
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            int busCount = busCountByDay.getOrDefault(day, 0);
            gridPanel.add(buildDayCell(day, busCount));
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private JPanel buildNavigationBar() {
        JButton prevButton = new JButton("< Previous");
        JButton nextButton = new JButton("Next >");
        UiTheme.styleSecondaryButton(prevButton);
        UiTheme.styleSecondaryButton(nextButton);
        monthLabel.setFont(UiTheme.SECTION_TITLE);
        monthLabel.setForeground(UiTheme.TEXT);

        prevButton.addActionListener(e -> {
            if (homeController != null && currentMonth != null) {
                homeController.loadMonth(currentMonth.minusMonths(1));
            }
        });
        nextButton.addActionListener(e -> {
            if (homeController != null && currentMonth != null) {
                homeController.loadMonth(currentMonth.plusMonths(1));
            }
        });

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 0, 12, 0));
        bar.add(prevButton,  BorderLayout.WEST);
        bar.add(monthLabel,  BorderLayout.CENTER);
        bar.add(nextButton,  BorderLayout.EAST);
        return bar;
    }

    private JPanel buildDayHeaderRow() {
        String[] headers = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        JPanel row = new JPanel(new GridLayout(1, 7, 4, 0));
        row.setOpaque(false);
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(UiTheme.META);
            lbl.setForeground(UiTheme.TEXT_MUTED);
            row.add(lbl);
        }
        return row;
    }

    /** Creates a single day cell. Cells with buses are clickable. */
    private JPanel buildDayCell(int day, int busCount) {
        JPanel cell = new JPanel(new GridLayout(2, 1));
        cell.setOpaque(true);
        cell.setBackground(UiTheme.SURFACE);
        cell.setBorder(new LineBorder(UiTheme.BORDER, 1, true));
        // No fixed preferred size — GridLayout distributes space evenly as window resizes

        JLabel dayLabel   = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLabel.setFont(UiTheme.HEADING);
        dayLabel.setForeground(UiTheme.TEXT);

        String countText = busCount > 0 ? busCount + (busCount == 1 ? " route" : " routes") : "No trips";
        JLabel countLabel = new JLabel(countText, SwingConstants.CENTER);
        countLabel.setFont(UiTheme.CAPTION);
        countLabel.setForeground(busCount > 0 ? UiTheme.TEXT_SECONDARY : UiTheme.TEXT_MUTED);

        if (busCount > 0) {
            cell.setBackground(UiTheme.SUBTLE);
            cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cell.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (homeController != null && currentMonth != null) {
                        homeController.onDaySelected(currentMonth.atDay(day));
                    }
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    cell.setBackground(UiTheme.HOVER);
                    cell.setBorder(new LineBorder(UiTheme.INK, 1, true));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    cell.setBackground(UiTheme.SUBTLE);
                    cell.setBorder(new LineBorder(UiTheme.BORDER, 1, true));
                }
            });
        }

        cell.add(dayLabel);
        cell.add(countLabel);
        return cell;
    }
}
