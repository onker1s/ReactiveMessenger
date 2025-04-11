package client.ui.FXcontrollers;

import client.ClientAppApplication;
import client.connection.ClientMessageHandler;
import client.connection.RSocketClientService;
import client.dto.Message;
import client.ui.DialogCreator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.springframework.messaging.rsocket.RSocketRequester;


import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DialoguesController implements DialogCreator {

    @FXML
    private ListView<String> dialoguesListView;

    @FXML
    private Button startDialogButton;

    @FXML
    private Button logoutButton;

    private Set<Stage> openChatStages = new HashSet<>();

    RSocketClientService clientService;

    @FXML
    public void initialize() throws IOException {
        ClientMessageHandler.setDialogCreator(this);
        clientService = new RSocketClientService(RSocketRequester.builder());
        loadDialogues();
        startDialogButton.setOnAction(event -> handleStartDialog());
        dialoguesListView.setOnMouseClicked(this::openChat);
    }


    private void loadDialogues() {

        clientService.getDialogues().doOnNext(message ->
                dialoguesListView.getItems().add(message))
                .subscribe();

    }

    private void handleStartDialog() {
        // Создаем всплывающее окно для ввода имени
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Начать диалог");
        dialog.setHeaderText("Введите имя пользователя:");
        dialog.setContentText("Имя пользователя:");

        // Показываем диалог и получаем результат
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(username -> {
            clientService.checkUserExists(username)
                    .doOnSuccess(userExists -> {
                        if (userExists  && !dialoguesListView.getItems().contains(username)) {

                            Platform.runLater(() -> {
                                openChat(username);
                                dialoguesListView.getItems().add(username);
                            });
                        } else if (dialoguesListView.getItems().contains(username)) {
                            // Если пользователя не существует, показываем сообщение
                            Platform.runLater(() -> showAlert("Ошибка", "Диалог с таким пользователем уже существует."));
                        }

                        else {
                            // Если пользователя не существует, показываем сообщение
                           Platform.runLater(() -> showAlert("Пользователь не найден", "Пользователь с таким именем не существует."));

                        }
                    })
                    .doOnError(error -> Platform.runLater(() ->showAlert("Ошибка", "Произошла ошибка при проверке пользователя: " + error.getMessage())))
                    .subscribe();
        });
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
         clientService.logout().subscribe();

        // Закрыть все окна чатов
        for (Stage chatStage : openChatStages) {
            chatStage.close();
        }
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FXMLLoader fxmlLoader = new FXMLLoader(ClientAppApplication.class.getResource("/view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("OnChat");
        stage.setScene(scene);
        stage.show();
    }

    private void openChat(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selectedUsername = dialoguesListView.getSelectionModel().getSelectedItem();
            if (selectedUsername == null) return;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/chat.fxml"));
                Parent root = loader.load();

                ChatController controller = loader.getController();
                controller.setRecipient(selectedUsername);
                controller.loadMessages(selectedUsername);
                Stage stage = new Stage();
                stage.setTitle("Чат с " + selectedUsername);
                stage.setScene(new Scene(root));
                stage.setUserData(selectedUsername);
                stage.show();
                // Добавляем окно чата в коллекцию
                openChatStages.add(stage);

                // Закрытие окна при закрытии Stage
                stage.setOnCloseRequest(e -> openChatStages.remove(stage));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void openChat(String recipientUsername) {
            if (recipientUsername == null) return;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/chat.fxml"));
                Parent root = loader.load();

                ChatController controller = loader.getController();
                controller.setRecipient(recipientUsername);
                controller.loadMessages(recipientUsername);
                Stage stage = new Stage();
                stage.setTitle("Чат с " + recipientUsername);
                stage.setScene(new Scene(root));
                stage.setUserData(recipientUsername);
                stage.show();
                // Добавляем окно чата в коллекцию
                openChatStages.add(stage);

                // Закрытие окна при закрытии Stage
                stage.setOnCloseRequest(e -> openChatStages.remove(stage));


            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    public void displayNewDialog(String sender) {
        Platform.runLater(() -> {
            System.out.println("newDialog");
            for (Stage stage : openChatStages) {
                if (!sender.equals(stage.getUserData())) {
                    openChat(sender);
                    break;
                }
            }
            if (!dialoguesListView.getItems().contains(sender)) {
                dialoguesListView.getItems().add(sender);
            }

        });
    }
}
