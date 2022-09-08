package ge.freeuni.informatics.repository.post;

import ge.freeuni.informatics.common.model.post.Post;

import java.util.List;

public interface IPostRepository {

    Post savePost(Post post);

    Post getPost(Long postId);

    List<Post> filter(Long roomId, Integer offset, Integer limit);
}
