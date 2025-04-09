package org.example.client.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.client.RSocketClient;
import org.example.client.dto.AuthData;
import org.example.client.dto.AuthResponse;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField1;

    @FXML
    private PasswordField passwordField2;

    @FXML
    private Label errorLabel;

    @FXML
    private void onRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password1 = passwordField1.getText();
        String password2 = passwordField2.getText();

        if (username == null || username.isEmpty() ||
                password1 == null || password1.isEmpty() ||
                password2 == null || password2.isEmpty()) {
            errorLabel.setText("Пожалуйста, заполните все поля.");
            return;
        }

        if (!password1.equals(password2)) {
            errorLabel.setText("Пароли не совпадают.");
            return;
        }

        errorLabel.setText(""); // очистить ошибку
        System.out.println("Регистрация: " + username + ", пароль: " + password1);
        // Создаём объект запроса
        AuthData request = new AuthData(username, password1);

        // Отправляем запрос на сервер через RSocketClient
        RSocketClient.connect();
        RSocketClient.sendRequest("registration", request, AuthResponse.class)
                .doOnTerminate(() -> Platform.runLater(() -> {
                    // Переход на другую сцену или уведомление об успешной регистрации
                    showAlert("Успех", "Регистрация прошла успешно.", Alert.AlertType.INFORMATION);
                }))
                .subscribe(response -> {
                    // Логика обработки ответа от сервера (например, если сервер вернул ошибку)
                    if (response.getStatus().equals("confirmed")) {
                        // Переход к следующему экрану или действие по успешной регистрации
                    } else {
                        // Обработка ошибки регистрации
                        showAlert("Ошибка", "Произошла ошибка при регистрации.", Alert.AlertType.ERROR);
                    }
                }, error -> {
                    // Обработка ошибок соединения или других
                    showAlert("Ошибка", "Не удалось подключиться к серверу.", Alert.AlertType.ERROR);
                });
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/client/login.fxml"));
            Parent loginRoot = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene registerScene = new Scene(loginRoot,320, 240);
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // отправить данные на сервер
    }


    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
