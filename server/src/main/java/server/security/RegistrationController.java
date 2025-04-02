package server.security;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import server.data.UserRepository;

@Controller
@Slf4j
public class RegistrationController {

    private UserRepository userRepo;
    private PasswordEncoder passwordEncoder;

    public RegistrationController(
            UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @MessageMapping("registration")
    public Mono<AuthResponse> processRegistration(Mono<AuthData> registerData) {
        return registerData.map(data -> {
            AuthResponse response = new AuthResponse();
            response.confirm();
            return response;
        });
    }

}