package ge.freeuni.informatics.repository.post;

import ge.freeuni.informatics.common.model.post.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentJpaRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdAndParentIdOrderByCreateDate(Long postId, Long parentId, Pageable pageable);

    Long countByParentId(Long parentId);

    List<PostComment> findByParentIdOrderByCreateDate(Long parentId, Pageable pageable);

    void deleteAllByParentId(Long parentId);

    void deleteAllByPostId(Long postId);
}
