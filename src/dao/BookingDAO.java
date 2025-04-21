package dao;

import model.Booking;
import db.DBConnection;

import java.sql.*;

public class BookingDAO {

    public static boolean addBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, flight_number, user_name, dob, phone, email, address, num_seats, seat_number, extra_passengers) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, booking.getUserId());
            stmt.setString(2, booking.getFlightNumber());
            stmt.setString(3, booking.getUserName());
            stmt.setString(4, booking.formatDob(booking.getDob()));
            stmt.setString(5, booking.getPhone());
            stmt.setString(6, booking.getEmail());
            stmt.setString(7, booking.getAddress());
            stmt.setInt(8, booking.getNumSeats());
            stmt.setString(9, booking.getSeatNumbers());

            String extraPassengers = booking.getExtraPassengersDetails();
            stmt.setString(10, (extraPassengers != null && !extraPassengers.isEmpty()) ? extraPassengers : null);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long bookingId = generatedKeys.getLong(1);
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding booking: " + e.getMessage());
        }
        return false;
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
