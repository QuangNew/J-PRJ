package com.buseasy.controller;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.buseasy.model.CartItem;
import com.buseasy.model.Ticket;
import com.buseasy.service.CartService;
import com.buseasy.service.MilitaryRequestService;
import com.buseasy.service.NotificationService;
import com.buseasy.service.ReminderService;
import com.buseasy.service.TicketService;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.cart.CartPanel;
import com.buseasy.view.cart.ReminderSetupDialog;

/**
 * Connects CartPanel events to CartService and TicketService.
 */
public class CartController {

    private final CartService     cartService     = new CartService();
    private final TicketService   ticketService   = new TicketService();
    private final ReminderService reminderService = new ReminderService();
    private final MilitaryRequestService militaryRequestService = new MilitaryRequestService();
    private final NotificationService notificationService = new NotificationService();

    private final int      userId;
    private final CartPanel cartPanel;

    public CartController(int userId, CartPanel cartPanel) {
        this.userId    = userId;
        this.cartPanel = cartPanel;
    }

    /** Loads cart items from the database and refreshes the cart panel. */
    public void loadCart() {
        try {
            List<CartItem> items = cartService.getCart(userId);
            cartPanel.renderCart(items, cartService);
        } catch (RuntimeException e) {
            cartPanel.showError("Failed to load cart: " + e.getMessage());
        }
    }

    /** Removes an item from the cart and reloads the panel. */
    public void removeItem(int cartItemId) {
        try {
            cartService.removeItem(cartItemId);
            loadCart();
        } catch (RuntimeException e) {
            cartPanel.showError("Could not remove item: " + e.getMessage());
        }
    }

    /** Updates qty/military for a cart item and reloads the panel. */
    public void updateItem(int cartItemId, int qtyAdult, int qtyChild, boolean isMilitary) {
        try {
            com.buseasy.model.CartItem item = new com.buseasy.model.CartItem();
            item.setId(cartItemId);
            item.setQtyAdult(qtyAdult);
            item.setQtyChild(qtyChild);
            item.setMilitary(isMilitary);
            cartService.updateItem(item);
            loadCart();
        } catch (RuntimeException e) {
            cartPanel.showError("Could not update item: " + e.getMessage());
        }
    }

    /**
     * Silently persists qty/military changes without reloading the panel.
     * Used by the cart row's live-price listeners.
     */
    public void updateItemSilent(int cartItemId, int qtyAdult, int qtyChild, boolean isMilitary) {
        try {
            if (isMilitary && !canUseMilitaryDiscount()) {
                cartPanel.showError("Military discount is waiting for admin approval.");
                return;
            }
            com.buseasy.model.CartItem item = new com.buseasy.model.CartItem();
            item.setId(cartItemId);
            item.setQtyAdult(qtyAdult);
            item.setQtyChild(qtyChild);
            item.setMilitary(isMilitary);
            cartService.updateItem(item);
        } catch (RuntimeException e) {
            cartPanel.showError("Auto-save failed: " + e.getMessage());
        }
    }

    public boolean canUseMilitaryDiscount() {
        return militaryRequestService.isApproved(userId);
    }

    public String submitMilitaryRequest(String serviceNumber, String unitName, String note) {
        try {
            militaryRequestService.submitRequest(userId, serviceNumber, unitName, note);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (RuntimeException e) {
            return "Could not submit military request: " + e.getMessage();
        }
    }

    /** Runs checkout: converts all cart items to tickets. */
    public void checkout() {
        int confirm = JOptionPane.showConfirmDialog(
            cartPanel, "Confirm purchase of all items in your cart?",
            "Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<Ticket> tickets;
        try {
            tickets = ticketService.checkout(userId);
            notificationService.notifyUser(
                userId,
                LanguageManager.text("payment.success"),
                "Your checkout completed successfully.",
                "PAYMENT_SUCCESS"
            );
            loadCart();
        } catch (IllegalArgumentException e) {
            cartPanel.showError(e.getMessage());
            notifyPaymentFailed(e.getMessage());
            return;
        } catch (RuntimeException e) {
            cartPanel.showError("Checkout failed: " + e.getMessage());
            notifyPaymentFailed(e.getMessage());
            return;
        }

        Integer selectedOffset = ReminderSetupDialog.show(
            SwingUtilities.getWindowAncestor(cartPanel), tickets);

        try {
            ReminderService.ReminderSaveResult result = reminderService.saveReminders(userId, tickets, selectedOffset);
            JOptionPane.showMessageDialog(
                cartPanel,
                buildCheckoutMessage(selectedOffset, result),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            cartPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(
                cartPanel,
                "Booking successful, but the reminder could not be saved.",
                "Booking Complete",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void notifyPaymentFailed(String message) {
        try {
            notificationService.notifyUser(
                userId,
                LanguageManager.text("payment.failed"),
                message,
                "PAYMENT_FAILED"
            );
        } catch (RuntimeException ignored) {
            System.err.println("Payment failure notification could not be saved.");
        }
    }

    private String buildCheckoutMessage(Integer selectedOffset, ReminderService.ReminderSaveResult result) {
        if (selectedOffset == null) {
            return "Booking successful! No reminder was set. Check your History tab.";
        }
        if (result.getSkippedCount() == 0) {
            return "Booking successful! Reminder saved for " + result.getSavedCount()
                + " ticket(s). Check your History tab.";
        }
        return "Booking successful! Reminder saved for " + result.getSavedCount()
            + " ticket(s), and " + result.getSkippedCount()
            + " ticket(s) were skipped because the trip is too soon.";
    }
}
