package com.buseasy.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import com.buseasy.dao.AdminDao;
import com.buseasy.model.AdminDashboardStats;
import com.buseasy.model.AdminTicketRow;
import com.buseasy.model.Bus;
import com.buseasy.model.BusSchedule;
import com.buseasy.model.Route;
import com.buseasy.model.User;

public class AdminService {

    private final AdminDao adminDao = new AdminDao();

    public AdminDashboardStats loadDashboardStats() {
        try {
            return adminDao.loadDashboardStats();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load admin dashboard.", e);
        }
    }

    public List<BusSchedule> findSchedules(String searchText, String statusFilter) {
        try {
            return adminDao.findSchedules(searchText, statusFilter);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load schedules.", e);
        }
    }

    public void updateSchedule(int scheduleId, int availableSeats, double priceAdult, String status) {
        if (availableSeats < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative.");
        }
        if (priceAdult <= 0) {
            throw new IllegalArgumentException("Adult price must be greater than 0.");
        }
        String normalizedStatus = normalizeScheduleStatus(status);
        try {
            adminDao.updateSchedule(scheduleId, availableSeats, priceAdult, normalizedStatus);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update schedule.", e);
        }
    }

    public void addSchedule(Bus bus, Route route, LocalDateTime departure, LocalDateTime arrival,
                            int availableSeats, double priceAdult, String status) {
        if (bus == null) {
            throw new IllegalArgumentException("Bus is required.");
        }
        if (route == null) {
            throw new IllegalArgumentException("Route is required.");
        }
        if (departure == null || arrival == null) {
            throw new IllegalArgumentException("Departure and arrival times are required.");
        }
        if (!departure.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Departure date must be in the future.");
        }
        if (!arrival.isAfter(departure)) {
            throw new IllegalArgumentException("Arrival time must be after departure.");
        }
        if (availableSeats < 0 || availableSeats > bus.getTotalSeats()) {
            throw new IllegalArgumentException("Available seats must be between 0 and " + bus.getTotalSeats() + ".");
        }
        if (priceAdult <= 0) {
            throw new IllegalArgumentException("Adult price must be greater than 0.");
        }

        BusSchedule schedule = new BusSchedule();
        schedule.setBus(bus);
        schedule.setRoute(route);
        schedule.setDepartureTime(departure);
        schedule.setArrivalTime(arrival);
        schedule.setAvailableSeats(availableSeats);
        schedule.setPriceAdult(priceAdult);
        schedule.setStatus(normalizeScheduleStatus(status));
        try {
            adminDao.insertSchedule(schedule);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add schedule.", e);
        }
    }

    public List<Bus> findBuses() {
        try {
            return adminDao.findBuses();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load buses.", e);
        }
    }

    public List<Route> findRoutes() {
        try {
            return adminDao.findRoutes();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load routes.", e);
        }
    }

    public List<User> findUsers(String searchText, String roleFilter) {
        try {
            return adminDao.findUsers(searchText, roleFilter);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load users.", e);
        }
    }

    public void updateUserRole(int actingAdminId, int targetUserId, String role) {
        String normalizedRole = normalizeRole(role);
        if (actingAdminId == targetUserId && !"ADMIN".equals(normalizedRole)) {
            throw new IllegalArgumentException("You cannot remove your own admin role while signed in.");
        }
        try {
            adminDao.updateUserRole(targetUserId, normalizedRole);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user role.", e);
        }
    }

    public List<AdminTicketRow> findTickets(String searchText, String statusFilter) {
        try {
            return adminDao.findTickets(searchText, statusFilter);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load tickets.", e);
        }
    }

    private String normalizeRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return "ADMIN";
        }
        if ("USER".equalsIgnoreCase(role)) {
            return "USER";
        }
        throw new IllegalArgumentException("Role must be USER or ADMIN.");
    }

    private String normalizeScheduleStatus(String status) {
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return "ACTIVE";
        }
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "CANCELLED";
        }
        throw new IllegalArgumentException("Schedule status must be ACTIVE or CANCELLED.");
    }
}
