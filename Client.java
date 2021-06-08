import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Client {
    private final EventLogger log = new EventLogger();
    private SecretKey aesKey;

    public Client() {
        aesKey = read_key();
        if (aesKey == null) {
            makeKey();
        }

        try {
            new Channel(aesKey, log).start();
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
            log.error("Couldn't read key from file: " + e);
        }
        return null;
    }

    private void makeKey() {
        // Generate a new cipher and add key to server
        try {
            log.info("Generating new key");
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            SecretKey newKey = kgen.generateKey();

            try (FileOutputStream key_file = new FileOutputStream("key.txt")) {
                key_file.write(newKey.getEncoded());
            }
            aesKey = newKey;

        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("An error occurred generating key: " + e);
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
