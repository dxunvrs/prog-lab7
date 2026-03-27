package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class UpdateCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public UpdateCommand() {
        super("update", "update id - обновить значение элемента по заданному id",
                List.of(ArgType.INT, ArgType.OBJECT));
    }

    @Override
    public Response execute(Request request) {
        collectionManager.updateProductById(request.getIntArgs().get(0), request.getObjectArgs().get(0));
        String responseMessage = "Продукт с id=" + request.getIntArgs().get(0) + " обновлен";
        return new Response(ResponseType.OK, responseMessage);
    }
}
