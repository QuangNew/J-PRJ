package com.buseasy.service;

import java.sql.SQLException;
import java.util.List;

import com.buseasy.dao.NotificationDao;
import com.buseasy.model.AppNotification;

public class NotificationService {

    private final NotificationDao notificationDao = new NotificationDao();

    public void notifyUser(int userId, String title, String message, String type) {
        try {
            notificationDao.insert(userId, title, message, type);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create notification.", e);
        }
    }

    public void notifyAdmins(String title, String message, String type) {
        try {
            notificationDao.insertForAdmins(title, message, type);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create admin notification.", e);
        }
    }

    public int countUnread(int userId) {
        try {
            return notificationDao.countUnread(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count notifications.", e);
        }
    }

    public List<AppNotification> getLatest(int userId) {
        try {
            return notificationDao.findLatestByUserId(userId, 12);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load notifications.", e);
        }
    }

    public void markAllRead(int userId) {
        try {
            notificationDao.markAllRead(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update notifications.", e);
        }
    }
}
