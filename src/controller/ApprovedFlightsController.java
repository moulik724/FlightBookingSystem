package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import model.Flight;
import dao.FlightDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;

public class ApprovedFlightsController {

    @FXML
    private TableView<Flight> approvedFlightsTable;

    @FXML
    private TableColumn<Flight, String> flightNumberCol;

    @FXML
    private TableColumn<Flight, String> departureCol;

    @FXML
    private TableColumn<Flight, String> originCol;

    @FXML
    private TableColumn<Flight, String> destinationCol;

    @FXML
    private TableColumn<Flight, Boolean> approvedCol;

    @FXML
    private void initialize() {
        // Set up the columns in the TableView
        flightNumberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFlightNumber()));
        departureCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDepartureTime().toString()));
        originCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrigin()));
        destinationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDestination()));
        approvedCol.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isApproved()).asObject());

        // Load approved flights
        loadApprovedFlights();
    }

    private void loadApprovedFlights() {
        // Fetch approved flights from the database
        approvedFlightsTable.setItems(FXCollections.observableArrayList(FlightDAO.getApprovedFlights()));
    }

    @FXML
    private void handleCloseWindow(ActionEvent event) {
        // Close the current window
        Stage stage = (Stage) approvedFlightsTable.getScene().getWindow();
        stage.close();
    }
}
