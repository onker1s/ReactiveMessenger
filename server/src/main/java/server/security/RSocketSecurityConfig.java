package server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import reactor.core.publisher.Mono;
import server.data.UserRepository;

@Configuration
@EnableRSocketSecurity
public class RSocketSecurityConfig {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final JwtAuthenticationManager jwtAuthenticationManager;

    public RSocketSecurityConfig(UserRepository userRepo, JwtUtil jwtUtil,
                                 JwtAuthenticationManager jwtAuthenticationManager) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.jwtAuthenticationManager = jwtAuthenticationManager;
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
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket) {
        rsocket
                .authorizePayload(authorize -> authorize
                        .setup().permitAll()
                        .route("login").permitAll()
                        .route("registration").permitAll()
                        .anyRequest().authenticated()
                )
                .jwt(jwtSpec -> jwtSpec.authenticationManager(jwtAuthenticationManager));
        return rsocket.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JwtUtil jwtUtil) {
        System.out.printf("Secret Key JWTDecoder: " +  jwtUtil.getSecretKey() + "\n");
        return NimbusJwtDecoder.withSecretKey(jwtUtil.getSecretKey()).build();
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
