package org.example.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.scene.Node;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();


        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            errorLabel.setText("Пожалуйста, заполните все поля.");
            return;
        }

        errorLabel.setText(""); // очистить ошибку
        System.out.println("Вход: " + username + ", пароль: " + password);

        // отправить данные на сервер
    }
    @FXML
    private void onRegisterButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/example/client/register.fxml"));
            Parent registerRoot = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene registerScene = new Scene(registerRoot,320, 240);
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
