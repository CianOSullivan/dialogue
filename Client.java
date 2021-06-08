import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {

    public Client() {
        SecretKey aesKey = read_key();
        if (aesKey == null) {
            System.out.println("Key doesn't exist. Quitting.");
            System.exit(1);
        }

        try {
            new Channel(aesKey).start();
        } catch (Exception e) {
            System.out.println("Couldn't start channel: " + e);
        }

    }

    public SecretKey read_key() {
        try {
            byte[] key_bytes = Files.readAllBytes(Paths.get("key.txt"));
            SecretKey key = new SecretKeySpec(key_bytes, "AES");
            return key;
        } catch (IOException e) {
            System.out.println("Couldn't read key from file: " + e);
        }
        return null;
    }

    public static void main(String[] args) {
        new Client();
    }
}
