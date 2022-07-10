package ge.freeuni.informatics.controller.model;

import java.util.List;

public class GetLanguagesResponse extends InformaticsResponse {

    private List<CodeLanguageDTO> languages;

    public List<CodeLanguageDTO> getLanguages() {
        return languages;
    }

    public void setLanguages(List<CodeLanguageDTO> languages) {
        this.languages = languages;
    }
}
