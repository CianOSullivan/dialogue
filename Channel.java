import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;

import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import org.jgroups.Message;
import org.jgroups.Address;

public class Channel extends ReceiverAdapter {
    private final EventLogger log;
    private Address local_address;

    JChannel channel;
    ChannelWindow window;
    String user_name = System.getProperty("user.name", "n/a");
    SecretKey aesKey;

    public Channel(SecretKey key, EventLogger l) {
        aesKey = key;
        log = l;
    }

    public void updateKey(SecretKey newKey) {
        aesKey = newKey;
    }

    public void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        window = new ChannelWindow(this, aesKey, log);
        channel.connect("ChatChannel"); // This takes a long time
        local_address = channel.getAddress();
    }

    public View getView() {
        return channel.getView();
    }

    public Address getLocalAddress() {
        return local_address;
    }

    public void viewAccepted(View new_view) {
        // System.out.println("** view: " + new_view);
    }

    public void send(String msg) {
        try {
            msg = msg.replace("\\n", "\n");
            msg = msg.replace("\\t", "    ");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            ChannelMessage message = new ChannelMessage(user_name, msg);
            SealedObject sealedMessage = new SealedObject(message, cipher);
            channel.send(new Message(null, null, sealedMessage));
            log.info("Sent message");
        } catch (Exception e) {
            log.error("Couldn't send message: " + e);

        }
    }

    public void send(SecretKey key) {
        try {
            ChannelMessage message = new ChannelMessage(user_name, key);
            channel.send(new Message(null, null, message));
            log.info("Sent SecretKey");
        } catch (Exception e) {
            log.error("Couldn't send secret key: " + e);
        }
    }

    public void send(File fileMeta, byte[] msg) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            ChannelMessage message = new ChannelMessage(user_name, fileMeta, msg);
            SealedObject sealedMessage = new SealedObject(message, cipher);
            channel.send(new Message(null, null, sealedMessage));
            log.info("Sent file");
        } catch (Exception e) {
            log.error("Couldn't send file: " + e);
        }
    }

    public void receive(Message msg) {
        window.processMessage(msg);
        log.info("Message received");
    }

    public void close() {
        channel.close();
        log.info("Channel closed");
    }
}
