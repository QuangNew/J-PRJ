package com.buseasy.controller;

import java.util.List;

import com.buseasy.model.Ticket;
import com.buseasy.service.TicketService;
import com.buseasy.view.history.HistoryPanel;

/**
 * Connects HistoryPanel events to TicketService.
 */
public class HistoryController {

    private final TicketService ticketService = new TicketService();

    private final int          userId;
    private final HistoryPanel historyPanel;

    public HistoryController(int userId, HistoryPanel historyPanel) {
        this.userId       = userId;
        this.historyPanel = historyPanel;
    }

    /** Loads valid and expired tickets and passes them to the history panel. */
    public void loadHistory() {
        try {
            ticketService.expireOverdueTickets(userId);
            List<Ticket> validTickets   = ticketService.getValidTickets(userId);
            List<Ticket> expiredTickets = ticketService.getExpiredTickets(userId);
            historyPanel.renderTickets(validTickets, expiredTickets);
        } catch (RuntimeException e) {
            historyPanel.showError("Failed to load ticket history: " + e.getMessage());
        }
    }

    /**
     * Cancels a valid ticket if departure is more than 48 hours away,
     * releases the seats, then reloads the history view.
     */
    public void cancelTicket(Ticket ticket) {
        try {
            ticketService.cancelTicket(ticket);
            loadHistory();
        } catch (IllegalArgumentException e) {
            historyPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            historyPanel.showError("Cancellation failed: " + e.getMessage());
        }
    }
}
