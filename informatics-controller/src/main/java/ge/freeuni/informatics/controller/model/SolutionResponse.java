package ge.freeuni.informatics.controller.model;

public class SolutionResponse extends InformaticsResponse {
    String code;
    String language;

    public SolutionResponse(String code, String language) {
        super(null);
        this.code = code;
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public String getLanguage() {
        return language;
    }
}
