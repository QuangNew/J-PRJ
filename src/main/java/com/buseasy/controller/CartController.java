package com.buseasy.controller;

import java.util.List;

import javax.swing.JOptionPane;

import com.buseasy.model.CartItem;
import com.buseasy.service.CartService;
import com.buseasy.service.TicketService;
import com.buseasy.view.cart.CartPanel;

/**
 * Connects CartPanel events to CartService and TicketService.
 */
public class CartController {

    private final CartService   cartService   = new CartService();
    private final TicketService ticketService = new TicketService();

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

    /** Runs checkout: converts all cart items to tickets. */
    public void checkout() {
        int confirm = JOptionPane.showConfirmDialog(
            cartPanel, "Confirm purchase of all items in your cart?",
            "Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            ticketService.checkout(userId);
            loadCart();
            JOptionPane.showMessageDialog(cartPanel,
                "Booking successful! Check your History tab.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException e) {
            cartPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            cartPanel.showError("Checkout failed: " + e.getMessage());
        }
    }
}
