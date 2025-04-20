package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.DatePicker;
import javafx.collections.FXCollections;
import model.Flight;
import dao.FlightDAO;
import utils.BookingContext;
import utils.SceneSwitcher;
import javafx.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

public class FlightSearchController {
    @FXML private TextField sourceField;
    @FXML private TextField destinationField;
    @FXML private DatePicker datePicker;
    @FXML private ListView<String> flightsList;

    private List<Flight> foundFlights;

    @FXML
    private void handleSearch() {
        String source = sourceField.getText();
        String dest = destinationField.getText();
        LocalDate date = datePicker.getValue();

        if (source.isEmpty() || dest.isEmpty() || date == null) {
            showAlert("Please fill in all fields.");
            return;
        }

        LocalDateTime dateTime = date.atStartOfDay();
        foundFlights = FlightDAO.searchFlights(source, dest, dateTime);

        if (!foundFlights.isEmpty()) {
            flightsList.setItems(FXCollections.observableArrayList(
                foundFlights.stream().map(Flight::toString).toList()
            ));
        } else {
            showAlert("No flights found.");
            flightsList.getItems().clear();
        }
    }
@FXML
    private void goBackToDashboard(ActionEvent event) {
        SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
    }
    
@FXML
    private void handleBook(ActionEvent event) {
        int index = flightsList.getSelectionModel().getSelectedIndex();
        if (index >= 0 && index < foundFlights.size()) {
            String selectedFlightNumber = foundFlights.get(index).getFlightNumber();
            BookingContext.setSelectedFlightNumber(selectedFlightNumber);
            SceneSwitcher.switchTo("book.fxml", "Book Flight", event);
        } else {
            showAlert("Please select a flight to book.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
