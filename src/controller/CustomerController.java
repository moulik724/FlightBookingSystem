package controller;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import model.Flight;
import model.User;
import dao.FlightDAO;
import dao.UserDAO;
import utils.SceneSwitcher;
import utils.BookingContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import db.DBConnection;

import controller.ViewTicketsController;
import utils.BookingContext;

import java.util.List;

public class CustomerController {

    @FXML
    private VBox flightsContainer;

    @FXML
    public void initialize() {
        List<Flight> flights = FlightDAO.getApprovedFlights();

        for (Flight flight : flights) {
            // Flight card container
            VBox flightCard = new VBox(10);
            flightCard.setStyle(
                "-fx-background-color: #ffffff; " +
                "-fx-border-color: #cccccc; " +
                "-fx-border-radius: 12; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
            );
            flightCard.setAlignment(Pos.CENTER_LEFT);

            // Styling for labels
            String labelStyle = "-fx-font-size: 16px; -fx-font-weight: 500; -fx-text-fill: #333333;";

            Label flightLabel = new Label("✈ Flight: " + flight.getFlightNumber());
            flightLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2a2a2a;");

            Label routeLabel = new Label("From: " + flight.getOrigin() + " → To: " + flight.getDestination());
            routeLabel.setStyle(labelStyle);

            Label departureLabel = new Label("Departure: " + flight.getDepartureTime());
            departureLabel.setStyle(labelStyle);

            Label seatsCostLabel = new Label("Seats: " + flight.getAvailableSeats() + "/" + flight.getTotalSeats() +
                    "  |  Cost: ₹" + flight.getCost());
            seatsCostLabel.setStyle(labelStyle);

            // Book button
            Button bookBtn = new Button("Book Now");
            bookBtn.setStyle(
                "-fx-background-color: #4C8BF5; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;"
            );
            bookBtn.setOnAction(e -> goToBooking(e, flight.getFlightNumber()));

            // Add everything to the card
            flightCard.getChildren().addAll(flightLabel, routeLabel, departureLabel, seatsCostLabel, bookBtn);

            // Add card to the main container
            flightsContainer.getChildren().add(flightCard);
        }
    }

    private void goToBooking(ActionEvent event, String flightNumber) {
        BookingContext.setSelectedFlightNumber(flightNumber);
        System.out.println("Selected Flight Number set to: " + flightNumber); // Debugging line
        SceneSwitcher.switchTo("book.fxml", "Book Flight", event);
    }

    @FXML
    private void handleViewTickets(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/viewtickets.fxml"));
            Parent root = loader.load();

            controller.ViewTicketsController controller = loader.getController();
            controller.setDashboardMode(true, utils.BookingContext.getLoggedInUserId()); // user ID from context

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Tickets");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
private void handleViewProfile(ActionEvent event) {
    // Get current user data from database
    int userId = BookingContext.getLoggedInUserId();
    User user = getUserById(userId);
    
    if (user == null) {
        // Handle error - user not found
        System.err.println("Error: Could not retrieve user profile");
        return;
    }
    
    // Create a new stage for the popup
    Stage popupStage = new Stage();
    popupStage.initModality(Modality.APPLICATION_MODAL); // This makes it a modal dialog
    popupStage.initOwner(((Node) event.getSource()).getScene().getWindow()); // Set owner to current window
    popupStage.setTitle("My Profile");
    
    // Create the grid layout
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));
    
    // Style constants
    String labelStyle = "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;";
    String textFieldStyle = "-fx-font-size: 14px; -fx-background-radius: 6;";
    String buttonStyle = "-fx-background-color: #4C8BF5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;";
    
    // User ID (non-editable)
    Label idLabel = new Label("User ID:");
    idLabel.setStyle(labelStyle);
    Label idValue = new Label(String.valueOf(user.getId()));
    idValue.setStyle(textFieldStyle);
    
    // Username
    Label usernameLabel = new Label("Username:");
    usernameLabel.setStyle(labelStyle);
    TextField usernameField = new TextField(user.getUsername());
    usernameField.setStyle(textFieldStyle);
    
