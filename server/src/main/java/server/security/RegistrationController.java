package server.security;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public Mono<RegistrationResponse> processRegistration(Mono<RegistrationData> registerData) {
        registerData.flatMap(data -> {
            RegistrationResponse response = new RegistrationResponse();
            response.confirm();
            return Mono.just(response);
        });
        return Mono.just(new RegistrationResponse());
    }

}