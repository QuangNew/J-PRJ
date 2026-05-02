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
        try {
            return scheduleDao.countPerDayInMonth(month);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load month overview.", e);
        }
    }

    /**
     * Returns all active schedules for a specific date.
     * Used to populate the timeline list when a day is clicked.
     */
    public List<BusSchedule> getSchedulesForDay(LocalDate date) {
        try {
            return scheduleDao.findByDate(date);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load schedules for date.", e);
        }
    }
}
