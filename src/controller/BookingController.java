package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Booking;
import dao.BookingDAO;
import utils.BookingContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import utils.SceneSwitcher;
import service.NotificationService;


import java.util.HashSet;
import java.util.Set;

public class BookingController {

    @FXML private TextField userIdField;
    @FXML private TextField flightNumberField;
    @FXML private TextField nameField;
    @FXML private TextField dobField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private ComboBox<String> seatsDropdown;
    @FXML private VBox passengerDetailsContainer;
    @FXML private Button confirmButton;
    @FXML private VBox seatLayoutContainer;
    @FXML private TextField seatNumbersField;
    @FXML private Button payButton;
    @FXML private Label paymentLabel;

    private int userId;
    private String flightNumber;
    private Set<Button> selectedSeats = new HashSet<>();
    private int totalSeats;
    private int seatsToSelect = 0;

    @FXML
    public void initialize() {
        flightNumber = BookingContext.getSelectedFlightNumber();
        flightNumberField.setText(flightNumber);
        flightNumberField.setEditable(false);

        userId = BookingContext.getLoggedInUserId();
        userIdField.setText(String.valueOf(userId));
        userIdField.setEditable(false);

        seatsDropdown.getItems().addAll("1", "2", "3", "4");
        seatsDropdown.setValue("Select");

        // Fetch totalSeats from the database
        totalSeats = BookingDAO.getTotalSeats(flightNumber);

        updateSeatLayout();
    }

    @FXML
    public void handleSeatSelection(ActionEvent event) {
        seatsToSelect = Integer.parseInt(seatsDropdown.getValue());
        selectedSeats.clear();
        seatLayoutContainer.getChildren().clear();
        passengerDetailsContainer.getChildren().clear();
        updateSeatLayout();
        updatePassengerDetailsForm();
    }

    private void updateSeatLayout() {
        int rows = 5;  // Number of rows in the seating layout
        int cols = (totalSeats + rows - 1) / rows;  // Calculate number of columns based on total seats

        seatLayoutContainer.getChildren().clear();  // Clear the current layout

        for (int i = 0; i < rows; i++) {
            HBox row = new HBox(5);  // Create a horizontal box for each row
            for (int j = i; j < totalSeats; j += rows) {
                int seatNum = j + 1;
                if (seatNum > totalSeats) break;  // If the seat number exceeds totalSeats, stop

                Button seatButton = new Button(String.valueOf(seatNum));
                
                // Set consistent size for both booked and available seats
                seatButton.setPrefSize(30, 30);  // Set preferred size for both types of buttons
                seatButton.setStyle("-fx-font-size:10px;");  // Use larger font size for better visibility
                
                if (BookingDAO.isSeatBooked(flightNumber, seatNum)) {  // Check if the seat is booked
                    seatButton.setStyle("-fx-background-color: red;-fx-text-fill: white; -fx-font-size: 10px;");
                    seatButton.setDisable(true);  // Disable booked seats
                } else {
                    seatButton.setStyle("-fx-background-color: green; -fx-font-size: 10px;");
                    seatButton.setOnAction(e -> handleSeatSelectionAction(seatButton, seatNum));  // Action for available seats
                }

                row.getChildren().add(seatButton);  // Add the seat button to the row
            }
            seatLayoutContainer.getChildren().add(row);  // Add the row to the seat layout container
        }
    }

    private void updatePassengerDetailsForm() {
        for (int i = 0; i < seatsToSelect - 1; i++) {
            VBox passengerForm = new VBox(10);
            TextField passengerName = new TextField();
            passengerName.setPromptText("Enter passenger " + (i + 2) + " name");
            TextField passengerDob = new TextField();
            passengerDob.setPromptText("Enter passenger " + (i + 2) + " date of birth (YYYY-MM-DD)");

            passengerForm.getChildren().addAll(
                new Label("Passenger " + (i + 2) + " Name:"), passengerName,
                new Label("Passenger " + (i + 2) + " Date of Birth:"), passengerDob
            );

            passengerDetailsContainer.getChildren().add(passengerForm);
        }
    }

