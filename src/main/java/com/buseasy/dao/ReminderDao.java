package com.buseasy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.buseasy.config.DBConnection;
import com.buseasy.model.Bus;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.Reminder;
import com.buseasy.model.Route;
import com.buseasy.model.Ticket;

public class ReminderDao {

    public void insertAll(List<Reminder> reminders) throws SQLException {
        if (reminders.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO ticket_reminders "
                   + "(ticket_id, user_id, offset_minutes, remind_at, delivered_at) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Reminder reminder : reminders) {
                stmt.setInt(1, reminder.getTicketId());
                stmt.setInt(2, reminder.getUserId());
                stmt.setInt(3, reminder.getOffsetMinutes());
                stmt.setTimestamp(4, Timestamp.valueOf(reminder.getRemindAt()));
                if (reminder.getDeliveredAt() == null) {
                    stmt.setNull(5, Types.TIMESTAMP);
                } else {
                    stmt.setTimestamp(5, Timestamp.valueOf(reminder.getDeliveredAt()));
                }
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public List<Reminder> findDueByUserId(int userId) throws SQLException {
        String sql = "SELECT tr.id AS reminder_id, tr.ticket_id, tr.user_id AS reminder_user_id, "
                   + "tr.offset_minutes, tr.remind_at, tr.delivered_at, tr.created_at, "
                   + "t.id, t.user_id, t.qty_adult, t.qty_child, t.is_military, t.total_price, "
                   + "t.status, t.purchased_at, "
                   + "s.id AS sched_id, s.departure_time, s.arrival_time, s.price_adult, "
                   + "s.available_seats, s.status AS sched_status, "
                   + "b.id AS bus_id, b.bus_number, b.bus_name, b.total_seats, "
                   + "r.id AS route_id, r.start_destination, r.end_destination "
                   + "FROM ticket_reminders tr "
                   + "JOIN tickets t ON tr.ticket_id = t.id "
                   + "JOIN bus_schedules s ON t.schedule_id = s.id "
                   + "JOIN buses b ON s.bus_id = b.id "
                   + "JOIN routes r ON s.route_id = r.id "
                   + "WHERE tr.user_id = ? "
                   + "  AND tr.delivered_at IS NULL "
                   + "  AND tr.remind_at <= NOW() "
                   + "  AND t.status = 'VALID' "
                   + "  AND s.departure_time >= NOW() "
                   + "ORDER BY s.departure_time ASC";
        List<Reminder> reminders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reminders.add(mapRow(rs));
                }
            }
        }
        return reminders;
    }

    public void markDelivered(List<Integer> reminderIds) throws SQLException {
        if (reminderIds.isEmpty()) {
            return;
        }
        StringJoiner placeholders = new StringJoiner(", ");
        for (int i = 0; i < reminderIds.size(); i++) {
            placeholders.add("?");
        }
        String sql = "UPDATE ticket_reminders SET delivered_at = NOW() WHERE id IN ("
                   + placeholders + ")";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            for (Integer reminderId : reminderIds) {
                stmt.setInt(index++, reminderId);
            }
            stmt.executeUpdate();
        }
    }

    private Reminder mapRow(ResultSet rs) throws SQLException {
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

        Reminder reminder = new Reminder();
        reminder.setId(rs.getInt("reminder_id"));
        reminder.setTicketId(rs.getInt("ticket_id"));
        reminder.setUserId(rs.getInt("reminder_user_id"));
        reminder.setOffsetMinutes(rs.getInt("offset_minutes"));
        reminder.setRemindAt(rs.getTimestamp("remind_at").toLocalDateTime());

        Timestamp deliveredAt = rs.getTimestamp("delivered_at");
        if (deliveredAt != null) {
            reminder.setDeliveredAt(deliveredAt.toLocalDateTime());
        }

        reminder.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        reminder.setTicket(ticket);
        return reminder;
    }
}
