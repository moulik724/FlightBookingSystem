package controller;
import db.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;
import java.sql.*;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import utils.SceneSwitcher;

public class ViewTicketsController {
    @FXML private VBox ticketDisplayBox;
    @FXML private TextField bookingIdField;
    @FXML private TextArea ticketText;
    @FXML private Button cancelBtn;
    @FXML private Button goBackButton;
    @FXML private Button goDashboardButton;
    
    private boolean isDashboardMode = false;
    private int userId = -1;
    private int selectedBookingId = -1;
    private TextArea selectedTicketArea = null;
    
    @FXML
    public void initialize() {
        // Hide dashboard button by default (will show only in dashboardMode)
        if (goDashboardButton != null) {
            goDashboardButton.setVisible(false);
        }
    }
    
    public void setDashboardMode(boolean value, int userId) {
        this.isDashboardMode = value;
        this.userId = userId;
        
        // Configure UI based on mode
        if (bookingIdField != null) {
            bookingIdField.setVisible(!value);
        }
        
         //Configure back button behavior
        if (goBackButton != null) {
            goBackButton.setText(value ? "Back to Dashboard" : "Back to Login");
        }
        
        // Show/hide dashboard button based on mode
        //if (goDashboardButton != null) {
         //   goDashboardButton.setVisible(value); // Only show in dashboard mode
       // }
        
        // Load tickets automatically if in dashboard mode
        if (value) {
            loadTickets();
        }
    }
    
