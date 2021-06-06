import java.io.Serializable;

public class ChannelMessage implements Serializable {
    private String msg;
    private String name;

    public ChannelMessage(String username, String message) {
        name = username;
        msg = message;
    }

    public String getAuthor() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return name + ":" + msg;
    }
}
