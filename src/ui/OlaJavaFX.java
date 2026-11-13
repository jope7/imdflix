package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class OlaJavaFX extends Application {
    @Override 
    public void start(Stage stage) {
        stage.setTitle("Teste - imd flix");
        stage.setScene(new Scene(new Label("IMDFlix funcionando"), 300, 100));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}