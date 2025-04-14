package server.web.api.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Message;
import server.data.MessageRepository;
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

    @MessageMapping("load-dialog")
    public Flux<Message> loadUserMessages(String dialogId) {
        return messageRepository.findAllByDialogId(dialogId);
    }





}