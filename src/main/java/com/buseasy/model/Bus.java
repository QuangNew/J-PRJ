package com.buseasy.model;

public class Bus {

    private int id;
    private String busNumber;
    private String busName;
    private int totalSeats;

    public Bus() {}

    public Bus(int id, String busNumber, String busName, int totalSeats) {
        this.id         = id;
        this.busNumber  = busNumber;
        this.busName    = busName;
        this.totalSeats = totalSeats;
    }

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getBusNumber()              { return busNumber; }
    public void setBusNumber(String number)   { this.busNumber = number; }

    public String getBusName()                { return busName; }
    public void setBusName(String name)       { this.busName = name; }

    public int getTotalSeats()                { return totalSeats; }
    public void setTotalSeats(int seats)      { this.totalSeats = seats; }

    @Override
    public String toString() {
        return busNumber + " — " + busName;
    }
}
