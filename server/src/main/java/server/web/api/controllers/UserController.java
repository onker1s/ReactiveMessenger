package server.web.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import server.data.UserRepository;

@Controller
public class UserController {

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                .map(user -> true)  // Если пользователь найден, возвращаем true
                .defaultIfEmpty(false);  // Если пользователь не найден, возвращаем false
    }
}
