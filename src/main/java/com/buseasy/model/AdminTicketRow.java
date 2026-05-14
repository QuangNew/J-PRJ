package com.buseasy.model;

import java.time.LocalDateTime;

public class AdminTicketRow {

    private int id;
    private String customerName;
    private String username;
    private BusSchedule schedule;
    private int qtyAdult;
    private int qtyChild;
    private boolean military;
    private double totalPrice;
    private String status;
    private LocalDateTime purchasedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BusSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(BusSchedule schedule) {
        this.schedule = schedule;
    }

    public int getQtyAdult() {
        return qtyAdult;
    }

    public void setQtyAdult(int qtyAdult) {
        this.qtyAdult = qtyAdult;
    }

    public int getQtyChild() {
        return qtyChild;
    }

    public void setQtyChild(int qtyChild) {
        this.qtyChild = qtyChild;
    }

    public boolean isMilitary() {
        return military;
    }

    public void setMilitary(boolean military) {
        this.military = military;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public int getPassengerCount() {
        return qtyAdult + qtyChild;
    }
}
