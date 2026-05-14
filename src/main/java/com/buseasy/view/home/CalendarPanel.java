package com.buseasy.view.home;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.buseasy.controller.HomeController;
import com.buseasy.model.BusSchedule;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.UiTheme;

/**
 * Draws a monthly calendar grid.
 * Each day cell shows how many bus schedules exist that day.
 * Clicking a day that has buses triggers HomeController.onDaySelected().
 */
public class CalendarPanel extends JPanel {

    private HomeController homeController;
    private static final String CARD_GRID = "GRID";
    private static final String CARD_RESULTS = "RESULTS";

    private YearMonth       currentMonth;
    private final JLabel    monthLabel   = new JLabel("", SwingConstants.CENTER);
    private final JPanel    gridPanel    = new JPanel();
    private final JTextField searchField  = new JTextField(14);
    private final JComboBox<CalendarFilter> filterBox = new JComboBox<>(CalendarFilter.values());
    private final JFormattedTextField dateField = new JFormattedTextField(DateUtil.formatDate(LocalDate.now()));
    private final CardLayout contentLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(contentLayout);
    private final JPanel resultsPanel = new JPanel();

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

        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(true);
        resultsPanel.setBackground(UiTheme.PAPER);
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        UiTheme.styleScrollPane(resultsScroll);

