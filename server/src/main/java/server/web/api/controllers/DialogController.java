package server.web.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Dialog;
import server.security.AuthData;
import server.web.api.service.DialogService;
import server.web.api.service.UserSessionService;

@Controller
@RequiredArgsConstructor
public class DialogController {

    private final DialogService dialogService;
    private final UserSessionService userSessionService;

    // Получение всех диалогов текущего пользователя
    @MessageMapping("load-dialogues")
    public Flux<Dialog> loadDialogues(String username) {
        return dialogService.getDialoguesByUser(username);
    }

    // Создание нового диалога и уведомление второго участника
    @MessageMapping("create-dialogue")
    public Mono<Dialog> createDialog(AuthData request) {
        String user1 = request.getUsername();
        String user2 = request.getPassword();

        return dialogService.getDialogBetween(user1, user2)
                .switchIfEmpty(
                        dialogService.createDialog(user1, user2)
                                .flatMap(dialog ->
                                        // Проверяем, онлайн ли второй участник
                                        userSessionService.isUserConnected(user2)
                                                .flatMap(isConnected -> {
                                                    // Если онлайн — отправим ему новый диалог
                                                    Mono<Void> dialogNotification = isConnected
                                                            ? userSessionService.sendNewDialog(user2, dialog)
                                                            : Mono.empty();

                                                    // Узнаем текущий статус каждого
                                                    String statusUser2 = isConnected ? "online" : "offline";
                                                    String statusUser1 =
                                                            userSessionService
                                                                    .isUserConnected(user1).block() ? "online" : "offline";
                                                    System.out.println(statusUser1 + " " + statusUser2);
                                                    // Отправляем обоим обновления статусов
                                                    Mono<Void> updateUser1 =
                                                            userSessionService
                                                                    .sendStatusUserUpdate(user2, user1, statusUser2);
                                                    Mono<Void> updateUser2 =
                                                            userSessionService
                                                                    .sendStatusUserUpdate(user1, user2, statusUser1);

                                                    return Mono.when(dialogNotification, updateUser1, updateUser2)
                                                            .thenReturn(dialog);
                                                })
                                )
                );
    }

}
