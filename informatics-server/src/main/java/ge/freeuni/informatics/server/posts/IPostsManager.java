package ge.freeuni.informatics.server.posts;

import ge.freeuni.informatics.common.dto.PostCommentDTO;
import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

import java.util.List;

public interface IPostsManager {

    PostDTO getPost(long postId) throws InformaticsServerException;

    List<PostDTO> getPosts(long roomId, Integer pageSize, Integer pageNum) throws InformaticsServerException;

    PostDTO addPostDraft(long roomId, PostDTO postDTO) throws InformaticsServerException;

    void savePost(PostDTO postDTO) throws InformaticsServerException;

    void deletePost(long postId) throws InformaticsServerException;

    void addComment(long postId, String commentText, Long parentId) throws InformaticsServerException;

    void deleteComment(long commentId) throws InformaticsServerException;

    List<PostCommentDTO> getHeadComments(long postId, int pageNum, int pageSize) throws InformaticsServerException;

    List<PostCommentDTO> getChildComments(long commentId, int pageNum, int pageSize) throws InformaticsServerException;
}
