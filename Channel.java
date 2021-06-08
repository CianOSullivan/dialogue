import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

import org.jgroups.Message;

public class Channel extends ReceiverAdapter {
    JChannel channel;
    ChannelWindow window;
    String user_name = System.getProperty("user.name", "n/a");
    SecretKey aesKey;

    public Channel(SecretKey key) {
        aesKey = key;
    }

    public void updateKey(SecretKey newKey) {
        aesKey = newKey;
    }

    public void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("ChatChannel");
        window = new ChannelWindow(this, aesKey);

        // eventLoop();
        // channel.close();
    }

    public View getView() {
        return channel.getView();
    }

    /*
     * private void eventLoop() throws Exception { BufferedReader in = new
     * BufferedReader(new InputStreamReader(System.in)); while (true) { try {
     * System.out.print("> "); System.out.flush(); String line =
     * in.readLine().toLowerCase(); if (line.startsWith("quit") ||
     * line.startsWith("exit")) break; // line = "[" + user_name + "] " + line;
     * ChannelMessage message = new ChannelMessage(user_name, line); SealedObject
     * sealedMessage = new SealedObject() Message msg = new Message(null, null,
     * message); channel.send(msg); System.out.println("Sent message"); } catch
     * (Exception e) { } } }
     */
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
            System.out.println("Sent message");
        } catch (Exception e) {
        }
    }

    public void send(SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            ChannelMessage message = new ChannelMessage(user_name, key);
            // SealedObject sealedMessage = new SealedObject(message, cipher);
            channel.send(new Message(null, null, message));
            System.out.println("Sent message");
        } catch (Exception e) {
        }
    }

    public void send(File fileMeta, byte[] msg) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            ChannelMessage message = new ChannelMessage(user_name, fileMeta, msg);
            SealedObject sealedMessage = new SealedObject(message, cipher);
            channel.send(new Message(null, null, sealedMessage));
            System.out.println("Sent message");
        } catch (Exception e) {
        }
    }

    public void receive(Message msg) {

        window.addMessage(msg);
        System.out.println("MSG RECEIVED");
    }

    public void close() {
        channel.close();
    }
}
