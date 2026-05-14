package com.buseasy.view.cart;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.buseasy.controller.CartController;
import com.buseasy.model.CartItem;
import com.buseasy.service.CartService;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.util.PriceCalculator;
import com.buseasy.view.UiTheme;
import com.buseasy.view.common.MilitaryRequestDialog;

/**
 * Tab 4 — Cart.
 * Lists all unpurchased items, shows the total price, and provides
 * Remove and Checkout buttons.
 */
public class CartPanel extends JPanel {

    private static final NumberFormat CURRENCY =
        NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private CartController cartController;
    private Consumer<Integer> badgeUpdater;

    /** Tracks each row's current calculated price for live grand-total updates. */
    private final Map<Integer, Double> rowPrices = new HashMap<>();

    private final JPanel itemListPanel = new JPanel();
    private final JLabel totalLabel    = new JLabel(LanguageManager.text("Total") + ": 0 VND", SwingConstants.RIGHT);
    private final JLabel errorLabel    = new JLabel(" ", SwingConstants.CENTER);
    private final JButton checkoutButton = new JButton(LanguageManager.text("Checkout"));

    public CartPanel() {
        setLayout(new BorderLayout(4, 8));
        setOpaque(true);
        setBackground(UiTheme.PAPER);

        JLabel title = new JLabel(LanguageManager.text("My Cart"), SwingConstants.CENTER);
        title.setFont(UiTheme.SECTION_TITLE);
        title.setForeground(UiTheme.TEXT);
        title.setBorder(BorderFactory.createEmptyBorder(16, 0, 12, 0));

        itemListPanel.setLayout(new BoxLayout(itemListPanel, BoxLayout.Y_AXIS));
        itemListPanel.setOpaque(true);
        itemListPanel.setBackground(UiTheme.PAPER);

        totalLabel.setFont(UiTheme.HEADING);
        totalLabel.setForeground(UiTheme.TEXT);

        UiTheme.stylePrimaryButton(checkoutButton);
        checkoutButton.addActionListener(e -> {
            if (cartController != null) cartController.checkout();
        });

        errorLabel.setForeground(UiTheme.ERROR);
        errorLabel.setFont(UiTheme.BODY);

        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        UiTheme.styleSurface(bottomBar);
        bottomBar.setBorder(UiTheme.createRoundedBorder(UiTheme.BORDER, 12, 16));
        bottomBar.add(totalLabel,    BorderLayout.CENTER);
        bottomBar.add(checkoutButton, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(itemListPanel);
        UiTheme.styleScrollPane(scrollPane);

        add(title,                          BorderLayout.NORTH);
        add(scrollPane,                     BorderLayout.CENTER);
        add(buildSouthPanel(bottomBar),     BorderLayout.SOUTH);
    }

    public void setCartController(CartController controller) {
        this.cartController = controller;
    }

    /** Called by MainFrame to propagate cart item count to the tab badge. */
    public void setBadgeUpdater(Consumer<Integer> updater) {
        this.badgeUpdater = updater;
    }

    /**
     * Re-renders the cart with the given items.
     * The CartService is passed in so item totals can be calculated here.
     */
    public void renderCart(List<CartItem> items, CartService cartService) {
        itemListPanel.removeAll();
        errorLabel.setText(" ");
        rowPrices.clear();

        if (items.isEmpty()) {
            itemListPanel.add(createEmptyState(LanguageManager.text("Your cart is empty.")));
        } else {
            for (CartItem item : items) {
                if (item.isMilitary() && cartController != null && !cartController.canUseMilitaryDiscount()) {
                    item.setMilitary(false);
                    cartController.updateItemSilent(item.getId(), item.getQtyAdult(), item.getQtyChild(), false);
                }
                double itemTotal = cartService.calculateItemTotal(item);
                rowPrices.put(item.getId(), itemTotal);
                itemListPanel.add(buildCartItemRow(item, itemTotal));
                itemListPanel.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        refreshGrandTotal();
        checkoutButton.setEnabled(!items.isEmpty());
        if (badgeUpdater != null) badgeUpdater.accept(items.size());
        itemListPanel.revalidate();
        itemListPanel.repaint();
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private JPanel buildCartItemRow(CartItem item, double initialTotal) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        UiTheme.styleSurface(row);
        row.setBorder(UiTheme.createCardBorder());

        // ---- left: route + departure info ----
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(2, 0, 2, 0);
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        infoPanel.add(boldLabel(item.getSchedule().getBus().getBusNumber()
            + "  \u2014  " + item.getSchedule().getRoute()), g);
        g.gridy = 1;
        infoPanel.add(createMetaLabel("Departs: " + DateUtil.formatDateTime(item.getSchedule().getDepartureTime())), g);

        // ---- right: spinners + checkbox (row 0), price + remove (row 1) ----
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        GridBagConstraints rg = new GridBagConstraints();
        rg.insets = new Insets(3, 5, 3, 5);
        rg.anchor = GridBagConstraints.EAST;

        JSpinner adultSpinner = new JSpinner(new SpinnerNumberModel(
            Math.max(1, item.getQtyAdult()), 0, 20, 1));
        UiTheme.styleSpinner(adultSpinner);
        adultSpinner.setPreferredSize(new Dimension(76, 38));
        adultSpinner.setMinimumSize(new Dimension(76, 38));
        JSpinner childSpinner = new JSpinner(new SpinnerNumberModel(
            Math.max(0, item.getQtyChild()), 0, 20, 1));
        UiTheme.styleSpinner(childSpinner);
        childSpinner.setPreferredSize(new Dimension(76, 38));
        childSpinner.setMinimumSize(new Dimension(76, 38));
        JCheckBox milBox = new JCheckBox(LanguageManager.text("Military"));
        UiTheme.styleCheckBox(milBox);
        milBox.setSelected(item.isMilitary()
            && (cartController == null || cartController.canUseMilitaryDiscount()));

        rg.gridx = 0; rg.gridy = 0; rg.gridwidth = 1;
        rightPanel.add(createMetaLabel(LanguageManager.text("Adult") + ":"), rg);
        rg.gridx = 1;
        rightPanel.add(adultSpinner, rg);
        rg.gridx = 2;
        rightPanel.add(createMetaLabel(LanguageManager.text("Child") + ":"), rg);
        rg.gridx = 3;
        rightPanel.add(childSpinner, rg);
        rg.gridx = 4;
        rightPanel.add(milBox, rg);

        JLabel priceLabel = new JLabel(CURRENCY.format((long) initialTotal) + " VND", SwingConstants.RIGHT);
        priceLabel.setFont(UiTheme.HEADING);
        priceLabel.setForeground(UiTheme.TEXT);
        rg.gridx = 0; rg.gridy = 1; rg.gridwidth = 4;
        rightPanel.add(priceLabel, rg);

        JButton removeButton = new JButton(LanguageManager.text("Remove"));
        UiTheme.styleSecondaryButton(removeButton);
        removeButton.addActionListener(e -> {
            if (cartController != null) cartController.removeItem(item.getId());
        });
        rg.gridx = 4; rg.gridy = 1; rg.gridwidth = 1;
        rightPanel.add(removeButton, rg);

        // Live price update + silent auto-save on each change
        double priceAdult = item.getSchedule().getPriceAdult();
        Runnable recalc = () -> {
            int     adults   = (Integer) adultSpinner.getValue();
            int     children = (Integer) childSpinner.getValue();
            boolean mil      = milBox.isSelected();
            double  newPrice = PriceCalculator.calculate(priceAdult, adults, children, mil);
            priceLabel.setText(CURRENCY.format((long) newPrice) + " VND");
            rowPrices.put(item.getId(), newPrice);
            refreshGrandTotal();
            if (cartController != null) {
                cartController.updateItemSilent(item.getId(), adults, children, mil);
            }
        };
        adultSpinner.addChangeListener(e -> recalc.run());
        childSpinner.addChangeListener(e -> recalc.run());
        milBox.addActionListener(e -> {
            if (milBox.isSelected() && cartController != null && !cartController.canUseMilitaryDiscount()) {
                MilitaryRequestDialog.MilitaryRequestForm form =
                    MilitaryRequestDialog.show(SwingUtilities.getWindowAncestor(this));
                if (form != null) {
                    String error = cartController.submitMilitaryRequest(
                        form.serviceNumber(), form.unitName(), form.note());
                    errorLabel.setText(error == null ? LanguageManager.text("military.pending") : error);
                }
                milBox.setSelected(false);
            }
            recalc.run();
        });

        row.add(infoPanel,  BorderLayout.CENTER);
        row.add(rightPanel, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private JLabel boldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UiTheme.HEADING);
        lbl.setForeground(UiTheme.TEXT);
        return lbl;
    }

    private void refreshGrandTotal() {
        double total = rowPrices.values().stream().mapToDouble(Double::doubleValue).sum();
        totalLabel.setText(LanguageManager.text("Total") + ": " + CURRENCY.format((long) total) + " VND");
    }

    private JPanel buildSouthPanel(JPanel bottomBar) {
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(errorLabel, BorderLayout.NORTH);
        south.add(bottomBar,  BorderLayout.SOUTH);
        return south;
    }

    private JLabel createMetaLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.BODY);
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
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
