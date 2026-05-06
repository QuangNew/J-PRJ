package com.buseasy.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.buseasy.dao.ReminderDao;
import com.buseasy.model.Reminder;
import com.buseasy.model.Ticket;

public class ReminderService {

    private static final Set<Integer> SUPPORTED_OFFSETS = new LinkedHashSet<>(Arrays.asList(15, 30, 60, 120, 1440, 2880));

    private final ReminderDao reminderDao = new ReminderDao();

    public ReminderSaveResult saveReminders(int userId, List<Ticket> tickets, Integer offsetMinutes) {
        if (offsetMinutes == null || tickets.isEmpty()) {
            return new ReminderSaveResult(0, 0);
        }
        if (!SUPPORTED_OFFSETS.contains(offsetMinutes)) {
            throw new IllegalArgumentException("Unsupported reminder option.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Reminder> reminders = new ArrayList<>();
        int skippedCount = 0;

        for (Ticket ticket : tickets) {
            LocalDateTime remindAt = ticket.getSchedule().getDepartureTime().minusMinutes(offsetMinutes);
            if (!remindAt.isAfter(now)) {
                skippedCount++;
                continue;
            }

            Reminder reminder = new Reminder();
            reminder.setTicketId(ticket.getId());
            reminder.setUserId(userId);
            reminder.setOffsetMinutes(offsetMinutes);
            reminder.setRemindAt(remindAt);
            reminders.add(reminder);
        }

        try {
            reminderDao.insertAll(reminders);
            return new ReminderSaveResult(reminders.size(), skippedCount);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save reminders.", e);
        }
    }

    public List<Reminder> getDueReminders(int userId) {
        try {
            return reminderDao.findDueByUserId(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load reminders.", e);
        }
    }

    public void markDelivered(List<Reminder> reminders) {
        if (reminders.isEmpty()) {
            return;
        }
        try {
            List<Integer> reminderIds = reminders.stream()
                .map(Reminder::getId)
                .collect(Collectors.toList());
            reminderDao.markDelivered(reminderIds);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update reminders.", e);
        }
    }

    public String formatOffsetLabel(int offsetMinutes) {
        return switch (offsetMinutes) {
            case 15 -> "15 minutes before departure";
            case 30 -> "30 minutes before departure";
            case 60 -> "1 hour before departure";
            case 120 -> "2 hours before departure";
            case 1440 -> "1 day before departure";
            case 2880 -> "2 days before departure";
            default -> offsetMinutes + " minutes before departure";
        };
    }

    public static class ReminderSaveResult {
        private final int savedCount;
        private final int skippedCount;

        public ReminderSaveResult(int savedCount, int skippedCount) {
            this.savedCount = savedCount;
            this.skippedCount = skippedCount;
        }

        public int getSavedCount() {
            return savedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }
    }
}
