package kz.nurlybek.coworking.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import kz.nurlybek.coworking.client.CoworkingFxApplication;
import kz.nurlybek.coworking.client.session.SessionManager;

public class MainController {

    @FXML private Label welcomeLabel;

    @FXML
    private void initialize() {
        String email = SessionManager.get().getUserEmail();
        welcomeLabel.setText("Вы вошли как: " + (email == null ? "—" : email));
    }

    @FXML
    private void onLogout() {
        SessionManager.get().clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    CoworkingFxApplication.class.getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 480, 420);
            scene.getStylesheets().add(
                    CoworkingFxApplication.class.getResource("/css/styles.css").toExternalForm());
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setTitle("Coworking Booking — Вход");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}