package ge.freeuni.informatics.controller.model;

import java.util.List;

public class SolutionLanguagesResponse extends InformaticsResponse {
    List<String> languages;

    public SolutionLanguagesResponse(List<String> languages) {
        super(null);
        this.languages = languages;
    }

    public List<String> getLanguages() {
        return languages;
    }
}
