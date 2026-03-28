package network;

import com.fasterxml.jackson.annotation.JsonInclude;
import commands.CommandDef;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private String message;
    private ResponseType type;

    private String token;

    private Map<String, CommandDef> syncData;

    public Response() {}

    public Response(ResponseType type, String message) {
        this.type = type;
        this.message = message;
    }

    public Response(ResponseType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, CommandDef> getSyncData() {
        return syncData;
    }

    public ResponseType getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public void setSyncData(Map<String, CommandDef> syncData) {
        this.syncData = syncData;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
