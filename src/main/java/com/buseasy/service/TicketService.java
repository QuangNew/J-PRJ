package com.buseasy.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buseasy.config.DBConnection;
import com.buseasy.dao.CartDao;
import com.buseasy.dao.ScheduleDao;
import com.buseasy.dao.TicketDao;
import com.buseasy.model.CartItem;
import com.buseasy.model.Ticket;
import com.buseasy.util.PriceCalculator;

/**
 * Handles ticket checkout and ticket history retrieval.
 */
public class TicketService {

    private final TicketDao   ticketDao   = new TicketDao();
    private final CartDao     cartDao     = new CartDao();
    private final ScheduleDao scheduleDao = new ScheduleDao();
    private final MilitaryRequestService militaryRequestService = new MilitaryRequestService();

    /**
     * Converts all cart items into tickets, then clears the cart.
     * This is the checkout flow.
     *
     * @throws RuntimeException if the cart is empty or a DB error occurs
     */
    public List<Ticket> checkout(int userId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            List<CartItem> cartItems = cartDao.findByUserId(conn, userId);
            if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("Your cart is empty.");
            }
            if (cartItems.stream().anyMatch(CartItem::isMilitary)
                    && !militaryRequestService.isApproved(userId)) {
                throw new IllegalArgumentException(
                    "Military discount is waiting for admin approval.");
            }

            reserveSeats(conn, cartItems);

            List<Ticket> tickets = convertCartItemsToTickets(userId, cartItems);
            ticketDao.insertAll(conn, tickets);
            cartDao.deleteAllByUserId(conn, userId);
            conn.commit();
            return tickets;
        } catch (IllegalArgumentException e) {
            rollbackQuietly(conn);
            throw e;
        } catch (SQLException e) {
            rollbackQuietly(conn);
            throw new RuntimeException("Checkout failed. Please try again.", e);
        } finally {
            closeQuietly(conn);
        }
    }

    /**
     * Returns this user's tickets whose departure has not yet passed.
     */
    public List<Ticket> getValidTickets(int userId) {
        try {
            return ticketDao.findValidByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load valid tickets.", e);
        }
    }

    /**
     * Returns this user's tickets whose departure has already passed.
     */
    public List<Ticket> getExpiredTickets(int userId) {
        try {
            return ticketDao.findExpiredByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load expired tickets.", e);
        }
    }

    /**
     * Marks VALID tickets as EXPIRED if their departure was more than 10 minutes ago.
     * Called before loading history to ensure the view reflects reality.
     */
    public void expireOverdueTickets(int userId) {
        try {
            ticketDao.expireOverdueTickets(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to expire overdue tickets.", e);
        }
    }

    /**
     * Cancels a ticket if its departure is still at least 48 hours away,
     * and returns the seats to the schedule.
     *
     * @throws IllegalArgumentException if the ticket is not eligible for cancellation
     */
    public void cancelTicket(Ticket ticket) {
        LocalDateTime cutoff = LocalDateTime.now().plusHours(48);
        if (!ticket.getSchedule().getDepartureTime().isAfter(cutoff)) {
            throw new IllegalArgumentException(
                "Cancellation is only allowed when departure is more than 48 hours away.");
        }
        int totalPassengers = ticket.getQtyAdult() + ticket.getQtyChild();
        try {
            ticketDao.cancelTicket(ticket.getId(), ticket.getSchedule().getId(), totalPassengers);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to cancel ticket.", e);
        }
    }

    /** Builds Ticket objects from CartItems, calculating the price for each. */
    private List<Ticket> convertCartItemsToTickets(int userId, List<CartItem> cartItems) {
        List<Ticket> tickets = new ArrayList<>();
        for (CartItem item : cartItems) {
            double price = PriceCalculator.calculate(
                item.getSchedule().getPriceAdult(),
                item.getQtyAdult(),
                item.getQtyChild(),
                item.isMilitary()
            );
            Ticket ticket = new Ticket();
            ticket.setUserId(userId);
            ticket.setSchedule(item.getSchedule());
            ticket.setQtyAdult(item.getQtyAdult());
            ticket.setQtyChild(item.getQtyChild());
            ticket.setMilitary(item.isMilitary());
            ticket.setTotalPrice(price);
            ticket.setStatus("VALID");
            tickets.add(ticket);
        }
        return tickets;
    }

    private void reserveSeats(Connection conn, List<CartItem> cartItems) throws SQLException {
        Map<Integer, Integer> seatCountBySchedule = new LinkedHashMap<>();
        Map<Integer, String> scheduleLabels = new LinkedHashMap<>();

        for (CartItem item : cartItems) {
            int scheduleId = item.getSchedule().getId();
            int passengers = item.getQtyAdult() + item.getQtyChild();
            seatCountBySchedule.merge(scheduleId, passengers, Integer::sum);
            scheduleLabels.putIfAbsent(scheduleId, describeSchedule(item));
        }

        for (Map.Entry<Integer, Integer> entry : seatCountBySchedule.entrySet()) {
            boolean reserved = scheduleDao.reserveSeats(conn, entry.getKey(), entry.getValue());
            if (!reserved) {
                throw new IllegalArgumentException(
                    "Not enough seats remaining for " + scheduleLabels.get(entry.getKey()) + "."
                );
            }
        }
    }

    private String describeSchedule(CartItem item) {
        return item.getSchedule().getBus().getBusNumber()
            + " (" + item.getSchedule().getRoute() + ")";
    }

    private void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("Rollback failed: " + e.getMessage());
        }
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }
}
