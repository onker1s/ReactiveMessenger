package server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import reactor.core.publisher.Mono;
import server.data.UserRepository;

@Configuration
@EnableRSocketSecurity
public class RSocketSecurityConfig {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    public RSocketSecurityConfig(UserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return username -> userRepo.findByUsername(username)
                .map(user -> User.withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles("USER")
                        .build()
                )
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)));
    }

    @Bean
    public JwtAuthenticationManager jwtAuthenticationManager() {
        return new JwtAuthenticationManager(jwtUtil,userRepo);  // Создание и настройка JwtAuthenticationManager
    }

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket) {
        rsocket
                .authorizePayload(authorize -> authorize
                        .setup().permitAll()
                        .route("login").permitAll()
                        .route("registration").permitAll()
                        .route("deleteUser").authenticated()
                        .anyRequest().authenticated()
                )

                .jwt(jwtSpec -> jwtSpec.authenticationManager(jwtAuthenticationManager())); // Теперь RSocket понимает JWT

        return rsocket.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
