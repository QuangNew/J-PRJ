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
import com.buseasy.model.CartItem;
import com.buseasy.model.Route;

/**
 * All SQL operations on the 'cart_items' table.
 */
public class CartDao {

    /**
     * Returns all cart items for a given user, newest first.
     */
    public List<CartItem> findByUserId(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByUserId(conn, userId);
        }
    }

    /**
     * Returns all cart items for a given user using the provided connection.
     */
    public List<CartItem> findByUserId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT ci.*, "
                   + "  s.id AS sched_id, s.departure_time, s.arrival_time, "
                   + "  s.price_adult, s.available_seats, s.status AS sched_status, "
                   + "  b.id AS bus_id, b.bus_number, b.bus_name, b.total_seats, "
                   + "  r.id AS route_id, r.start_destination, r.end_destination "
                   + "FROM cart_items ci "
                   + "JOIN bus_schedules s ON ci.schedule_id = s.id "
                   + "JOIN buses b ON s.bus_id = b.id "
                   + "JOIN routes r ON s.route_id = r.id "
                   + "WHERE ci.user_id = ? "
                   + "ORDER BY ci.added_at DESC";

        List<CartItem> items = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    /**
     * Inserts a new cart item and sets the generated id on the object.
     */
    public void insert(CartItem item) throws SQLException {
        String sql = "INSERT INTO cart_items (user_id, schedule_id, qty_adult, qty_child, is_military) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getUserId());
            stmt.setInt(2, item.getSchedule().getId());
            stmt.setInt(3, item.getQtyAdult());
            stmt.setInt(4, item.getQtyChild());
            stmt.setBoolean(5, item.isMilitary());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
        }
    }

    /** Removes a single cart item by its id. */
    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /** Removes all cart items belonging to a user — used after checkout. */
    public void deleteAllByUserId(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            deleteAllByUserId(conn, userId);
        }
    }

    /** Removes all cart items belonging to a user using the provided connection. */
    public void deleteAllByUserId(Connection conn, int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    /** Updates qty_adult, qty_child, and is_military for an existing cart item. */
    public void updateItem(CartItem item) throws SQLException {
        String sql = "UPDATE cart_items SET qty_adult = ?, qty_child = ?, is_military = ? "
                   + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, item.getQtyAdult());
            stmt.setInt(2, item.getQtyChild());
            stmt.setBoolean(3, item.isMilitary());
            stmt.setInt(4, item.getId());
            stmt.executeUpdate();
        }
    }

    /** Maps a ResultSet row (with joined schedule, bus, route) to a CartItem. */
    private CartItem mapRow(ResultSet rs) throws SQLException {
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

        CartItem item = new CartItem();
        item.setId(rs.getInt("id"));
        item.setUserId(rs.getInt("user_id"));
        item.setSchedule(schedule);
        item.setQtyAdult(rs.getInt("qty_adult"));
        item.setQtyChild(rs.getInt("qty_child"));
        item.setMilitary(rs.getBoolean("is_military"));
        item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
        return item;
    }
}
