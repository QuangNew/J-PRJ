package com.buseasy.model;

import java.time.LocalDateTime;

public class Ticket {

    private int id;
    private int userId;
    private BusSchedule schedule;
    private int qtyAdult;
    private int qtyChild;
    private boolean isMilitary;
    private double totalPrice;
    private String status;   // "VALID", "EXPIRED", or "CANCELLED"
    private LocalDateTime purchasedAt;

    public Ticket() {}

    public Ticket(int id, int userId, BusSchedule schedule,
                  int qtyAdult, int qtyChild, boolean isMilitary,
                  double totalPrice, String status, LocalDateTime purchasedAt) {
        this.id          = id;
        this.userId      = userId;
        this.schedule    = schedule;
        this.qtyAdult    = qtyAdult;
        this.qtyChild    = qtyChild;
        this.isMilitary  = isMilitary;
        this.totalPrice  = totalPrice;
        this.status      = status;
        this.purchasedAt = purchasedAt;
    }

    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }

    public int getUserId()                              { return userId; }
    public void setUserId(int userId)                   { this.userId = userId; }

    public BusSchedule getSchedule()                    { return schedule; }
    public void setSchedule(BusSchedule schedule)       { this.schedule = schedule; }

    public int getQtyAdult()                            { return qtyAdult; }
    public void setQtyAdult(int qty)                    { this.qtyAdult = qty; }

    public int getQtyChild()                            { return qtyChild; }
    public void setQtyChild(int qty)                    { this.qtyChild = qty; }

    public boolean isMilitary()                         { return isMilitary; }
    public void setMilitary(boolean military)           { this.isMilitary = military; }

    public double getTotalPrice()                       { return totalPrice; }
    public void setTotalPrice(double price)             { this.totalPrice = price; }

    public String getStatus()                           { return status; }
    public void setStatus(String status)                { this.status = status; }

    public LocalDateTime getPurchasedAt()               { return purchasedAt; }
    public void setPurchasedAt(LocalDateTime dt)        { this.purchasedAt = dt; }
}
