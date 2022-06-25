package ge.freeuni.informatics.model.exception;

public class InformaticsServerException extends Exception {

    String code;

    public InformaticsServerException(String code, Exception ex) {
        super(ex);
        this.code = code;
    }
    public InformaticsServerException(String code) {
        super();
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
