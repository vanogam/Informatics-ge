package ge.freeuni.informatics.repository.post;

import ge.freeuni.informatics.common.model.post.Post;

import java.util.List;

public interface IPostRepository {

    Post savePost(Post post);

    Post getPost(long postId);

    List<Post> filter(long roomId, Integer offset, Integer limit);
}
