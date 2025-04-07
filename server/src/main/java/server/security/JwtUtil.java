package server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    @Getter
    private final SecretKey secretKey; // Секретный ключ
    private final long validityInMs = 3600000; // 1 час

    public JwtUtil() {
        this.secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        System.out.printf("Secret Key JWTUtil: " +  secretKey + "\n");
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validityInMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Claims parseToken(String token) {
        System.out.println("///////// PARSING TOKEN");
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("///////// VALIDATING TOKEN");
            return !parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}

