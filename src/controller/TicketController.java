package controller;

import db.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import model.Booking;
import model.Flight;
import service.NotificationService;
import utils.BookingContext;
import utils.SceneSwitcher;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class TicketController implements Initializable {
    @FXML private Label bookingIdText;
    @FXML private Label flightNumberText;
    @FXML private Label departureTimeText;
    @FXML private Label seatNumbersText;
    @FXML private Label userDetailsText;
    @FXML private Label costText;

    private NotificationService notificationService = NotificationService.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Booking booking = BookingContext.getCurrentBooking();
            System.out.println("DEBUG - Current booking: " + booking);
            
            if (booking != null) {
                // If booking ID is 0, try to get it from the database
                if (booking.getBookingId() == 0) {
                    int retrievedId = retrieveBookingId(booking);
                    if (retrievedId > 0) {
                        booking.setBookingId(retrievedId);
                        System.out.println("DEBUG - Retrieved ID from database: " + retrievedId);
                    }
                }
                
                // If extra passengers is null, try to get it from the database
                if (booking.getExtraPassengers() == null || booking.getExtraPassengers().isEmpty()) {
                    fetchExtraPassengersFromDb(booking);
                }
                
                // Get flight details from database using flight number
                Flight flightDetails = getFlightDetails(booking.getFlightNumber());
                
                if (flightDetails != null) {
                    setTicketDetails(booking, flightDetails);
                } else {
                    bookingIdText.setText("No flight details found.");
                }
            } else {
                bookingIdText.setText("No booking found.");
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            bookingIdText.setText("Error retrieving flight information.");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            bookingIdText.setText("An unexpected error occurred.");
        }
    }
    
    private void fetchExtraPassengersFromDb(Booking booking) {
        try {
            Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT extra_passengers FROM bookings WHERE id = ?"
            );
            statement.setInt(1, booking.getBookingId());
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String extraPassengers = resultSet.getString("extra_passengers");
                booking.setExtraPassengers(extraPassengers);
                System.out.println("DEBUG - Fetched extra passengers: " + extraPassengers);
            }
            
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving extra passengers: " + e.getMessage());
        }
    }
    
    /**
     * Extracts just the name from passenger data that includes DOB in parentheses
     * Example: "John Smith (1990-01-01)" -> "John Smith"
     */
    private String extractPassengerName(String passengerData) {
        if (passengerData == null || passengerData.isEmpty()) {
            return "";
        }
        
        // Find the opening parenthesis for the date of birth
        int parenthesisIndex = passengerData.indexOf('(');
        if (parenthesisIndex > 0) {
            // Return everything before the opening parenthesis, trimmed
            return passengerData.substring(0, parenthesisIndex).trim();
        } else {
            // If no parenthesis found, return the whole string trimmed
            return passengerData.trim();
        }
    }
    
    private int retrieveBookingId(Booking booking) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        int bookingId = 0;
        
        try {
            connection = DBConnection.getConnection();
            // Get the booking ID using other booking details
            String query = "SELECT id FROM bookings WHERE user_id = ? AND flight_number = ? AND user_name = ?";
            
            // If we have seat information, use it for more precise matching
            if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
                query += " AND seat_number = ?";
            }
            
            // Get the most recent booking if multiple matches exist
            query += " ORDER BY booking_date DESC LIMIT 1";
            
            statement = connection.prepareStatement(query);
            statement.setInt(1, booking.getUserId());
            statement.setString(2, booking.getFlightNumber());
            statement.setString(3, booking.getUserName());
            
            if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
                statement.setString(4, booking.getSeatNumbers());
            }
            
            System.out.println("DEBUG - Executing query: " + query);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                bookingId = resultSet.getInt("id");
                System.out.println("DEBUG - Found booking ID: " + bookingId);
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
        
        return bookingId;
    }
    
    private Flight getFlightDetails(String flightNumber) throws SQLException {
        Flight flight = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT flight_number, origin, destination, departure_time, total_seats, available_seats, cost, approved " +
                           "FROM flights WHERE flight_number = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, flightNumber);
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                flight = new Flight(
                    resultSet.getString("flight_number"),
                    resultSet.getString("origin"),
                    resultSet.getString("destination"),
                    resultSet.getTimestamp("departure_time").toLocalDateTime(),
                    resultSet.getInt("total_seats"),
                    resultSet.getInt("available_seats"),
                    resultSet.getDouble("cost"),
                    resultSet.getBoolean("approved")
                );
            }
        } finally {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
        }
        
        return flight;
    }
    
    // Get the number of seats booked
    private int getBookedSeatsCount(Booking booking) {
        int count = 0;
        
        // Count seats from the seat numbers string if available
        if (booking.getSeatNumbers() != null && !booking.getSeatNumbers().isEmpty()) {
            // If seats are stored as comma-separated values
            String[] seats = booking.getSeatNumbers().split(",");
            count = seats.length;
        } else {
            // Try to get the count from the num_seats field in the database
            try {
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT num_seats FROM bookings WHERE id = ?"
                );
                statement.setInt(1, booking.getBookingId());
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    count = resultSet.getInt("num_seats");
                }
                
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println("Error retrieving seat count: " + e.getMessage());
                // Default to 1 if we can't determine the count
                count = 1;
            }
        }
        
        // Make sure we return at least 1
        return Math.max(1, count);
    }
    
    public void setTicketDetails(Booking booking, Flight flight) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        
        bookingIdText.setText(" " + booking.getBookingId());
        flightNumberText.setText(" " + flight.getFlightNumber());
        
        // Origin, destination, and departure time
        String flightInfo = flight.getOrigin() + " to " + flight.getDestination();
        departureTimeText.setText(" Route: " + flightInfo + 
                                 "\n Departure: " + flight.getDepartureTime().format(formatter));
        
        // Seat information
        seatNumbersText.setText(" " + booking.getSeatNumbers());
        
        // User ID and passenger details
        String passengerInfo = " User ID: " + booking.getUserId() + 
                              "\n Passenger 1: " + booking.getUserName();
        
        // Format additional passengers as numbered list
        if (booking.getExtraPassengers() != null && !booking.getExtraPassengers().isEmpty()) {
            System.out.println("DEBUG - Processing extra passengers: " + booking.getExtraPassengers());
            
            // Split by comma if there are multiple passengers
            if (booking.getExtraPassengers().contains(",")) {
                String[] additionalPassengers = booking.getExtraPassengers().split(",");
                for (int i = 0; i < additionalPassengers.length; i++) {
                    String passengerName = extractPassengerName(additionalPassengers[i]);
                    passengerInfo += "\n Passenger " + (i + 2) + ": " + passengerName;
                }
            } else {
                // Just one extra passenger
                String passengerName = extractPassengerName(booking.getExtraPassengers());
                passengerInfo += "\n Passenger 2: " + passengerName;
            }
        }
        
        userDetailsText.setText(passengerInfo);
        
        // Calculate total cost based on number of seats
        int seatCount = getBookedSeatsCount(booking);
        double totalCost = flight.getCost() * seatCount;
        
        // Display ticket cost
        if (costText != null) {
            costText.setText(" Cost per seat: ₹" + flight.getCost() + 
                            "\n Number of seats: " + seatCount + 
                            "\n Total cost: ₹" + totalCost);
        } else {
            // Add cost information to the departure time text if cost label is not available
            departureTimeText.setText(departureTimeText.getText() + 
                                     "\n Cost per seat: ₹" + flight.getCost() + 
                                     "\n Number of seats: " + seatCount + 
                                     "\n Total cost: ₹" + totalCost);
        }
    }
    
    @FXML
    private void goBackToDashboard(ActionEvent event) {
        SceneSwitcher.switchTo("customer_dashboard.fxml", "Dashboard", event);
    }
}