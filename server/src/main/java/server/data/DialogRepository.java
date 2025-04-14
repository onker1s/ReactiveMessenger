package server.data;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import server.Dialog;
import reactor.core.publisher.Flux;

import java.util.List;

public interface DialogRepository extends ReactiveMongoRepository<Dialog, String> {
    Flux<Dialog> findAllByParticipantIdsContaining(String username);
    Mono<Dialog> findByParticipantIds(List<String> participantIds);

}
