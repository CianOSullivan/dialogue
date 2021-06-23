package dialogue;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * Key handler class which manages the security key.
 */
public class KeyHandler {
    private SecretKey aesKey;
    private final EventLogger log;
    private KeyGenerator kgen;
    private String keyLoc = System.getProperty("user.home") + "/.config/cian/dialogue/key.txt";

    /**
     * Create a new key handler instance
     *
     * @param l the EventLogger instance
     */
    public KeyHandler(EventLogger l) {
        log = l;

        // Initialise a new key generator
        try {
			kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

        // Read the key or generate if not exists
        aesKey = read_key();
        if (aesKey == null) {
            acceptNewKey(genKey());
        }
    }

    /**
     * Read the key from the filesystem
     *
     * @return the key that has been read
     */
    public SecretKey read_key() {
        try {
            byte[] key_bytes = Files.readAllBytes(Paths.get(keyLoc));
            return new SecretKeySpec(key_bytes, "AES");
        } catch (IOException e) {
            log.error("Couldn't read key from file: " + e);
        }
        return null;
    }

    /**
     * Returns whether the key exists or not
     *
     * @return true if key exists, false otherwise
     */
    public boolean doesKeyExist() {
        return new File(keyLoc).isFile();
    }

    /**
     * Accept a new aes key
     *
     * @param key the new key to be accepted
     */
    public void acceptNewKey(SecretKey key) {
        try (FileOutputStream key_file = new FileOutputStream(keyLoc)) {
            key_file.write(key.getEncoded());
        } catch (IOException e) {
            log.error("Couldn't accept key: " + e);
        }
        setKey(key);
    }

    /**
     * Generate a new aes key
     *
     * @return the new aes key
     */
    public SecretKey genKey() {
        log.info("Generating new key");
        return kgen.generateKey();
    }

    /**
     * Get the current key
     *
     * @return the current key
     */
	public SecretKey getKey() {
		return aesKey;
	}

    /**
     * Set to a new key
     *
     * @param aesKey the new aes key
     */
	public void setKey(SecretKey aesKey) {
		this.aesKey = aesKey;
	}
}