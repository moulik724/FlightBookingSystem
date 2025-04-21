package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import main.MainApp;

public class CustomerDashboardView implements View {

    @Override
    public Scene getScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/customer_dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/styles.css").toExternalForm());
        return scene;
    }

    @Override
    public String getTitle() {
        return "Customer Dashboard";
    }
}
