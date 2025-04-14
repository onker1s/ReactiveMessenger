package client.ui.FXcontrollers;

import client.ClientAppApplication;
import client.connection.ClientMessageHandler;
import client.connection.RSocketClientService;
import client.dto.AuthData;
import client.dto.Message;
import client.ui.DialogCreator;
import client.ui.StatusUpdater;
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
import java.util.*;

import client.dto.Dialog;
public class DialoguesController implements DialogCreator, StatusUpdater {

    @FXML
    private ListView<String> dialoguesListView;

    @FXML
    private Button startDialogButton;

    @FXML
    private Button logoutButton;

    private final Set<Stage> openChatStages = new HashSet<>();
    private final List<Dialog> dialogList = new ArrayList<>();
    RSocketClientService clientService;

    @FXML
    public void initialize() throws IOException {
        ClientMessageHandler.setDialogCreator(this);
        ClientMessageHandler.setStatusUpdater(this);
        clientService = new RSocketClientService(RSocketRequester.builder());
        loadDialogues();
        loadStatuses();
        startDialogButton.setOnAction(event -> handleStartDialog());
        dialoguesListView.setOnMouseClicked(this::openChat);
    }

    private void loadStatuses() {
        clientService.getStatuses().subscribe(userData -> {
            Platform.runLater(() -> {
                String username = userData.getUsername();
                String status = userData.getPassword();
                for (int i = 0; i < dialoguesListView.getItems().size(); i++) {
                    String item = dialoguesListView.getItems().get(i);
                    String[] parts = item.split(" ");
                    if (parts.length > 0 && parts[0].equals(username)) {
                        dialoguesListView.getItems().set(i, username + " " + status);
                        break;
                    }
                }
            });
        });
    }



    private void loadDialogues() {
        clientService.getDialogues()
                .doOnNext(dialog -> {
                    dialogList.add(dialog);
                    String otherUsername = getOtherUsername(dialog);
                    Platform.runLater(() -> dialoguesListView.getItems().add(otherUsername + " offline"));
                })
                .subscribe();
    }

    public static String getOtherUsername(Dialog dialog) {
        String currentUser = RSocketClientService.getUsername();
        return dialog.getParticipantIds()
                .stream()
                .filter(username -> !username.equals(currentUser))
                .findFirst()
                .orElse("неизвестный");
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
            if (!username.isEmpty()) {
                clientService.checkUserExists(username)
                        .doOnSuccess(userExists -> {
                            if (userExists  && !dialoguesListView.getItems().contains(username) &&
                                    !username.equals(RSocketClientService.getUsername()) &&
                                    !dialoguesListView.getItems().contains(username + " offline") &&
                                    !dialoguesListView.getItems().contains(username + " online") &&
                                    !username.contains(" ")) {
                                clientService.createDialog(new AuthData(RSocketClientService.getUsername(), username))
                                        .doOnNext(dialog1 -> {
                                            Platform.runLater(() -> {
                                                dialoguesListView.getItems().add(username);
                                                displayNewDialog(dialog1);
                                            });})
                                        .doOnError(error ->
                                                Platform.runLater(() ->
                                                        showAlert("Ошибка",
                                                                "Произошла ошибка при создании диалога: "
                                                                        + error.getMessage())))
                                        .subscribe();

                            } else if (dialoguesListView.getItems().contains(username + " offline")
                                    || dialoguesListView.getItems().contains(username + " online") ||
                                    dialoguesListView.getItems().contains(username)) {
                                Platform.runLater(() -> showAlert("Ошибка",
                                        "Диалог с таким пользователем уже существует."));
                            } else if (username.equals(RSocketClientService.getUsername())) {
                                Platform.runLater(() -> showAlert("Ошибка",
                                        "Нельзя начать диалог с самим собой."));
                            }
                            else {
                                // Если пользователя не существует, показываем сообщение
                                Platform.runLater(() -> showAlert("Пользователь не найден",
                                        "Пользователь с таким именем не существует."));

                            }
                        })
                        .doOnError(error -> Platform.runLater(() ->showAlert("Ошибка",
                                "Произошла ошибка при проверке пользователя: " + error.getMessage())))
                        .subscribe();
            }
            else{
                // Если пользователя не существует, показываем сообщение
                Platform.runLater(() -> showAlert("Ошибка",
                        "Введите имя пользователя."));
            }

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
            String item = dialoguesListView.getSelectionModel().getSelectedItem();
            String[] parts = item.split(" ");
            String selectedUsername = parts[0];
            if (selectedUsername == null) return;
            openChat(item);
        }
    }
    private void openChat(String recipientUsername) {
        if (recipientUsername == null) return;
        String[] parts ;
        String selectedUsername;
        if (recipientUsername.contains(" ")) {
            parts = recipientUsername.split(" ");
            selectedUsername = parts[0];
        }
        else {
            parts = null;
            selectedUsername = recipientUsername;
        }
        // Находим диалог по имени пользователя
        Dialog dialog = dialogList.stream()
                .filter(d -> getOtherUsername(d).equals(selectedUsername))
                .findFirst()
                .orElse(null);

        if (dialog == null) {
            showAlert("Ошибка", "Диалог с пользователем не найден.");
            return;
        }
        if(!openChatStages.isEmpty()) {
            for (Stage stage : openChatStages) {
                if (selectedUsername.equals(stage.getUserData())) {
                    Platform.runLater(() ->
                            showAlert("Ошибка",
                                    "Чат с эти пользователем уже открыт"));
                    break;
                }
            }
        }
        else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/chat.fxml"));
                Parent root = loader.load();

                ChatController controller = loader.getController();
                controller.setDialog(dialog);
                controller.loadMessages(dialog.getId());

                Stage stage = new Stage();
                if (parts != null && parts.length > 1) {
                    stage.setTitle("Чат с " + selectedUsername + " " + parts[1]);
                } else {
                    stage.setTitle("Чат с " + selectedUsername);
                }
                stage.setScene(new Scene(root));
                stage.setUserData(recipientUsername);
                stage.show();

                openChatStages.add(stage);
                stage.setOnCloseRequest(e -> openChatStages.remove(stage));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void displayNewDialog(Dialog dialog) {
        dialogList.add(dialog);
        String sender = getOtherUsername(dialog);
        Platform.runLater(() -> {
            if(!dialoguesListView.getItems().contains(sender)) {
                dialoguesListView.getItems().add(sender);
            }
            if(!openChatStages.isEmpty()) {
                boolean exists = false;
                for (Stage stage : openChatStages) {
                    if (getOtherUsername(dialog).equals(stage.getUserData())) {
                        exists = true;
                        break;
                    }
                }
                 if(!exists) {
                    openChat(sender);
                }
            }
            else {
                openChat(sender);
            }
        });
    }

    public void updateStatus(AuthData statusData) {
        Platform.runLater(() -> {
            String username = statusData.getUsername();
            String newStatus = statusData.getPassword();

            for (int i = 0; i < dialoguesListView.getItems().size(); i++) {
                String item = dialoguesListView.getItems().get(i);
                String[] parts = item.split(" ");
                if (parts.length > 0 && parts[0].equals(username)) {
                    dialoguesListView.getItems().set(i, username + " " + newStatus);
                    break;
                }
            }

            if(!openChatStages.isEmpty()) {
                for (Stage stage : openChatStages) {
                    if (username.equals(stage.getUserData())) {
                        System.out.println("update status on chat stage");
                        stage.setTitle("Чат с " + username + " " + newStatus);
                        break;
                    }
                }
            }
        });
    }

}
