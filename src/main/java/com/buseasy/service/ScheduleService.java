package com.buseasy.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import com.buseasy.dao.ScheduleDao;
import com.buseasy.model.BusSchedule;

/**
 * Provides schedule data to the UI.
 * No SQL here — delegates entirely to ScheduleDao.
 */
public class ScheduleService {

    private final ScheduleDao scheduleDao = new ScheduleDao();

    /**
     * Returns the number of active bus schedules per day in a given month.
     * Used to annotate each calendar cell with a bus count.
     *
     * @return map of { day-of-month → schedule count }
     */
    public Map<Integer, Integer> getMonthOverview(YearMonth month) {
        return getMonthOverview(month, null, "ALL");
    }

    /**
     * Returns the number of active bus schedules per day after applying search/filter criteria.
     */
    public Map<Integer, Integer> getMonthOverview(YearMonth month, String searchText, String filterCode) {
        try {
            return scheduleDao.countPerDayInMonth(month, searchText, filterCode);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load month overview.", e);
        }
    }

    /**
     * Returns all active schedules for a specific date.
     * Used to populate the timeline list when a day is clicked.
     */
    public List<BusSchedule> getSchedulesForDay(LocalDate date) {
        return getSchedulesForDay(date, null, "ALL");
    }

    /**
     * Returns all active schedules for a date after applying search/filter criteria.
     */
    public List<BusSchedule> getSchedulesForDay(LocalDate date, String searchText, String filterCode) {
        try {
            return scheduleDao.findByDate(date, searchText, filterCode);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load schedules for date.", e);
        }
    }

    public List<BusSchedule> searchFutureSchedules(String searchText, String filterCode) {
        String query = searchText == null ? "" : searchText.trim();
        if (query.isBlank()) {
            return List.of();
        }
        try {
            return scheduleDao.findFutureMatches(query, filterCode);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search schedules.", e);
        }
    }
}
