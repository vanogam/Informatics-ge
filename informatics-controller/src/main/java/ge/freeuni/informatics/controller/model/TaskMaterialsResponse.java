package ge.freeuni.informatics.controller.model;

import java.util.List;

public class TaskMaterialsResponse extends InformaticsResponse {
    boolean editorialAvailable;
    List<String> solutionLanguages;

    public TaskMaterialsResponse(boolean editorialAvailable, List<String> solutionLanguages) {
        super(null);
        this.editorialAvailable = editorialAvailable;
        this.solutionLanguages = solutionLanguages;
    }

    public boolean isEditorialAvailable() {
        return editorialAvailable;
    }

    public List<String> getSolutionLanguages() {
        return solutionLanguages;
    }
}
