package kz.nurlybek.coworking.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CoworkingFxApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                CoworkingFxApplication.class.getResource("/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 480, 420);
        scene.getStylesheets().add(
                CoworkingFxApplication.class.getResource("/css/styles.css").toExternalForm());

        stage.setTitle("Coworking Booking — Вход");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}