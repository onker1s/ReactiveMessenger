package server.web.api.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import server.Message;
import server.web.api.service.MessageService;

@Controller
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("send-message")
    public Mono<Void> sendMessage(Mono<Message> messageMono) {
        return messageService.processMessage(messageMono);
    }
}