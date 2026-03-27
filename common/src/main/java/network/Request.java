package network;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.Product;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {
    private String commandName;
    private RequestType type;

    private final List<String> stringArgs = new ArrayList<>();
    private final List<Integer> intArgs = new ArrayList<>();
    private final List<Product> objectArgs = new ArrayList<>();

    public Request() {}

    public Request(RequestType type, String commandName) {
        this.type = type;
        this.commandName = commandName;
    }

    public String getCommandName() {
        return commandName;
    }

    public RequestType getType() {
        return type;
    }

    public void addStringArg(String value) {
        stringArgs.add(value);
    }

    public void addIntArg(int value) {
        intArgs.add(value);
    }

    public void addObjectArg(Product value) {
        objectArgs.add(value);
    }

    public List<String> getStringArgs() {
        return stringArgs;
    }

    public List<Integer> getIntArgs() {
        return intArgs;
    }

    public List<Product> getObjectArgs() {
        return objectArgs;
    }
}
