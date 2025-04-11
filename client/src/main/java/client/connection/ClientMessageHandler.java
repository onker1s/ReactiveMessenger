package client.connection;

import client.dto.Message;
import client.ui.DialogCreator;
import javafx.application.Platform;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import client.ui.MessageDisplay;

@Controller
public class ClientMessageHandler {
    // Ссылка на UI-контроллер
    @Setter
    private static MessageDisplay messageDisplay;
    @Setter
    private static DialogCreator dialogCreator;
    @MessageMapping("receive-message")
    public Mono<Void> receiveMessage(Message message) {
        System.out.println("Получено сообщение от сервера: " + message.getMessage());

        if (messageDisplay != null) {
            Platform.runLater(() -> {
                messageDisplay.displayMessage(message.getSenderUsername(), message.getMessage());
                dialogCreator.displayNewDialog(message.getSenderUsername());
            });
        }

        return Mono.empty();
    }
}
