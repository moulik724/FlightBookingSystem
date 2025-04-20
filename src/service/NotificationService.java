package service;
import observer.EmailObserver;
import observer.Subject;
import model.Booking;
/**
 * Service to handle notifications in the application
 */
public class NotificationService extends Subject {
    
    private static NotificationService instance;
    
    private NotificationService() {
        // Private constructor for singleton
    }
    
    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    
    /**
     * Send booking confirmation notification
     */
    public void sendBookingConfirmation(Booking booking) {
        // Create email observer with the email from booking
        EmailObserver emailObserver = new EmailObserver(booking.getEmail());
        
        // Register observer
        registerObserver(emailObserver);
        
        // Notify about booking confirmation
        notifyObservers("BOOKING_CONFIRMED", booking);
        
        // Remove observer after notification (optional)
        removeObserver(emailObserver);
    }
}