package com.buseasy.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDashboardStats {

    private int customerCount;
    private int adminCount;
    private int upcomingScheduleCount;
    private int ticketCount;
    private int lowSeatScheduleCount;
    private double revenue;
    private Map<String, Double> revenueByWeekday = new LinkedHashMap<>();
    private int adultTicketQuantity;
    private int childTicketQuantity;
    private int militaryTicketQuantity;

    public int getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public int getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(int adminCount) {
        this.adminCount = adminCount;
    }

    public int getUpcomingScheduleCount() {
        return upcomingScheduleCount;
    }

    public void setUpcomingScheduleCount(int upcomingScheduleCount) {
        this.upcomingScheduleCount = upcomingScheduleCount;
    }

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public int getLowSeatScheduleCount() {
        return lowSeatScheduleCount;
    }

    public void setLowSeatScheduleCount(int lowSeatScheduleCount) {
        this.lowSeatScheduleCount = lowSeatScheduleCount;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public Map<String, Double> getRevenueByWeekday() {
        return revenueByWeekday;
    }

    public void setRevenueByWeekday(Map<String, Double> revenueByWeekday) {
        this.revenueByWeekday = revenueByWeekday;
    }

    public int getAdultTicketQuantity() {
        return adultTicketQuantity;
    }

    public void setAdultTicketQuantity(int adultTicketQuantity) {
        this.adultTicketQuantity = adultTicketQuantity;
    }

    public int getChildTicketQuantity() {
        return childTicketQuantity;
    }

    public void setChildTicketQuantity(int childTicketQuantity) {
        this.childTicketQuantity = childTicketQuantity;
    }

    public int getMilitaryTicketQuantity() {
        return militaryTicketQuantity;
    }

    public void setMilitaryTicketQuantity(int militaryTicketQuantity) {
        this.militaryTicketQuantity = militaryTicketQuantity;
    }
}