    @FXML
    public void loadTickets() {
        ticketDisplayBox.getChildren().clear();
        selectedBookingId = -1; // Reset selection
        selectedTicketArea = null;
        
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps;
            
            if (isDashboardMode) {
                // In dashboard mode, get all tickets for this user with flight information
                ps = con.prepareStatement(
                    "SELECT b.*, f.cost, f.departure_time, f.origin, f.destination " +
                    "FROM bookings b " +
                    "LEFT JOIN flights f ON b.flight_number = f.flight_number " +
                    "WHERE b.user_id = ?");
                ps.setInt(1, userId);
            } else {
                // In regular mode, get specific ticket by booking ID with flight information
                if (bookingIdField.getText().isEmpty()) {
                    ticketText.setText("Please enter a booking ID.");
                    return;
                }
                
                try {
                    Integer.parseInt(bookingIdField.getText());
                } catch (NumberFormatException e) {
                    ticketText.setText("Please enter a valid booking ID (numbers only).");
                    return;
                }
                
                ps = con.prepareStatement(
                    "SELECT b.*, f.cost, f.departure_time, f.origin, f.destination " +
                    "FROM bookings b " +
                    "LEFT JOIN flights f ON b.flight_number = f.flight_number " +
                    "WHERE b.id = ?");
                ps.setInt(1, Integer.parseInt(bookingIdField.getText()));
            }
            
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            
            while (rs.next()) {
                found = true;
                final int bookingId = rs.getInt("id");
                TextArea ta = new TextArea(formatTicket(rs));
                ta.setEditable(false);
                ta.setPrefHeight(200);
                ta.setWrapText(true);
                
                // Add click event to select tickets in dashboard mode
                if (isDashboardMode) {
                    ta.setStyle("-fx-border-color: transparent;");
                    
                    // Add click listener to select ticket
                    ta.setOnMouseClicked(event -> {
                        // Deselect previous ticket
                        if (selectedTicketArea != null) {
                            selectedTicketArea.setStyle("-fx-border-color: transparent;");
                        }
                        
                        // Select this ticket
                        selectedBookingId = bookingId;
                        selectedTicketArea = ta;
                        ta.setStyle("-fx-border-color: #1976D2; -fx-border-width: 2px;");
                        ticketText.setText("Selected Booking ID: " + bookingId);
                    });
                }
                
                ticketDisplayBox.getChildren().add(ta);
            }
            
            if (!found) {
                ticketText.setText(isDashboardMode ? 
                    "You have no bookings." : 
                    "No booking found with ID: " + bookingIdField.getText());
            } else if (!isDashboardMode) {
                ticketText.setText(""); // Clear status area when tickets are shown
            }
        } catch (Exception e) {
            ticketText.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    public void cancelBooking() {
        int bookingId;
        
        try {
            if (isDashboardMode) {
                if (selectedBookingId != -1) {
                    // Use the selected booking ID
                    bookingId = selectedBookingId;
                } else {
                    // No ticket selected, prompt for booking ID
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setTitle("Cancel Booking");
                    dialog.setHeaderText("Enter Booking ID to cancel");
                    dialog.setContentText("Booking ID:");
                    
                    Optional<String> result = dialog.showAndWait();
                    if (result.isEmpty()) {
                        return; // User cancelled
                    }
                    
                    bookingId = Integer.parseInt(result.get());
                }
                
                // Verify this booking belongs to the current user
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement checkUserPs = con.prepareStatement(
                        "SELECT user_id FROM bookings WHERE id = ?");
                    checkUserPs.setInt(1, bookingId);
                    ResultSet rs = checkUserPs.executeQuery();
                    
                    if (rs.next()) {
                        int actualUserId = rs.getInt("user_id");
                        if (userId != actualUserId) {
                            ticketText.setText("This booking doesn't belong to your account.");
                            return;
                        }
                    } else {
                        ticketText.setText("Booking ID not found.");
                        return;
                    }
                } catch (SQLException e) {
                    ticketText.setText("Error verifying user: " + e.getMessage());
                    return;
                }
            } else {
                // In regular mode, use the ID from the text field
                if (bookingIdField.getText().isEmpty()) {
                    ticketText.setText("Please enter a booking ID first.");
                    return;
                }
                bookingId = Integer.parseInt(bookingIdField.getText());
                
                // Prompt for user ID for verification
                TextInputDialog userIdDialog = new TextInputDialog();
                userIdDialog.setTitle("User Verification");
                userIdDialog.setHeaderText("Security Verification");
                userIdDialog.setContentText("Please enter your User ID:");
                
                Optional<String> userIdResult = userIdDialog.showAndWait();
                if (userIdResult.isEmpty()) {
                    return; // User cancelled
                }
                
                int enteredUserId;
                try {
                    enteredUserId = Integer.parseInt(userIdResult.get());
                } catch (NumberFormatException e) {
                    ticketText.setText("Invalid User ID. Please enter a valid number.");
                    return;
                }
                
                // Check if the user ID matches the booking
                try (Connection con = DBConnection.getConnection()) {
                    PreparedStatement checkUserPs = con.prepareStatement(
                        "SELECT user_id FROM bookings WHERE id = ?");
                    checkUserPs.setInt(1, bookingId);
                    ResultSet rs = checkUserPs.executeQuery();
                    
                    if (rs.next()) {
                        int actualUserId = rs.getInt("user_id");
                        if (enteredUserId != actualUserId) {
                            ticketText.setText("User ID and Booking ID don't match. Cannot cancel the ticket.");
                            return;
                        }
                    } else {
                        ticketText.setText("Booking ID not found.");
                        return;
                    }
                } catch (SQLException e) {
                    ticketText.setText("Error verifying user: " + e.getMessage());
                    return;
                }
            }
        } catch (NumberFormatException e) {
            ticketText.setText("Invalid booking ID. Please enter numbers only.");
            return;
        }
        
        // Verify the booking exists and get flight information
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement checkPs = con.prepareStatement(
                "SELECT b.id, f.flight_number FROM bookings b " +
                "JOIN flights f ON b.flight_number = f.flight_number " +
                "WHERE b.id = ?");
            checkPs.setInt(1, bookingId);
            
            ResultSet rs = checkPs.executeQuery();
            
            if (!rs.next()) {
                ticketText.setText("Booking ID not found.");
                return;
            }
            
            String flightNumber = rs.getString("flight_number");
            
            // Show confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Are you sure you want to cancel booking ID " + bookingId + "?", 
                ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Cancel Booking Confirmation");
            
            confirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    try (Connection conn = DBConnection.getConnection()) {
                        // First update available seats in flights table
                        // Count seats by counting commas + 1
                        PreparedStatement seatCountPs = conn.prepareStatement(
                            "SELECT seat_number FROM bookings WHERE id = ?");
                        seatCountPs.setInt(1, bookingId);
                        ResultSet seatRs = seatCountPs.executeQuery();
                        
                        if (seatRs.next()) {
                            String seatNumbers = seatRs.getString("seat_number");
                            int seatCount = 0;
                            if (seatNumbers != null && !seatNumbers.isEmpty()) {
                                seatCount = seatNumbers.split(",").length;
                            }
                            
                            // Update available seats
                            PreparedStatement updateSeatsPs = conn.prepareStatement(
                                "UPDATE flights SET available_seats = available_seats + ? " +
                                "WHERE flight_number = ?");
                            updateSeatsPs.setInt(1, seatCount);
                            updateSeatsPs.setString(2, flightNumber);
                            updateSeatsPs.executeUpdate();
                        }
                        
                        // Delete the booking
                        PreparedStatement deletePs = conn.prepareStatement(
                            "DELETE FROM bookings WHERE id = ?");
                        deletePs.setInt(1, bookingId);
                        int result = deletePs.executeUpdate();
                        
                        ticketText.setText(result > 0 ? 
                            "Booking #" + bookingId + " has been cancelled successfully." : 
                            "Failed to cancel booking. Please try again.");
                        
                        // Reset selected ticket
                        selectedBookingId = -1;
                        selectedTicketArea = null;
                        
                        // Refresh tickets display
                        loadTickets();
                    } catch (Exception e) {
                        ticketText.setText("Error: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            ticketText.setText("Error: " + e.getMessage());
        }
    }
    
    @FXML
    private void goBack(ActionEvent event) {
        // Go back to appropriate screen based on mode
        if (isDashboardMode) {
            SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
        } else {
            SceneSwitcher.switchTo("login.fxml", "Login", event);
        }
    }
    
    @FXML
    private void goToDashboard(ActionEvent event) {
        SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
    }
    
    private String formatTicket(ResultSet rs) throws SQLException {
        // Calculate the total cost based on number of seats
        String seats = rs.getString("seat_number");
        int seatCount = 1; // Default to 1 seat
        if (seats != null && !seats.isEmpty()) {
            seatCount = seats.split(",").length;
        }
        double baseCost = rs.getDouble("cost");
        double totalCost = baseCost * seatCount;
        
        StringBuilder ticket = new StringBuilder();
        ticket.append("====== FLIGHT TICKET ======\n\n")
              .append("Booking ID: ").append(rs.getInt("id")).append("\n")
              .append("Flight: ").append(rs.getString("flight_number")).append("\n")
              .append("Route: ").append(rs.getString("origin")).append(" → ").append(rs.getString("destination")).append("\n")
              .append("Name: ").append(rs.getString("user_name")).append("\n")
              .append("Email: ").append(rs.getString("email")).append("\n")
              .append("Seats: ").append(seats != null && !seats.isEmpty() ? seats : "None").append("\n");
        
        String extraPassengers = rs.getString("extra_passengers");
        ticket.append("Passengers: ").append(extraPassengers != null && !extraPassengers.isEmpty() ? extraPassengers : "None").append("\n")
              .append("Departure: ").append(rs.getTimestamp("departure_time")).append("\n")
              .append("Base Cost: ₹").append(String.format("%.2f", baseCost)).append("\n")
              .append("Number of Seats: ").append(seatCount).append("\n")
              .append("Total Cost: ₹").append(String.format("%.2f", totalCost)).append("\n")
              .append("\n==========================");
        
        return ticket.toString();
    }
}