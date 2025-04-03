import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import server.ServerApplication;
import server.security.AuthData;
import server.security.AuthResponse;

import java.time.Duration;

@SpringBootTest(classes = ServerApplication.class)

public class AuthControllerITest {

    private static RSocketRequester requester;
    private static final String TEST_USERNAME = "test1User";
    private static final String TEST_PASSWORD = "test1Password";
    private static String jwtToken; // Переменная для хранения токена

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder) {
        requester = builder.connectTcp("localhost", 7000).block();
    }

    @Test
    void testRegisterLoginAndDeleteUser() {
        // 1) Регистрация пользователя
        AuthData registerData = new AuthData(TEST_USERNAME, TEST_PASSWORD);
        Mono<AuthResponse> registerResponseMono = requester
                .route("registration")
                .data(registerData)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(registerResponseMono)
                .expectNextMatches(response -> {
                    System.out.println("[TEST] Регистрация: " + response.getStatus());
                    return "confirmed".equals(response.getStatus());
                })
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 2) Логин и получение токена
        AuthData loginData = new AuthData(TEST_USERNAME, TEST_PASSWORD);
        Mono<AuthResponse> loginResponseMono = requester
                .route("login")
                .data(loginData)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(loginResponseMono)
                .expectNextMatches(response -> {
                    if (response != null) {
                        jwtToken = response.getToken(); // Сохраняем токен
                        System.out.println("[TEST] Полученный токен: " + jwtToken);
                        return true;
                    }
                    return false;
                })
                .expectComplete()
                .verify(Duration.ofSeconds(5));

        // 3) Удаление пользователя с использованием JWT
        if (jwtToken != null) {
            Mono<Void> deleteUserMono = requester.route("deleteUser")
                    .metadata("Bearer " + jwtToken, MimeTypeUtils.parseMimeType("message/x.rsocket.authentication.bearer"))
                    .data(TEST_USERNAME)
                    .retrieveMono(Void.class);

            StepVerifier.create(deleteUserMono)
                    .expectComplete()
                    .verify(Duration.ofSeconds(5));
            System.out.println("[TEST] Пользователь удалён");
        } else {
            System.err.println("[TEST] Ошибка: Токен не получен, удаление невозможно!");
        }
    }

    @AfterAll
    public static void tearDownOnce() {
        requester.rsocket().dispose();
    }
}
