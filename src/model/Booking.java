package model;

import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Booking {
    private int userId;
    private String flightNumber;
    private String userName;
    private String dob; // Store the date as a String
    private String phone;
    private String email;
    private String address;
    private int numSeats;
    private String seatNumbers;
    private int bookingId;
    private String departureTime;

    // NEW FIELDS
    private String bookingDate;
    private String extraPassengers;

    // List to hold multiple passengers (main + extras)
    private List<Passenger> passengers = new ArrayList<>();

    // Constructor
    public Booking(String flightNumber, String userName, String dob, String phone,
                   String email, String address, int numSeats, int userId, String seatNumbers) {
        this.flightNumber = flightNumber;
        this.userName = userName;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.numSeats = numSeats;
        this.userId = userId;
        this.seatNumbers = seatNumbers;

        // Add the main passenger to the list
        this.passengers.add(new Passenger(userName, dob));
    }
// No-arg constructor for DAO usage
public Booking() {
}

    // Method to add additional passengers
    public void addPassenger(String userName, String dob) {
        this.passengers.add(new Passenger(userName, dob));
    }

    // Returns a formatted string of extra passengers (excluding the main passenger)
    public String getExtraPassengersDetails() {
        StringBuilder details = new StringBuilder();
        for (int i = 1; i < passengers.size(); i++) {
            Passenger p = passengers.get(i);
            details.append(p.getUserName())
                   .append(" (").append(p.getDob()).append("), ");
        }
        if (details.length() > 0) {
            details.setLength(details.length() - 2);
        }
        return details.toString();
    }

    public String formatDob(String dob) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date parsedDate = inputFormat.parse(dob);
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
            return outputFormat.format(parsedDate);
        } catch (Exception e) {
            e.printStackTrace();
            return dob;
        }
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getNumSeats() { return numSeats; }
    public void setNumSeats(int numSeats) { this.numSeats = numSeats; }

    public String getSeatNumbers() { return seatNumbers; }
    public void setSeatNumbers(String seatNumbers) { this.seatNumbers = seatNumbers; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public List<Passenger> getPassengers() { return passengers; }

    // NEW Getters and Setters
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String bookingDate) { this.bookingDate = bookingDate; }

    public String getExtraPassengers() { return extraPassengers; }
    public void setExtraPassengers(String extraPassengers) { this.extraPassengers = extraPassengers; }

    // Optional seat number (used if different from seatNumbers)
    public void setSeatNumber(String seatNumber) {
        this.seatNumbers = seatNumber;
    }

    // Inner class for Passenger
    public static class Passenger {
        private String userName;
        private String dob;

        public Passenger(String userName, String dob) {
            this.userName = userName;
            this.dob = dob;
        }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getDob() { return dob; }
        public void setDob(String dob) { this.dob = dob; }
    }
}
