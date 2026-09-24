package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.dto.UserSimpleDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.server.annotation.ContestTeacherRestricted;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.server.annotation.MemberTaskRestricted;
import ge.freeuni.informatics.server.annotation.RoomTeacherRestricted;
import ge.freeuni.informatics.server.annotation.TeacherContestRestricted;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.task.ITaskManager;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import org.hibernate.Hibernate;
import org.hibernate.StaleStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class ContestManager implements IContestManager {

    final IContestRoomManager contestRoomManager;

    final IUserManager userManager;

    final ContestJpaRepository contestRepository;

    final ITaskManager taskManager;

    final ApplicationEventPublisher eventPublisher;

    @Autowired
    public ContestManager(IContestRoomManager contestRoomManager, IUserManager userManager, ContestJpaRepository contestRepository, ITaskManager taskManager, ApplicationEventPublisher eventPublisher) {
        this.contestRoomManager = contestRoomManager;
        this.userManager = userManager;
        this.contestRepository = contestRepository;
        this.taskManager = taskManager;
        this.eventPublisher = eventPublisher;
    }

    @RoomTeacherRestricted
    @Override
    public ContestDTO createContest(Long roomId, ContestDTO contestDTO) throws InformaticsServerException {
        // Validations
        if (contestDTO.getId() != null) {
            throw InformaticsServerException.CONTEST_ID_SHOULD_NOT_BE_PROVIDED;
        }
        validateContest(contestDTO);
        if (contestDTO.getRoomId() == null || !contestDTO.getRoomId().equals(roomId)) {
            throw InformaticsServerException.CONTEST_ROOM_ID_MISMATCH;
        }
        
        Contest contest = ContestDTO.fromDTO(contestDTO);
        contest.setStandings(new ArrayList<>());
        contest = contestRepository.saveAndPublish(contest, eventPublisher);
        return ContestDTO.toDTO(contest, contest.getTasks(), contest.getParticipants(),
                contest.getStandings(), contest.getUpsolvingStandings());
    }

    @Override
    public ContestDTO getContest(Long contestId,
                                 boolean loadParticipants,
                                 boolean loadTasks,
                                 boolean loadStandings,
                                 boolean loadUpsolvingStandings
    ) throws InformaticsServerException {
        Contest contest = getContestInternal(contestId,
                loadParticipants,
                loadTasks,
                loadStandings,
                loadUpsolvingStandings
        );
        ContestDTO contestDTO = ContestDTO.toDTO(contest,
                loadTasks ? contest.getTasks() : null,
                loadParticipants ? contest.getParticipants() : null,
                loadStandings ? contest.getStandings() : null,
                loadUpsolvingStandings ? contest.getUpsolvingStandings() : null);
        if (loadUpsolvingStandings) {
            // ContestDTO.toDTO has no username lookup of its own (it lives in informatics-common,
            // with no access to IUserManager), so every contestant comes back with a null
            // username unless resolved here - the same way ContestService.getStandings() does
            // for the live leaderboard.
            contestDTO.setUpsolvingStandings(contest.getUpsolvingStandings()
                    .stream()
                    .map(res -> ContestantResultDTO.toDTO(res, getUsername(res.getContestantId())))
                    .toList());
        }
        return contestDTO;
    }

    private String getUsername(Long userId) {
        User user = userManager.getUser(userId);
        return user == null ? null : user.getUsername();
    }

    @MemberTaskRestricted
    private Contest getContestInternal(Long contestId,
                                       boolean loadParticipants,
                                       boolean loadTasks,
                                       boolean loadStandings,
                                       boolean loadUpsolvingStandings
    ) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        if (loadTasks) {
            Hibernate.initialize(contest.getTasks());
        }
        if (loadParticipants) {
            Hibernate.initialize(contest.getParticipants());
        }
        if (loadStandings) {
            Hibernate.initialize(contest.getStandings());
        }
        if (loadUpsolvingStandings) {
            Hibernate.initialize(contest.getUpsolvingStandings());
        }
        return contest;
    }

    @Override
    public List<ContestDTO> getContests(Long roomId, String name, Boolean upsolving, Date minStartDate, Date maxStartDate,
                                        Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return contestRepository.findContests(roomId, name, minStartDate, maxStartDate, null, null, upsolving, pageable)
                .stream()
                .map(ContestDTO::toDTO)
                .toList();
    }

    @Override
    public ContestDTO updateContest(ContestDTO contestDTO) {
        // fromDTO builds the entity straight from the DTO's own fields, so its collections are
        // plain (already-loaded) lists rather than lazy proxies - safe to read back directly.
        Contest contest = contestRepository.saveAndPublish(ContestDTO.fromDTO(contestDTO), eventPublisher);
        return ContestDTO.toDTO(contest, contest.getTasks(), contest.getParticipants(),
                contest.getStandings(), contest.getUpsolvingStandings());
    }

    @TeacherContestRestricted
    @Override
    public ContestDTO modifyContest(Long contestId, ContestDTO contestDTO) throws InformaticsServerException {
        if (contestDTO.getId() == null || !contestDTO.getId().equals(contestId)) {
            throw InformaticsServerException.CONTEST_ID_MISMATCH;
        }
        validateContest(contestDTO);

        Contest contest = contestRepository.getReferenceById(contestId);
        updateContest(contest, contestDTO);
        contest = contestRepository.saveAndPublish(contest, eventPublisher);
        // Only scalar fields were changed above; the caller doesn't need tasks/participants/
        // standings echoed back on an edit, so they're never touched (they're still lazy here).
        return ContestDTO.toDTO(contest);
    }

    @Override
    @ContestTeacherRestricted
    public void deleteContest(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        List<Task> tasks = contest.getTasks();
        for (Task task : tasks) {
            taskManager.removeTask(task.getId(), contestId);
        }
        contestRepository.deleteById(contestId);
    }

    @Override
    public void registerUser(long contestId) throws InformaticsServerException {
        boolean retry = false;
        int numRetries = 5;
        do {
            Contest contest = contestRepository.getReferenceById(contestId);
            UserDTO user = userManager.getAuthenticatedUser();
            long userId = user.id();
            ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
            if (!room.isMember(userId)) {
                throw InformaticsServerException.PERMISSION_DENIED;
            }
            if (contest.getStatus() == ContestStatus.PAST) {
                throw new InformaticsServerException("contestAlreadyOver");
            }
            if (contest.getParticipants() == null) {
                contest.setParticipants(new ArrayList<>());
            }
            if (contest.getParticipants().stream().noneMatch(u -> u.getId() == userId)) {
                contest.getParticipants().add(userManager.getUser(userId));
                contest.getStandings().add(createContestantResult(contest, userId));
            }
            try {
                contestRepository.saveAndPublish(contest, eventPublisher);
            } catch (StaleStateException | ObjectOptimisticLockingFailureException ignored) {
                retry = true;
            }

        } while (retry && --numRetries > 0);
        if (retry) {
            throw new InformaticsServerException("contestRegistrationFailed");
        }
    }

    private ContestantResult createContestantResult(Contest contest, long userId) {
        ContestantResult contestantResult = new ContestantResult();
        contestantResult.setContest(contest);
        contestantResult.setContestant(userId);
        contestantResult.setTotalScore(0f);
        contestantResult.setTaskResults(new HashMap<>());
        return contestantResult;
    }

    @Override
    public void unregisterUser(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        UserDTO user = userManager.getAuthenticatedUser();
        long userId = user.id();
        if (contest.getParticipants() == null || !contest.hasParticipant(userId)) {
            return;
        }
        if (contest.getStatus() == ContestStatus.LIVE || contest.getStatus() == ContestStatus.PAST) {
            throw new InformaticsServerException("actionNotAvailable");
        }
        // Remove user from participants - use removeIf to avoid concurrent modification
        contest.getParticipants().removeIf(u -> u.getId().equals(userId));
        
        // Remove corresponding ContestantResult from standings - use removeIf for safe removal
        contest.getStandings().removeIf(result -> result.getContestantId().equals(userId));
        
        contestRepository.save(contest);
    }

    /**
     * The full, unpaged standings a viewer is allowed to see for a contest - shared by
     * {@link #getStandings} and {@link #getStandingsCount} so paging and counting can never
     * disagree about which rows are in scope.
     */
    private List<ContestantResult> getFilteredStandings(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        long userId = userManager.getAuthenticatedUserIdOrAnonymous();
        if (!room.isMember(userId)) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        boolean viewerIsAdmin = userManager.isAdmin(userId);
        return contest.getStandings().stream()
                .filter(r -> r.getUpsolvingContest() == null)
                .filter(r -> viewerIsAdmin || !userManager.isAdmin(r.getContestantId()))
                .toList();
    }

    @Override
    public List<ContestantResult> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        return ArrayUtils.getPage(getFilteredStandings(contestId), offset, size);
    }

    @Override
    public long getStandingsCount(long contestId) throws InformaticsServerException {
        return getFilteredStandings(contestId).size();
    }

    @Override
    public boolean isCurrentUserRegistered(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        long userId = userManager.getAuthenticatedUserIdOrAnonymous();
        return contest.getParticipants().stream().map(User::getId).anyMatch(id -> id == userId);
    }

    @Override
    public List<UserSimpleDTO> getRegistrants(long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = contestRoomManager.getRoom(contest.getRoomId());
        if (!room.isOpen() && !room.isMember(userManager.getAuthenticatedUser().id())) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        List<UserSimpleDTO> registrants = new ArrayList<>();
        for (User user : contest.getParticipants()) {
            registrants.add(UserSimpleDTO.toSimpleDTO(user));
        }
        return registrants;
    }

    private void validateContest(ContestDTO contestDTO) throws InformaticsServerException {
        if ((contestDTO.getStartDate() == null && contestDTO.getEndDate() != null)
            || (contestDTO.getStartDate() != null && contestDTO.getEndDate() == null)
        ) {
            throw InformaticsServerException.START_DATE_AND_DURATION_ERROR;
        }
        if (contestDTO.getName() == null || contestDTO.getName().trim().isEmpty()) {
            throw InformaticsServerException.CONTEST_NAME_REQUIRED;
        }
    }

    private void updateContest(Contest contest, ContestDTO contestDTO) {
        contest.setId(contestDTO.getId());
        contest.setName(contestDTO.getName());
        contest.setStartDate(contestDTO.getStartDate());
        contest.setEndDate(contestDTO.getEndDate());
        contest.setUpsolving(contestDTO.isUpsolving());
        contest.setUpsolvingAfterFinished(contestDTO.isUpsolvingAfterFinish());
        contest.setScoringType(contestDTO.getScoringType());
    }
}
