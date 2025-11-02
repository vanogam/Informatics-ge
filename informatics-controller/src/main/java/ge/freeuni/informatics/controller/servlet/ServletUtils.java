package ge.freeuni.informatics.controller.servlet;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.InformaticsResponse;

public class ServletUtils {

    public static String sanitizeTestKey(String key) throws InformaticsServerException {
        if (key == null || key.isEmpty() || key.length() > 10) {
            throw InformaticsServerException.INVALID_TEST_KEY;
        }
        return key.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    public static int getResponseCode(InformaticsServerException ex) {
        switch (ex.getExceptionType()) {
            case VALIDATION_ERROR:
                return 400;
            case PERMISSION_DENIED:
                return 403;
            case UNEXPECTED_ERROR:
            default:
                return 500;
        }
    }
}
