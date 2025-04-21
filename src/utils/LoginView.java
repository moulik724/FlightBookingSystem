package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import main.MainApp;

public class LoginView implements View {

    @Override
    public Scene getScene() throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/views/login.fxml"));
        Scene scene = new Scene(loader.load());
        
        return scene;
    }

    @Override
    public String getTitle() {
        return "Login - Flight Booking System";
    }
}
