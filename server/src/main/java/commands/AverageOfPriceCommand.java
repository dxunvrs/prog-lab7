package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class AverageOfPriceCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public AverageOfPriceCommand() {
        super("average_of_price", "average_of_price - вывести среднее значение цены для всех элементов коллекции", List.of(ArgType.NONE));
    }

    @Override
    public Response execute(Request request) {
        String responseMessage = String.format("Среднее значение цены для всех элементов коллекции: %.2f", collectionManager.getAvgOfPrice());
        return new Response(ResponseType.OK, responseMessage);
    }
}
