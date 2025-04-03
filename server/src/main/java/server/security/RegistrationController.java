package server.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import server.User;
import server.data.UserRepository;


@Controller

public class RegistrationController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepo, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @MessageMapping("registration")
    public Mono<AuthResponse> processRegistration(Mono<AuthData> registerData) {
        System.out.println("------------processRegistration------------------");
        return registerData.flatMap(data ->
                userRepo.findByUsername(data.getUsername())
                        .flatMap(existingUser -> {
                            AuthResponse response = new AuthResponse();
                            response.cancel();
                            System.out.println("------------UserExists------------------");
                            return Mono.just(response);
                        })
                        .switchIfEmpty(
                                userRepo.save(new User(data.getUsername(), passwordEncoder.encode(data.getPassword())))
                                        .then(Mono.fromSupplier(() -> {
                                            AuthResponse response = new AuthResponse();
                                            response.confirm();
                                            System.out.println("------------CONFIRMED------------------");
                                            return response;
                                        }))
                        )
        );
    }
}
