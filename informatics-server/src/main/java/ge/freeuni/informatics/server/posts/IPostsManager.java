package ge.freeuni.informatics.server.posts;

import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;

import java.io.File;
import java.util.List;

public interface IPostsManager {

    PostDTO getPost(long postId) throws InformaticsServerException;

    List<PostDTO> getPosts(long roomId, Integer offset, Integer limit) throws InformaticsServerException;

    File getPostImage(long postId) throws InformaticsServerException;

    Long addPost(PostDTO post) throws InformaticsServerException;

    void uploadImage(long postId, byte[] image) throws InformaticsServerException;
}
