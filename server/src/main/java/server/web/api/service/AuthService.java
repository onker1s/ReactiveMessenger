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
    private final DialogService dialogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public AuthService(UserRepository userRepo, UserSessionService userSessionService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil, DialogService dialogService) {
        this.dialogService = dialogService;
        this.userRepo = userRepo;
        this.userSessionService = userSessionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

    }

    public Mono<AuthResponse> login(AuthData data, RSocketRequester requester) {
        AuthResponse response = new AuthResponse();
        return userRepo.findByUsername(data.getUsername())
                .flatMap(user -> {
                    if (passwordEncoder.matches(data.getPassword(), user.getPassword())) {
                        String token = jwtUtil.generateToken(user.getUsername());

                        Mono<Void> registerMono = userSessionService.registerUser(user.getUsername(), requester);

                        Flux<Void> statusUpdates = dialogService.getContacts(user.getUsername())
                                .flatMap(contact ->
                                        userSessionService.sendStatusUserUpdate(user.getUsername(), contact, "online")
                                                .onErrorResume(e -> {
                                                    System.err.println("Не удалось отправить статус для "
                                                            + contact + ": "
                                                            + e.getMessage());
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
        return dialogService.getContacts(username)
                .flatMap(contactUsername ->
                        userSessionService.sendStatusUserUpdate(username, contactUsername, "offline")
                                .onErrorResume(e -> {
                                    System.err.println("Не удалось отправить статус для "
                                            + contactUsername + ": " + e.getMessage());
                                    return Mono.empty();
                                })
                )
                .then(userSessionService.unregisterUser(username));
    }




}
