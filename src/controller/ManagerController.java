package controller;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Flight;
import utils.SceneSwitcher;
import dao.FlightDAO;
import javafx.event.ActionEvent;

import java.net.URL;
import java.io.IOException;
import java.util.ResourceBundle;

public class ManagerController implements Initializable {

    @FXML private TableView<Flight> seatsTable;
    @FXML private TableColumn<Flight, String> flightNumberCol;
    @FXML private TableColumn<Flight, String> originCol;
    @FXML private TableColumn<Flight, String> destinationCol;
    @FXML private TableColumn<Flight, String> departureTimeCol;
    @FXML private TableColumn<Flight, Integer> totalSeatsCol;
    @FXML private TableColumn<Flight, Integer> availableSeatsCol;
    @FXML private TableColumn<Flight, Double> costCol;
    @FXML private TableColumn<Flight, Boolean> approvedCol;

    private final ObservableList<Flight> flightList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Setting up the columns
        flightNumberCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getFlightNumber()));  // Show flight number
        originCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getOrigin())); // Show origin
        destinationCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDestination()));  // Show destination
        departureTimeCol.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getDepartureTime().toString()));  // Show departure time
        totalSeatsCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getTotalSeats()).asObject());  // Show total seats
        availableSeatsCol.setCellValueFactory(data -> 
            new SimpleIntegerProperty(data.getValue().getAvailableSeats()).asObject());  // Show available seats
        costCol.setCellValueFactory(data -> 
            new SimpleDoubleProperty(data.getValue().getCost()).asObject());  // Show cost
        approvedCol.setCellValueFactory(data -> 
            new SimpleBooleanProperty(data.getValue().isApproved()).asObject());  // Show approved status

        loadPendingFlights();
    }

    private void loadPendingFlights() {
        flightList.clear();
        flightList.addAll(FlightDAO.getPendingFlights());
        seatsTable.setItems(flightList);
    }

    @FXML
    private void viewAllFlights() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/approved_flights.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Approved Flights");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading approved flights window.");
        }
    }

    @FXML
    private void approveSelected() {
        Flight selected = seatsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a flight to approve.");
            return;
        }

        boolean result = FlightDAO.approveFlight(selected.getFlightNumber());
        if (result) {
            showAlert("Flight approved successfully!");
            loadPendingFlights();
        } else {
            showAlert("Failed to approve the flight.");
        }
    }

    @FXML
    private void disapproveSelected() {
        Flight selected = seatsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a flight to disapprove.");
            return;
        }

        boolean result = FlightDAO.disapproveFlight(selected.getFlightNumber());
        if (result) {
            showAlert("Flight disapproved and removed.");
            loadPendingFlights();
        } else {
            showAlert("Failed to disapprove the flight.");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        SceneSwitcher.switchTo("login.fxml", "Login", event);
    }

    @FXML
    private void handleAddManager(ActionEvent event) {
        SceneSwitcher.switchTo("register.fxml", "Register New Manager", event, "manager");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
