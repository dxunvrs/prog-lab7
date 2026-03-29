package commands;

import core.CollectionManager;
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
    public Response execute(CommandContext context) {
        collectionManager.removeProductById(context.getIntArgs().get(0), context.getCurrentUserId());
        String responseMessage = "Продукт с id=" + context.getIntArgs().get(0) + " удален";
        return new Response(ResponseType.OK, responseMessage);
    }
}
