package server.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import reactor.core.publisher.Mono;
import server.data.UserRepository;
import server.security.JwtUtil;

public class JwtAuthenticationManager implements ReactiveAuthenticationManager {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthenticationManager(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString(); // Получаем JWT
        String username = jwtUtil.extractUsername(token); // Извлекаем имя пользователя

        if (username == null || !jwtUtil.validateToken(token)) {
            return Mono.empty(); // Токен недействителен
        }

        return userRepository.findByUsername(username)
                .map(user -> new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities()));
    }
}
