package ge.freeuni.informatics.common.exception;

public class InformaticsServerException extends Exception {

    private final String code;

    private final ExceptionType exceptionType;

    // General exceptions
    public static final InformaticsServerException UNEXPECTED_ERROR = new InformaticsServerException("unexpectedException", ExceptionType.UNEXPECTED_ERROR);
    public static final InformaticsServerException PERMISSION_DENIED = new InformaticsServerException("permissionDenied", ExceptionType.PERMISSION_DENIED);

    // Room related exceptions
    public static final InformaticsServerException ROOM_MISMATCH = new InformaticsServerException("roomMismatch", ExceptionType.VALIDATION_ERROR);

    // Post and Comment related exceptions
    public static final InformaticsServerException POST_NOT_FOUND = new InformaticsServerException("postNotFound", ExceptionType.VALIDATION_ERROR);
    public static final InformaticsServerException COMMENT_NOT_FOUND = new InformaticsServerException("commentNotFound", ExceptionType.VALIDATION_ERROR);

    // Task/tests related exceptions
    public static final InformaticsServerException TASK_NOT_FOUND = new InformaticsServerException("taskNotFound", ExceptionType.VALIDATION_ERROR);
    public static final InformaticsServerException TEST_NOT_FOUND = new InformaticsServerException("testNotFound", ExceptionType.VALIDATION_ERROR);
    public static final InformaticsServerException INVALID_TEST_KEY = new InformaticsServerException("invalidTestKey", ExceptionType.VALIDATION_ERROR);
    public static final InformaticsServerException INVALID_STATEMENT = new InformaticsServerException("invalidStatement", ExceptionType.UNEXPECTED_ERROR);

    public InformaticsServerException(String code, ExceptionType exceptionType) {
        super();
        this.code = code;
        this.exceptionType = exceptionType;
    }

    // TODO: remove this constructor
    public InformaticsServerException(String code, Exception ex) {
        super(ex);
        this.code = code;
        this.exceptionType = null;
    }
    public InformaticsServerException(String code) {
        super();
        this.code = code;
        this.exceptionType = null;
    }

    public String getCode() {
        return code;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }
}
