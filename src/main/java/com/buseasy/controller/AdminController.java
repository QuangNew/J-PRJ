package com.buseasy.controller;

import com.buseasy.model.Bus;
import com.buseasy.model.Route;
import com.buseasy.model.User;
import com.buseasy.service.AdminService;
import com.buseasy.service.MilitaryRequestService;
import com.buseasy.view.admin.AdminPanel;

public class AdminController {

    private final User currentAdmin;
    private final AdminPanel adminPanel;
    private final AdminService adminService = new AdminService();
    private final MilitaryRequestService militaryRequestService = new MilitaryRequestService();

    public AdminController(User currentAdmin, AdminPanel adminPanel) {
        this.currentAdmin = currentAdmin;
        this.adminPanel = adminPanel;
    }

    public void initialize() {
        refreshDashboard();
        searchSchedules("", "ALL");
        searchUsers("", "ALL");
        searchTickets("", "ALL");
        searchMilitaryRequests("", "PENDING");
    }

    public void refreshDashboard() {
        try {
            adminPanel.renderDashboard(adminService.loadDashboardStats());
            adminPanel.showInfo("Dashboard refreshed.");
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void refreshAll() {
        refreshDashboard();
        searchSchedules(adminPanel.getScheduleSearchText(), adminPanel.getScheduleStatusFilter());
        searchUsers(adminPanel.getUserSearchText(), adminPanel.getUserRoleFilter());
        searchTickets(adminPanel.getTicketSearchText(), adminPanel.getTicketStatusFilter());
        searchMilitaryRequests(adminPanel.getMilitarySearchText(), adminPanel.getMilitaryStatusFilter());
        adminPanel.finishRefresh();
    }

    public void searchSchedules(String searchText, String statusFilter) {
        try {
            adminPanel.renderSchedules(adminService.findSchedules(searchText, statusFilter));
            adminPanel.showInfo("Schedules loaded.");
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void saveSchedule(AdminPanel.ScheduleEdit edit) {
        if (edit == null) {
            return;
        }
        try {
            adminService.updateSchedule(edit.scheduleId(), edit.availableSeats(), edit.priceAdult(), edit.status());
            searchSchedules(adminPanel.getScheduleSearchText(), adminPanel.getScheduleStatusFilter());
            refreshDashboard();
            adminPanel.showSuccess("Schedule updated.");
        } catch (IllegalArgumentException e) {
            adminPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void addScheduleRequested() {
        try {
            AdminPanel.ScheduleCreate create = adminPanel.promptScheduleCreate(
                adminService.findBuses(), adminService.findRoutes());
            if (create == null) {
                return;
            }
            adminService.addSchedule(
                create.bus(),
                create.route(),
                create.departureTime(),
                create.arrivalTime(),
                create.availableSeats(),
                create.priceAdult(),
                create.status()
            );
            searchSchedules(adminPanel.getScheduleSearchText(), adminPanel.getScheduleStatusFilter());
            refreshDashboard();
            adminPanel.showSuccess("Schedule added.");
        } catch (IllegalArgumentException e) {
            adminPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void cancelSchedule(int scheduleId) {
        try {
            adminService.updateSchedule(scheduleId, adminPanel.getSelectedScheduleSeats(), adminPanel.getSelectedSchedulePrice(), "CANCELLED");
            searchSchedules(adminPanel.getScheduleSearchText(), adminPanel.getScheduleStatusFilter());
            refreshDashboard();
            adminPanel.showSuccess("Schedule cancelled.");
        } catch (IllegalArgumentException e) {
            adminPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void searchUsers(String searchText, String roleFilter) {
        try {
            adminPanel.renderUsers(adminService.findUsers(searchText, roleFilter));
            adminPanel.showInfo("Users loaded.");
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void updateUserRole(int userId, String role) {
        try {
            adminService.updateUserRole(currentAdmin.getId(), userId, role);
            searchUsers(adminPanel.getUserSearchText(), adminPanel.getUserRoleFilter());
            refreshDashboard();
            adminPanel.showSuccess("User role updated.");
        } catch (IllegalArgumentException e) {
            adminPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void searchTickets(String searchText, String statusFilter) {
        try {
            adminPanel.renderTickets(adminService.findTickets(searchText, statusFilter));
            adminPanel.showInfo("Tickets loaded.");
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void searchMilitaryRequests(String searchText, String statusFilter) {
        try {
            adminPanel.renderMilitaryRequests(militaryRequestService.findRequests(searchText, statusFilter));
            adminPanel.showInfo("Military requests loaded.");
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }

    public void reviewMilitaryRequest(int requestId, boolean approved, String adminNote) {
        try {
            militaryRequestService.reviewRequest(requestId, currentAdmin.getId(), approved, adminNote);
            searchMilitaryRequests(adminPanel.getMilitarySearchText(), adminPanel.getMilitaryStatusFilter());
            refreshDashboard();
            adminPanel.showSuccess("Military request reviewed.");
        } catch (IllegalArgumentException e) {
            adminPanel.showError(e.getMessage());
        } catch (RuntimeException e) {
            adminPanel.showError(e.getMessage());
        }
    }
}
