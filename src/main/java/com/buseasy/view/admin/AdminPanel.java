package com.buseasy.view.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.buseasy.controller.AdminController;
import com.buseasy.model.AdminDashboardStats;
import com.buseasy.model.AdminTicketRow;
import com.buseasy.model.Bus;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.MilitaryRequest;
import com.buseasy.model.Route;
import com.buseasy.model.User;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.UiTheme;

public class AdminPanel extends JPanel {

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0 VND");

    private AdminController adminController;
    private javax.swing.Timer autoRefreshTimer;

    private final JPanel statsGrid = new JPanel(new GridLayout(2, 3, 12, 12));
    private final ProfitChartPanel profitChartPanel = new ProfitChartPanel();
    private final TicketPieChartPanel ticketPieChartPanel = new TicketPieChartPanel();
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton refreshButton = new JButton(LanguageManager.text("refresh.all"));

    private final JTextField scheduleSearchField = new JTextField(18);
    private final JComboBox<FilterChoice> scheduleStatusFilter = new JComboBox<>(new FilterChoice[] {
        new FilterChoice("ALL", "All schedules"),
        new FilterChoice("ACTIVE", "Active"),
        new FilterChoice("CANCELLED", "Cancelled")
    });
    private final DefaultTableModel scheduleModel = readOnlyModel(
        "ID", LanguageManager.text("Departure"), LanguageManager.text("Arrival"), LanguageManager.text("Bus"),
        LanguageManager.text("Route"), LanguageManager.text("Seats"), LanguageManager.text("Adult price"), LanguageManager.text("Status"));
    private final JTable scheduleTable = new JTable(scheduleModel);
    private final Map<Integer, BusSchedule> schedulesById = new HashMap<>();

    private final JTextField userSearchField = new JTextField(18);
    private final JComboBox<FilterChoice> userRoleFilter = new JComboBox<>(new FilterChoice[] {
        new FilterChoice("ALL", "All roles"),
        new FilterChoice("USER", "Customers"),
        new FilterChoice("ADMIN", "Admins")
    });
    private final DefaultTableModel userModel = readOnlyModel(
        "ID", LanguageManager.text("Username"), LanguageManager.text("Full name"), "Email",
        LanguageManager.text("Phone"), LanguageManager.text("Military"), LanguageManager.text("Role"), LanguageManager.text("Created"));
    private final JTable userTable = new JTable(userModel);

    private final JTextField ticketSearchField = new JTextField(18);
    private final JComboBox<FilterChoice> ticketStatusFilter = new JComboBox<>(new FilterChoice[] {
        new FilterChoice("ALL", "All tickets"),
        new FilterChoice("VALID", "Valid"),
        new FilterChoice("EXPIRED", "Expired"),
        new FilterChoice("CANCELLED", "Cancelled")
    });
    private final DefaultTableModel ticketModel = readOnlyModel(
        "ID", LanguageManager.text("Customer"), LanguageManager.text("Route"), LanguageManager.text("Bus"),
        LanguageManager.text("Departure"), LanguageManager.text("Passengers"), LanguageManager.text("Total"),
        LanguageManager.text("Status"), LanguageManager.text("Purchased"));
    private final JTable ticketTable = new JTable(ticketModel);

    private final JTextField militarySearchField = new JTextField(18);
    private final JComboBox<FilterChoice> militaryStatusFilter = new JComboBox<>(new FilterChoice[] {
        new FilterChoice("ALL", "All requests"),
        new FilterChoice("PENDING", "Pending"),
        new FilterChoice("APPROVED", "Approved"),
        new FilterChoice("DENIED", "Denied")
    });
    private final DefaultTableModel militaryModel = readOnlyModel(
        "ID", LanguageManager.text("User"), LanguageManager.text("Service No."), LanguageManager.text("Unit"),
        LanguageManager.text("Note"), LanguageManager.text("Status"), LanguageManager.text("Created"), LanguageManager.text("Reviewed"));
    private final JTable militaryTable = new JTable(militaryModel);