    // Password field with show/hide functionality
    Label passwordLabel = new Label("Password:");
    passwordLabel.setStyle(labelStyle);
    
    HBox passwordBox = new HBox(10);
    PasswordField passwordField = new PasswordField();
    passwordField.setText(user.getPassword());
    passwordField.setStyle(textFieldStyle);
    
    TextField visiblePasswordField = new TextField(user.getPassword());
    visiblePasswordField.setStyle(textFieldStyle);
    visiblePasswordField.setManaged(false);
    visiblePasswordField.setVisible(false);
    
    Button showPasswordButton = new Button("Show");
    showPasswordButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-background-radius: 6;");
    
    showPasswordButton.setOnAction(e -> {
        if (passwordField.isVisible()) {
            showPasswordButton.setText("Hide");
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
        } else {
            showPasswordButton.setText("Show");
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
            passwordField.setManaged(true);
            passwordField.setVisible(true);
        }
    });
    
    passwordBox.getChildren().addAll(passwordField, visiblePasswordField, showPasswordButton);
    
    // Save and cancel buttons
    Button saveButton = new Button("Save Changes");
    saveButton.setStyle(buttonStyle);
    
    Button cancelButton = new Button("Cancel");
    cancelButton.setStyle("-fx-background-color: #ff4c4c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
    
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(saveButton, cancelButton);
    
    // Add components to grid
    grid.add(idLabel, 0, 0);
    grid.add(idValue, 1, 0);
    grid.add(usernameLabel, 0, 1);
    grid.add(usernameField, 1, 1);
    grid.add(passwordLabel, 0, 2);
    grid.add(passwordBox, 1, 2);
    grid.add(buttonBox, 0, 3, 2, 1);
    
    // Event handlers
    saveButton.setOnAction(e -> {
        // Update user details
        String newUsername = usernameField.getText().trim();
        String newPassword = passwordField.isVisible() ? 
                             passwordField.getText() : 
                             visiblePasswordField.getText();
        
        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            // Show error message - fields cannot be empty
            showErrorMessage(popupStage, "Username and password cannot be empty!");
            return;
        }
        
        // Update the user in database
        boolean success = updateUserProfile(user.getId(), newUsername, newPassword);
        
        if (success) {
            // Close the popup
            popupStage.close();
        } else {
            // Show error message
            showErrorMessage(popupStage, "Failed to update profile. Username may already be taken.");
        }
    });
    
    cancelButton.setOnAction(e -> popupStage.close());
    
    // Set the scene and show the stage
    Scene scene = new Scene(grid, 400, 250);
    popupStage.setScene(scene);
    popupStage.setResizable(false);
    popupStage.showAndWait(); // This will block until the dialog is closed
}
    
    // Helper method to get user by ID
    private User getUserById(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        User user = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        
        return user;
    }
    
    // Helper method to update user profile
    private boolean updateUserProfile(int userId, String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        boolean success = false;
        
        try {
            conn = DBConnection.getConnection();
            
            // First check if the username already exists (for another user)
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) > 0) {
                // Username is already taken
                return false;
            }
            
            // If username is available, update the user
            String sql = "UPDATE users SET username = ?, password = ? WHERE id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setInt(3, userId);
            
            int rowsAffected = stmt.executeUpdate();
            success = (rowsAffected > 0);
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null) {
                try { stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
        
        return success;
    }
    
    private void showErrorMessage(Stage owner, String message) {
        Stage errorStage = new Stage();
        errorStage.initModality(Modality.APPLICATION_MODAL);
        errorStage.initOwner(owner);
        errorStage.setTitle("Error");
        
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ff4c4c;");
        
        Button okButton = new Button("OK");
        okButton.setStyle("-fx-background-color: #4C8BF5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        okButton.setOnAction(e -> errorStage.close());
        
        vbox.getChildren().addAll(errorLabel, okButton);
        
        Scene scene = new Scene(vbox, 300, 150);
        errorStage.setScene(scene);
        errorStage.showAndWait();
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        SceneSwitcher.switchTo("login.fxml", "Login", event);
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        SceneSwitcher.switchTo("search.fxml", "Search Flights", event);
    }
}