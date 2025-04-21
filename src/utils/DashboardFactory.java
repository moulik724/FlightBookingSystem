package utils;

import javafx.event.ActionEvent;

public class DashboardFactory {

    public static void redirectToDashboard(String role, ActionEvent event) {
        switch (role.toLowerCase()) {
            case "admin":
                SceneSwitcher.switchTo("dashboard.fxml", "Admin Dashboard", event);
                break;
            case "manager":
                SceneSwitcher.switchTo("manager_dashboard.fxml", "Manager Dashboard", event);
                break;
            case "customer":
                SceneSwitcher.switchTo("customer_dashboard.fxml", "Customer Dashboard", event);
                break;
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }
    }
}
