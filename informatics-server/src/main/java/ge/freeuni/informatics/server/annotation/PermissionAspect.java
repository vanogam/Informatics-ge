package ge.freeuni.informatics.server.annotation;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ContestJpaRepository contestJpaRepository;

    @Autowired
    private IUserManager userManager;

    @Autowired
    private ContestRoomJpaRepository roomJpaRepository;

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
}