package utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTProvider {
    private static final long EXPIRATION_TIME = 60_000; // 1 минута
    private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String createToken(String username, int userId) {
        return Jwts.builder().setSubject(username).claim("userId", userId)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("Токен протух");
        } catch (SignatureException e) {
            System.out.println("Подпись не совпадает! Секрет на сервере изменился?");
        } catch (Exception e) {
            System.out.println("Ошибка валидации: " + e.getMessage());
        }
        return null;
    }
}
