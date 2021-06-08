import java.io.File;
import java.io.Serializable;

public class ChannelMessage implements Serializable {
    private String msg;
    private String name;
    private byte[] file;
    private boolean isFile = false;
    private File fileMeta;

    public ChannelMessage(String username, String message) {
        name = username;
        msg = message;
    }

    public ChannelMessage(String username, File fileMeta, byte[] f) {
        name = username;
        setFile(fileMeta, f);
    }

    public String getAuthor() {
        return name;
    }

    public String getMsg() {
        return msg;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(File fm, byte[] f) {
        this.isFile = true;
        this.file = f;
        this.fileMeta = fm;
    }

    public File getFileMeta() {
        return fileMeta;
    }

    public boolean isFile() {
        return isFile;
    }

    @Override
    public String toString() {
        return name + ":" + msg;
    }
}
