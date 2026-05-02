package com.buseasy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.buseasy.config.DBConnection;
import com.buseasy.model.Bus;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.Route;
import com.buseasy.model.Ticket;

/**
 * All SQL operations on the 'tickets' table.
 */
public class TicketDao {

    /**
     * Returns tickets for a user where the bus has NOT yet departed.
     * These are the "Valid" tickets in the history screen.
     */
    public List<Ticket> findValidByUserId(int userId) throws SQLException {
        String sql = buildTicketQuery()
                   + "WHERE t.user_id = ? AND s.departure_time >= NOW() "
                   + "  AND t.status = 'VALID' "
                   + "ORDER BY s.departure_time ASC";
        return queryTickets(sql, userId);
    }

    /**
     * Returns tickets for a user where the bus has already departed.
     * These are the "Expired" tickets in the history screen.
     */
    public List<Ticket> findExpiredByUserId(int userId) throws SQLException {
        String sql = buildTicketQuery()
                   + "WHERE t.user_id = ? AND s.departure_time < NOW() "
                   + "ORDER BY s.departure_time DESC";
        return queryTickets(sql, userId);
    }

    /**
     * Sets status = 'EXPIRED' on tickets whose scheduled departure was more than
     * 10 minutes ago and are still marked VALID.
     */
    public void expireOverdueTickets(int userId) throws SQLException {
        String sql = "UPDATE tickets t "
                   + "JOIN bus_schedules s ON t.schedule_id = s.id "
                   + "SET t.status = 'EXPIRED' "
                   + "WHERE t.user_id = ? AND t.status = 'VALID' "
                   + "  AND s.departure_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Inserts a list of tickets in a single database connection.
     * Used during checkout to persist all cart items at once.
     */
    public void insertAll(List<Ticket> tickets) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            insertAll(conn, tickets);
        }
    }

    /**
     * Inserts a list of tickets using the provided connection.
     */
    public void insertAll(Connection conn, List<Ticket> tickets) throws SQLException {
        String sql = "INSERT INTO tickets "
                   + "(user_id, schedule_id, qty_adult, qty_child, is_military, total_price) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (Ticket ticket : tickets) {
                stmt.setInt(1, ticket.getUserId());
                stmt.setInt(2, ticket.getSchedule().getId());
                stmt.setInt(3, ticket.getQtyAdult());
                stmt.setInt(4, ticket.getQtyChild());
                stmt.setBoolean(5, ticket.isMilitary());
                stmt.setDouble(6, ticket.getTotalPrice());
                stmt.addBatch();
            }
            stmt.executeBatch();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                int index = 0;
                while (keys.next() && index < tickets.size()) {
                    tickets.get(index).setId(keys.getInt(1));
                    index++;
                }
            }
        }
    }

    /**
     * Cancels a single VALID ticket and returns its seat count to the schedule.
     * Both operations are wrapped in one transaction.
     *
     * @param ticketId       the ticket to cancel
     * @param scheduleId     the schedule whose available_seats should be incremented
     * @param totalPassengers adult + child count on this ticket
     */
    public void cancelTicket(int ticketId, int scheduleId, int totalPassengers) throws SQLException {
        String cancelSql  = "UPDATE tickets SET status = 'CANCELLED' WHERE id = ? AND status = 'VALID'";
        String releaseSql = "UPDATE bus_schedules SET available_seats = available_seats + ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement cancel  = conn.prepareStatement(cancelSql);
                 PreparedStatement release = conn.prepareStatement(releaseSql)) {
                cancel.setInt(1, ticketId);
                int updated = cancel.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    throw new SQLException("Ticket not found or already cancelled.");
                }
                release.setInt(1, totalPassengers);
                release.setInt(2, scheduleId);
                release.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** Common SELECT fragment used by both valid and expired queries. */
    private String buildTicketQuery() {
        return "SELECT t.*, "
             + "  s.id AS sched_id, s.departure_time, s.arrival_time, "
             + "  s.price_adult, s.available_seats, s.status AS sched_status, "
             + "  b.id AS bus_id, b.bus_number, b.bus_name, b.total_seats, "
             + "  r.id AS route_id, r.start_destination, r.end_destination "
             + "FROM tickets t "
             + "JOIN bus_schedules s ON t.schedule_id = s.id "
             + "JOIN buses b ON s.bus_id = b.id "
             + "JOIN routes r ON s.route_id = r.id ";
    }

    private List<Ticket> queryTickets(String sql, int userId) throws SQLException {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapRow(rs));
                }
            }
        }
        return tickets;
    }

    /** Maps a ResultSet row (with joined schedule, bus, route) to a Ticket. */
    private Ticket mapRow(ResultSet rs) throws SQLException {
        Bus bus = new Bus(
            rs.getInt("bus_id"),
            rs.getString("bus_number"),
            rs.getString("bus_name"),
            rs.getInt("total_seats")
        );
        Route route = new Route(
            rs.getInt("route_id"),
            rs.getString("start_destination"),
            rs.getString("end_destination")
        );
        BusSchedule schedule = new BusSchedule();
        schedule.setId(rs.getInt("sched_id"));
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
        schedule.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
        schedule.setPriceAdult(rs.getDouble("price_adult"));
        schedule.setAvailableSeats(rs.getInt("available_seats"));
        schedule.setStatus(rs.getString("sched_status"));

        Ticket ticket = new Ticket();
        ticket.setId(rs.getInt("id"));
        ticket.setUserId(rs.getInt("user_id"));
        ticket.setSchedule(schedule);
        ticket.setQtyAdult(rs.getInt("qty_adult"));
        ticket.setQtyChild(rs.getInt("qty_child"));
        ticket.setMilitary(rs.getBoolean("is_military"));
        ticket.setTotalPrice(rs.getDouble("total_price"));
        ticket.setStatus(rs.getString("status"));
        ticket.setPurchasedAt(rs.getTimestamp("purchased_at").toLocalDateTime());
        return ticket;
    }
}
