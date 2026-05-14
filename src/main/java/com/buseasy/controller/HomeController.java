package com.buseasy.controller;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.buseasy.model.BusSchedule;
import com.buseasy.model.User;
import com.buseasy.service.CartService;
import com.buseasy.service.MilitaryRequestService;
import com.buseasy.service.ScheduleService;
import com.buseasy.util.DateUtil;
import com.buseasy.util.LanguageManager;
import com.buseasy.view.home.HomePanel;

/**
 * Connects HomePanel events to ScheduleService and CartService.
 */
public class HomeController {

    private final ScheduleService scheduleService = new ScheduleService();
    private final CartService     cartService     = new CartService();
    private final MilitaryRequestService militaryRequestService = new MilitaryRequestService();

    private final User      currentUser;
    private final HomePanel homePanel;
    private Consumer<Integer> cartBadgeUpdater;
    private String calendarSearchText = "";
    private String calendarFilterCode = "ALL";

    public HomeController(User currentUser, HomePanel homePanel) {
        this.currentUser = currentUser;
        this.homePanel   = homePanel;
    }

    public void setCartBadgeUpdater(Consumer<Integer> cartBadgeUpdater) {
        this.cartBadgeUpdater = cartBadgeUpdater;
    }

    public boolean isMilitaryDiscountEligible() {
        return militaryRequestService.isApproved(currentUser.getId());
    }

    /** Loads the bus-count-per-day for the given month and refreshes the calendar. */
    public void loadMonth(YearMonth month) {
        try {
            Map<Integer, Integer> busCountByDay = scheduleService.getMonthOverview(
                month, calendarSearchText, calendarFilterCode);
            homePanel.getCalendarPanel().renderMonth(month, busCountByDay);
            homePanel.clearStatus();
        } catch (RuntimeException e) {
            homePanel.showError("Failed to load schedule data: " + e.getMessage());
        }
    }

    public void onCalendarFilterChanged(String searchText, String filterCode) {
        this.calendarSearchText = searchText == null ? "" : searchText.trim();
        this.calendarFilterCode = filterCode == null || filterCode.isBlank() ? "ALL" : filterCode;
        if (!this.calendarSearchText.isBlank()) {
            searchSchedules(this.calendarSearchText);
            return;
        }
        YearMonth month = homePanel.getCalendarPanel().getCurrentMonth();
        loadMonth(month == null ? YearMonth.now() : month);
    }

    public void onCalendarSearchChanged(String searchText) {
        this.calendarSearchText = searchText == null ? "" : searchText.trim();
        if (this.calendarSearchText.isBlank()) {
            homePanel.getCalendarPanel().showCalendarView();
            YearMonth month = homePanel.getCalendarPanel().getCurrentMonth();
            loadMonth(month == null ? YearMonth.now() : month);
            return;
        }
        searchSchedules(this.calendarSearchText);
    }

    public void onDateJumpRequested(String dateText) {
        try {
            onDaySelected(DateUtil.parseDate(dateText));
        } catch (DateTimeException e) {
            homePanel.showError(LanguageManager.text("invalid.date"));
        }
    }

    /**
     * Called when the user clicks a day on the calendar.
     * Loads the schedules for that day and shows the timeline panel.
     */
    public void onDaySelected(LocalDate date) {
        try {
            if (date.isBefore(LocalDate.now())) {
                homePanel.showCalendarPanel();
                homePanel.showError(LanguageManager.text("past.date"));
                return;
            }

            List<BusSchedule> schedules = scheduleService.getSchedulesForDay(
                date, calendarSearchText, calendarFilterCode);
            if (schedules.isEmpty()) {
                homePanel.showCalendarPanel();
                homePanel.showInfo(LanguageManager.text("no.trips.day") + DateUtil.formatDate(date));
                return;
            }
            homePanel.getTimelinePanel().renderSchedules(date, schedules);
            homePanel.showTimelinePanel();
            homePanel.clearStatus();
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
            if (isMilitary && !isMilitaryDiscountEligible()) {
                return "Military discount is waiting for admin approval.";
            }
            cartService.addToCart(currentUser.getId(), scheduleId, qtyAdult, qtyChild, isMilitary);
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

    public String submitMilitaryRequest(String serviceNumber, String unitName, String note) {
        try {
            militaryRequestService.submitRequest(currentUser.getId(), serviceNumber, unitName, note);
            return null;
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (RuntimeException e) {
            return "Could not submit military request: " + e.getMessage();
        }
    }

    private void searchSchedules(String searchText) {
        try {
            List<BusSchedule> schedules = scheduleService.searchFutureSchedules(searchText, calendarFilterCode);
            homePanel.getCalendarPanel().renderSearchResults(searchText, schedules);
            homePanel.showCalendarPanel();
            homePanel.clearStatus();
        } catch (RuntimeException e) {
            homePanel.showError("Failed to search schedules: " + e.getMessage());
        }
    }

    private void refreshCartBadge() {
        if (cartBadgeUpdater == null) {
            return;
        }
        cartBadgeUpdater.accept(cartService.getCart(currentUser.getId()).size());
    }
}
