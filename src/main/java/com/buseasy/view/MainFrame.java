package com.buseasy.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.buseasy.controller.AdminController;
import com.buseasy.controller.AuthController;
import com.buseasy.controller.CartController;
import com.buseasy.controller.HistoryController;
import com.buseasy.controller.HomeController;
import com.buseasy.controller.ProfileController;
import com.buseasy.model.AppNotification;
import com.buseasy.model.Reminder;
import com.buseasy.model.User;
import com.buseasy.service.NotificationService;
import com.buseasy.service.ReminderService;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.admin.AdminPanel;
import com.buseasy.view.auth.LoginPanel;
import com.buseasy.view.auth.RegisterPanel;
import com.buseasy.view.cart.CartPanel;
import com.buseasy.view.history.HistoryPanel;
import com.buseasy.view.home.HomePanel;
import com.buseasy.view.profile.ProfilePanel;

/**
 * Root JFrame. All screen transitions happen here.
 *
 * Uses a CardLayout with two top-level cards:
 *   "AUTH"  — login / register panels
 *   "MAIN"  — the 4-tab application
 */
public class MainFrame extends JFrame {

    private static final String CARD_AUTH = "AUTH";
    private static final String CARD_MAIN = "MAIN";

    private final CardLayout rootLayout = new CardLayout();
    private final JPanel     rootPanel  = new JPanel(rootLayout);

    // Auth screens
    private final LoginPanel    loginPanel    = new LoginPanel();
    private final RegisterPanel registerPanel = new RegisterPanel();

    // These are created fresh every time the user logs in
    private JTabbedPane mainTabs;
    private JPanel currentMainCard;
    private AuthController authController;
    private Timer reminderTimer;
    private Timer notificationTimer;
    private JButton notificationButton;

    private final ReminderService reminderService = new ReminderService();
    private final NotificationService notificationService = new NotificationService();

    public MainFrame() {
        setTitle("BusEasy — Bus Ticket System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        UiTheme.stylePage(rootPanel);

        // Auth card: login and register share one card via a nested CardLayout
        JPanel authCard = buildAuthCard();
        rootPanel.add(authCard, CARD_AUTH);

        add(rootPanel);
    }

    // ----------------------------------------------------------------
    // Screen navigation methods (called by controllers)
    // ----------------------------------------------------------------

    public void showLogin() {
        stopReminderTimer();
        stopNotificationTimer();
        switchAuthCard("LOGIN");
        rootLayout.show(rootPanel, CARD_AUTH);
    }

    public void showRegister() {
        switchAuthCard("REGISTER");
        rootLayout.show(rootPanel, CARD_AUTH);
    }

    /**
     * Builds the main 4-tab interface for the logged-in user,
     * wires up all controllers, and switches to it.
     */
    public void showMainTabs(User user) {
        stopReminderTimer();
        stopNotificationTimer();

        if (user.isAdmin()) {
            showAdminWorkspace(user);
            return;
        }

        removeCurrentMainCard();

        HomePanel    homePanel    = new HomePanel();
        HistoryPanel historyPanel = new HistoryPanel();
        ProfilePanel profilePanel = new ProfilePanel();
        CartPanel    cartPanel    = new CartPanel();

        HomeController    homeController    = new HomeController(user, homePanel);
        HistoryController historyController = new HistoryController(user.getId(), historyPanel);
        ProfileController profileController = new ProfileController(user, profilePanel);
        CartController    cartController    = new CartController(user.getId(), cartPanel);

        homePanel.setHomeController(homeController);
        historyPanel.setHistoryController(historyController);
        profilePanel.setProfileController(profileController);
        cartPanel.setCartController(cartController);

        mainTabs = new JTabbedPane();
        mainTabs.addTab(LanguageManager.text("home"),    homePanel);
        mainTabs.addTab(LanguageManager.text("cart"),    cartPanel);
        mainTabs.addTab(LanguageManager.text("profile"), profilePanel);
        mainTabs.addTab(LanguageManager.text("history"), historyPanel);
        UiTheme.styleTabs(mainTabs);

        // Custom tab components keep the language switch consistent across rebuilds.
        Font tabFont = UiTheme.HEADING;
        String[] tabLabels = {
            LanguageManager.text("home"),
            LanguageManager.text("cart"),
            LanguageManager.text("profile"),
            LanguageManager.text("history")
        };
        for (int i = 0; i < tabLabels.length; i++) {
            JLabel lbl = new JLabel(tabLabels[i]);
            lbl.setFont(tabFont);
            JPanel comp = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            comp.setOpaque(false);
            comp.add(lbl);
            mainTabs.setTabComponentAt(i, comp);
        }

        // Red badge on the Cart tab (index 1) — replace the plain comp with badge variant
        JLabel cartBadge = new JLabel();
        cartBadge.setFont(UiTheme.META);
        cartBadge.setForeground(Color.WHITE);
        cartBadge.setOpaque(true);
        cartBadge.setBackground(new Color(208, 34, 34));
        cartBadge.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 6, 1, 6));
        cartBadge.setHorizontalAlignment(SwingConstants.CENTER);
        cartBadge.setVisible(false);

