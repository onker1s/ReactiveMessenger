package client.connection;

import ch.qos.logback.core.joran.sanity.Pair;
import client.dto.AuthData;
import client.dto.AuthResponse;
import client.dto.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.rsocket.SocketAcceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
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
        SocketAcceptor responder = RSocketMessageHandler.responder(strategies, new ClientMessageHandler());
        this.requester = builder
                .rsocketStrategies(strategies)
                .rsocketConnector(connector -> connector.acceptor(responder))
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

    public Mono<Void> logout() {
        return requester
                .route("logout")
                .metadata("Bearer " + jwtToken, MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"))
                .data(username)
                .retrieveMono(Void.class);
    }

    public Mono<Void> sendMessage(String recipientUsername, String password) {
        Message message = new Message(username,recipientUsername, password);
        return requester
                .route("send-message")
                .metadata("Bearer " + jwtToken, MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"))
                .data(message)
                .retrieveMono(Void.class);
    }
    public Flux<Message> getDialog(String recipientUsername) {
        AuthData d = new AuthData(username, recipientUsername);
        System.out.println("getDialog");
        return requester
                .route("load-dialog")
                .metadata("Bearer " + jwtToken, MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"))
                .data(d)
                .retrieveFlux(Message.class);
    }
    public Flux<String> getDialogues() {
        return requester
                .route("load-dialogues")
                .metadata("Bearer " + jwtToken, MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"))
                .data(username)
                .retrieveFlux(String.class);
    }

    public Mono<Boolean> checkUserExists(String username) {

        return requester
                .route("check-user-exists")
                .metadata("Bearer " + jwtToken, MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"))
                .data(username)
                .retrieveMono(Boolean.class);
    }
    public static void setToken(String token) {
         jwtToken = token;
    }

    public static String getToken() {
        return jwtToken;
    }

    public void disconnect() {
        requester.dispose();
    }
}
