package core;

import commands.CommandContext;
import io.jsonwebtoken.Claims;
import network.Request;
import network.Response;
import network.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.AuthService;
import utility.JWTProvider;

public class RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final CommandManager commandManager;
    private final AuthService authService;
    private final JWTProvider jwtProvider = new JWTProvider();

    public RequestHandler(CollectionManager collectionManager, AuthService authService) {
        this.authService = authService;
        commandManager = new CommandManager(collectionManager);
    }

    public Response handle(Request request) {
        switch (request.getType()) {
            case SYNC -> {
                return commandManager.syncCommands();
            }
            case SERVER_COMMAND -> {
                Claims claims = jwtProvider.validateToken(request.getToken());

                if (claims == null) {
                    return new Response(ResponseType.AUTH_REQUIRED, "Пройдите авторизацию снова");
                }

                int userId = claims.get("userId", Integer.class);

                return commandManager.executeCommand(new CommandContext(request, userId));
            }
            case LOGIN -> {
                int userId = authService.login(request.getUsername(), request.getPassword());
                if (userId == -1) {
                    return new Response(ResponseType.AUTH_FAILED, "Ошибка входа");
                }
                Response response = new Response(ResponseType.AUTH_SUCCESS, "Вы успешно вошли");
                response.setToken(jwtProvider.createToken(request.getUsername(), userId));
                return response;
            }
            case REGISTER -> {
                int userId = authService.register(request.getUsername(), request.getPassword());
                if (userId == -1) {
                    return new Response(ResponseType.AUTH_FAILED, "Ошибка регистрации");
                }
                Response response = new Response(ResponseType.AUTH_SUCCESS, "Вы успешно зарегистрировались");
                response.setToken(jwtProvider.createToken(request.getUsername(), userId));
                return response;
            }
            default -> {
                return new Response(ResponseType.ERROR, "Неизвестный тип запроса");
            }
        }
    }
}