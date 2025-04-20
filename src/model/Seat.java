package model;

public class Seat {
    private int id;
    private String seatNumber;
    private String status;  // "available" or "booked"
    private String flightNumber;

    // Constructor
    public Seat(int id, String seatNumber, String status, String flightNumber) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.status = status;
        this.flightNumber = flightNumber;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    @Override
    public String toString() {
        return "Seat{" +
               "id=" + id +
               ", seatNumber='" + seatNumber + '\'' +
               ", status='" + status + '\'' +
               ", flightNumber='" + flightNumber + '\'' +
               '}';
    }
}
