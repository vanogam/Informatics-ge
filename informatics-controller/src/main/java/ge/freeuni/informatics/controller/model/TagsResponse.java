package ge.freeuni.informatics.controller.model;

import java.util.List;

public class TagsResponse extends InformaticsResponse {
    List<String> tags;

    public TagsResponse(List<String> tags) {
        super(null);
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }
}
