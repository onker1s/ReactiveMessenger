package server.web.api.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import server.security.AuthData;
import server.security.AuthResponse;
import server.web.api.service.AuthService;
import server.web.api.service.MessageService;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @MessageMapping("login")
    public Mono<AuthResponse> login(Mono<AuthData> authData) {
        System.out.println("----------------LOGIN-----------------------");
        return authData.flatMap(data -> authService.login(authData));
    }
    public Mono<Void> logout(Mono<AuthData> authData) {
        System.out.println("----------------LOGOUT-----------------------");
        return authService.logout(authData);
    }

}
