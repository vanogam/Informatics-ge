package ge.freeuni.informatics.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserUtils {

    private static final int SALT_LENGTH = 8;

    public static String getSalt() {
        return StringUtils.getRandomBase64String(SALT_LENGTH);
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
