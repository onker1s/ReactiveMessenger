package server.web.api.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import server.Message;
import server.data.MessageRepository;
import server.security.AuthData;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Mono<Void> proccesMessage(Mono<Message> messageMono) {
        return messageMono
                .doOnNext(message ->
                        System.out.println("data: "
                                + message.getSenderUsername() + ", "
                                + message.getRecipientUsername() + ", "
                                + message.getMessage() + ", "
                                + message.getSentAt())
                )
                .thenEmpty(Mono.empty());
    }
}
