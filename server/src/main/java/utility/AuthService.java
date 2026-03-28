package utility;

import network.DBManager;
import network.Response;
import network.ResponseType;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final DBManager dbManager;

    public AuthService(DBManager dbManager) {
        this.dbManager = dbManager;
    }

    public int register(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            logger.warn("Пользователь ввел пустое имя или пароль");
            return -1;
            // return new Response(ResponseType.AUTH_FAILED, "Имя или пароль не могут быть пустыми");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

        int userId = dbManager.registerUser(username, hash);
        if (userId != -1) {
            logger.info("Пользователь с именем {} зарегистрирован", username);
            return userId;
        }
        logger.warn("Пользователь ввел занятое имя");
        return -1;
    }

    public int login(String username, String password) {
        String hashFromDB = dbManager.getUserHash(username);

        if (hashFromDB == null || !BCrypt.checkpw(password, hashFromDB)) {
            logger.error("Пользователь ввел неверный пароль");
            return -1;
        }

        return dbManager.getUserId(username);
    }
}
