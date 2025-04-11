package server.web.api.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Message;
import server.data.MessageRepository;
import server.security.AuthData;
import server.web.api.service.MessageService;

@Controller
public class MessageController {

    private final MessageService messageService;
    private final MessageRepository messageRepository;
    public MessageController(MessageService messageService, MessageRepository messageRepository) {
        this.messageService = messageService;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("send-message")
    public Mono<Void> sendMessage(Mono<Message> messageMono) {
        return messageService.processMessage(messageMono);
    }
    @MessageMapping("delete-messages-between")
    public Mono<Void> deleteMessagesBetween(@Payload Message message) {
        return messageService
                .deleteMessages(message.getSenderUsername(), message.getRecipientUsername());
    }
    @MessageMapping("load-dialog")
    public Flux<Message> loadUserMessages(AuthData d) {
        System.out.println("Loading user messages");
        return messageRepository
                .findAllByRecipientUsernameAndSenderUsernameOrRecipientUsernameAndSenderUsername(
                        d.getUsername(), d.getPassword(),
                        d.getPassword(), d.getUsername()
                )
                .doOnNext(message -> System.out.println("Отправляется сообщение: " + message));
    }
    @MessageMapping("load-dialogues")
    public Flux<String> loadDialogues(String username) {
        System.out.println("Loading dialogues");
        return messageRepository
                .findAllByRecipientUsernameOrSenderUsername(username, username)
                .map(message -> {
                    if (message.getSenderUsername().equals(username)) {
                        return message.getRecipientUsername();
                    } else {
                        return message.getSenderUsername();
                    }
                })
                .distinct()
                .doOnNext(message -> System.out.println("Отправляется сообщение: " + message));
    }



}