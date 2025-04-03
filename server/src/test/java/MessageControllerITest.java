import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import server.ServerApplication;
import server.Message;
import server.data.MessageRepository;
import server.security.AuthData;
import server.security.AuthResponse;

import java.time.Duration;

@SpringBootTest(classes = ServerApplication.class)
public class MessageControllerITest {

    private static RSocketRequester requester;
    private static final String USER1 = "user1";
    private static final String USER2 = "user2";
    private static final String PASSWORD = "password";

    @Autowired
    private MessageRepository messageRepository;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder) {
        requester = builder
                .connectTcp("localhost", 7000)
                .block();  // Подключение для инициализации
    }

    @Test
    void testSendMessage() {
        // 1) Регистрация первого пользователя
        AuthData registerData1 = new AuthData(USER1, PASSWORD);
        Mono<AuthResponse> registerResponseMono1 = requester
                .route("registration")
                .data(registerData1)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(registerResponseMono1)
                .expectNextMatches(response -> "confirmed".equals(response.getStatus()))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 2) Регистрация второго пользователя
        AuthData registerData2 = new AuthData(USER2, PASSWORD);
        Mono<AuthResponse> registerResponseMono2 = requester
                .route("registration")
                .data(registerData2)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(registerResponseMono2)
                .expectNextMatches(response -> "confirmed".equals(response.getStatus()))
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 3) Логин для первого пользователя
        AuthData loginData1 = new AuthData(USER1, PASSWORD);
        Mono<AuthResponse> loginResponseMono1 = requester
                .route("login")
                .data(loginData1)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(loginResponseMono1)
                .expectNextMatches(response -> response != null)
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 4) Логин для второго пользователя
        AuthData loginData2 = new AuthData(USER2, PASSWORD);
        Mono<AuthResponse> loginResponseMono2 = requester
                .route("login")
                .data(loginData2)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(loginResponseMono2)
                .expectNextMatches(response -> response != null)
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 5) Отправка сообщения от USER1 к USER2
        Message message = new Message(USER1, USER2, "Hello from user1", false);

        Mono<Void> sendMessageMono = requester
                .route("send-message")
                .data(message)
                .retrieveMono(Void.class);

        StepVerifier.create(sendMessageMono)
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 6) Проверка, что сообщение было сохранено в базе данных
        StepVerifier.create(messageRepository.findBySenderUsernameAndRecipientUsername(USER1, USER2))
                .expectNextMatches(savedMessage -> savedMessage.getSenderUsername().equals(USER1)
                        && savedMessage.getRecipientUsername().equals(USER2)
                        && savedMessage.getMessage().equals("Hello from user1"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }

    @AfterAll
    public static void tearDownOnce() {
        requester.rsocket().dispose();
    }
}
