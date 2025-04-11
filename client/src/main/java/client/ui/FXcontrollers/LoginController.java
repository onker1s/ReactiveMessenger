package client.ui.FXcontrollers;

import client.connection.RSocketClientService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void onLogin(ActionEvent event) throws IOException, InterruptedException {
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
                            try {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/dialogues.fxml"));
                                Parent loginRoot = fxmlLoader.load();
                                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                                Scene registerScene = new Scene(loginRoot, 320, 240);
                                stage.setScene(registerScene);
                                stage.setOnCloseRequest(wevent -> {
                                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Закрыть чат?", ButtonType.YES, ButtonType.NO);
                                    alert.setHeaderText(null);
                                    alert.setTitle("Подтверждение");
                                    Optional<ButtonType> result = alert.showAndWait();
                                    if (result.isPresent() && result.get() == ButtonType.NO) {
                                        wevent.consume(); // Отменить закрытие
                                    }
                                    else {
                                        clientService.logout().subscribe();
                                        clientService.disconnect();
                                        Platform.exit();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                })
                .doOnError(e -> {
                    Platform.runLater(() -> {
                        System.err.println("Ошибка входа: " + e.getMessage());
                    });
                })
                .subscribe();
        errorLabel.setText("");
        System.out.println("Вход: " + username + ", пароль: " + password);



    }
    @FXML
    private void onRegisterButtonClick(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/register.fxml"));
            Parent registerRoot = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene registerScene = new Scene(registerRoot,320, 340);
            stage.setScene(registerScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
