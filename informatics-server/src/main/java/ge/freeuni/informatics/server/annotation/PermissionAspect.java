package ge.freeuni.informatics.server.annotation;

import ge.freeuni.informatics.common.dto.PostDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.post.Post;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.post.PostJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    Logger log;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ContestJpaRepository contestJpaRepository;

    @Autowired
    private IUserManager userManager;

    @Autowired
    private ContestRoomJpaRepository roomJpaRepository;

    @Autowired
    private PostJpaRepository postJpaRepository;



    @Before("@annotation(teacherTaskRestricted) && args(taskId,..)")
    public void teacherRestricted(TeacherTaskRestricted teacherTaskRestricted, Long taskId) throws InformaticsServerException {
        Long userId = userManager.getAuthenticatedUser().id();
        Task task = taskRepository.getReferenceById(taskId);
        Contest contest = task.getContest();
        ContestRoom room = roomJpaRepository.getReferenceById(contest.getRoomId());

        if (!room.isOpen() && !room.isTeacher(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
    }

    @Before("@annotation(teacherContestRestricted) && args(contestId,..)")
    public void teacherRestrictedContest(TeacherContestRestricted teacherContestRestricted, Long contestId) throws InformaticsServerException {
        Long userId = userManager.getAuthenticatedUser().id();
        Contest contest = contestJpaRepository.getReferenceById(contestId);
        ContestRoom room = roomJpaRepository.getReferenceById(contest.getRoomId());

        if (!room.isOpen() && !room.isTeacher(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
    }

    @Before("@annotation(memberTaskRestricted) && args(taskId,..)")
    public void memberRestricted(MemberTaskRestricted memberTaskRestricted, Long taskId) throws InformaticsServerException {
        Long userId = userManager.getAuthenticatedUser().id();
        var task = taskRepository.getReferenceById(taskId);
        var contest = task.getContest();
        var room = roomJpaRepository.getReferenceById(contest.getRoomId());

        if (!room.isOpen() && !room.isMember(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
    }

    @Before("@annotation(memberContestRestricted) && args(contestId,..)")
    public void memberRestrictedContest(MemberContestRestricted memberContestRestricted, Long contestId) throws InformaticsServerException {
        Long userId = userManager.getAuthenticatedUser().id();
        Contest contest = contestJpaRepository.getReferenceById(contestId);
        ContestRoom room = roomJpaRepository.getReferenceById(contest.getRoomId());

        if (!room.isOpen() && !room.isMember(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
    }

    @Before("@annotation(roomMemberRestricted) && args(roomId,..)")
    public void roomMemberRestricted(RoomMemberRestricted roomMemberRestricted, Long roomId) throws InformaticsServerException {
        ContestRoom room = roomJpaRepository.getReferenceById(roomId);
        if (!room.isOpen()) {
            Long userId = userManager.getAuthenticatedUser().id();
            if (!room.isMember(userId)) {
                throw new InformaticsServerException("permissionDenied");
            }

        }
    }

    @Before("@annotation(roomTeacherRestricted) && args(roomId,..)")
    public void roomTeacherRestricted(RoomTeacherRestricted roomTeacherRestricted, Long roomId) throws InformaticsServerException {
        Long userId = userManager.getAuthenticatedUser().id();
        ContestRoom room = roomJpaRepository.getReferenceById(roomId);

        if (!room.isOpen() && !room.isTeacher(userId)) {
            throw new InformaticsServerException("permissionDenied");
        }
    }

    @Before("@annotation(postAuthorRestricted) && args(postDTO,..)")
    public void postAuthorRestricted(PostAuthorRestricted postAuthorRestricted, PostDTO postDTO) throws InformaticsServerException {
        if (postDTO == null) {
            return;
        }
        Post post;
        try {
            post = postJpaRepository.getReferenceById(postDTO.id());
        } catch (Exception e) {
            log.error("Post with id {} not found", postDTO.id(), e);
            throw InformaticsServerException.POST_NOT_FOUND;
        }
        Long userId = userManager.getAuthenticatedUser().id();
        if (!post.getAuthor().getId().equals(userId)) {
            log.error("User {} is not the author of post {}", userManager.getAuthenticatedUser().id(), postDTO.id());
            throw InformaticsServerException.PERMISSION_DENIED;
        }
    }

    @Before("@annotation(postIdAuthorRestricted) && args(postId,..)")
    public void postAuthorRestricted(PostIdAuthorRestricted postIdAuthorRestricted, Long postId) throws InformaticsServerException {
        Post post;
        try {
            post = postJpaRepository.getReferenceById(postId);
        } catch (Exception e) {
            log.error("Post with id {} not found", postId, e);
            throw InformaticsServerException.POST_NOT_FOUND;
        }
        Long userId = userManager.getAuthenticatedUser().id();
        if (!post.getAuthor().getId().equals(userId)) {
            log.error("User {} is not the author of post {}", userManager.getAuthenticatedUser().id(), postId);
            throw InformaticsServerException.PERMISSION_DENIED;
        }
    }
}