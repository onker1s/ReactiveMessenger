package server.web.api.service;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Dialog;
import server.Message;
import server.data.DialogRepository;
import server.security.AuthData;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class UserSessionService {

    private final Map<String, RSocketRequester> userRequesters = new ConcurrentHashMap<>();
    private final DialogRepository dialogRepository;

    public UserSessionService(DialogRepository dialogRepository) {
        this.dialogRepository = dialogRepository;
    }

    public Mono<Void> registerUser(String username, RSocketRequester requester) {

        userRequesters.put(username, requester);
        return Mono.empty();
    }

    public Mono<Void> unregisterUser(String username) {

        userRequesters.remove(username);
        System.out.println(userRequesters.keySet());
        return Mono.empty();
    }

    public Mono<Boolean> isUserConnected(String username) {
        return Mono.just(userRequesters.containsKey(username));
    }

    public Mono<Void> sendMessageToUser(String username, Message message) {
        RSocketRequester requester = userRequesters.get(username);
        if (requester != null) {
            return requester.route("receive-message")
                    .data(message)
                    .send();
        } else {
            return Mono.error(new RuntimeException("User not connected"));
        }
    }
    public Mono<Void> sendStatusUserUpdate(String username, String recipientName, String status) {
        RSocketRequester requester;
        if (userRequesters.containsKey(recipientName)) {
             requester = userRequesters.get(recipientName);
        }
        else{
            return Mono.empty();
        }
        AuthData authData = new AuthData(username, status);
        if (requester != null) {
            return requester.route("receive-status")
                    .data(authData)
                    .send();
        } else {
            return Mono.error(new RuntimeException("User not connected"));
        }
    }

    public Flux<AuthData> sendStatusesToUser(String username) {
        return dialogRepository
                .findAllByParticipantIdsContaining(username)
                .flatMap(dialog -> {
                    // Найдём имя собеседника
                    return Flux.fromIterable(dialog.getParticipantIds())
                            .filter(other -> !other.equals(username)) // исключаем самого себя
                            .next(); // берём одного собеседника
                })
                .distinct()
                .map(otherUser -> {
                    boolean isOnline = userRequesters.containsKey(otherUser);
                    String status = isOnline ? "online" : "offline";
                    return new AuthData(otherUser, status);
                });
    }

    public Mono<Void> sendNewDialog(String username, Dialog dialog) {
        RSocketRequester requester = userRequesters.get(username);
        if (requester != null) {
            return requester.route("new-dialog")
                            .data(dialog)
                            .send();
        } else {
            return Mono.error(new RuntimeException("User not connected"));
        }

    }



}
