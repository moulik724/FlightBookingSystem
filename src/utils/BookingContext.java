package utils;
import model.Booking;


public class BookingContext {
    private static String selectedFlightNumber;
    private static int loggedInUserId;
    private static Booking currentBooking;

    // Getter and Setter for selectedFlightNumber
    public static String getSelectedFlightNumber() {
        return selectedFlightNumber;
    }

    public static void setSelectedFlightNumber(String flightNumber) {
        selectedFlightNumber = flightNumber;
    }

    // Getter and Setter for loggedInUserId
    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(int userId) {
        loggedInUserId = userId;
    }

    

    public static void setCurrentBooking(Booking booking) {
        currentBooking = booking;
    }

    public static Booking getCurrentBooking() {
        return currentBooking;
    }
}
