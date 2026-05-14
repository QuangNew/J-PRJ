package com.buseasy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buseasy.config.DBConnection;
import com.buseasy.model.AdminDashboardStats;
import com.buseasy.model.AdminTicketRow;
import com.buseasy.model.Bus;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.Route;
import com.buseasy.model.User;

public class AdminDao {

    public AdminDashboardStats loadDashboardStats() throws SQLException {
        AdminDashboardStats stats = new AdminDashboardStats();
        try (Connection conn = DBConnection.getConnection()) {
            stats.setCustomerCount(queryInt(conn, "SELECT COUNT(*) FROM users WHERE role = 'USER'"));
            stats.setAdminCount(queryInt(conn, "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'"));
            stats.setUpcomingScheduleCount(queryInt(conn,
                "SELECT COUNT(*) FROM bus_schedules WHERE status = 'ACTIVE' AND departure_time > NOW()"));
            stats.setTicketCount(queryInt(conn, "SELECT COUNT(*) FROM tickets"));
            stats.setLowSeatScheduleCount(queryInt(conn,
                "SELECT COUNT(*) FROM bus_schedules "
                    + "WHERE status = 'ACTIVE' AND departure_time > NOW() AND available_seats BETWEEN 1 AND 5"));
            stats.setRevenue(queryDouble(conn,
                "SELECT COALESCE(SUM(total_price), 0) FROM tickets WHERE status <> 'CANCELLED'"));
            stats.setRevenueByWeekday(queryRevenueByWeekday(conn));
            loadTicketMix(conn, stats);
        }
        return stats;
    }

