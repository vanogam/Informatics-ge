package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.PostDTO;

import java.util.List;

public class GetPostsResponse extends InformaticsResponse {

    List<PostDTO> posts;

    public GetPostsResponse(List<PostDTO> posts) {
        this.posts = posts;
    }

    public GetPostsResponse(String message, List<PostDTO> posts) {
        super(message);
        this.posts = posts;
    }

    public List<PostDTO> getPosts() {
        return posts;
    }

    public void setPosts(List<PostDTO> posts) {
        this.posts = posts;
    }
}
