package utils;

import javafx.scene.Scene;

public interface View {
    Scene getScene() throws Exception;
    String getTitle();
}
