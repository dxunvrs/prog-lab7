package network;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.Product;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Request {
    private RequestType type;

    private String username;
    private String password;
    private String commandName;

    private String token;

    private final List<String> stringArgs = new ArrayList<>();
    private final List<Integer> intArgs = new ArrayList<>();
    private final List<Product> objectArgs = new ArrayList<>();

    public Request() {}

    public Request(RequestType type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getCommandName() {
        return commandName;
    }

    public RequestType getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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
