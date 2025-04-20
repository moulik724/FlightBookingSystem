package observer;

import model.Booking;
import model.Flight;
import db.DBConnection;

import javax.mail.*;
import javax.mail.internet.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Email notification observer that sends emails when events occur
 */
public class EmailObserver implements Observer {
    private String recipientEmail;
    
    public EmailObserver(String email) {
        this.recipientEmail = email;
    }
    
    @Override
    public void update(String eventType, Object data) {
        if (data instanceof Booking && "BOOKING_CONFIRMED".equals(eventType)) {
            Booking booking = (Booking) data;
            
            // If booking ID is 0, try to retrieve it from database
            if (booking.getBookingId() == 0) {
                int retrievedId = retrieveBookingId(booking);
                if (retrievedId > 0) {
                    booking.setBookingId(retrievedId);
                    System.out.println("DEBUG - Retrieved ID for email: " + retrievedId);
                }
            }
            
            // Get flight details
            Flight flightDetails = getFlightDetails(booking.getFlightNumber());
            
            // Send email with complete information
            sendBookingConfirmation(booking, flightDetails);
        }
    }
    
    private int retrieveBookingId(Booking booking) {
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
            
            System.out.println("DEBUG - Email Observer executing query: " + query);
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                bookingId = resultSet.getInt("id");
                System.out.println("DEBUG - Email Observer found booking ID: " + bookingId);
            }
        } catch (SQLException e) {
            System.err.println("Database error in EmailObserver: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return bookingId;
    }
    
    private Flight getFlightDetails(String flightNumber) {
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
        } catch (SQLException e) {
            System.err.println("Error retrieving flight details: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return flight;
    }
    
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
    
    private String getExtraPassengers(Booking booking) {
        String extraPassengers = booking.getExtraPassengers();
        
        // If not available in the booking object, fetch from database
        if (extraPassengers == null || extraPassengers.isEmpty()) {
            try {
                Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                    "SELECT extra_passengers FROM bookings WHERE id = ?"
                );
                statement.setInt(1, booking.getBookingId());
                ResultSet resultSet = statement.executeQuery();
                
                if (resultSet.next()) {
                    extraPassengers = resultSet.getString("extra_passengers");
                    booking.setExtraPassengers(extraPassengers);
                }
                
                resultSet.close();
                statement.close();
            } catch (SQLException e) {
                System.err.println("Error retrieving extra passengers: " + e.getMessage());
            }
        }
        
        return extraPassengers;
    }
    
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
    
    private void sendBookingConfirmation(Booking booking, Flight flight) {
        if (flight == null) {
            // If flight details couldn't be fetched, send a simplified confirmation
            sendSimpleBookingConfirmation(booking);
            return;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        String extraPassengers = getExtraPassengers(booking);
        int seatCount = getBookedSeatsCount(booking);
        double totalCost = flight.getCost() * seatCount;
        
        String subject = "Booking Confirmation - Flight " + booking.getFlightNumber();
        
        StringBuilder message = new StringBuilder();
        message.append("Dear ").append(booking.getUserName()).append(",\n\n");
        message.append("Your booking has been confirmed!\n\n");
        message.append("BOOKING DETAILS\n");
        message.append("==============\n");
        message.append("Booking ID: ").append(booking.getBookingId()).append("\n");
        message.append("Flight Number: ").append(flight.getFlightNumber()).append("\n\n");
        
        message.append("FLIGHT INFORMATION\n");
        message.append("=================\n");
        message.append("Route: ").append(flight.getOrigin()).append(" to ").append(flight.getDestination()).append("\n");
        message.append("Departure: ").append(flight.getDepartureTime().format(formatter)).append("\n\n");
        
        message.append("SEAT INFORMATION\n");
        message.append("===============\n");
        message.append("Seat Number(s): ").append(booking.getSeatNumbers()).append("\n\n");
        
        message.append("PASSENGER DETAILS\n");
        message.append("================\n");
        message.append("User ID: ").append(booking.getUserId()).append("\n");
        message.append("Passenger 1: ").append(booking.getUserName()).append("\n");
        
        // Add additional passengers
        if (extraPassengers != null && !extraPassengers.isEmpty()) {
            if (extraPassengers.contains(",")) {
                String[] additionalPassengers = extraPassengers.split(",");
                for (int i = 0; i < additionalPassengers.length; i++) {
                    String passengerName = extractPassengerName(additionalPassengers[i]);
                    message.append("Passenger ").append(i + 2).append(": ").append(passengerName).append("\n");
                }
            } else {
                String passengerName = extractPassengerName(extraPassengers);
                message.append("Passenger 2: ").append(passengerName).append("\n");
            }
        }
        message.append("\n");
        
        message.append("PRICING DETAILS\n");
        message.append("==============\n");
        message.append("Cost per seat: ₹").append(flight.getCost()).append("\n");
        message.append("Number of seats: ").append(seatCount).append("\n");
        message.append("Total cost: ₹").append(totalCost).append("\n\n");
        
        message.append("Thank you for choosing our service!\n\n");
        message.append("Safe travels,\n");
        message.append("The Flight Booking Team");
        
        sendEmail(subject, message.toString());
    }
    
    private void sendSimpleBookingConfirmation(Booking booking) {
        String subject = "Booking Confirmation - Flight " + booking.getFlightNumber();
        String message = "Dear " + booking.getUserName() + ",\n\n" +
                "Your booking has been confirmed!\n" +
                "Booking ID: " + booking.getBookingId() + "\n" +
                "Flight: " + booking.getFlightNumber() + "\n" +
                "Seat(s): " + booking.getSeatNumbers() + "\n\n" +
                "Thank you for choosing our service.\n\n" +
                "Safe travels!";
        
        sendEmail(subject, message);
    }
    
    private void sendEmail(String subject, String messageBody) {
        // Email server configuration
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "moulikmachaiah0724@gmail.com"; // Replace with actual email
        String password = "lzxl fofd ipiu zpqh"; // Replace with actual app password
        
        // Set properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        
        try {
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(messageBody);
            
            // Send message
            Transport.send(message);
            
            System.out.println("Email sent successfully to " + recipientEmail);
            
        } catch (MessagingException e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
}