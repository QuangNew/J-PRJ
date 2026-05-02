package com.buseasy.model;

public class Route {

    private int id;
    private String startDestination;
    private String endDestination;

    public Route() {}

    public Route(int id, String startDestination, String endDestination) {
        this.id               = id;
        this.startDestination = startDestination;
        this.endDestination   = endDestination;
    }

    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }

    public String getStartDestination()                 { return startDestination; }
    public void setStartDestination(String start)       { this.startDestination = start; }

    public String getEndDestination()                   { return endDestination; }
    public void setEndDestination(String end)           { this.endDestination = end; }

    @Override
    public String toString() {
        return startDestination + " → " + endDestination;
    }
}
