package client.FXcontrollers;

import client.connection.RSocketClientService;
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
import javafx.application.Platform;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();


        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            errorLabel.setText("Пожалуйста, заполните все поля.");
            return;
        }
        RSocketClientService clientService = new RSocketClientService(RSocketRequester.builder());
        clientService.login(username, password)
                .doOnNext(token -> {
                    Platform.runLater(() -> {
                        System.out.println("Токен: " + token);
                        if(!token.getToken().isEmpty()) {
                            RSocketClientService.setToken(token.getToken());
                            RSocketClientService.setUsername(username);
                        }
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
        System.out.println("Вход: " + username + ", пароль: " + password);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/chat.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene registerScene = new Scene(loginRoot,320, 240);
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @FXML
    private void onRegisterButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent registerRoot = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene registerScene = new Scene(registerRoot,320, 240);
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
