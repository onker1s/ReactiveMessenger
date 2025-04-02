package server.web.api.controllers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import server.Message;
import server.ServerApplication;

import java.time.Duration;
@SpringBootTest(classes = ServerApplication.class)
class MessageControllerITest {

    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder) {
        requester = builder
                .connectTcp("localhost", 7000)
                .block();
    }

    @Test
    void testSendMessageStream() {
        Mono<Message> messageFlux = Mono.just(
                new Message("user1", "user2", "Hello", Boolean.FALSE)
        );

        Mono<Void> responseMono = requester
                .route("send-message")
                .data(messageFlux)
                .retrieveMono(Void.class);

        StepVerifier.create(responseMono)
                .expectComplete()
                .verify(Duration.ofSeconds(5));
    }
    @AfterAll
    public static void tearDownOnce() {
        requester.rsocket().dispose();
    }
}

