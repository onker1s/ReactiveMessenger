package client.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@RequiredArgsConstructor

public class Message {
    private String id;
    @NonNull
    private String senderUsername;
    @NonNull
    private String recipientUsername;
    @NonNull
    private String message;
    private boolean deliveredStatus;
    private Date sentAt = new Date();


}