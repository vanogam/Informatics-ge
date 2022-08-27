package ge.freeuni.informatics.server.posts;

import ge.freeuni.informatics.common.model.post.Post;

import java.util.List;

public interface IPostsManager {

    List<Post> getPosts(long roomId);

    void addPost(Post post);
}
