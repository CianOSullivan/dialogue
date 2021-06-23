package dialogue;

public class Client {
    public Client() {
        EventLogger log = new EventLogger();
        KeyHandler keyHandler = new KeyHandler(log);

        try {
            new Channel(keyHandler, log).start();
        } catch (Exception e) {
            log.error("Couldn't start channel: " + e);
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
