package server.web.api.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import server.Message;
import server.data.MessageRepository;
import server.data.UserRepository;
import server.security.AuthData;
import server.security.AuthResponse;
import server.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import java.util.ArrayList;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final UserSessionService userSessionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageRepository messageRepository;

    public AuthService(UserRepository userRepo, UserSessionService userSessionService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, MessageRepository messageRepository) {
        this.userRepo = userRepo;
        this.userSessionService = userSessionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.messageRepository = messageRepository;
    }

    public Mono<AuthResponse> login(AuthData data, RSocketRequester requester) {
        AuthResponse response = new AuthResponse();
        return userRepo.findByUsername(data.getUsername())
                .flatMap(user -> {
                    if (passwordEncoder.matches(data.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getUsername());
                        // Регистрируем пользователя
                        Mono<Void> registerMono = userSessionService.registerUser(user.getUsername(), requester);

                        // Рассылаем статус "online" контактам
                        Flux<Void> statusUpdates = messageRepository
                                .findAllByRecipientUsernameOrSenderUsername(user.getUsername(), user.getUsername())
                                .map(message -> {
                                    if (message.getSenderUsername().equals(user.getUsername())) {
                                        return message.getRecipientUsername();
                                    } else {
                                        return message.getSenderUsername();
                                    }
                                })
                                .distinct()
                                .flatMap(contact ->
                                        userSessionService.sendStatusUserUpdate(data.getUsername(), contact, "online")
                                                .onErrorResume(e -> {
                                                    System.err.println("Не удалось отправить статус для " + contact + ": " + e.getMessage());
                                                    return Mono.empty();
                                                })
                                );

                        return registerMono
                                .thenMany(statusUpdates)
                                .then(Mono.fromCallable(() -> {
                                    response.confirm(token);
                                    return response;
                                }));
                    } else {
                        response.cancel();
                        return Mono.just(response);
                    }
                })
                .switchIfEmpty(Mono.just(response));
    }



    public Mono<Void> logout(String username) {
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
                .flatMap(contactUsername ->
                        userSessionService.sendStatusUserUpdate(username, contactUsername, "offline")
                                .onErrorResume(e -> {
                                    // можно залогировать, если нужно
                                    System.err.println("Не удалось отправить статус для " + contactUsername + ": " + e.getMessage());
                                    return Mono.empty();
                                })
                )
                .then(userSessionService.unregisterUser(username));
    }



}
