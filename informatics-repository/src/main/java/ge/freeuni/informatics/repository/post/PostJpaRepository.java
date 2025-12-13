package ge.freeuni.informatics.repository.post;

import ge.freeuni.informatics.common.model.post.Post;
import ge.freeuni.informatics.common.model.post.PostStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    List<Post> getPostsByRoomIdAndStatus(Long roomId, PostStatus status, Pageable pageable);

    List<Post> getPostsByRoomIdAndAuthor_IdAndStatus(Long roomId, Long authorId, PostStatus status, Pageable pageable);
}
