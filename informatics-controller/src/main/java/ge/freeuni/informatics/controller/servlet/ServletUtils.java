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
        if (ex.getExceptionType() == null) {
            return 500;
        }
        return switch (ex.getExceptionType()) {
            case VALIDATION_ERROR -> 400;
            case PERMISSION_DENIED -> 403;
            case NOT_FOUND -> 404;
            case CONFLICT -> 409;
            default -> 500;
        };
    }
}
