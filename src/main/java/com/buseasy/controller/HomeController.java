package com.buseasy.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.buseasy.model.BusSchedule;
import com.buseasy.service.CartService;
import com.buseasy.service.ScheduleService;
import com.buseasy.view.home.HomePanel;

/**
 * Connects HomePanel events to ScheduleService and CartService.
 */
public class HomeController {

    private final ScheduleService scheduleService = new ScheduleService();
    private final CartService     cartService     = new CartService();

    private final int       userId;
    private final HomePanel homePanel;
    private Consumer<Integer> cartBadgeUpdater;

    public HomeController(int userId, HomePanel homePanel) {
        this.userId    = userId;
        this.homePanel = homePanel;
    }

    public void setCartBadgeUpdater(Consumer<Integer> cartBadgeUpdater) {
        this.cartBadgeUpdater = cartBadgeUpdater;
    }

    /** Loads the bus-count-per-day for the given month and refreshes the calendar. */
    public void loadMonth(YearMonth month) {
        try {
            Map<Integer, Integer> busCountByDay = scheduleService.getMonthOverview(month);
            homePanel.getCalendarPanel().renderMonth(month, busCountByDay);
        } catch (RuntimeException e) {
            homePanel.showError("Failed to load schedule data: " + e.getMessage());
        }
    }

    /**
     * Called when the user clicks a day on the calendar.
     * Loads the schedules for that day and shows the timeline panel.
     */
    public void onDaySelected(LocalDate date) {
        try {
            List<BusSchedule> schedules = scheduleService.getSchedulesForDay(date);
            homePanel.getTimelinePanel().renderSchedules(date, schedules);
            homePanel.showTimelinePanel();
        } catch (RuntimeException e) {
            homePanel.showError("Failed to load schedules: " + e.getMessage());
        }
    }

    /**
     * Called when the user confirms adding a schedule to the cart.
     * Returns to the calendar view after success.
     */
    public String addToCart(int scheduleId, int qtyAdult, int qtyChild, boolean isMilitary) {
        try {
            cartService.addToCart(userId, scheduleId, qtyAdult, qtyChild, isMilitary);
            refreshCartBadge();
            homePanel.showCalendarPanel();
            homePanel.showSuccess("Added to cart!");
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (RuntimeException e) {
            return "Could not add to cart: " + e.getMessage();
        }
    }

    private void refreshCartBadge() {
        if (cartBadgeUpdater == null) {
            return;
        }
        cartBadgeUpdater.accept(cartService.getCart(userId).size());
    }
}