    public AdminPanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(UiTheme.PAPER);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(LanguageManager.text("dashboard"), buildDashboardTab());
        tabs.addTab(LanguageManager.text("schedules"), buildSchedulesTab());
        tabs.addTab(LanguageManager.text("users"), buildUsersTab());
        tabs.addTab(LanguageManager.text("tickets"), buildTicketsTab());
        tabs.addTab(LanguageManager.text("military.requests"), buildMilitaryRequestsTab());
        UiTheme.styleTabs(tabs);

        UiTheme.styleStatusLabel(statusLabel);
        add(buildHeader(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void setAdminController(AdminController adminController) {
        this.adminController = adminController;
        startAutoRefreshTimer();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startAutoRefreshTimer();
    }

    @Override
    public void removeNotify() {
        stopAutoRefreshTimer();
        super.removeNotify();
    }

    public void renderDashboard(AdminDashboardStats stats) {
        statsGrid.removeAll();
        statsGrid.add(buildMetricCard(LanguageManager.text("Customers"), String.valueOf(stats.getCustomerCount()), LanguageManager.text("Registered USER accounts")));
        statsGrid.add(buildMetricCard(LanguageManager.text("Admins"), String.valueOf(stats.getAdminCount()), LanguageManager.text("Accounts with admin access")));
        statsGrid.add(buildMetricCard(LanguageManager.text("Upcoming trips"), String.valueOf(stats.getUpcomingScheduleCount()), LanguageManager.text("Active future schedules")));
        statsGrid.add(buildMetricCard(LanguageManager.text("Tickets"), String.valueOf(stats.getTicketCount()), LanguageManager.text("All purchased tickets")));
        statsGrid.add(buildMetricCard(LanguageManager.text("Revenue"), moneyFormat.format(stats.getRevenue()), LanguageManager.text("Non-cancelled ticket total")));
        statsGrid.add(buildMetricCard(LanguageManager.text("Low seats"), String.valueOf(stats.getLowSeatScheduleCount()), LanguageManager.text("Active trips with 1-5 seats")));
        statsGrid.revalidate();
        statsGrid.repaint();

        profitChartPanel.setData(stats.getRevenueByWeekday());
        ticketPieChartPanel.setData(
            stats.getAdultTicketQuantity(),
            stats.getChildTicketQuantity(),
            stats.getMilitaryTicketQuantity()
        );
        profitChartPanel.revalidate();
        ticketPieChartPanel.revalidate();
        profitChartPanel.repaint();
        ticketPieChartPanel.repaint();
    }

    public void renderSchedules(List<BusSchedule> schedules) {
        schedulesById.clear();
        scheduleModel.setRowCount(0);
        for (BusSchedule schedule : schedules) {
            schedulesById.put(schedule.getId(), schedule);
            scheduleModel.addRow(new Object[] {
                schedule.getId(),
                new SortCell(schedule.getDepartureTime(), DateUtil.formatDateTime(schedule.getDepartureTime())),
                new SortCell(schedule.getArrivalTime(), DateUtil.formatDateTime(schedule.getArrivalTime())),
                schedule.getBus().getBusNumber() + " - " + schedule.getBus().getBusName(),
                schedule.getRoute().toString(),
                schedule.getAvailableSeats(),
                new SortCell(schedule.getPriceAdult(), moneyFormat.format(schedule.getPriceAdult())),
                schedule.getStatus()
            });
        }
    }

    public void renderUsers(List<User> users) {
        userModel.setRowCount(0);
        for (User user : users) {
            userModel.addRow(new Object[] {
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone() == null ? "" : user.getPhone(),
                user.isMilitary() ? "Yes" : "No",
                user.getRole(),
                user.getCreatedAt() == null ? "" : new SortCell(user.getCreatedAt(), DateUtil.formatDateTime(user.getCreatedAt()))
            });
        }
    }

    public void renderTickets(List<AdminTicketRow> tickets) {
        ticketModel.setRowCount(0);
        for (AdminTicketRow ticket : tickets) {
            ticketModel.addRow(new Object[] {
                ticket.getId(),
                ticket.getCustomerName() + " (" + ticket.getUsername() + ")",
                ticket.getSchedule().getRoute().toString(),
                ticket.getSchedule().getBus().getBusNumber(),
                new SortCell(ticket.getSchedule().getDepartureTime(), DateUtil.formatDateTime(ticket.getSchedule().getDepartureTime())),
                ticket.getPassengerCount(),
                new SortCell(ticket.getTotalPrice(), moneyFormat.format(ticket.getTotalPrice())),
                ticket.getStatus(),
                ticket.getPurchasedAt() == null ? "" : new SortCell(ticket.getPurchasedAt(), DateUtil.formatDateTime(ticket.getPurchasedAt()))
            });
        }
    }

    public void renderMilitaryRequests(List<MilitaryRequest> requests) {
        militaryModel.setRowCount(0);
        for (MilitaryRequest request : requests) {
            militaryModel.addRow(new Object[] {
                request.getId(),
                request.getFullName() + " (" + request.getUsername() + ")",
                request.getServiceNumber(),
                request.getUnitName(),
                request.getNote() == null ? "" : request.getNote(),
                request.getStatus(),
                request.getCreatedAt() == null ? "" : new SortCell(request.getCreatedAt(), DateUtil.formatDateTime(request.getCreatedAt())),
                request.getReviewedAt() == null ? "" : new SortCell(request.getReviewedAt(), DateUtil.formatDateTime(request.getReviewedAt()))
            });
        }
    }

    public String getScheduleSearchText() {
        return scheduleSearchField.getText();
    }

    public String getScheduleStatusFilter() {
        return selectedCode(scheduleStatusFilter);
    }

    public String getUserSearchText() {
        return userSearchField.getText();
    }

    public String getUserRoleFilter() {
        return selectedCode(userRoleFilter);
    }

    public String getTicketSearchText() {
        return ticketSearchField.getText();
    }

    public String getTicketStatusFilter() {
        return selectedCode(ticketStatusFilter);
    }

    public String getMilitarySearchText() {
        return militarySearchField.getText();
    }

    public String getMilitaryStatusFilter() {
        return selectedCode(militaryStatusFilter);
    }

    public int getSelectedScheduleSeats() {
        BusSchedule schedule = selectedSchedule();
        return schedule == null ? 0 : schedule.getAvailableSeats();
    }

    public double getSelectedSchedulePrice() {
        BusSchedule schedule = selectedSchedule();
        return schedule == null ? 0 : schedule.getPriceAdult();
    }

    public void showInfo(String message) {
        statusLabel.setForeground(UiTheme.TEXT_SECONDARY);
        statusLabel.setText(message);
    }

    public void showSuccess(String message) {
        statusLabel.setForeground(UiTheme.SUCCESS);
        statusLabel.setText(message);
    }

    public void showError(String message) {
        statusLabel.setForeground(UiTheme.ERROR);
        statusLabel.setText(message);
    }

    public void beginRefresh() {
        refreshButton.setEnabled(false);
        refreshButton.setText(LanguageManager.text("refreshing"));
        refreshButton.setBackground(UiTheme.TEXT_SECONDARY);
        statusLabel.setForeground(UiTheme.TEXT_SECONDARY);
        statusLabel.setText(LanguageManager.text("refreshing"));
    }

    public void finishRefresh() {
        refreshButton.setText(LanguageManager.text("refresh.all"));
        refreshButton.setBackground(UiTheme.ADMIN_BLUE);
        refreshButton.setEnabled(true);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel eyebrow = UiTheme.createEyebrow(LanguageManager.text("Admin workspace"));
        JLabel title = new JLabel("BusEasy Operations");
        title.setFont(UiTheme.DISPLAY);
        title.setForeground(UiTheme.TEXT);
        JLabel subtitle = new JLabel(LanguageManager.text("Monitor schedules, customers, roles, tickets, and requests from one focused console."));
        subtitle.setFont(UiTheme.BODY);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);

        textPanel.add(eyebrow);
        textPanel.add(title);
        textPanel.add(subtitle);

        styleAdminPrimaryButton(refreshButton);
        refreshButton.addActionListener(e -> {
            if (adminController != null) {
                beginRefresh();
                javax.swing.Timer clickFeedbackTimer = new javax.swing.Timer(150, event -> {
                    ((javax.swing.Timer) event.getSource()).stop();
                    adminController.refreshAll();
                });
                clickFeedbackTimer.setRepeats(false);
                clickFeedbackTimer.start();
            }
        });

        header.add(textPanel, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JPanel buildDashboardTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        statsGrid.setOpaque(false);

        JPanel note = buildSurfacePanel();
        note.setLayout(new GridLayout(1, 2, 12, 0));
        note.add(profitChartPanel);
        note.add(ticketPieChartPanel);

        panel.add(statsGrid, BorderLayout.NORTH);
        panel.add(note, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSchedulesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JButton searchButton = new JButton(LanguageManager.text("Search"));
        JButton addButton = new JButton(LanguageManager.text("Add new trip"));
        JButton editButton = new JButton(LanguageManager.text("Edit selected"));
        JButton cancelButton = new JButton(LanguageManager.text("Cancel trip"));
        styleAdminSecondaryButton(searchButton);
        styleAdminPrimaryButton(addButton);
        styleAdminPrimaryButton(editButton);
        styleAdminDangerButton(cancelButton);
        styleTextField(scheduleSearchField);
        styleCombo(scheduleStatusFilter);
        styleTable(scheduleTable);

        searchButton.addActionListener(e -> runScheduleSearch());
        scheduleSearchField.addActionListener(e -> runScheduleSearch());
        scheduleStatusFilter.addActionListener(e -> runScheduleSearch());
        addButton.addActionListener(e -> {
            if (adminController != null) {
                adminController.addScheduleRequested();
            }
        });
        editButton.addActionListener(e -> {
            if (adminController != null) {
                adminController.saveSchedule(promptScheduleEdit());
            }
        });
        cancelButton.addActionListener(e -> cancelSelectedSchedule());

        panel.add(buildToolbar(LanguageManager.text("search.route.bus"), scheduleSearchField, scheduleStatusFilter,
                searchButton, addButton, editButton, cancelButton),
            BorderLayout.NORTH);
        panel.add(wrapTable(scheduleTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JButton searchButton = new JButton(LanguageManager.text("Search"));
        JButton roleButton = new JButton(LanguageManager.text("Change role"));
        styleAdminSecondaryButton(searchButton);
        styleAdminPrimaryButton(roleButton);
        styleTextField(userSearchField);
        styleCombo(userRoleFilter);
        styleTable(userTable);

        searchButton.addActionListener(e -> runUserSearch());
        userSearchField.addActionListener(e -> runUserSearch());
        userRoleFilter.addActionListener(e -> runUserSearch());
        roleButton.addActionListener(e -> promptUserRoleChange());

        panel.add(buildToolbar(LanguageManager.text("search.user"), userSearchField, userRoleFilter, searchButton, roleButton), BorderLayout.NORTH);
        panel.add(wrapTable(userTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTicketsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JButton searchButton = new JButton(LanguageManager.text("Search"));
        styleAdminSecondaryButton(searchButton);
        styleTextField(ticketSearchField);
        styleCombo(ticketStatusFilter);
        styleTable(ticketTable);

        searchButton.addActionListener(e -> runTicketSearch());
        ticketSearchField.addActionListener(e -> runTicketSearch());
        ticketStatusFilter.addActionListener(e -> runTicketSearch());

        panel.add(buildToolbar(LanguageManager.text("search.ticket"), ticketSearchField, ticketStatusFilter, searchButton), BorderLayout.NORTH);
        panel.add(wrapTable(ticketTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMilitaryRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JButton searchButton = new JButton(LanguageManager.text("Search"));
        JButton approveButton = new JButton(LanguageManager.text("Approve"));
        JButton denyButton = new JButton(LanguageManager.text("Deny"));
        styleAdminSecondaryButton(searchButton);
        styleAdminPrimaryButton(approveButton);
        styleAdminDangerButton(denyButton);
        styleTextField(militarySearchField);
        styleCombo(militaryStatusFilter);
        styleTable(militaryTable);

        searchButton.addActionListener(e -> runMilitarySearch());
        militarySearchField.addActionListener(e -> runMilitarySearch());
        militaryStatusFilter.addActionListener(e -> runMilitarySearch());
        approveButton.addActionListener(e -> reviewSelectedMilitaryRequest(true));
        denyButton.addActionListener(e -> reviewSelectedMilitaryRequest(false));

        panel.add(buildToolbar(LanguageManager.text("search.request"), militarySearchField, militaryStatusFilter,
                searchButton, approveButton, denyButton), BorderLayout.NORTH);
        panel.add(wrapTable(militaryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMetricCard(String title, String value, String helper) {
        JPanel card = buildSurfacePanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiTheme.META);
        titleLabel.setForeground(UiTheme.TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(UiTheme.SECTION_TITLE);
        valueLabel.setForeground(UiTheme.TEXT);

        JLabel helperLabel = new JLabel(helper);
        helperLabel.setFont(UiTheme.CAPTION);
        helperLabel.setForeground(UiTheme.TEXT_SECONDARY);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(helperLabel);
        return card;
    }

    private JPanel buildToolbar(String placeholder, JTextField searchField, JComboBox<FilterChoice> filterBox,
                                JButton searchButton, JButton... actionButtons) {
        JPanel toolbar = buildSurfacePanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JLabel searchLabel = new JLabel(placeholder);
        searchLabel.setFont(UiTheme.BODY);
        searchLabel.setForeground(UiTheme.TEXT_SECONDARY);

        toolbar.add(searchLabel);
        toolbar.add(searchField);
        toolbar.add(filterBox);
        toolbar.add(searchButton);
        for (JButton button : actionButtons) {
            toolbar.add(button);
        }
        return toolbar;
    }

    private JScrollPane wrapTable(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        UiTheme.styleScrollPane(scrollPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        return scrollPane;
    }

    private JPanel buildSurfacePanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UiTheme.SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.BORDER),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    private void runScheduleSearch() {
        if (adminController != null) {
            adminController.searchSchedules(getScheduleSearchText(), getScheduleStatusFilter());
        }
    }

    private void startAutoRefreshTimer() {
        if (adminController == null || autoRefreshTimer != null) {
            return;
        }
        autoRefreshTimer = new javax.swing.Timer(10_000, e -> adminController.refreshDashboard());
        autoRefreshTimer.setInitialDelay(10_000);
        autoRefreshTimer.start();
    }

    private void stopAutoRefreshTimer() {
        if (autoRefreshTimer == null) {
            return;
        }
        autoRefreshTimer.stop();
        autoRefreshTimer = null;
    }

    private void runUserSearch() {
        if (adminController != null) {
            adminController.searchUsers(getUserSearchText(), getUserRoleFilter());
        }
    }

    private void runTicketSearch() {
        if (adminController != null) {
            adminController.searchTickets(getTicketSearchText(), getTicketStatusFilter());
        }
    }

    private void runMilitarySearch() {
        if (adminController != null) {
            adminController.searchMilitaryRequests(getMilitarySearchText(), getMilitaryStatusFilter());
        }
    }

    private ScheduleEdit promptScheduleEdit() {
        BusSchedule schedule = selectedSchedule();
        if (schedule == null) {
            showError(LanguageManager.text("Select a schedule first."));
            return null;
        }

        JSpinner seatsSpinner = new JSpinner(new SpinnerNumberModel(
            schedule.getAvailableSeats(), 0, schedule.getBus().getTotalSeats(), 1));
        UiTheme.styleSpinner(seatsSpinner);

        NumberFormat numberFormat = NumberFormat.getNumberInstance();
        JFormattedTextField priceField = new JFormattedTextField(numberFormat);
        priceField.setValue(schedule.getPriceAdult());
        styleTextField(priceField);

        JComboBox<FilterChoice> statusBox = new JComboBox<>(new FilterChoice[] {
            new FilterChoice("ACTIVE", "Active"),
            new FilterChoice("CANCELLED", "Cancelled")
        });
        statusBox.setSelectedIndex("CANCELLED".equalsIgnoreCase(schedule.getStatus()) ? 1 : 0);
        styleCombo(statusBox);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel(LanguageManager.text("Available seats")));
        form.add(seatsSpinner);
        form.add(new JLabel(LanguageManager.text("Adult price")));
        form.add(priceField);
        form.add(new JLabel(LanguageManager.text("Status")));
        form.add(statusBox);

        int result = JOptionPane.showConfirmDialog(
            this, form, LanguageManager.text("Edit schedule") + " #" + schedule.getId(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        Number price = (Number) priceField.getValue();
        return new ScheduleEdit(
            schedule.getId(),
            (Integer) seatsSpinner.getValue(),
            price == null ? schedule.getPriceAdult() : price.doubleValue(),
            selectedCode(statusBox)
        );
    }

    public ScheduleCreate promptScheduleCreate(List<Bus> buses, List<Route> routes) {
        if (buses.isEmpty() || routes.isEmpty()) {
            showError(LanguageManager.text("Please seed buses and routes before adding trips."));
            return null;
        }

        JComboBox<Bus> busBox = new JComboBox<>(buses.toArray(new Bus[0]));
        JComboBox<Route> routeBox = new JComboBox<>(routes.toArray(new Route[0]));
        JFormattedTextField departureField = new JFormattedTextField(DateUtil.formatDateTime(LocalDateTime.now().plusDays(1)));
        JFormattedTextField arrivalField = new JFormattedTextField(DateUtil.formatDateTime(LocalDateTime.now().plusDays(1).plusHours(3)));
        JSpinner seatsSpinner = new JSpinner(new SpinnerNumberModel(buses.get(0).getTotalSeats(), 0, 999, 1));
        JFormattedTextField priceField = new JFormattedTextField(NumberFormat.getNumberInstance());
        priceField.setValue(100000);
        JComboBox<FilterChoice> statusBox = new JComboBox<>(new FilterChoice[] {
            new FilterChoice("ACTIVE", "Active"),
            new FilterChoice("CANCELLED", "Cancelled")
        });

        styleCombo(busBox);
        styleCombo(routeBox);
        styleTextField(departureField);
        styleTextField(arrivalField);
        UiTheme.styleSpinner(seatsSpinner);
        styleTextField(priceField);
        styleCombo(statusBox);
        busBox.addActionListener(e -> {
            Bus selected = (Bus) busBox.getSelectedItem();
            if (selected != null) {
                seatsSpinner.setValue(selected.getTotalSeats());
            }
        });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel(LanguageManager.text("Bus")));
        form.add(busBox);
        form.add(new JLabel(LanguageManager.text("Route")));
        form.add(routeBox);
        form.add(new JLabel(LanguageManager.text("Departure") + " (dd/MM/yyyy HH:mm)"));
        form.add(departureField);
        form.add(new JLabel(LanguageManager.text("Arrival") + " (dd/MM/yyyy HH:mm)"));
        form.add(arrivalField);
        form.add(new JLabel(LanguageManager.text("Available seats")));
        form.add(seatsSpinner);
        form.add(new JLabel(LanguageManager.text("Adult price")));
        form.add(priceField);
        form.add(new JLabel(LanguageManager.text("Status")));
        form.add(statusBox);

        int result = JOptionPane.showConfirmDialog(
            this, form, LanguageManager.text("Add new trip"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        try {
            Number price = (Number) priceField.getValue();
            return new ScheduleCreate(
                (Bus) busBox.getSelectedItem(),
                (Route) routeBox.getSelectedItem(),
                DateUtil.parseDateTime(departureField.getText().trim()),
                DateUtil.parseDateTime(arrivalField.getText().trim()),
                (Integer) seatsSpinner.getValue(),
                price == null ? 0 : price.doubleValue(),
                selectedCode(statusBox)
            );
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(LanguageManager.text("Trip dates must use dd/MM/yyyy HH:mm."));
        }
    }

    private void cancelSelectedSchedule() {
        Integer id = selectedScheduleId();
        if (id == null) {
            showError(LanguageManager.text("Select a schedule first."));
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(
            this,
            LanguageManager.text("Cancel trip") + " #" + id + "? " + LanguageManager.text("Customers will no longer be able to book it."),
            LanguageManager.text("Cancel trip"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION && adminController != null) {
            adminController.cancelSchedule(id);
        }
    }

    private void promptUserRoleChange() {
        Integer userId = selectedTableId(userTable);
        if (userId == null) {
            showError(LanguageManager.text("Select a user first."));
            return;
        }

        JComboBox<FilterChoice> roleBox = new JComboBox<>(new FilterChoice[] {
            new FilterChoice("USER", "Customer"),
            new FilterChoice("ADMIN", "Admin")
        });
        styleCombo(roleBox);

        int result = JOptionPane.showConfirmDialog(
            this, roleBox, LanguageManager.text("Change role for user") + " #" + userId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION && adminController != null) {
            adminController.updateUserRole(userId, selectedCode(roleBox));
        }
    }

    private void reviewSelectedMilitaryRequest(boolean approved) {
        Integer requestId = selectedTableId(militaryTable);
        if (requestId == null) {
            showError(LanguageManager.text("Select a military request first."));
            return;
        }
        String note = JOptionPane.showInputDialog(
            this,
            approved ? LanguageManager.text("Approval note") : LanguageManager.text("Denial reason"),
            approved ? LanguageManager.text("Approve request") : LanguageManager.text("Deny request"),
            JOptionPane.PLAIN_MESSAGE
        );
        if (note == null) {
            return;
        }
        if (adminController != null) {
            adminController.reviewMilitaryRequest(requestId, approved, note);
        }
    }

    private BusSchedule selectedSchedule() {
        Integer id = selectedScheduleId();
        return id == null ? null : schedulesById.get(id);
    }

    private Integer selectedScheduleId() {
        return selectedTableId(scheduleTable);
    }

    private Integer selectedTableId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        Object value = table.getModel().getValueAt(modelRow, 0);
        if (value instanceof Integer intValue) {
            return intValue;
        }
        return Integer.valueOf(String.valueOf(value));
    }

    private DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object value = getValueAt(row, columnIndex);
                    if (value != null) {
                        return value.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    private String selectedCode(JComboBox<FilterChoice> comboBox) {
        FilterChoice choice = (FilterChoice) comboBox.getSelectedItem();
        return choice == null ? "ALL" : choice.code();
    }

    private void styleTable(JTable table) {
        table.setFont(UiTheme.BODY);
        table.setRowHeight(34);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setGridColor(UiTheme.BORDER);
        table.getTableHeader().setFont(UiTheme.META);
        table.getTableHeader().setBackground(UiTheme.SUBTLE);
        table.getTableHeader().setForeground(UiTheme.TEXT);
        if (table.getTableHeader().getDefaultRenderer() instanceof DefaultTableCellRenderer headerRenderer) {
            headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        }

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, leftRenderer);
        table.setDefaultRenderer(Number.class, leftRenderer);
        table.setDefaultRenderer(Integer.class, leftRenderer);
        table.setDefaultRenderer(Double.class, leftRenderer);
        table.setDefaultRenderer(SortCell.class, leftRenderer);
    }

    private void styleTextField(JTextField field) {
        field.setFont(UiTheme.BODY);
        field.setForeground(UiTheme.TEXT);
        field.setBackground(UiTheme.SUBTLE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, UiTheme.ADMIN_BLUE),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(UiTheme.BODY);
        combo.setBackground(UiTheme.SUBTLE);
        combo.setForeground(UiTheme.TEXT);
        combo.setFocusable(false);
    }

    private void styleAdminPrimaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(UiTheme.ADMIN_BLUE);
        button.setForeground(UiTheme.SURFACE);
        button.setFont(UiTheme.BODY);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    private void styleAdminSecondaryButton(JButton button) {
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBackground(UiTheme.SURFACE);
        button.setForeground(UiTheme.ADMIN_BLUE);
        button.setFont(UiTheme.BODY);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UiTheme.ADMIN_BLUE),
            BorderFactory.createEmptyBorder(9, 15, 9, 15)
        ));
    }

    private void styleAdminDangerButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(UiTheme.ERROR);
        button.setForeground(UiTheme.SURFACE);
        button.setFont(UiTheme.BODY);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
    }

    public record ScheduleEdit(int scheduleId, int availableSeats, double priceAdult, String status) {
    }

    public record ScheduleCreate(Bus bus, Route route, LocalDateTime departureTime, LocalDateTime arrivalTime,
                                 int availableSeats, double priceAdult, String status) {
    }

    private static final class SortCell implements Comparable<SortCell> {
        private final Comparable value;
        private final String display;

        private SortCell(Comparable value, String display) {
            this.value = value;
            this.display = display;
        }

        @Override
        public int compareTo(SortCell other) {
            if (other == null) {
                return 1;
            }
            return value.compareTo(other.value);
        }

        @Override
        public String toString() {
            return display;
        }
    }

    private static final class ProfitChartPanel extends JPanel {
        private Map<String, Double> data = Map.of();

        private ProfitChartPanel() {
            setOpaque(true);
            setBackground(UiTheme.SURFACE);
            setPreferredSize(new Dimension(360, 260));
        }

        private void setData(Map<String, Double> data) {
            this.data = data == null ? Map.of() : data;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiTheme.TEXT);
            g2.setFont(UiTheme.HEADING);
            g2.drawString("Profit by weekday", 16, 26);

            int chartX = 32;
            int chartY = 52;
            int chartW = getWidth() - 54;
            int chartH = getHeight() - 92;
            double max = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            int count = Math.max(1, data.size());
            int gap = 8;
            int barW = Math.max(12, (chartW - gap * (count - 1)) / count);
            int index = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int barH = (int) Math.round((entry.getValue() / Math.max(1, max)) * chartH);
                int x = chartX + index * (barW + gap);
                int y = chartY + chartH - barH;
                g2.setColor(UiTheme.ADMIN_BLUE);
                g2.fillRect(x, y, barW, barH);
                g2.setColor(UiTheme.TEXT_SECONDARY);
                g2.setFont(UiTheme.CAPTION);
                g2.drawString(entry.getKey(), x, chartY + chartH + 18);
                index++;
            }
            g2.dispose();
        }
    }

    private static final class TicketPieChartPanel extends JPanel {
        private int adults;
        private int children;
        private int military;

        private TicketPieChartPanel() {
            setOpaque(true);
            setBackground(UiTheme.SURFACE);
            setPreferredSize(new Dimension(360, 260));
        }

        private void setData(int adults, int children, int military) {
            this.adults = Math.max(0, adults);
            this.children = Math.max(0, children);
            this.military = Math.max(0, military);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UiTheme.TEXT);
            g2.setFont(UiTheme.HEADING);
            g2.drawString("Ticket mix", 16, 26);

            int total = adults + children + military;
            int size = Math.min(getWidth() / 2, getHeight() - 76);
            int x = 30;
            int y = 52;
            if (total == 0) {
                g2.setColor(UiTheme.BORDER);
                g2.fillOval(x, y, size, size);
            } else {
                int start = 90;
                start = drawSlice(g2, x, y, size, start, adults, total, UiTheme.ADMIN_BLUE);
                start = drawSlice(g2, x, y, size, start, children, total, UiTheme.SUCCESS);
                drawSlice(g2, x, y, size, start, military, total, new Color(138, 63, 252));
            }
            drawLegend(g2, x + size + 24, y + 12, "Adult", adults, UiTheme.ADMIN_BLUE);
            drawLegend(g2, x + size + 24, y + 44, "Child", children, UiTheme.SUCCESS);
            drawLegend(g2, x + size + 24, y + 76, "Military", military, new Color(138, 63, 252));
            g2.dispose();
        }

        private int drawSlice(Graphics2D g2, int x, int y, int size, int start, int value, int total, Color color) {
            int angle = (int) Math.round((value / (double) total) * 360);
            g2.setColor(color);
            g2.fillArc(x, y, size, size, start, -angle);
            return start - angle;
        }

        private void drawLegend(Graphics2D g2, int x, int y, String label, int value, Color color) {
            g2.setColor(color);
            g2.fillRect(x, y - 10, 12, 12);
            g2.setColor(UiTheme.TEXT_SECONDARY);
            g2.setFont(UiTheme.BODY);
            g2.drawString(label + ": " + value, x + 20, y);
        }
    }

    private record FilterChoice(String code, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
