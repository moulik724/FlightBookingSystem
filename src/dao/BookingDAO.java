package dao;
import model.Booking;
import db.DBConnection;
import java.sql.*;

public class BookingDAO {
    public static boolean addBooking(Booking booking) {
        Connection conn = null;
        PreparedStatement bookingStmt = null;
        PreparedStatement updateFlightStmt = null;
        
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Insert the booking
            String bookingSql = "INSERT INTO bookings (user_id, flight_number, user_name, dob, phone, email, address, num_seats, seat_number, extra_passengers) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            bookingStmt = conn.prepareStatement(bookingSql, Statement.RETURN_GENERATED_KEYS);
            bookingStmt.setInt(1, booking.getUserId());
            bookingStmt.setString(2, booking.getFlightNumber());
            bookingStmt.setString(3, booking.getUserName());
            bookingStmt.setString(4, booking.formatDob(booking.getDob()));
            bookingStmt.setString(5, booking.getPhone());
            bookingStmt.setString(6, booking.getEmail());
            bookingStmt.setString(7, booking.getAddress());
            bookingStmt.setInt(8, booking.getNumSeats());
            bookingStmt.setString(9, booking.getSeatNumbers());
            String extraPassengers = booking.getExtraPassengersDetails();
            bookingStmt.setString(10, (extraPassengers != null && !extraPassengers.isEmpty()) ? extraPassengers : null);
            
            int affectedRows = bookingStmt.executeUpdate();
            
            if (affectedRows > 0) {
                // 2. Update the available_seats in the flights table
                String updateFlightSql = "UPDATE flights SET available_seats = available_seats - ? WHERE flight_number = ?";
                updateFlightStmt = conn.prepareStatement(updateFlightSql);
                updateFlightStmt.setInt(1, booking.getNumSeats());
                updateFlightStmt.setString(2, booking.getFlightNumber());
                
                updateFlightStmt.executeUpdate();
                conn.commit(); // Commit transaction
                
                // Get the generated booking ID
                try (ResultSet generatedKeys = bookingStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long bookingId = generatedKeys.getLong(1);
                    }
                }
                return true;
            }
            
            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback on error
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
            }
            System.err.println("Error adding booking: " + e.getMessage());
            return false;
        } finally {
            try {
                if (bookingStmt != null) bookingStmt.close();
                if (updateFlightStmt != null) updateFlightStmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    public static int getTotalSeats(String flightNumber) {
        String sql = "SELECT total_seats FROM flights WHERE flight_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flightNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_seats");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching total seats: " + e.getMessage());
        }
        return 0;
    }
    
    public static boolean isSeatBooked(String flightNumber, int seatNum) {
        String sql = "SELECT seat_number FROM bookings WHERE flight_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, flightNumber);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String seatNumbers = rs.getString("seat_number");
                if (seatNumbers != null) {
                    String[] seats = seatNumbers.split(",");
                    for (String seat : seats) {
                        if (seat.trim().equals(String.valueOf(seatNum))) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking seat booking: " + e.getMessage());
        }
        return false;
    }
}