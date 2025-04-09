package client.FXcontrollers;

import client.connection.RSocketClientService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Setter;
import org.springframework.messaging.rsocket.RSocketRequester;

import java.io.IOException;

public class ChatController {

    @FXML
    private TextArea messageArea;

    @FXML
    private TextField messageField;

    @FXML
    private TextField recipientField;

    @FXML
    private Button sendButton;

    @FXML
    public void initialize() {
        messageArea.setText(""); // Инициализация поля
    }

    @FXML
    private void sendMessage() throws IOException {
        String recipient = recipientField.getText().trim();
        String text = messageField.getText().trim();

        if (recipient.isEmpty() || text.isEmpty()) {
            messageArea.appendText("Введите получателя и сообщение!\n");
            return;
        }
        RSocketClientService clientService = new RSocketClientService(RSocketRequester.builder());
        clientService.sendMessage(recipient, text)
                .doOnSuccess(unused -> {
                    messageArea.appendText("Вы -> " + recipient + ": " + text + "\n");
                    messageField.clear();
                })
                .doOnError(error -> messageArea.appendText("Ошибка отправки: " + error.getMessage() + "\n"))
                .subscribe();

    }

    public void displayIncomingMessage(String sender, String text) {
        messageArea.appendText(sender + " -> Вы: " + text + "\n");
    }
}
