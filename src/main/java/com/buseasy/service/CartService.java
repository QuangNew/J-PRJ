package com.buseasy.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.buseasy.dao.CartDao;
import com.buseasy.dao.ScheduleDao;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.CartItem;
import com.buseasy.util.PriceCalculator;

/**
 * Manages the shopping cart.
 * Business rules: item validation, price preview.
 * Checkout is handled by TicketService.
 */
public class CartService {

    private final CartDao    cartDao    = new CartDao();
    private final ScheduleDao scheduleDao = new ScheduleDao();

    /**
     * Adds a schedule to the user's cart.
     *
     * @throws IllegalArgumentException if quantities are invalid
     * @throws RuntimeException         if the schedule does not exist or DB error
     */
    public void addToCart(int userId, int scheduleId,
                          int qtyAdult, int qtyChild, boolean isMilitary) {
        if (qtyAdult < 0 || qtyChild < 0) {
            throw new IllegalArgumentException("Quantities cannot be negative.");
        }
        if (qtyAdult + qtyChild == 0) {
            throw new IllegalArgumentException("Select at least one passenger.");
        }
        try {
            BusSchedule schedule = scheduleDao.findById(scheduleId);
            if (schedule == null) {
                throw new IllegalArgumentException("Selected schedule no longer exists.");
            }
            if (!schedule.getDepartureTime().isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Cannot book a bus that has already departed.");
            }
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setSchedule(schedule);
            item.setQtyAdult(qtyAdult);
            item.setQtyChild(qtyChild);
            item.setMilitary(isMilitary);
            cartDao.insert(item);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add item to cart.", e);
        }
    }

    /**
     * Returns all cart items for a user.
     */
    public List<CartItem> getCart(int userId) {
        try {
            return cartDao.findByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load cart.", e);
        }
    }

    /**
     * Removes a single item from the cart.
     */
    public void removeItem(int cartItemId) {
        try {
            cartDao.deleteById(cartItemId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove cart item.", e);
        }
    }

    /**
     * Updates the quantities and military flag of an existing cart item.
     */
    public void updateItem(CartItem item) {
        if (item.getQtyAdult() < 0 || item.getQtyChild() < 0) {
            throw new IllegalArgumentException("Quantities cannot be negative.");
        }
        if (item.getQtyAdult() + item.getQtyChild() == 0) {
            throw new IllegalArgumentException("Select at least one passenger.");
        }
        try {
            cartDao.updateItem(item);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update cart item.", e);
        }
    }

    /**
     * Calculates the total price for a cart item using PriceCalculator.
     */
    public double calculateItemTotal(CartItem item) {
        return PriceCalculator.calculate(
            item.getSchedule().getPriceAdult(),
            item.getQtyAdult(),
            item.getQtyChild(),
            item.isMilitary()
        );
    }
}
