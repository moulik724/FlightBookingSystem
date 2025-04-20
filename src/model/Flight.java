package model;

import java.time.LocalDateTime;

public class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private int totalSeats;
    private int availableSeats;
    private double cost;  // New field
    private boolean approved;

    public Flight(String flightNumber, String origin, String destination, LocalDateTime departureTime, int totalSeats, int availableSeats, double cost, boolean approved) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.cost = cost;
        this.approved = approved;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getTotalSeats() { return totalSeats; }
    public int getAvailableSeats() { return availableSeats; }
    public double getCost() { return cost; }
    public boolean isApproved() {return approved;}
    

    @Override
    public String toString() {
        return flightNumber + " | " + origin + " → " + destination + " | " + departureTime + " | ₹" + cost + " | Seats: " + availableSeats + "/" + totalSeats;
    }
}
