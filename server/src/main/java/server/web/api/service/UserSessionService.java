package server.web.api.service;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import server.Message;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class UserSessionService {
    private final Map<String, Sinks.Many<Object>> userSessions = new ConcurrentHashMap<>();
    private final Map<String, RSocketRequester> userRequesters = new ConcurrentHashMap<>();

    public Mono<Void> registerUser(String username, String sessionId, RSocketRequester requester) {
        userSessions.put(sessionId, Sinks.many().multicast().onBackpressureBuffer());
        userRequesters.put(sessionId, requester);
        return Mono.empty();
    }

    public Mono<Void> unregisterUser(String username) {
        userSessions.remove(username);
        userRequesters.remove(username);
        return Mono.empty();
    }

    public Mono<Boolean> isUserConnected(String username) {
        return Mono.just(userSessions.containsKey(username));
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
