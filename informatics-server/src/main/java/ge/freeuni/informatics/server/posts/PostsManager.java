package ge.freeuni.informatics.server.posts;

import ge.freeuni.informatics.common.dto.PostCommentDTO;
import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.post.Post;
import ge.freeuni.informatics.common.model.post.PostComment;
import ge.freeuni.informatics.common.model.post.PostStatus;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.post.CommentJpaRepository;
import ge.freeuni.informatics.repository.post.PostJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.server.annotation.PostAuthorRestricted;
import ge.freeuni.informatics.server.annotation.RoomMemberRestricted;
import ge.freeuni.informatics.server.annotation.RoomTeacherRestricted;
import ge.freeuni.informatics.server.user.IUserManager;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class PostsManager implements IPostsManager {

    private static final Logger log = LoggerFactory.getLogger(PostsManager.class);

    @Autowired
    PostJpaRepository postRepository;

    @Autowired
    CommentJpaRepository commentRepository;

    @Autowired
    ContestRoomJpaRepository contestRoomRepository;

    @Autowired
    IUserManager userManager;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Override
    public PostDTO getPost(long postId) throws InformaticsServerException {
        Post post = postRepository.getReferenceById(postId);
        if (post.getStatus() == PostStatus.DRAFT && !Objects.equals(post.getAuthor().getId(), userManager.getAuthenticatedUser().id())) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }

        ContestRoom contestRoom = contestRoomRepository.getReferenceById(post.getRoomId());
        if (!contestRoom.isMember(userManager.getAuthenticatedUser().id())) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        return PostDTO.toDTO(post);
    }

    @Override
    @RoomMemberRestricted
    public List<PostDTO> getPosts(long roomId, Integer pageSize, Integer pageNum) throws InformaticsServerException {
        return postRepository.getPostsByRoomIdAndStatus(roomId, PostStatus.PUBLISHED, Pageable.ofSize(pageSize).withPage(pageNum))
                .stream()
                .map(PostDTO::toDTO)
                .toList();
    }

    @Override
    @RoomTeacherRestricted
    @PostAuthorRestricted
    public PostDTO addPostDraft(long roomId, PostDTO postDTO) throws InformaticsServerException {
        Post post;
        if (postDTO.id() != null) {
            post = postRepository.getReferenceById(postDTO.id());
            if (!Objects.equals(post.getRoomId(), roomId)) {
                throw InformaticsServerException.ROOM_MISMATCH;
            }
            post.setDraftContent(postDTO.draftContent());
            post.setVersion(postDTO.version() + 1);
        } else {
            post = postRepository.getPostsByRoomIdAndAuthor_IdAndStatus(roomId, userManager.getAuthenticatedUser().id(), PostStatus.DRAFT, Pageable.ofSize(1))
                    .stream()
                    .findFirst().orElse(null);
            if (post == null) {
                post = new Post();
                post.setCreateDate(new Date());
                post.setVersion(1L);
                post.setAuthor(userJpaRepository.getReferenceById(userManager.getAuthenticatedUser().id()));
                post.setStatus(PostStatus.DRAFT);
                post.setRoomId(roomId);
            } else {
                return PostDTO.toDTO(post);
            }
        }
        post.setLastUpdateDate(new Date());
        post = postRepository.save(post);
        return PostDTO.toDTO(post);
    }

    @Override
    @PostAuthorRestricted
    public void savePost(PostDTO postDTO) throws InformaticsServerException {
        Post post = postRepository.getReferenceById(postDTO.id());
        if (!Objects.equals(post.getRoomId(), postDTO.roomId())) {
            throw new InformaticsServerException("roomIdMismatch");
        }
        if (post.getAuthor().getId() != userManager.getAuthenticatedUser().id()) {
            log.error("User {} is not the author of post {}", userManager.getAuthenticatedUser().id(), postDTO.id());
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        post.setTitle(postDTO.title());
        post.setContent(postDTO.content());
        post.setStatus(postDTO.status());
        post.setLastUpdateDate(new Date());
        post.setVersion(postDTO.version() + 1);
        postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(long postId) throws InformaticsServerException {
        if (!postRepository.getReferenceById(postId).getAuthor().getId().equals(userManager.getAuthenticatedUser().id())) {
            log.error("User {} is not the author of post {}", userManager.getAuthenticatedUser().id(), postId);
            throw new InformaticsServerException("permissionDenied");
        }
        try {
            commentRepository.deleteAllByPostId(postId);
            postRepository.deleteById(postId);
        } catch (Exception e) {
            log.error("Post with id {} not found", postId, e);
            throw InformaticsServerException.POST_NOT_FOUND;
        }

    }

    @Override
    @Transactional
    public void addComment(long postId, String commentText, Long parentId) throws InformaticsServerException {
        Post post;
        try {
            post = postRepository.getReferenceById(postId);
            if (parentId != null) {
                if (commentRepository.getReferenceById(parentId).getParentId() != null) {
                    log.error("Parent comment with id {} is not a head comment", parentId);
                    throw new InformaticsServerException("parentCommentNotHead");
                }
            }
        } catch (EntityNotFoundException e) {
            log.error("Post with id {} not found", postId, e);
            throw new InformaticsServerException("postNotFound", e);
        }

        PostComment comment = new PostComment();
        comment.setComment(commentText);
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setCreateDate(new Date());
        comment.setLastUpdateDate(new Date());
        comment.setAuthorId(userManager.getAuthenticatedUser().id());
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(long commentId) throws InformaticsServerException {
        try {
            PostComment comment = commentRepository.getReferenceById(commentId);
            if (!comment.getAuthorId().equals(userManager.getAuthenticatedUser().id()) &&
                postRepository.getReferenceById(comment.getPostId()).getAuthor().getId().equals(userManager.getAuthenticatedUser().id())
            ) {
                log.error("User {} is not the author of comment {}", userManager.getAuthenticatedUser().id(), commentId);
                throw new InformaticsServerException("permissionDenied");
            }
            commentRepository.deleteAllByParentId(commentId);
            commentRepository.deleteById(commentId);
        } catch (Exception e) {
            log.error("Comment with id {} not found", commentId, e);
            throw InformaticsServerException.COMMENT_NOT_FOUND;
        }
    }

    @Override
    public List<PostCommentDTO> getHeadComments(long postId, int pageNum, int pageSize) {
        try {
            return commentRepository.findByPostIdAndParentIdOrderByCreateDate(postId, null,
                    Pageable.ofSize(pageSize)
                            .withPage(pageNum)
            ).stream().map(comment -> PostCommentDTO.toDTO(
                    comment,
                    userManager.getUser(comment.getAuthorId()).getUsername(),
                    commentRepository.countByParentId(comment.getId())
            )).toList();
        } catch (Exception e) {
            log.error("Unexpected exception while loading comments for post {}", postId, e);
            return List.of();
        }
    }

    @Override
    public List<PostCommentDTO> getChildComments(long commentId, int pageNum, int pageSize) {
        try {
            return commentRepository.findByParentIdOrderByCreateDate(commentId, Pageable.ofSize(pageSize).withPage(pageNum))
                    .stream()
                    .map(comment -> PostCommentDTO.toDTO(
                            comment,
                            userManager.getUser(comment.getAuthorId()).getUsername(),
                            0
                    )).toList();
        } catch (Exception e) {
            log.error("Unexpected exception while loading child comments for comment {}", commentId, e);
            return List.of();
        }
    }
}
