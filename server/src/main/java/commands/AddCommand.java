package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class AddCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public AddCommand() {
        super("add", "add - добавление нового элемента", List.of(ArgType.OBJECT));
    }

    @Override
    public Response execute(Request request) {
        collectionManager.addProduct(request.getObjectArgs().get(0));
        return new Response(ResponseType.OK, "Продукт добавлен");
    }
}
