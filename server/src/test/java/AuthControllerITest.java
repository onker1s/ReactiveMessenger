import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import server.ServerApplication;
import server.security.AuthData;
import server.security.AuthResponse;

import java.time.Duration;

@SpringBootTest(classes = ServerApplication.class)
public class AuthControllerITest {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder) {
        requester = builder
                .connectTcp("localhost", 7000)
                .block();
    }

    @Test
    void testLogin() {
        AuthData authData = new AuthData("user", "password");

        Mono<AuthResponse> responseMono = requester
                .route("login")
                .data(authData)
                .retrieveMono(AuthResponse.class);

        StepVerifier.create(responseMono)
                .expectNextMatches(response -> response.getStatus().equals("confirmed"))
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }
    @AfterAll
    public static void tearDownOnce() {
        requester.rsocket().dispose();
    }
}
