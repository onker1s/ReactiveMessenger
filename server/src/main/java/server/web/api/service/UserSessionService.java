package server.web.api.service;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import server.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class UserSessionService {

    private final Map<String, RSocketRequester> userRequesters = new ConcurrentHashMap<>();

    public Mono<Void> registerUser(String username, RSocketRequester requester) {

        userRequesters.put(username, requester);
        return Mono.empty();
    }

    public Mono<Void> unregisterUser(String username) {
        userRequesters.remove(username);
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
}
