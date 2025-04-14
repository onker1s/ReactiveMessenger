package server.web.api.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import server.Dialog;
import server.data.DialogRepository;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

@Service
public class DialogService {

    private final DialogRepository dialogRepository;

    public DialogService(DialogRepository dialogRepository) {
        this.dialogRepository = dialogRepository;
    }

    // Возвращает список всех пользователей, с которыми у данного пользователя есть диалоги
    public Flux<String> getContacts(String username) {
        return dialogRepository.findAllByParticipantIdsContaining(username)
                .flatMap(dialog -> Flux.fromIterable(dialog.getParticipantIds()))
                .filter(participant -> !participant.equals(username))
                .distinct();
    }

    public Mono<Dialog> getDialogBetween(String user1, String user2) {
        return dialogRepository.findAllByParticipantIdsContaining(user1)
                .filter(dialog -> dialog.getParticipantIds().contains(user2))
                .next();
    }
    public Flux<Dialog> getDialoguesByUser(String username) {
        return dialogRepository.findAllByParticipantIdsContaining(username);
    }
    public Mono<Dialog> createDialog(String user1, String user2) {
        // Сортируем участников, чтобы порядок был одинаковым всегда
        List<String> participants = Stream.of(user1, user2)
                .sorted()
                .collect(Collectors.toList());
        // Проверяем, существует ли уже такой диалог
        return dialogRepository.findByParticipantIds(participants)
                .switchIfEmpty(Mono.defer(() -> {
                    Dialog newDialog = new Dialog();
                    newDialog.setParticipantIds(participants);
                    return dialogRepository.save(newDialog);
                }));
    }

}
