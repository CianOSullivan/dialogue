import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.jgroups.Message;


public class Channel extends ReceiverAdapter {
    JChannel channel;
    String user_name=System.getProperty("user.name", "n/a");

    public void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("AuctionCluster");
        eventLoop();
        channel.close();
    }

    private void eventLoop() throws Exception{
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit"))
                    break;
                line="[" + user_name + "] " + line;
                Message msg=new Message(null, null, line);
                channel.send(msg);
            }
            catch(Exception e) {
            }
        }
    }

    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        System.out.println(msg.getSrc() + ": " + msg.getObject());
    }

    public static void main(String[] args) {
        try {
            new Channel().start();
        } catch (Exception e) {
            System.out.println("Couldn't start channel: " + e);
        }
    }
}
