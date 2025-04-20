package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import db.DBInitializer;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        // Save primary stage for scene switching
        primaryStage = stage;

        // Initialize database
        //DBInitializer.initializeDatabase();

        // Load initial login scene
        switchToLogin();
    }

    public static void switchToLogin() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/login.fxml"));
        Scene scene = new Scene(loader.load());
        //scene.getStylesheets().add(MainApp.class.getResource("/styles/styles.css").toExternalForm//()
//);
//scene.getStylesheets().add(MainApp.class.getResource("/styles/login.css").toExternalForm(//));
//scene.getStylesheets().add(MainApp.class.getResource("/styles/customer_dashboard.css").toExternalForm());
        primaryStage.setTitle("Login - Flight Booking System");
        primaryStage.setFullScreen(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void switchToManagerDashboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/manager_dashboard.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(MainApp.class.getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Manager Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
