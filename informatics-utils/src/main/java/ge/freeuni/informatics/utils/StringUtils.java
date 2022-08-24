package ge.freeuni.informatics.utils;

import org.apache.tomcat.util.codec.binary.Base64;

import java.util.Random;

public class StringUtils {

    public static String getRandomBase64String(int length) {
        byte[] saltBytes = new byte[length];
        new Random().nextBytes(saltBytes);
        return Base64.encodeBase64String(saltBytes);
    }

}
