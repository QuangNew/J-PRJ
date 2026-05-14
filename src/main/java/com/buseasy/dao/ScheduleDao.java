package com.buseasy.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buseasy.config.DBConnection;
import com.buseasy.model.Bus;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.Route;

/**
 * All SQL operations on the 'bus_schedules' table.
 * Also joins with 'buses' and 'routes' to populate full objects.
 */
public class ScheduleDao {

    /**
     * Returns all ACTIVE schedules whose departure date matches the given date.
     */
    public List<BusSchedule> findByDate(LocalDate date) throws SQLException {
        return findByDate(date, null, "ALL");
    }

    /**
     * Returns all ACTIVE schedules for a date after applying calendar filters.
     */
    public List<BusSchedule> findByDate(LocalDate date, String searchText, String filterCode) throws SQLException {
        StringBuilder sql = new StringBuilder(selectScheduleQuery())
            .append("WHERE DATE(s.departure_time) = ? AND s.status = 'ACTIVE' ")
            .append("  AND s.departure_time > NOW() ");
        List<Object> params = new ArrayList<>();
        params.add(Date.valueOf(date));
        appendCalendarFilters(sql, params, searchText, filterCode);
        sql.append("ORDER BY s.departure_time");

        List<BusSchedule> schedules = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapRow(rs));
                }
            }
        }
        return schedules;
    }

    public List<BusSchedule> findFutureMatches(String searchText, String filterCode) throws SQLException {
        StringBuilder sql = new StringBuilder(selectScheduleQuery())
            .append("WHERE s.status = 'ACTIVE' AND s.departure_time > NOW() ");
        List<Object> params = new ArrayList<>();
        appendCalendarFilters(sql, params, searchText, filterCode);
        sql.append("ORDER BY s.departure_time ASC");

        List<BusSchedule> schedules = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    schedules.add(mapRow(rs));
                }
            }
        }
        return schedules;
    }

    /**
     * Returns a map of { day-of-month → count-of-schedules } for a given month.
     * Only counts ACTIVE schedules.
     */
    public Map<Integer, Integer> countPerDayInMonth(YearMonth month) throws SQLException {
        return countPerDayInMonth(month, null, "ALL");
    }

    /**
     * Returns a map of { day-of-month -> count-of-schedules } after applying calendar filters.
     */
    public Map<Integer, Integer> countPerDayInMonth(YearMonth month, String searchText, String filterCode)
            throws SQLException {
        StringBuilder sql = new StringBuilder()
            .append("SELECT DAY(s.departure_time) AS day, COUNT(*) AS cnt ")
            .append("FROM bus_schedules s ")
            .append("JOIN buses b ON s.bus_id = b.id ")
            .append("JOIN routes r ON s.route_id = r.id ")
            .append("WHERE YEAR(s.departure_time) = ? AND MONTH(s.departure_time) = ? ")
            .append("  AND s.status = 'ACTIVE' AND s.departure_time > NOW() ");
        List<Object> params = new ArrayList<>();
        params.add(month.getYear());
        params.add(month.getMonthValue());
        appendCalendarFilters(sql, params, searchText, filterCode);
        sql.append("GROUP BY DAY(s.departure_time)");

        Map<Integer, Integer> countByDay = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    countByDay.put(rs.getInt("day"), rs.getInt("cnt"));
                }
            }
        }
        return countByDay;
    }

    /**
     * Finds a single schedule by its primary key.
     *
     * @return the BusSchedule, or null if not found.
     */
    public BusSchedule findById(int id) throws SQLException {
        String sql = selectScheduleQuery() + "WHERE s.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Decrements available seats only when the schedule is active and still has capacity.
     *
     * @return true when seats were reserved, false otherwise.
     */
    public boolean reserveSeats(Connection conn, int scheduleId, int seatCount) throws SQLException {
        String sql = "UPDATE bus_schedules "
                   + "SET available_seats = available_seats - ? "
                   + "WHERE id = ? AND status = 'ACTIVE' AND available_seats >= ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, seatCount);
            stmt.setInt(2, scheduleId);
            stmt.setInt(3, seatCount);
            return stmt.executeUpdate() == 1;
        }
    }

    private String selectScheduleQuery() {
        return "SELECT s.*, "
             + "  b.id AS bus_id, b.bus_number, b.bus_name, b.total_seats, "
             + "  r.id AS route_id, r.start_destination, r.end_destination "
             + "FROM bus_schedules s "
             + "JOIN buses b ON s.bus_id = b.id "
             + "JOIN routes r ON s.route_id = r.id ";
    }

    private void appendCalendarFilters(StringBuilder sql, List<Object> params,
                                       String searchText, String filterCode) {
        String query = searchText == null ? "" : searchText.trim().toLowerCase();
        if (!query.isBlank()) {
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

        if ("AVAILABLE".equalsIgnoreCase(filterCode)) {
            sql.append("AND s.available_seats > 0 ");
        } else if ("LOW_SEATS".equalsIgnoreCase(filterCode)) {
            sql.append("AND s.available_seats BETWEEN 1 AND 5 ");
        }
    }

    private void bindParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value instanceof Integer intValue) {
                stmt.setInt(i + 1, intValue);
            } else if (value instanceof Date dateValue) {
                stmt.setDate(i + 1, dateValue);
            } else {
                stmt.setString(i + 1, String.valueOf(value));
            }
        }
    }

    /** Maps a ResultSet row (with joined bus and route data) to a BusSchedule. */
    private BusSchedule mapRow(ResultSet rs) throws SQLException {
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
}
