package server.web.api.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import server.data.UserRepository;
import server.security.AuthData;
import server.security.AuthResponse;
import server.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;

@Service
public class AuthService {
    private final UserRepository userRepo;
    private final UserSessionService userSessionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    public AuthService(UserRepository userRepo, UserSessionService userSessionService,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil ) {
        this.userRepo = userRepo;
        this.userSessionService = userSessionService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Mono<AuthResponse> login(Mono<AuthData> authData) {
        AuthResponse response = new AuthResponse();
        response.cancel();
        return authData.flatMap(data ->
                    userRepo.findByUsername(data.getUsername())
                            .flatMap(user -> {

                                if (passwordEncoder.matches(data.getPassword(), user.getPassword())) {
                                    String token = jwtUtil.generateToken(user.getUsername());


                                    response.confirm(token);
//Надо придумать создания подключений к каждому пользователю
                                    userSessionService.registerUser(user.getUsername(),user.getUsername(), requester);

                                    return Mono.just(response);
                                } else {
                                    response.cancel();
                                    return Mono.just(response);
                                }
                            })
                            .switchIfEmpty(Mono.just(response))
        );

    }

    public Mono<Void> logout(Mono<AuthData> authData) {
        return userSessionService.unregisterUser(authData.block().getUsername());
    }

}