        JLabel cartTabTitle = new JLabel(LanguageManager.text("cart"));
        cartTabTitle.setFont(tabFont);
        JPanel cartTabComp = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        cartTabComp.setOpaque(false);
        cartTabComp.add(cartTabTitle);
        cartTabComp.add(cartBadge);
        mainTabs.setTabComponentAt(1, cartTabComp);

        cartPanel.setBadgeUpdater(count -> {
            cartBadge.setText(count > 0 ? String.valueOf(count) : "");
            cartBadge.setVisible(count > 0);
            cartTabComp.revalidate();
            cartTabComp.repaint();
        });
        homeController.setCartBadgeUpdater(count -> {
            cartBadge.setText(count > 0 ? String.valueOf(count) : "");
            cartBadge.setVisible(count > 0);
            cartTabComp.revalidate();
            cartTabComp.repaint();
        });

        // Reload data whenever the user switches to a tab
        mainTabs.addChangeListener(e -> onTabSwitched(
            mainTabs.getSelectedIndex(),
            homeController, historyController,
            profileController, cartController));

        // Logout button in the corner
        JButton logoutButton = new JButton(LanguageManager.text("logout"));
        UiTheme.styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(e -> {
            stopReminderTimer();
            stopNotificationTimer();
            if (authController != null) {
                authController.handleLogout();
            }
        });
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(UiTheme.INK);
        topBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel welcomeLabel = new JLabel(LanguageManager.text("welcome") + user.getFullName());
        welcomeLabel.setFont(UiTheme.HEADING);
        welcomeLabel.setForeground(Color.WHITE);

        notificationButton = buildNotificationButton(user);
        topBar.add(buildLanguageSelector(user));
        topBar.add(notificationButton);
        topBar.add(welcomeLabel);
        topBar.add(logoutButton);

        JPanel mainCard = new JPanel(new BorderLayout());
        UiTheme.stylePage(mainCard);
        mainCard.add(topBar,   BorderLayout.NORTH);
        mainCard.add(mainTabs, BorderLayout.CENTER);

        currentMainCard = mainCard;
        rootPanel.add(currentMainCard, CARD_MAIN);
        rootLayout.show(rootPanel, CARD_MAIN);