        contentCards.setOpaque(false);
        contentCards.add(calScroll, CARD_GRID);
        contentCards.add(resultsScroll, CARD_RESULTS);
        add(contentCards, BorderLayout.CENTER);
    }

    public void setHomeController(HomeController controller) {
        this.homeController = controller;
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
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

    public void renderSearchResults(String searchText, List<BusSchedule> schedules) {
        resultsPanel.removeAll();

        JLabel title = new JLabel(LanguageManager.text("Search results for") + " \"" + searchText + "\"");
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(8, 4, 12, 4));
        resultsPanel.add(title);

        if (schedules.isEmpty()) {
            resultsPanel.add(createSearchEmptyState());
        } else {
            for (BusSchedule schedule : schedules) {
                resultsPanel.add(buildResultRow(schedule));
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
        contentLayout.show(contentCards, CARD_RESULTS);
    }

    public void showCalendarView() {
        contentLayout.show(contentCards, CARD_GRID);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private JPanel buildNavigationBar() {
        JButton prevButton = new JButton(LanguageManager.text("previous"));
        JButton nextButton = new JButton(LanguageManager.text("next"));
        JButton jumpButton = new JButton(LanguageManager.text("go"));
        UiTheme.styleSecondaryButton(prevButton);
        UiTheme.styleSecondaryButton(nextButton);
        UiTheme.stylePrimaryButton(jumpButton);
        UiTheme.styleTextInput(searchField);
        UiTheme.styleTextInput(dateField);
        searchField.setToolTipText("Search by bus, route, start, or destination");
        dateField.setColumns(10);
        dateField.setToolTipText("dd/MM/yyyy");
        filterBox.setFont(UiTheme.BODY);
        filterBox.setBackground(UiTheme.SURFACE);
        filterBox.setForeground(UiTheme.TEXT);
        filterBox.setFocusable(false);
        filterBox.setToolTipText("Filter trips shown on the calendar");
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
        searchField.addActionListener(e -> applyCalendarFilters());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onSearchChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onSearchChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onSearchChanged();
            }
        });
        filterBox.addActionListener(e -> applyCalendarFilters());
        jumpButton.addActionListener(e -> jumpToDate());

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftControls.setOpaque(false);
        leftControls.add(prevButton);
        leftControls.add(nextButton);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchControls.setOpaque(false);
        searchControls.add(new JLabel(LanguageManager.text("Filter")));
        searchControls.add(filterBox);
        searchControls.add(searchField);
        searchControls.add(Box.createHorizontalStrut(8));
        searchControls.add(new JLabel(LanguageManager.text("date")));
        searchControls.add(dateField);
        searchControls.add(jumpButton);

        bar.add(leftControls,    BorderLayout.WEST);
        bar.add(monthLabel,      BorderLayout.CENTER);
        bar.add(searchControls,  BorderLayout.EAST);
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

    /** Creates a single day cell. Every day can be clicked so empty/past states are explicit. */
    private JPanel buildDayCell(int day, int busCount) {
        JPanel cell = new JPanel(new GridLayout(2, 1));
        cell.setOpaque(true);
        LocalDate cellDate = currentMonth.atDay(day);
        boolean isPast = cellDate.isBefore(LocalDate.now());
        boolean hasTrips = busCount > 0;
        ColorState colors = resolveCellColors(hasTrips, isPast);
        cell.setBackground(colors.background);
        cell.setBorder(new LineBorder(UiTheme.BORDER, 1, true));
        // No fixed preferred size — GridLayout distributes space evenly as window resizes

        JLabel dayLabel   = new JLabel(String.valueOf(day), SwingConstants.CENTER);
        dayLabel.setFont(UiTheme.HEADING);
        dayLabel.setForeground(colors.text);

        String countText = busCount > 0 ? busCount + (busCount == 1 ? " route" : " routes") : "No trips";
        JLabel countLabel = new JLabel(countText, SwingConstants.CENTER);
        countLabel.setFont(UiTheme.CAPTION);
        countLabel.setForeground(colors.meta);

        cell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cell.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (homeController != null && currentMonth != null) {
                    homeController.onDaySelected(cellDate);
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cell.setBackground(UiTheme.HOVER);
                cell.setBorder(new LineBorder(UiTheme.INK, 1, true));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cell.setBackground(colors.background);
                cell.setBorder(new LineBorder(UiTheme.BORDER, 1, true));
            }
        });

        cell.add(dayLabel);
        cell.add(countLabel);
        return cell;
    }

    private void applyCalendarFilters() {
        if (homeController == null) {
            return;
        }
        CalendarFilter selected = (CalendarFilter) filterBox.getSelectedItem();
        homeController.onCalendarFilterChanged(
            searchField.getText(),
            selected == null ? "ALL" : selected.code
        );
    }

    private void onSearchChanged() {
        if (homeController == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> homeController.onCalendarSearchChanged(searchField.getText()));
    }

    private void jumpToDate() {
        if (homeController == null) {
            return;
        }
        homeController.onDateJumpRequested(dateField.getText().trim());
    }

    private JPanel buildResultRow(BusSchedule schedule) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        UiTheme.styleSurface(row);
        row.setBorder(UiTheme.createCardBorder());

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JLabel routeLabel = new JLabel(schedule.getRoute().toString());
        routeLabel.setFont(UiTheme.HEADING);
        routeLabel.setForeground(UiTheme.TEXT);

        JLabel metaLabel = new JLabel(
            DateUtil.formatDateTime(schedule.getDepartureTime())
                + " | " + schedule.getBus().getBusNumber()
                + " | " + schedule.getAvailableSeats() + " " + LanguageManager.text("seats left"));
        metaLabel.setFont(UiTheme.BODY);
        metaLabel.setForeground(UiTheme.TEXT_SECONDARY);

        details.add(routeLabel);
        details.add(Box.createVerticalStrut(4));
        details.add(metaLabel);

        JButton addButton = new JButton(LanguageManager.text("Add"));
        UiTheme.stylePrimaryButton(addButton);
        addButton.addActionListener(e -> AddToCartDialog.show(
            SwingUtilities.getWindowAncestor(this), schedule, homeController));

        row.add(details, BorderLayout.CENTER);
        row.add(addButton, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JPanel createSearchEmptyState() {
        JPanel panel = new JPanel(new BorderLayout());
        UiTheme.styleSurface(panel);
        panel.setBorder(UiTheme.createCardBorder());

        JLabel label = new JLabel(LanguageManager.text("No matching trips."), SwingConstants.CENTER);
        label.setFont(UiTheme.BODY);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        panel.add(label, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height + 40));
        return panel;
    }

    private ColorState resolveCellColors(boolean hasTrips, boolean isPast) {
        if (isPast) {
            return new ColorState(UiTheme.SURFACE, UiTheme.TEXT_MUTED, UiTheme.TEXT_MUTED);
        }
        if (hasTrips) {
            return new ColorState(UiTheme.SUBTLE, UiTheme.TEXT, UiTheme.TEXT_SECONDARY);
        }
        return new ColorState(UiTheme.SURFACE, UiTheme.TEXT, UiTheme.TEXT_MUTED);
    }

    private enum CalendarFilter {
        ALL("ALL", "all.trips"),
        AVAILABLE("AVAILABLE", "seats.available"),
        LOW_SEATS("LOW_SEATS", "low.seats");

        private final String code;
        private final String label;

        CalendarFilter(String code, String label) {
            this.code = code;
            this.label = label;
        }

        @Override
        public String toString() {
            return LanguageManager.text(label);
        }
    }

    private static final class ColorState {
        private final java.awt.Color background;
        private final java.awt.Color text;
        private final java.awt.Color meta;

        private ColorState(java.awt.Color background, java.awt.Color text, java.awt.Color meta) {
            this.background = background;
            this.text = text;
            this.meta = meta;
        }
    }
}
