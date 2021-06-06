import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.jgroups.Message;

public class Channel extends ReceiverAdapter {
    JChannel channel;
    ChannelWindow window;
    String user_name = System.getProperty("user.name", "n/a");

    public void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("ChatChannel");
        window = new ChannelWindow(this);

        eventLoop();
        channel.close();
    }

    private void eventLoop() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();
                if (line.startsWith("quit") || line.startsWith("exit"))
                    break;
                // line = "[" + user_name + "] " + line;
                ChannelMessage message = new ChannelMessage(user_name, line);
                Message msg = new Message(null, null, message);
                channel.send(msg);
                System.out.println("Sent message");
            } catch (Exception e) {
            }
        }
    }

    public void viewAccepted(View new_view) {
        // System.out.println("** view: " + new_view);
    }

    public void send(String msg) {
        try {
            ChannelMessage message = new ChannelMessage(user_name, msg);
            channel.send(new Message(null, null, message));
            System.out.println("Sent message");
        } catch (Exception e) {
        }
    }

    public void receive(Message msg) {

        window.addMessage(msg);
        System.out.println("MSG RECEIVED");
    }

    public static void main(String[] args) {
        try {
            new Channel().start();
        } catch (Exception e) {
            System.out.println("Couldn't start channel: " + e);
        }
    }
}
