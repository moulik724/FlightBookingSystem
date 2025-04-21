package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.View;
import utils.ViewFactory;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        switchView("login");
    }

    public static void switchView(String viewKey) throws Exception {
        View view = ViewFactory.getView(viewKey);
        Scene scene = view.getScene();

        primaryStage.setTitle(view.getTitle());
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
