package server.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.messaging.rsocket.RSocketRequester;

@AllArgsConstructor
@Data
public class AuthData {
    private String username;
    private String password;
}
