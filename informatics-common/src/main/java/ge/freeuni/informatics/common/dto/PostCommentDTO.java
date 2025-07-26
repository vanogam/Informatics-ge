package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.post.PostComment;

import java.util.Date;

public record PostCommentDTO (
        Long id,
        String content,
        String authorName,
        Long postId,
        Long parentId,
        Long childrenCount,
        Date createDate

){
    public PostCommentDTO (Long id, String content, String authorName, Long postId, Long parentId, Long childrenCount, Date createDate){
        this.id = id;
        this.content = content;
        this.authorName = authorName;
        this.postId = postId;
        this.parentId = parentId;
        this.childrenCount = childrenCount;
        this.createDate = createDate;
    }

    public static  PostCommentDTO toDTO(PostComment comment, String authorName, long childrenCount) {
        return new PostCommentDTO(
                comment.getId(),
                comment.getComment(),
                authorName,
                comment.getPostId(),
                comment.getParentId(),
                childrenCount,
                comment.getCreateDate()
        );
    }
}
