package common;

public class Message {
    private final Protocol type;
    private final String payload;

    public Message(Protocol type, String payload) {
        this.type = type;
        this.payload = payload == null ? "" : payload;
    }

    public Protocol getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public String encode() {
        return type.name() + "|" + payload;
    }

    public static Message decode(String line) {
        if (line == null) {
            return new Message(Protocol.LOGIN_FAIL, "reason=EmptyMessage");
        }
        String[] parts = line.split("\\|", 2);
        Protocol type;
        try {
            type = Protocol.valueOf(parts[0]);
        } catch (Exception e) {
            return new Message(Protocol.LOGIN_FAIL, "reason=UnknownType");
        }
        String payload = parts.length == 2 ? parts[1] : "";
        return new Message(type, payload);
    }
}
