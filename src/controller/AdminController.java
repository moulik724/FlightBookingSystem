package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import model.Flight;
import dao.FlightDAO;
import utils.SceneSwitcher;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private TableView<Flight> flightsTable;
    @FXML private TableColumn<Flight, String> flightNumberColumn;
    @FXML private TableColumn<Flight, String> originColumn;
    @FXML private TableColumn<Flight, String> destinationColumn;
    @FXML private TableColumn<Flight, String> departureTimeColumn;
    @FXML private TableColumn<Flight, Integer> availableSeatsColumn;
    @FXML private TableColumn<Flight, Double> costColumn;

    @FXML private TextField flightNumberField;
    @FXML private TextField originField;
    @FXML private TextField destinationField;
    @FXML private TextField departureField;
    @FXML private TextField seatsField;
    @FXML private TextField costField;

    private final ObservableList<Flight> flightList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        flightNumberColumn.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        originColumn.setCellValueFactory(new PropertyValueFactory<>("origin"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));

        departureTimeColumn.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getDepartureTime();
            String formatted = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return new SimpleStringProperty(formatted);
        });

        availableSeatsColumn.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));

        loadFlightsFromDB();
    }

    private void loadFlightsFromDB() {
        flightList.setAll(FlightDAO.getApprovedFlights());
        flightsTable.setItems(flightList);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setContentText("Are you sure you want to logout?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SceneSwitcher.switchTo("login.fxml", "Login", event);
        }
    }

    @FXML
    private void handleAddAdmin(ActionEvent event) {
        SceneSwitcher.switchTo("register.fxml", "Register Admin", event, "admin");
    }

    @FXML
    private void addFlight() {
        String flightNumber = flightNumberField.getText();
        String origin = originField.getText();
        String destination = destinationField.getText();
        String departure = departureField.getText();
        String seatsText = seatsField.getText();
        String costText = costField.getText();

        if (flightNumber.isEmpty() || origin.isEmpty() || destination.isEmpty() || departure.isEmpty() || seatsText.isEmpty() || costText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "All fields must be filled!");
            return;
        }

        try {
            LocalDateTime dt = LocalDateTime.parse(departure, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            int seats = Integer.parseInt(seatsText);
            double cost = Double.parseDouble(costText);

            Flight flight = new Flight(flightNumber, origin, destination, dt, seats, seats, cost, false);
            boolean success = FlightDAO.addFlight(flight);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Flight Added", "Flight added successfully but pending approval.");
                loadFlightsFromDB();
                clearFields();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add flight to the database.");
            }
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Date Format Error", "Please use the format: yyyy-MM-dd HH:mm");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format Error", "Seats and Cost must be numeric.");
        }
    }

    private void clearFields() {
        flightNumberField.clear();
        originField.clear();
        destinationField.clear();
        departureField.clear();
        seatsField.clear();
        costField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
