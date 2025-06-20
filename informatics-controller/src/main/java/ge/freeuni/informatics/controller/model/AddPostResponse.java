package ge.freeuni.informatics.controller.model;

public class AddPostResponse extends InformaticsResponse{

    private Long postId;

    public AddPostResponse(String status, String message, Long postId) {
        super(message);
        this.postId = postId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
