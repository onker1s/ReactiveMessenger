package client.connection;

import client.dto.AuthData;
import client.dto.Message;
import client.ui.DialogCreator;
import client.ui.StatusUpdater;
import javafx.application.Platform;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import client.ui.MessageDisplay;

@Controller
public class ClientMessageHandler {
    @Setter
    private static MessageDisplay messageDisplay;
    @Setter
    private static DialogCreator dialogCreator;
    @Setter
    private static StatusUpdater statusUpdater;
    @MessageMapping("receive-message")
    public Mono<Void> receiveMessage(Message message) {
        if(messageDisplay !=null) {
            Platform.runLater(() -> {
                messageDisplay.displayMessage(message.getSenderUsername(), message.getMessage());
            });
        }
        if (dialogCreator != null) {
            Platform.runLater(() -> {
                dialogCreator.displayNewDialog(message.getSenderUsername());
            });
        }

        return Mono.empty();
    }
    @MessageMapping("receive-status")
    public Mono<Void> receiveStatus(AuthData authData) {
        if (statusUpdater != null) {
            Platform.runLater(() -> {
                statusUpdater.updateStatus(authData);
            });
        }
        return Mono.empty();
    }
}
