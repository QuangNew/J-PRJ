package com.buseasy.model;

import java.time.LocalDateTime;

public class CartItem {

    private int id;
    private int userId;
    private BusSchedule schedule;
    private int qtyAdult;
    private int qtyChild;
    private boolean isMilitary;
    private LocalDateTime addedAt;

    public CartItem() {}

    public CartItem(int id, int userId, BusSchedule schedule,
                    int qtyAdult, int qtyChild, boolean isMilitary,
                    LocalDateTime addedAt) {
        this.id         = id;
        this.userId     = userId;
        this.schedule   = schedule;
        this.qtyAdult   = qtyAdult;
        this.qtyChild   = qtyChild;
        this.isMilitary = isMilitary;
        this.addedAt    = addedAt;
    }

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public int getUserId()                          { return userId; }
    public void setUserId(int userId)               { this.userId = userId; }

    public BusSchedule getSchedule()                { return schedule; }
    public void setSchedule(BusSchedule schedule)   { this.schedule = schedule; }

    public int getQtyAdult()                        { return qtyAdult; }
    public void setQtyAdult(int qty)                { this.qtyAdult = qty; }

    public int getQtyChild()                        { return qtyChild; }
    public void setQtyChild(int qty)                { this.qtyChild = qty; }

    public boolean isMilitary()                     { return isMilitary; }
    public void setMilitary(boolean military)       { this.isMilitary = military; }

    public LocalDateTime getAddedAt()               { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt)   { this.addedAt = addedAt; }
}
