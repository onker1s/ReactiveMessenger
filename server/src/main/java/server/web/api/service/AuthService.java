package server.web.api.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import server.data.UserRepository;
import server.security.AuthData;
import server.security.AuthResponse;

@Service
public class AuthService {
    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }
    public Mono<AuthResponse> login(Mono<AuthData> authData) {
        return authData
                .map(in -> {
                    AuthData data = in;
                    System.out.println("data: " + data.getUsername() + ", " + data.getPassword());
                    AuthResponse authResponse = new AuthResponse();
                    authResponse.confirm();
                    return authResponse;
                });
    }
}
