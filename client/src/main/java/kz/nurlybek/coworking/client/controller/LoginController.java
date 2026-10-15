package kz.nurlybek.coworking.client.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import kz.nurlybek.coworking.client.CoworkingFxApplication;
import kz.nurlybek.coworking.client.api.ApiClient;
import kz.nurlybek.coworking.client.api.ApiException;
import kz.nurlybek.coworking.client.dto.AuthResponse;
import kz.nurlybek.coworking.client.session.SessionManager;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Заполните email и пароль");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Вход...");

        Task<AuthResponse> task = new Task<>() {
            @Override
            protected AuthResponse call() {
                return ApiClient.get().login(email, password);
            }
        };

        task.setOnSucceeded(e -> {
            AuthResponse resp = task.getValue();
            SessionManager.get().store(resp.accessToken, resp.refreshToken, email, null);
            openMainWindow();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = (ex instanceof ApiException ae) ? ae.getMessage() : ex.getMessage();
            errorLabel.setText(msg);
            loginButton.setDisable(false);
            loginButton.setText("Войти");
        });

        Thread t = new Thread(task, "login-task");
        t.setDaemon(true);
        t.start();
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    CoworkingFxApplication.class.getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1100, 700);
            scene.getStylesheets().add(
                    CoworkingFxApplication.class.getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("Coworking Booking — Главное окно");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
            Platform.runLater(() -> errorLabel.setText("Ошибка: " + ex.getMessage()));
        }
    }
}