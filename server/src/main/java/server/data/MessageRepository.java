package server.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import server.Message;

public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {
}
