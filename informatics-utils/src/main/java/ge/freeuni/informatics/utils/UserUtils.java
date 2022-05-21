package ge.freeuni.informatics.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class UserUtils {

    private static final int SALT_LENGTH = 8;

    public static String getSalt() {
        byte[] saltBytes = new byte[SALT_LENGTH];

        new Random().nextBytes(saltBytes);

        return Base64.encodeBase64String(saltBytes);
    }

    public static String getHash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedPassword = salt + password;

            byte[] hashBytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(hashBytes);
        } catch (NoSuchAlgorithmException ignored) {
            return "";
        }
    }
}