    public List<BusSchedule> findSchedules(String searchText, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder()
            .append(selectScheduleQuery())
            .append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        appendScheduleSearch(sql, params, searchText);

        if ("ACTIVE".equalsIgnoreCase(statusFilter) || "CANCELLED".equalsIgnoreCase(statusFilter)) {
            sql.append("AND s.status = ? ");
            params.add(statusFilter.toUpperCase());
        }

        sql.append("ORDER BY s.departure_time DESC");

        List<BusSchedule> schedules = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapSchedule(rs));
                }
            }
        }
        return schedules;
    }

    public void updateSchedule(int scheduleId, int availableSeats, double priceAdult, String status)
            throws SQLException {
        String sql = "UPDATE bus_schedules "
                   + "SET available_seats = ?, price_adult = ?, status = ? "
                   + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, availableSeats);
            stmt.setDouble(2, priceAdult);
            stmt.setString(3, status);
            stmt.setInt(4, scheduleId);
            stmt.executeUpdate();
        }
    }

    public void insertSchedule(BusSchedule schedule) throws SQLException {
        String sql = "INSERT INTO bus_schedules "
                   + "(bus_id, route_id, departure_time, arrival_time, price_adult, available_seats, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, schedule.getBus().getId());
            stmt.setInt(2, schedule.getRoute().getId());
            stmt.setTimestamp(3, Timestamp.valueOf(schedule.getDepartureTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(schedule.getArrivalTime()));
            stmt.setDouble(5, schedule.getPriceAdult());
            stmt.setInt(6, schedule.getAvailableSeats());
            stmt.setString(7, schedule.getStatus());
            stmt.executeUpdate();
        }
    }

    public List<Bus> findBuses() throws SQLException {
        String sql = "SELECT * FROM buses ORDER BY bus_number";
        List<Bus> buses = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                buses.add(new Bus(
                    rs.getInt("id"),
                    rs.getString("bus_number"),
                    rs.getString("bus_name"),
                    rs.getInt("total_seats")
                ));
            }
        }
        return buses;
    }

    public List<Route> findRoutes() throws SQLException {
        String sql = "SELECT * FROM routes ORDER BY start_destination, end_destination";
        List<Route> routes = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                routes.add(new Route(
                    rs.getInt("id"),
                    rs.getString("start_destination"),
                    rs.getString("end_destination")
                ));
            }
        }
        return routes;
    }

    public List<User> findUsers(String searchText, String roleFilter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        String query = normalize(searchText);
        if (!query.isBlank()) {
            String like = "%" + query + "%";
            sql.append("AND (LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? ")
               .append("OR LOWER(email) LIKE ? OR LOWER(COALESCE(phone, '')) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if ("USER".equalsIgnoreCase(roleFilter) || "ADMIN".equalsIgnoreCase(roleFilter)) {
            sql.append("AND role = ? ");
            params.add(roleFilter.toUpperCase());
        }
        sql.append("ORDER BY created_at DESC, id DESC");

        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    public void updateUserRole(int userId, String role) throws SQLException {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public List<AdminTicketRow> findTickets(String searchText, String statusFilter) throws SQLException {
        StringBuilder sql = new StringBuilder()
            .append("SELECT t.*, ")
            .append("  u.username, u.full_name, ")
            .append("  s.id AS sched_id, s.departure_time, s.arrival_time, ")
            .append("  s.price_adult, s.available_seats, s.status AS sched_status, ")
            .append("  b.id AS bus_id, b.bus_number, b.bus_name, b.total_seats, ")
            .append("  r.id AS route_id, r.start_destination, r.end_destination ")
            .append("FROM tickets t ")
            .append("JOIN users u ON t.user_id = u.id ")
            .append("JOIN bus_schedules s ON t.schedule_id = s.id ")
            .append("JOIN buses b ON s.bus_id = b.id ")
            .append("JOIN routes r ON s.route_id = r.id ")
            .append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        String query = normalize(searchText);
        if (!query.isBlank()) {
            String like = "%" + query + "%";
            sql.append("AND (LOWER(u.username) LIKE ? OR LOWER(u.full_name) LIKE ? ")
               .append("OR LOWER(b.bus_number) LIKE ? OR LOWER(b.bus_name) LIKE ? ")
               .append("OR LOWER(r.start_destination) LIKE ? OR LOWER(r.end_destination) LIKE ?) ");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if ("VALID".equalsIgnoreCase(statusFilter)
                || "EXPIRED".equalsIgnoreCase(statusFilter)
                || "CANCELLED".equalsIgnoreCase(statusFilter)) {
            sql.append("AND t.status = ? ");
            params.add(statusFilter.toUpperCase());
        }
        sql.append("ORDER BY t.purchased_at DESC, t.id DESC");

        List<AdminTicketRow> tickets = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicketRow(rs));
                }
            }
        }
        return tickets;
    }

    private String selectScheduleQuery() {
        return "SELECT s.*, "
             + "  b.id AS bus_id, b.bus_number, b.bus_name, b.total_seats, "
             + "  r.id AS route_id, r.start_destination, r.end_destination "
             + "FROM bus_schedules s "
             + "JOIN buses b ON s.bus_id = b.id "
             + "JOIN routes r ON s.route_id = r.id ";
    }

    private void appendScheduleSearch(StringBuilder sql, List<Object> params, String searchText) {
        String query = normalize(searchText);
        if (query.isBlank()) {
            return;
        }
        String like = "%" + query + "%";
        sql.append("AND (LOWER(b.bus_number) LIKE ? ")
           .append("OR LOWER(b.bus_name) LIKE ? ")
           .append("OR LOWER(r.start_destination) LIKE ? ")
           .append("OR LOWER(r.end_destination) LIKE ?) ");
        params.add(like);
        params.add(like);
        params.add(like);
        params.add(like);
    }

    private int queryInt(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private double queryDouble(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private Map<String, Double> queryRevenueByWeekday(Connection conn) throws SQLException {
        Map<String, Double> revenue = new LinkedHashMap<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            revenue.put(day.name().substring(0, 3), 0.0);
        }

        String sql = "SELECT DAYOFWEEK(purchased_at) AS weekday, COALESCE(SUM(total_price), 0) AS total "
                   + "FROM tickets WHERE status <> 'CANCELLED' "
                   + "GROUP BY DAYOFWEEK(purchased_at)";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int mysqlDay = rs.getInt("weekday");
                DayOfWeek day = switch (mysqlDay) {
                    case 1 -> DayOfWeek.SUNDAY;
                    case 2 -> DayOfWeek.MONDAY;
                    case 3 -> DayOfWeek.TUESDAY;
                    case 4 -> DayOfWeek.WEDNESDAY;
                    case 5 -> DayOfWeek.THURSDAY;
                    case 6 -> DayOfWeek.FRIDAY;
                    default -> DayOfWeek.SATURDAY;
                };
                revenue.put(day.name().substring(0, 3), rs.getDouble("total"));
            }
        }
        return revenue;
    }

    private void loadTicketMix(Connection conn, AdminDashboardStats stats) throws SQLException {
        String sql = "SELECT "
                   + "COALESCE(SUM(qty_adult), 0) AS adults, "
                   + "COALESCE(SUM(qty_child), 0) AS children, "
                   + "COALESCE(SUM(CASE WHEN is_military THEN qty_adult + qty_child ELSE 0 END), 0) AS military "
                   + "FROM tickets WHERE status <> 'CANCELLED'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.setAdultTicketQuantity(rs.getInt("adults"));
                stats.setChildTicketQuantity(rs.getInt("children"));
                stats.setMilitaryTicketQuantity(rs.getInt("military"));
            }
        }
    }

    private void bindParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer intValue) {
                stmt.setInt(i + 1, intValue);
            } else if (value instanceof Double doubleValue) {
                stmt.setDouble(i + 1, doubleValue);
            } else {
                stmt.setString(i + 1, String.valueOf(value));
            }
        }
    }

    private BusSchedule mapSchedule(ResultSet rs) throws SQLException {
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
        schedule.setId(rs.getInt("id"));
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setDepartureTime(rs.getTimestamp("departure_time").toLocalDateTime());
        schedule.setArrivalTime(rs.getTimestamp("arrival_time").toLocalDateTime());
        schedule.setPriceAdult(rs.getDouble("price_adult"));
        schedule.setAvailableSeats(rs.getInt("available_seats"));
        schedule.setStatus(rs.getString("status"));
        return schedule;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setMilitary(rs.getBoolean("is_military"));
        user.setRole(rs.getString("role"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }
        return user;
    }

    private AdminTicketRow mapTicketRow(ResultSet rs) throws SQLException {
        AdminTicketRow row = new AdminTicketRow();
        row.setId(rs.getInt("id"));
        row.setCustomerName(rs.getString("full_name"));
        row.setUsername(rs.getString("username"));
        row.setSchedule(mapTicketSchedule(rs));
        row.setQtyAdult(rs.getInt("qty_adult"));
        row.setQtyChild(rs.getInt("qty_child"));
        row.setMilitary(rs.getBoolean("is_military"));
        row.setTotalPrice(rs.getDouble("total_price"));
        row.setStatus(rs.getString("status"));
        Timestamp purchasedAt = rs.getTimestamp("purchased_at");
        if (purchasedAt != null) {
            row.setPurchasedAt(purchasedAt.toLocalDateTime());
        }
        return row;
    }

    private BusSchedule mapTicketSchedule(ResultSet rs) throws SQLException {
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
        return schedule;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }
}
