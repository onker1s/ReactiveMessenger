package server.web.api.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Message;
import server.data.MessageRepository;
import server.web.api.service.UserSessionService;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserSessionService userSessionService;

    public MessageService(MessageRepository messageRepository, UserSessionService userSessionService) {
        this.messageRepository = messageRepository;
        this.userSessionService = userSessionService;
    }

    public Mono<Void> processMessage(Mono<Message> messageMono) {
        return messageMono
                .flatMap(message -> {
                    message.setDeliveredStatus(false); // Изначально сообщение не доставлено
                    System.out.println("message recieved");
                    return messageRepository.save(message)
                            .then(sendMessageIfOnline(message));
                })
                .then();
    }

    private Mono<Void> sendMessageIfOnline(Message message) {
        return userSessionService.isUserConnected(message.getRecipientUsername())
                .flatMap(isConnected -> {
                    if (isConnected) {
                        return userSessionService.sendMessageToUser(message.getRecipientUsername(), message)
                                .then(messageRepository.findById(message.getId())
                                        .flatMap(existingMessage -> {
                                            existingMessage.setDeliveredStatus(true);
                                            return messageRepository.save(existingMessage);
                                        })
                                );
                    }
                    return Mono.empty();
                })
                .then();
    }

    public Mono<Void> deleteMessages(String senderUsername, String recipientUsername) {
        return messageRepository.deleteBySenderUsernameAndRecipientUsername(senderUsername, recipientUsername);
    }

    public Flux<Message> loadUserMessages(String username) {
        return messageRepository.findByRecipientUsernameAndDeliveredStatusFalse(username)
                .flatMap(message -> userSessionService.sendMessageToUser(username, message)
                        .thenReturn(message));
    }
}
