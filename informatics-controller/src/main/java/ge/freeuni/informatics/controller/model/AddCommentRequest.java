package ge.freeuni.informatics.controller.model;

public record AddCommentRequest (
        String content,
        Long parentId
) {


}
