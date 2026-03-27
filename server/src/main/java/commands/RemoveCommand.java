package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class RemoveCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public RemoveCommand() {
        super("remove_by_id", "remove_by_id - удалить элемент из коллекции по id",
                List.of(ArgType.INT));
    }

    @Override
    public Response execute(Request request) {
        collectionManager.removeProductById(request.getIntArgs().get(0));
        String responseMessage = "Продукт с id=" + request.getIntArgs().get(0) + " удален";
        return new Response(ResponseType.OK, responseMessage);
    }
}
