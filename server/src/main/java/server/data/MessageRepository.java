package server.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Message;

public interface MessageRepository extends ReactiveCrudRepository<Message, String> {
    Flux<Message> findByRecipientUsernameAndDeliveredStatusFalse(String recipientUsername);
    Mono<Message> findBySenderUsernameAndRecipientUsername(String senderUsername, String recipientUsername);
    Mono<Void> deleteBySenderUsernameAndRecipientUsername(String sender, String recipient);
}
