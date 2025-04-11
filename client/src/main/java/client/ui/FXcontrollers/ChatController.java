package client.ui.FXcontrollers;

import client.connection.RSocketClientService;
import client.connection.ClientMessageHandler;
import client.dto.Message;
import client.ui.MessageDisplay;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;

public class ChatController implements MessageDisplay {

    RSocketClientService clientService;
    @FXML
    private TextArea messageArea;

    @FXML
    private TextField messageField;


    @Setter
    private String recipient;

    @FXML
    private Button sendButton;

    @FXML
    public void initialize() throws IOException {
        ClientMessageHandler.setMessageDisplay(this);
        clientService = new RSocketClientService(RSocketRequester.builder());
    }
    public void loadMessages(String recipient) {
        clientService.getDialog(recipient).doOnNext(message ->
                        dispMessage(message.getSenderUsername(),
                                message.getRecipientUsername(), message.getMessage()))
                .subscribe();
    }
    @FXML
    private void sendMessage() throws IOException {

        String text = messageField.getText().trim();

        if (text.isEmpty()) {
            messageArea.appendText("Введите сообщение!\n");
            return;
        }

        clientService.sendMessage(recipient, text)
                .doOnSuccess(unused -> {
                    Platform.runLater(() -> {
                        messageArea.appendText("Вы -> " + recipient + ": " + text + "\n");
                        messageField.clear();
                    });

                })
                .doOnError(error -> messageArea.appendText("Ошибка отправки: " + error.getMessage() + "\n"))
                .subscribe();

    }

    public void dispMessage(String sender, String recipient, String text) {
        Platform.runLater(() -> {
            if(Objects.equals(sender, RSocketClientService.getUsername()))
                messageArea.appendText( "Вы -> " + recipient + " : "+ text + "\n");
            else
                messageArea.appendText(sender + " -> Вы: " + text + "\n");
        });
    }

    public void displayMessage(String sender, String text) {
        Platform.runLater(() -> messageArea.appendText(sender + " -> Вы: " + text + "\n"));
    }
}
