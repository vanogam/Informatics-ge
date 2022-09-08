package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.PostDTO;

import java.util.List;

public class GetPostResponse extends InformaticsResponse {

    PostDTO post;

    public GetPostResponse(String status, String message, PostDTO post) {
        super(status, message);
        this.post = post;
    }

    public PostDTO getPost() {
        return post;
    }

    public void setPost(PostDTO post) {
        this.post = post;
    }
}
