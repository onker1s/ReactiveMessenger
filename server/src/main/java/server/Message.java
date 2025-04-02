package server;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@RequiredArgsConstructor
@Document
public class Message{

    @Id
    private Long id;
    @NonNull
    private String senderUsername;
    @NonNull
    private String recipientUsername;
    @NonNull
    private String message;
    @NonNull
    private Boolean deliveredStatus;
    private Date sentAt = new Date();


}