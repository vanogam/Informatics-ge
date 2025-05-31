package ge.freeuni.informatics.server.posts;

import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.post.Post;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.post.IPostRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class PostsManager implements IPostsManager {

    @Autowired
    IPostRepository postRepository;

    @Autowired
    ContestRoomJpaRepository roomRepository;

    @Autowired
    IUserManager userManager;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Override
    public PostDTO getPost(long postId) throws InformaticsServerException {
        Post post = postRepository.getPost(postId);
        ContestRoom room = roomRepository.getReferenceById(post.getRoomId());

        Long userId = userManager.getAuthenticatedUser().id();
        if (!room.isMember(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getUsername(),
                post.getPostDate(),
                post.getRoomId()
        );
    }

    @Override
    public List<PostDTO> getPosts(long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        ContestRoom room = roomRepository.getReferenceById(roomId);
        Long userId = userManager.getAuthenticatedUser().id();

        if (!room.isMember(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
        return postRepository.filter(roomId, offset, limit)
                .stream()
                .map(post ->
                    new PostDTO(
                            post.getId(),
                            post.getTitle(),
                            post.getContent(),
                            post.getAuthor().getUsername(),
                            post.getPostDate(),
                            post.getRoomId()
                    )
                )
                .toList();
    }

    @Override
    public Long addPost(PostDTO post) throws InformaticsServerException {
        Post postEntity = PostDTO.fromDTO(post);

        postEntity.setAuthor(userJpaRepository.getReferenceById(userManager.getAuthenticatedUser().id()));
        postEntity.setPostDate(new Date());
        ContestRoom room = roomRepository.getReferenceById(post.roomId());
        long userId = userManager.getAuthenticatedUser().id();
        if (room.getTeachers().stream().map(User::getId)
                .noneMatch(id -> userId == id)) {
            throw new InformaticsServerException("permissionDenied");
        }
        if (post.id() != null) {
            Post entityAtDB = postRepository.getPost(post.id());

            if (entityAtDB != null) {
                if (!Objects.equals(entityAtDB.getRoomId(), postEntity.getRoomId())) {
                    throw new InformaticsServerException("invalidAction");
                }
            } else {
                throw new InformaticsServerException("notFound");
            }
        }
        postEntity = postRepository.savePost(postEntity);
        return postEntity.getId();
    }
}