        cartController.loadCart();
        homeController.loadMonth(java.time.YearMonth.now());
        startReminderTimer(user);
        startNotificationTimer(user);
    }

    private void showAdminWorkspace(User user) {
        removeCurrentMainCard();

        AdminPanel adminPanel = new AdminPanel();
        AdminController adminController = new AdminController(user, adminPanel);
        adminPanel.setAdminController(adminController);

        JButton logoutButton = new JButton(LanguageManager.text("logout"));
        UiTheme.styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(e -> {
            stopReminderTimer();
            stopNotificationTimer();
            if (authController != null) {
                authController.handleLogout();
            }
        });

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UiTheme.INK);
        topBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel(LanguageManager.text("admin.console"));
        titleLabel.setFont(UiTheme.HEADING);
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel(LanguageManager.text("welcome") + user.getFullName());
        welcomeLabel.setFont(UiTheme.BODY);
        welcomeLabel.setForeground(Color.WHITE);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        notificationButton = buildNotificationButton(user);
        rightPanel.add(buildLanguageSelector(user));
        rightPanel.add(notificationButton);
        rightPanel.add(welcomeLabel);
        rightPanel.add(logoutButton);

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        JPanel mainCard = new JPanel(new BorderLayout());
        UiTheme.stylePage(mainCard);
        mainCard.add(topBar, BorderLayout.NORTH);
        mainCard.add(adminPanel, BorderLayout.CENTER);

        currentMainCard = mainCard;
        rootPanel.add(currentMainCard, CARD_MAIN);
        rootLayout.show(rootPanel, CARD_MAIN);
        adminController.initialize();
        startNotificationTimer(user);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private void removeCurrentMainCard() {
        stopNotificationTimer();
        if (currentMainCard == null) {
            return;
        }
        rootPanel.remove(currentMainCard);
        currentMainCard = null;
        mainTabs = null;
    }

    private JButton buildNotificationButton(User user) {
        JButton button = new JButton();
        UiTheme.styleSecondaryButton(button);
        button.addActionListener(e -> showNotifications(user));
        try {
            int unread = notificationService.countUnread(user.getId());
            button.setText(LanguageManager.text("notifications") + " (" + unread + ")");
        } catch (RuntimeException e) {
            button.setText(LanguageManager.text("notifications"));
        }
        return button;
    }

    private JComboBox<LanguageManager.Language> buildLanguageSelector(User user) {
        JComboBox<LanguageManager.Language> languageBox =
            new JComboBox<>(LanguageManager.Language.values());
        languageBox.setSelectedItem(LanguageManager.getCurrent());
        languageBox.setFont(UiTheme.BODY);
        languageBox.setFocusable(false);
        languageBox.addActionListener(e -> {
            LanguageManager.Language selected = (LanguageManager.Language) languageBox.getSelectedItem();
            if (selected != null && selected != LanguageManager.getCurrent()) {
                LanguageManager.setCurrent(selected);
                showMainTabs(user);
            }
        });
        return languageBox;
    }

    private void startNotificationTimer(User user) {
        updateNotificationButton(user);
        notificationTimer = new Timer(15_000, e -> updateNotificationButton(user));
        notificationTimer.start();
    }

    private void stopNotificationTimer() {
        if (notificationTimer == null) {
            return;
        }
        notificationTimer.stop();
        notificationTimer = null;
    }

    private void updateNotificationButton(User user) {
        if (notificationButton == null) {
            return;
        }
        try {
            int unread = notificationService.countUnread(user.getId());
            notificationButton.setText(LanguageManager.text("notifications") + " (" + unread + ")");
        } catch (RuntimeException e) {
            notificationButton.setText(LanguageManager.text("notifications"));
        }
    }

    private void showNotifications(User user) {
        try {
            List<AppNotification> notifications = notificationService.getLatest(user.getId());
            if (notifications.isEmpty()) {
                JOptionPane.showMessageDialog(this, LanguageManager.text("no.notifications"),
                    LanguageManager.text("notifications"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder message = new StringBuilder();
            for (AppNotification notification : notifications) {
                message.append(notification.isRead() ? "" : "* ")
                    .append(notification.getTitle())
                    .append("\n")
                    .append(notification.getMessage())
                    .append("\n")
                    .append(notification.getCreatedAt() == null ? "" : DateUtil.formatDateTime(notification.getCreatedAt()))
                    .append("\n\n");
            }
            JOptionPane.showMessageDialog(this, message.toString(),
                LanguageManager.text("notifications"), JOptionPane.INFORMATION_MESSAGE);
            notificationService.markAllRead(user.getId());
            updateNotificationButton(user);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this, "Failed to load notifications: " + e.getMessage(),
                LanguageManager.text("notifications"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildAuthCard() {
        CardLayout authLayout = new CardLayout();
        JPanel     authCard   = new JPanel(authLayout);
        UiTheme.stylePage(authCard);

        authCard.add(loginPanel,    "LOGIN");
        authCard.add(registerPanel, "REGISTER");

        authLayout.show(authCard, "LOGIN");

        // Store the auth card and its layout so showLogin/showRegister can use them
        loginPanel.putClientProperty("authCard",   authCard);
        loginPanel.putClientProperty("authLayout", authLayout);
        registerPanel.putClientProperty("authCard",   authCard);
        registerPanel.putClientProperty("authLayout", authLayout);

        return authCard;
    }

    private void switchAuthCard(String name) {
        JPanel     authCard   = (JPanel) loginPanel.getClientProperty("authCard");
        CardLayout authLayout = (CardLayout) loginPanel.getClientProperty("authLayout");
        if (authCard != null && authLayout != null) {
            authLayout.show(authCard, name);
        }
    }

    /** Wires the AuthController after the frame is fully constructed. */
    public void wireAuthController(AuthController controller) {
        this.authController = controller;
        loginPanel.setAuthController(controller);
        registerPanel.setAuthController(controller);
    }

    private void startReminderTimer(User user) {
        reminderTimer = new Timer(60_000, e -> showDueReminders(user));
        reminderTimer.setInitialDelay(1_000);
        reminderTimer.start();
    }

    private void stopReminderTimer() {
        if (reminderTimer == null) {
            return;
        }
        reminderTimer.stop();
        reminderTimer = null;
    }

    private void showDueReminders(User user) {
        try {
            List<Reminder> reminders = reminderService.getDueReminders(user.getId());
            if (reminders.isEmpty()) {
                return;
            }
            JOptionPane.showMessageDialog(
                this,
                buildReminderMessage(reminders),
                "Trip Reminder",
                JOptionPane.INFORMATION_MESSAGE);
            reminderService.markDelivered(reminders);
        } catch (RuntimeException e) {
            System.err.println("Failed to load reminders: " + e.getMessage());
        }
    }

    private String buildReminderMessage(List<Reminder> reminders) {
        StringBuilder message = new StringBuilder("Your upcoming trips:\n\n");
        for (Reminder reminder : reminders) {
            message.append("• ")
                .append(reminder.getTicket().getSchedule().getRoute())
                .append(" — ")
                .append(DateUtil.formatDateTime(reminder.getTicket().getSchedule().getDepartureTime()))
                .append(" (")
                .append(reminderService.formatOffsetLabel(reminder.getOffsetMinutes()))
                .append(")\n");
        }
        return message.toString();
    }

    /** Reloads data for the tab the user just switched to. */
    private void onTabSwitched(int tabIndex,
                                HomeController    homeController,
                                HistoryController historyController,
                                ProfileController profileController,
                                CartController    cartController) {
        switch (tabIndex) {
            case 0 -> homeController.loadMonth(java.time.YearMonth.now());
            case 1 -> cartController.loadCart();
            case 2 -> profileController.loadProfile();
            case 3 -> historyController.loadHistory();
        }
    }
}
