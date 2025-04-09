package client.connection;

import client.dto.AuthData;
import client.dto.AuthResponse;
import client.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class RSocketClientService {

    @Getter
    private final RSocketRequester requester;
    private static String jwtToken;
    @Getter
    @Setter
    private static String username;
    public RSocketClientService(RSocketRequester.Builder builder) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src/server.properties"))) {
            props.load(in);
        }
        String host = props.getProperty("host");
        String port = props.getProperty("port");

        // Настраиваем стратегию с Jackson-декодером
        RSocketStrategies strategies = RSocketStrategies.builder()
                .decoder(new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build();

        this.requester = builder
                .rsocketStrategies(strategies)
                .tcp(host, Integer.parseInt(port));
    }

    public Mono<AuthResponse> register(String username, String password) {
        AuthData request = new AuthData(username, password);
        return requester
                .route("registration")
                .data(request)
                .retrieveMono(AuthResponse.class)
                .doOnNext(response -> log.info("Registration response: {}", response.getStatus()));
    }

    public Mono<AuthResponse> login(String username, String password) {
        AuthData request = new AuthData(username, password);
        return requester
                .route("login")
                .data(request)
                .retrieveMono(AuthResponse.class)
                .doOnNext(response -> {
                    log.info("Login successful. Token received: {}", response.getStatus() + " " + response.getToken());
                });
    }

    public Mono<Void> sendMessage(String recipientUsername, String password) {
        Message message = new Message(username,recipientUsername, password);
        return requester
                .route("send-message")
                .metadata("Bearer " + jwtToken, MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"))
                .data(message)
                .retrieveMono(Void.class);
    }

    public static void setToken(String token) {
         jwtToken = token;
    }

    public static String getToken() {
        return jwtToken;
    }

}
