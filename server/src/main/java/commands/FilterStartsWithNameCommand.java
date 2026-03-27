package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class FilterStartsWithNameCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public FilterStartsWithNameCommand() {
        super("filter_starts_with_name", "filter_starts_with_name - вывести элементы, название которых начинается с заданной подстроки",
                List.of(ArgType.STR));
    }

    @Override
    public Response execute(Request request) {
        String responseMessage = collectionManager.getFormattedCollection(product -> product.getName().startsWith(request.getStringArgs().get(0)));
        return new Response(ResponseType.OK, responseMessage);
    }
}
