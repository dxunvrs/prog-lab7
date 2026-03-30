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

    private Response(Builder builder) {
        this.message = builder.message;
        this.type = builder.type;
        this.token = builder.token;
        this.syncData = builder.syncData;
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

    public static class Builder {
        private String message;
        private ResponseType type;

        private String token;

        private Map<String, CommandDef> syncData;

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder type(ResponseType type) {
            this.type = type;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder syncData(Map<String, CommandDef> syncData) {
            this.syncData = syncData;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
