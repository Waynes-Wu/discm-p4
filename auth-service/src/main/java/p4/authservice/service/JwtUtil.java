package p4.authservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private String secretKey = "I_Need_4.0_Please_Thank_You_Very_Much";  
    private final SecretKey signingKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    
    public String generateToken(String username) {
        long expirationDur = 1000 * 60 * 60;
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationDur);

        return Jwts.builder()
            .subject(username)      
            .issuedAt(now)          
            .expiration(expiry)     
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.after(new Date());
    }
}
