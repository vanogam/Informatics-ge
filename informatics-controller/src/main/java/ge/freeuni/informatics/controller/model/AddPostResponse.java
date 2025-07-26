package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.PostDTO;

public class AddPostResponse extends InformaticsResponse{

    private PostDTO post;

    public AddPostResponse(String message, PostDTO post) {
        super(message);
        this.post = post;
    }

    public PostDTO getPost() {
        return post;
    }
}
