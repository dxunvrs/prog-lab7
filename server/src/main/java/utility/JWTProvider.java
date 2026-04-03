package utility;

import exceptions.AuthExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTProvider {
    private static final long EXPIRATION_TIME = 60_000*5*10; // 1 минута * 5 * 10
    private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String createToken(String username, int userId) {
        return Jwts.builder().setSubject(username).claim("userId", userId)
                .setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    public int validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            return claims.get("userId", Integer.class);
        } catch (Exception e){
            throw new AuthExpiredException("Пройдите авторизацию снова");
        }
    }
}
