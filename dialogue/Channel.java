package dialogue;

import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.ReceiverAdapter;
import org.jgroups.Message;
import org.jgroups.Address;

import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

/**
 * The channel that is joined to connect to the chat cluster
 */
public class Channel extends ReceiverAdapter {
    private final EventLogger log;
    private Address local_address; // The address of this channel

    private JChannel channel;
    private ChannelWindow window;
    private final String user_name = System.getProperty("user.name", "n/a");
    private final KeyHandler keyHandler;
    private boolean connected = false;

    /**
     * Make a new chat channel
     *
     * @param k   the key handler instance
     * @param l   the logger
     */
    public Channel(KeyHandler k, EventLogger l) {
        keyHandler = k;
        log = l;
    }

    /**
     * Join the cluster
     *
     * @throws Exception occurs when channel not started
     */
    public void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        window = new ChannelWindow(this, keyHandler, log);
        channel.connect("ChatChannel"); // This takes a long time
        connected = true;
        local_address = channel.getAddress();
    }

    /**
     * Get the view of the channel
     *
     * @return the view of the channel
     */
    public View getView() {
        return channel.getView();
    }

    /**
     * Get the name of the channel
     *
     * @return the cluster name
     */
    public String getName() {
        return channel.getClusterName();
    }

    /**
     * Set the channel to a different name
     *
     * @param name the new cluster name
     */
    public void setChannel(String name) {
        try {
            channel.disconnect();
            channel.connect(name);
            log.info("Channel set to " + name);
        } catch (Exception e) {
            log.error("Could not set channel to " + name);
        }
    }

    /**
     * Returns channel connection status
     *
     * @return whether the channel is connected or not
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Get the address of this device
     *
     * @return the address of the channel
     */
    public Address getLocalAddress() {
        return local_address;
    }

    /**
     * Accept a new view of the channel
     *
     * @param new_view the new view
     */
    public void viewAccepted(View new_view) {
        log.info("View accepted: " + new_view);
    }

    /**
     * Send a sealed message to the channel
     *
     * @param msg the message string
     */
    public void send(String msg) {
        try {
            // Perform string formatting
            msg = msg.replace("\\n", "\n");
            msg = msg.replace("\\t", "    ");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keyHandler.getKey());
            ChannelMessage message = new ChannelMessage(user_name, msg);
            SealedObject sealedMessage = new SealedObject(message, cipher);
            channel.send(new Message(null, null, sealedMessage));
            log.info("Sent message");
        } catch (Exception e) {
            log.error("Couldn't send message: " + e);
        }
    }

    /**
     * Send a secret key to the channel
     *
     * @param key the key
     */
    public void send(SecretKey key) {
        try {
            ChannelMessage message = new ChannelMessage(user_name, key);
            channel.send(new Message(null, null, message));
            log.info("Sent SecretKey");
        } catch (Exception e) {
            log.error("Couldn't send secret key: " + e);
        }
    }

    /**
     * Send a sealed file to the channel
     *
     * @param fileMeta the file metadata
     * @param msg      the file byte array
     */
    public void send(File fileMeta, byte[] msg) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, keyHandler.getKey());

            ChannelMessage message = new ChannelMessage(user_name, fileMeta, msg);
            SealedObject sealedMessage = new SealedObject(message, cipher);
            channel.send(new Message(null, null, sealedMessage));
            log.info("Sent file");
        } catch (Exception e) {
            log.error("Couldn't send file: " + e);
        }
    }

    /**
     * Process an incoming message
     *
     * @param msg the message to process
     */
    public void receive(Message msg) {
        window.processMessage(msg);
        log.info("Message received");
    }

    /**
     * Close the channel
     */
    public void close() {
        channel.close();
        log.info("Channel closed");
    }
}
