package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.post.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostDTO {

    Long id;

    String title;

    String content;

    Long authorId;

    String authorName;

    Date postDate;

    Long roomId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public static PostDTO toDTO(Post post) {
        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setTitle(post.getTitle());
        postDTO.setContent(post.getContent());
        postDTO.setAuthorId(post.getId());
        postDTO.setPostDate(post.getPostDate());
        postDTO.setRoomId(post.getRoomId());

        return postDTO;
    }

    public static Post fromDTO(PostDTO postDTO) {
        Post post = new Post();
        post.setId(postDTO.getId());
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setAuthorId(postDTO.getId());
        post.setPostDate(postDTO.getPostDate());
        post.setRoomId(postDTO.getRoomId());

        return post;
    }

    public static List<PostDTO> toDTOs(List<Post> posts) {
        List<PostDTO> postDTOList = new ArrayList<>();

        for (Post post : posts) {
            postDTOList.add(PostDTO.toDTO(post));
        }
        return postDTOList;
    }
}
