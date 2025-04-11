package server.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import server.data.UserRepository;

@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final UserRepository userRepository;

    public JwtAuthenticationManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return Mono.empty();
        }
        Jwt jwt = jwtAuth.getToken();
        String username = jwt.getSubject();
        return userRepository.findByUsername(username)
                .map(user -> new UsernamePasswordAuthenticationToken(user, jwt.getTokenValue(), user.getAuthorities()));
    }
}
