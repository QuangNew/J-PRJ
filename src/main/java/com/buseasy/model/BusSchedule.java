package com.buseasy.model;

import java.time.LocalDateTime;

public class BusSchedule {

    private int id;
    private Bus bus;
    private Route route;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private double priceAdult;
    private int availableSeats;
    private String status;   // "ACTIVE" or "CANCELLED"

    public BusSchedule() {}

    public BusSchedule(int id, Bus bus, Route route,
                       LocalDateTime departureTime, LocalDateTime arrivalTime,
                       double priceAdult, int availableSeats, String status) {
        this.id             = id;
        this.bus            = bus;
        this.route          = route;
        this.departureTime  = departureTime;
        this.arrivalTime    = arrivalTime;
        this.priceAdult     = priceAdult;
        this.availableSeats = availableSeats;
        this.status         = status;
    }

    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }

    public Bus getBus()                                 { return bus; }
    public void setBus(Bus bus)                         { this.bus = bus; }

    public Route getRoute()                             { return route; }
    public void setRoute(Route route)                   { this.route = route; }

    public LocalDateTime getDepartureTime()             { return departureTime; }
    public void setDepartureTime(LocalDateTime dt)      { this.departureTime = dt; }

    public LocalDateTime getArrivalTime()               { return arrivalTime; }
    public void setArrivalTime(LocalDateTime dt)        { this.arrivalTime = dt; }

    public double getPriceAdult()                       { return priceAdult; }
    public void setPriceAdult(double price)             { this.priceAdult = price; }

    public int getAvailableSeats()                      { return availableSeats; }
    public void setAvailableSeats(int seats)            { this.availableSeats = seats; }

    public String getStatus()                           { return status; }
    public void setStatus(String status)                { this.status = status; }
}
