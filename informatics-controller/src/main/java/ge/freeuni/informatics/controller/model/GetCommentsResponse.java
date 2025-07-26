package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.PostCommentDTO;

import java.util.List;

public record GetCommentsResponse(
        List<PostCommentDTO> comments
) {
    public GetCommentsResponse(List<PostCommentDTO> comments) {
        this.comments = comments;
    }
}
