package ge.freeuni.informatics.model.exception;

public class InformaticsServerException extends Exception {

    String code;

    public InformaticsServerException(String code, Exception ex) {
        super(ex);
        this.code = code;
    }
}
