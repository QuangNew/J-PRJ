package com.buseasy.service;

import java.sql.SQLException;
import java.util.List;

import com.buseasy.dao.MilitaryRequestDao;
import com.buseasy.dao.UserDao;
import com.buseasy.model.MilitaryRequest;
import com.buseasy.model.User;
import com.buseasy.util.LanguageManager;

public class MilitaryRequestService {

    private final MilitaryRequestDao requestDao = new MilitaryRequestDao();
    private final UserDao userDao = new UserDao();
    private final NotificationService notificationService = new NotificationService();

    public boolean isApproved(int userId) {
        try {
            User user = userDao.findById(userId);
            return user != null && user.isMilitary();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check military status.", e);
        }
    }

    public MilitaryRequest latestForUser(int userId) {
        try {
            return requestDao.findLatestByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load military request.", e);
        }
    }

    public void submitRequest(int userId, String serviceNumber, String unitName, String note) {
        String normalizedServiceNumber = requireText(serviceNumber, LanguageManager.text("military.service.no"));
        String normalizedUnitName = requireText(unitName, LanguageManager.text("military.unit"));
        String normalizedNote = note == null ? "" : note.trim();
        try {
            User user = userDao.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found.");
            }
            if (user.isMilitary()) {
                throw new IllegalArgumentException("Military discount is already approved.");
            }
            if (requestDao.findPendingByUserId(userId) != null) {
                throw new IllegalArgumentException("You already have a pending military request.");
            }

            MilitaryRequest request = new MilitaryRequest();
            request.setUserId(userId);
            request.setServiceNumber(normalizedServiceNumber);
            request.setUnitName(normalizedUnitName);
            request.setNote(normalizedNote);
            requestDao.insert(request);

            notificationService.notifyAdmins(
                "New military request",
                user.getFullName() + " submitted a military discount request.",
                "MILITARY_REQUEST"
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to submit military request.", e);
        }
    }

    public List<MilitaryRequest> findRequests(String searchText, String statusFilter) {
        try {
            return requestDao.findAll(searchText, statusFilter);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load military requests.", e);
        }
    }

    public void reviewRequest(int requestId, int adminId, boolean approved, String adminNote) {
        String status = approved ? "APPROVED" : "DENIED";
        String note = adminNote == null ? "" : adminNote.trim();
        try {
            MilitaryRequest request = requestDao.findById(requestId);
            if (request == null) {
                throw new IllegalArgumentException("Military request not found.");
            }
            if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
                throw new IllegalArgumentException("Only pending requests can be reviewed.");
            }

            requestDao.review(requestId, adminId, status, note);
            userDao.updateMilitaryStatus(request.getUserId(), approved);

            notificationService.notifyUser(
                request.getUserId(),
                approved ? LanguageManager.text("military.approved") : LanguageManager.text("military.denied"),
                note.isBlank() ? status : note,
                "MILITARY_" + status
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to review military request.", e);
        }
    }

    private String requireText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return normalized;
    }
}
