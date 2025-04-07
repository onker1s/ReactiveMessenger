import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import server.ServerApplication;

import java.net.URI;

@SpringBootTest(classes = ServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ConnectionTest {
    @Autowired
    private RSocketRequester.Builder builder;
    private static RSocketRequester requester;

    @BeforeEach
    public void setup() {
        requester = builder
                .connectTcp("localhost", 7000)
                .block();
    }

    @Test
    void testRSocketConnection() {


        Mono<Void> connectionCheck = requester.rsocketClient().onClose();

        StepVerifier.create(connectionCheck)
                .expectSubscription()
                .thenCancel() // Просто проверяем, что соединение открывается
                .verify();
    }

    @AfterEach
    public void tearDown() {
        requester.rsocket().dispose();
    }
}
