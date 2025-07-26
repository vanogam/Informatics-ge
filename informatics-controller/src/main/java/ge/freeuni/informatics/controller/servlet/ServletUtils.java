package ge.freeuni.informatics.controller.servlet;

import ge.freeuni.informatics.common.exception.InformaticsServerException;

public class ServletUtils {

    public static String sanitizeTestKey(String key) throws InformaticsServerException {
        if (key == null || key.isEmpty() || key.length() > 10) {
            throw InformaticsServerException.INVALID_TEST_KEY;
        }
        return key.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}
