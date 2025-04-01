package server;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@RequiredArgsConstructor
@Document
public class Message{

    @Id
    private Long id;

    private final String senderUsername;
    private final String recipientUsername;
    private final String message;
    private String deliveredStatus;
    private Date sentAt = new Date();


}