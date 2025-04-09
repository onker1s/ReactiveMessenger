package client.FXcontrollers;


import client.connection.RSocketClientService;
import io.rsocket.core.RSocketConnector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.messaging.rsocket.RSocketRequester;
import javafx.application.Platform;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
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
    private void onRegister(ActionEvent event) throws IOException {
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
        RSocketClientService clientService = new RSocketClientService(RSocketRequester.builder());

        clientService.register(username, password1)
                .doOnNext(token -> {
                    Platform.runLater(() -> {
                        System.out.println("Токен: " + token);
                        // можно открыть главное окно приложения
                    });
                })
                .doOnError(e -> {
                    Platform.runLater(() -> {
                        System.err.println("Ошибка входа: " + e.getMessage());
                    });
                })
                .subscribe();
        errorLabel.setText(""); // очистить ошибку
        System.out.println("Регистрация: " + username + ", пароль: " + password1);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene registerScene = new Scene(loginRoot,320, 240);
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