    private void handleSeatSelectionAction(Button seatButton, int seatNum) {
        if (seatButton.getStyle().contains("red")) {
            return; // Booked seat, can't select
        }

        if (seatButton.getStyle().contains("green")) {
            if (selectedSeats.size() < seatsToSelect) {
                seatButton.setStyle("-fx-background-color: yellow; -fx-font-size: 10px; -fx-padding: 0;");
                seatButton.setPrefSize(30, 30);
                selectedSeats.add(seatButton);
            }
        } else if (seatButton.getStyle().contains("yellow")) {
            seatButton.setStyle("-fx-background-color: green; -fx-font-size: 10px; -fx-padding: 0;");
            seatButton.setPrefSize(30, 30);
            selectedSeats.remove(seatButton);
        }

        updateSeatNumbersField();
        updateConfirmButtonState();
    }

    private void updateSeatNumbersField() {
        StringBuilder seatNumbers = new StringBuilder();
        for (Button seat : selectedSeats) {
            if (seatNumbers.length() > 0) {
                seatNumbers.append(", ");
            }
            seatNumbers.append(seat.getText());
        }
        seatNumbersField.setText(seatNumbers.toString());
    }

    private void updateConfirmButtonState() {
        confirmButton.setDisable(selectedSeats.size() != seatsToSelect);
    }

@FXML
    private void goBackToDashboard(ActionEvent event) {
        SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
    }
    

@FXML
private void confirmBooking(ActionEvent event) {
    try {
        String name = nameField.getText();
        String dob = dobField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String address = addressField.getText();
        int numSeats = Integer.parseInt(seatsDropdown.getValue());

        if (name.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            throw new IllegalArgumentException("All fields must be filled.");
        }

        String seatNumbers = seatNumbersField.getText();
        if (seatNumbers.isEmpty()) {
            throw new IllegalArgumentException("Please select seats.");
        }

        // Create a booking object
        Booking booking = new Booking(flightNumber, name, dob, phone, email, address, numSeats, userId, seatNumbers);

        // Add passengers' details
        for (int i = 0; i < numSeats - 1; i++) {
            VBox passengerForm = (VBox) passengerDetailsContainer.getChildren().get(i);
            String passengerName = ((TextField) passengerForm.getChildren().get(1)).getText();
            String passengerDob = ((TextField) passengerForm.getChildren().get(3)).getText();

            if (passengerName.isEmpty() || passengerDob.isEmpty()) {
                throw new IllegalArgumentException("Please fill in all fields for passenger " + (i + 2));
            }

            booking.addPassenger(passengerName, passengerDob);
        }

        boolean success = BookingDAO.addBooking(booking);

        if (success) {
            System.out.println("Booking successful. Redirecting to ticket page...");

            // Save booking into context
            BookingContext.setCurrentBooking(booking);

            // Send booking confirmation notification
            NotificationService.getInstance().sendBookingConfirmation(booking);

            // Use SceneSwitcher to switch to ticket.fxml
            SceneSwitcher.switchTo("ticket.fxml", "Your Ticket", event);

        } else {
            showAlert("Booking Failed", "There was an issue with your booking. Please try again.");
        }

    } catch (NumberFormatException e) {
        showAlert("Invalid Input", "Number of seats must be a valid number.");
    } catch (IllegalArgumentException e) {
        showAlert("Invalid Input", e.getMessage());
    } catch (Exception e) {
        showAlert("Error", "Something went wrong. Please try again.");
        e.printStackTrace();
    }
}



    @FXML
    private void handlePayment(ActionEvent event) {
        payButton.setText("Paid");
        payButton.setStyle("-fx-background-color: green;");
        paymentLabel.setText("Payment Successful!");

        // Enable the confirm button after payment
        confirmButton.setDisable(false);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
