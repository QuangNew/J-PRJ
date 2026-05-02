package com.buseasy.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.buseasy.controller.AuthController;
import com.buseasy.controller.CartController;
import com.buseasy.controller.HistoryController;
import com.buseasy.controller.HomeController;
import com.buseasy.controller.ProfileController;
import com.buseasy.model.User;
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
    private AuthController authController;

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
        // Remove any previous main tab panel before rebuilding
        if (mainTabs != null) {
            rootPanel.remove(mainTabs.getParent());
        }

        HomePanel    homePanel    = new HomePanel();
        HistoryPanel historyPanel = new HistoryPanel();
        ProfilePanel profilePanel = new ProfilePanel();
        CartPanel    cartPanel    = new CartPanel();

        HomeController    homeController    = new HomeController(user.getId(), homePanel);
        HistoryController historyController = new HistoryController(user.getId(), historyPanel);
        ProfileController profileController = new ProfileController(user, profilePanel);
        CartController    cartController    = new CartController(user.getId(), cartPanel);

        homePanel.setHomeController(homeController);
        historyPanel.setHistoryController(historyController);
        profilePanel.setProfileController(profileController);
        cartPanel.setCartController(cartController);

        mainTabs = new JTabbedPane();
        mainTabs.addTab("Home",    homePanel);
        mainTabs.addTab("Cart",    cartPanel);
        mainTabs.addTab("Profile", profilePanel);
        mainTabs.addTab("History", historyPanel);
        UiTheme.styleTabs(mainTabs);

        // Set custom tab components so emoji render via Segoe UI Emoji on Windows
        Font emojiFont = new Font("Segoe UI Emoji", Font.BOLD, 15);
        String[] tabLabels = { "🏠 Home", "🛒 Cart", "👤 Profile", "🎫 History" };
        for (int i = 0; i < tabLabels.length; i++) {
            JLabel lbl = new JLabel(tabLabels[i]);
            lbl.setFont(emojiFont);
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

        JLabel cartTabTitle = new JLabel("🛒 Cart");
        cartTabTitle.setFont(emojiFont);
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

        // Reload data whenever the user switches to a tab
        mainTabs.addChangeListener(e -> onTabSwitched(
            mainTabs.getSelectedIndex(),
            homeController, historyController,
            profileController, cartController));

        // Logout button in the corner
        JButton logoutButton = new JButton("Logout");
        UiTheme.styleSecondaryButton(logoutButton);
        logoutButton.addActionListener(e -> {
            if (authController != null) {
                authController.handleLogout();
            }
        });
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setBackground(UiTheme.INK);
        topBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName());
        welcomeLabel.setFont(UiTheme.HEADING);
        welcomeLabel.setForeground(Color.WHITE);

        topBar.add(welcomeLabel);
        topBar.add(logoutButton);

        JPanel mainCard = new JPanel(new BorderLayout());
        UiTheme.stylePage(mainCard);
        mainCard.add(topBar,   BorderLayout.NORTH);
        mainCard.add(mainTabs, BorderLayout.CENTER);

        rootPanel.add(mainCard, CARD_MAIN);
        rootLayout.show(rootPanel, CARD_MAIN);

        // Load initial data for the home tab
        homeController.loadMonth(java.time.YearMonth.now());
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

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
