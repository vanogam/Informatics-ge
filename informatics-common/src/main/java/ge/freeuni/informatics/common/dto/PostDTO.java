package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.post.Post;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public record PostDTO(
    Long id,
    String title,
    String content,
    String authorName,
    Date postDate,
    Long roomId
) {
    public static PostDTO toDTO(Post post) {
        return new PostDTO(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getAuthor().getUsername(),
            post.getPostDate(),
            post.getRoomId()
        );
    }

    public static Post fromDTO(PostDTO postDTO) {
        Post post = new Post();
        post.setId(postDTO.id());
        post.setTitle(postDTO.title());
        post.setContent(postDTO.content());
        post.setPostDate(postDTO.postDate());
        post.setRoomId(postDTO.roomId());
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