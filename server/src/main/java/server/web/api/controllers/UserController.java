package server.web.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.data.UserRepository;
import server.security.AuthData;
import server.web.api.service.UserSessionService;

@Controller
public class UserController {

    private final UserRepository userRepository;
    private final UserSessionService userSessionService;
    @Autowired
    public UserController(UserRepository userRepository, UserSessionService userSessionService) {
        this.userRepository = userRepository;
        this.userSessionService = userSessionService;
    }

    @MessageMapping("deleteUser")
    public Mono<Void> deleteUser(String username) {
        return userRepository.findByUsername(username)
                .flatMap(userRepository::delete)
                .then();
    }
    @MessageMapping("check-user-exists")
    public Mono<Boolean> checkUserExists(String username) {
        return userRepository.findByUsername(username)
                .map(user -> true)
                .defaultIfEmpty(false);
    }
    @MessageMapping("load-statuses")
    public Flux<AuthData> loadStatuses(String username) {
        System.out.println("Loading statuses");
        return userSessionService.sendStatusesToUser(username);
    }
    @MessageMapping("get-user-status")
    public Flux<AuthData> getUserStatus(String username) {
        return userSessionService.sendStatusesToUser(username);
    }

}
