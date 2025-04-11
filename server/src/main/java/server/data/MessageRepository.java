package server.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Message;

public interface MessageRepository extends ReactiveCrudRepository<Message, String> {

    Mono<Message> findBySenderUsernameAndRecipientUsername(String senderUsername, String recipientUsername);
    Mono<Void> deleteBySenderUsernameAndRecipientUsername(String sender, String recipient);
    Flux<Message> findAllByRecipientUsernameAndSenderUsernameOrRecipientUsernameAndSenderUsername(String username1,
                                                                                                  String username2,
                                                                                                  String username22,
                                                                                                  String username11);
    Flux<Message> findAllByRecipientUsernameOrSenderUsername(String username, String username1);
}
