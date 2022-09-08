package ge.freeuni.informatics.repository.post;

import ge.freeuni.informatics.common.model.post.Post;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class PostRepository implements IPostRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public Post savePost(Post post) {
        return em.merge(post);
    }

    @Override
    public Post getPost(long postId) {
        return em.find(Post.class, postId);
    }

    @Override
    public List<Post> filter(Long roomId, Integer offset, Integer limit) {
        TypedQuery<Post> query = em.createQuery("SELECT p FROM Post p WHERE p.roomId = :roomId", Post.class)
                .setParameter("roomId", roomId);
        query.setFirstResult(offset);
        query.setMaxResults(limit);
        return query.getResultList();
    }
}
