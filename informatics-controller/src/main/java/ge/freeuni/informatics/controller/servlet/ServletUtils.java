package ge.freeuni.informatics.controller.servlet;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;

public class ServletUtils {

    public static String sanitizeTestKey(String key) throws InformaticsServerException {
        if (key == null || key.isEmpty() || key.length() > 10) {
            throw InformaticsServerException.INVALID_TEST_KEY;
        }
        return key.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    /** A blank filter value means "no filter", not "match the empty string". */
    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    /** Blank/unrecognized values fall back to null (no filter) rather than rejecting the request. */
    public static SubmissionStatus parseSubmissionStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return SubmissionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int getResponseCode(InformaticsServerException ex) {
        if (ex.getExceptionType() == null) {
            return 500;
        }
        return switch (ex.getExceptionType()) {
            case VALIDATION_ERROR -> 400;
            case UNAUTHORIZED -> 401;
            case PERMISSION_DENIED -> 403;
            case NOT_FOUND -> 404;
            case CONFLICT -> 409;
            default -> 500;
        };
    }
}
