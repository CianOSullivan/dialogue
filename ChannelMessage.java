import java.io.File;
import java.io.Serializable;

import javax.crypto.SecretKey;

public class ChannelMessage implements Serializable {
    private String msg;
    private String name;
    private byte[] file;
    private boolean isFile = false;
    private boolean isKey = false;
    private SecretKey key;
    private File fileMeta;

    public ChannelMessage(String username, String message) {
        name = username;
        msg = message;
    }

    public ChannelMessage(String username, File fileMeta, byte[] f) {
        name = username;
        this.isFile = true;
        this.file = f;
        this.fileMeta = fileMeta;
    }

    public ChannelMessage(String username, SecretKey k) {
        isKey = true;
        name = username;
        key = k;
    }

    public String getAuthor() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public SecretKey getKey() {
        return key;
    }

    public byte[] getFile() {
        return file;
    }

    public File getFileMeta() {
        return fileMeta;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isKey() {
        return isKey;
    }

    @Override
    public String toString() {
        return name + ":" + msg;
    }
}
