import java.io.File;
import java.io.Serializable;
import javax.crypto.SecretKey;

/**
 * A serializable message that can be sent on the channel
 */
public class ChannelMessage implements Serializable {
    private String msg;
    private String name;
    private byte[] file;
    private boolean isFile = false;
    private boolean isKey = false;
    private SecretKey key;
    private File fileMeta;

    /**
     * Make a new channel message
     * 
     * @param username the sender of the message
     * @param message  the message to be sent
     */
    public ChannelMessage(String username, String message) {
        name = username;
        msg = message;
    }

    /**
     * Make a new file message
     * 
     * @param username the sender of the file
     * @param fileMeta the metadata of the file
     * @param f        the byte array of the file
     */
    public ChannelMessage(String username, File fileMeta, byte[] f) {
        name = username;
        this.isFile = true;
        this.file = f;
        this.fileMeta = fileMeta;
    }

    /**
     * Make a new secret key message
     * 
     * @param username the sender of the key
     * @param k        the key to send to the channel
     */
    public ChannelMessage(String username, SecretKey k) {
        isKey = true;
        name = username;
        key = k;
    }

    /**
     * Get the author of the message
     * 
     * @return the author of the message
     */
    public String getAuthor() {
        return name;
    }

    /**
     * Get the message string
     * 
     * @return the message string
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Get the secret key of the message
     * 
     * @return the secret key
     */
    public SecretKey getKey() {
        return key;
    }

    /**
     * Get the file byte array
     * 
     * @return the file byte array
     */
    public byte[] getFile() {
        return file;
    }

    /**
     * Get the file metadata
     * 
     * @return the file metadata
     */
    public File getFileMeta() {
        return fileMeta;
    }

    /**
     * Check if the message contains a file
     * 
     * @return whether the message is a file
     */
    public boolean isFile() {
        return isFile;
    }

    /**
     * Check whether the message is a secret key
     * 
     * @return whether the message is a key
     */
    public boolean isKey() {
        return isKey;
    }

    /**
     * Required by serializable
     */
    @Override
    public String toString() {
        return name + ":" + msg;
    }
}
