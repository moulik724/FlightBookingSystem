package utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

public class SceneSwitcher {

    public static void switchTo(String fxmlFile, String title, ActionEvent event) {
        try {
            URL fxmlPath = SceneSwitcher.class.getResource("/views/" + fxmlFile);
            if (fxmlPath == null) {
                throw new IOException("FXML file not found: /views/" + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(fxmlPath);
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void switchTo(String fxmlFile, String title, ActionEvent event, String role) {
        try {
            URL fxmlPath = SceneSwitcher.class.getResource("/views/" + fxmlFile);
            if (fxmlPath == null) {
                throw new IOException("FXML file not found: /views/" + fxmlFile);
            }

            FXMLLoader loader = new FXMLLoader(fxmlPath);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof controller.RegisterController) {
                ((controller.RegisterController) controller).setRole(role);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading scene: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
