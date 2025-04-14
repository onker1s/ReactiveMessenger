package server.web.api.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Message;
import server.data.DialogRepository;
import server.data.MessageRepository;
import server.web.api.service.UserSessionService;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserSessionService userSessionService;
    private final DialogRepository dialogRepository;

    public MessageService(MessageRepository messageRepository,
                          UserSessionService userSessionService,
                          DialogRepository dialogRepository) {
        this.messageRepository = messageRepository;
        this.userSessionService = userSessionService;
        this.dialogRepository = dialogRepository;
    }

    public Mono<Void> processMessage(Mono<Message> messageMono) {
        return messageMono
                .flatMap(message -> {
                    message.setDeliveredStatus(false); // Изначально сообщение не доставлено
                    return messageRepository.save(message)
                            .then(sendMessageIfOnline(message));
                })
                .then();
    }

    public Mono<Void> sendMessageIfOnline(Message message) {
        return dialogRepository.findById(message.getDialogId())
                .flatMap(dialog -> {
                    // Находим имя получателя — тот, кто не отправитель
                    String recipientUsername = dialog.getParticipantIds().stream()
                            .filter(name -> !name.equals(message.getSenderUsername()))
                            .findFirst()
                            .orElse(null);

                    if (recipientUsername == null) {
                        return Mono.empty(); // если что-то пошло не так
                    }

                    return userSessionService.isUserConnected(recipientUsername)
                            .flatMap(isConnected -> {
                                if (isConnected) {
                                    return userSessionService.sendMessageToUser(recipientUsername, message)
                                            .then(messageRepository.findById(message.getId())
                                                    .flatMap(existingMessage -> {
                                                        existingMessage.setDeliveredStatus(true);
                                                        return messageRepository.save(existingMessage);
                                                    }));
                                }
                                return Mono.empty();
                            });
                })
                .then();
    }
}
