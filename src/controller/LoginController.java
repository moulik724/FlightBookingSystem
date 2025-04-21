package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import model.User;
import dao.UserDAO;
import utils.SceneSwitcher;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import javafx.event.ActionEvent;
import utils.DashboardFactory;


import utils.BookingContext;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;


@FXML
private void handleLogin(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
        showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter both username and password.");
        return;
    }

    // Database login check
    User user = UserDAO.getUser(username, password);
    if (user != null) {
        // Set the logged-in user's ID in BookingContext
        BookingContext.setLoggedInUserId(user.getId());

        // Use the DashboardFactory to redirect based on user role
        try {
            DashboardFactory.redirectToDashboard(user.getRole(), event);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", e.getMessage());
        }
    } else {
        showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials. Try again.");
    }
}



@FXML
private void handleViewTickets(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/viewtickets.fxml"));
        Parent root = loader.load();

        controller.ViewTicketsController controller = loader.getController();
        controller.setDashboardMode(false, -1); // login-side mode

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Find Ticket by ID");
        stage.setFullScreenExitHint("");  // ← this line hides ESC message
        stage.setFullScreen(true);
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    

@FXML
    private void handleRegisterRedirect(ActionEvent event) {
        SceneSwitcher.switchTo("register.fxml", "Register", event);
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
