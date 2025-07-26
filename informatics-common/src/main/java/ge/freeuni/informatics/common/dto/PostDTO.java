package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.post.Post;
import ge.freeuni.informatics.common.model.post.PostStatus;

import java.util.Date;

public record PostDTO(
    Long id,
    String title,
    String content,
    String draftContent,
    String authorName,
    Date createDate,
    PostStatus status,
    Long roomId,
    Long version
) {
    public static PostDTO toDTO(Post post) {
        return new PostDTO(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getDraftContent(),
            post.getAuthor().getUsername(),
            post.getCreateDate(),
            post.getStatus(),
            post.getRoomId(),
            post.getVersion()
        );
    }

    public static Post fromDTO(PostDTO postDTO) {
        Post post = new Post();
        post.setId(postDTO.id());
        post.setTitle(postDTO.title());
        post.setContent(postDTO.content());
        post.setDraftContent(postDTO.draftContent());
        post.setRoomId(postDTO.roomId());
        post.setStatus(postDTO.status());
        post.setVersion(postDTO.version());
        return post;
    }
}