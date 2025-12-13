package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.TaskResultDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ScoringType;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contest.ContestantResultJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
@Scope("singleton")
public class ContestService {

    @Autowired
    Logger log;

    @Autowired
    private IContestManager contestManager;

    @Autowired
    private IUserManager userManager;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ContestJpaRepository contestRepository;

    @Autowired
    private ContestRoomJpaRepository contestRoomJpaRepository;

    @Autowired
    private ContestantResultJpaRepository contestantResultJpaRepository;

    private final ConcurrentHashMap<Long, LiveContestState> liveContests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> startSchedules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> endSchedules = new ConcurrentHashMap<>();

    @PostConstruct
    public void startup() {
        List<ContestDTO> futureContests = contestRepository.findContests(null,
                        null,
                        new Date(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        true,
                        true,
                        true)
                .stream()
                .map(ContestDTO::toDTO)
                .toList();
        List<ContestDTO> liveContests = contestRepository.findContests(null,
                        null,
                        null,
                        new Date(),
                        new Date(),
                        null,
                        null,
                        null,
                        true,
                        true,
                        true,
                        true)
                .stream()
                .filter(contest -> contest.getStartDate() != null)
                .map(ContestDTO::toDTO)
                .toList();
        scheduleFutureContests(futureContests);
        manageLiveContests(liveContests);
    }

    public List<ContestantResultDTO> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        LiveContestState state = liveContests.get(contestId);
        if (state != null) {
            return ArrayUtils.getPage(state.getStandingsSnapshot(), offset, size);
        } else {
            return contestManager.getStandings(contestId, offset, size)
                    .stream()
                    .map(res -> ContestantResultDTO.toDTO(res, getUsername(res.getContestantId())))
                    .toList();
        }
    }

    public String getUsername(Long userId) {
        User user = userManager.getUser(userId);
        if (user == null) {
            return null;
        }
        return user.getUsername();
    }

    public List<Long> getLiveContests() {
        return new ArrayList<>(liveContests.keySet());
    }

    private void scheduleFutureContests(List<ContestDTO> futureContests) {
        futureContests.forEach(this::scheduleContestStart);
    }

    private void manageLiveContests(List<ContestDTO> contests) {
        contests.forEach(this::activateContest);
    }

    private void scheduleContestStart(ContestDTO contest) {
        cancelStartSchedule(contest.getId());
        Date startDate = contest.getStartDate();
        if (startDate == null) {
            return;
        }
        Instant startInstant = startDate.toInstant();
        Runnable launch = () -> safeContestStart(contest.getId());
        if (Instant.now().isAfter(startInstant)) {
            launch.run();
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(launch, startInstant);
        if (future != null) {
            startSchedules.put(contest.getId(), future);
        }
    }

    private void scheduleContestEnd(ContestDTO contest) {
        cancelEndSchedule(contest.getId());
        Date endDate = contest.getEndDate();
        if (endDate == null) {
            return;
        }
        Runnable finish = () -> safeContestEnd(contest.getId());
        Instant endInstant = endDate.toInstant();
        if (Instant.now().isAfter(endInstant)) {
            finish.run();
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(finish, endInstant);
        if (future != null) {
            endSchedules.put(contest.getId(), future);
        }
    }

    @EventListener
    public void addSubmission(SubmissionEvent event) throws InformaticsServerException {
        Submission submission = (Submission) event.getSource();
        long contestId = submission.getContest().getId();
        LiveContestState state = liveContests.get(contestId);
        if (state == null) {
            addUpsolvingSubmission(submission);
            return;
        }

        Long roomId = state.getContestRoomId();
        if (roomId == null) {
            log.warn("Contest [{}] has no associated roomId; rejecting submission {}", contestId, submission.getId());
            throw InformaticsServerException.PERMISSION_DENIED;
        }

        ContestRoom room = contestRoomJpaRepository.getReferenceById(roomId);
        if (!room.isMember(submission.getUser().getId())) {
            log.info("User {} is not a member of contest room {}", submission.getUser().getId(), room.getId());
            throw InformaticsServerException.PERMISSION_DENIED;
        }

        ContestDTO snapshotForPersistence = state.updateStandings(submission);
        ContestDTO persisted = contestManager.updateContest(snapshotForPersistence);
        if (persisted != null) {
            state.refresh(persisted);
        }
    }

    private static Long getSuccessTime(Submission submission, TaskResultDTO taskResult, ContestDTO contestDTO) {
        Long newTime = submission.getSubmissionTime().getTime() - contestDTO.getStartDate().getTime();
        if (taskResult == null) {
            return newTime;
        }
        if (taskResult.getScore() < submission.getScore()) {
            return newTime;
        }
        if (taskResult.getScore().equals(submission.getScore())) {
            return Math.min(newTime, taskResult.getSuccessTime());
        }
        return taskResult.getSuccessTime();
    }

    private static ContestantResultDTO updateTaskResult(
            ContestantResultDTO currentResult,
            TaskResultDTO newTaskResult,
            ScoringType scoringType,
            Long successTime) {
        
        Map<String, TaskResultDTO> taskResults = currentResult.taskResults() != null 
                ? new HashMap<>(currentResult.taskResults()) 
                : new HashMap<>();
        
        Float currentTotalScore = currentResult.totalScore() != null ? currentResult.totalScore() : 0f;
        
        TaskResultDTO existingTaskResult = taskResults.get(newTaskResult.getTaskCode());
        float initialScore = existingTaskResult != null ? existingTaskResult.getScore() : 0f;
        
        // Always increment attempts
        int attempts = existingTaskResult != null ? existingTaskResult.getAttempts() + 1 : 1;
        
        // Determine if we should update the score based on ScoringType
        boolean shouldUpdate = scoringType == ScoringType.LAST_SUBMISSION 
                || newTaskResult.getScore() > initialScore 
                || initialScore == 0;
        
        TaskResultDTO taskResultToUse;
        Float newTotalScore;
        
        if (shouldUpdate) {
            // Use new score
            taskResultToUse = new TaskResultDTO(
                    newTaskResult.getTaskCode(),
                    newTaskResult.getScore(),
                    attempts,
                    successTime
            );
            newTotalScore = currentTotalScore + newTaskResult.getScore() - initialScore;
        } else {
            // Keep existing score but increment attempts
            taskResultToUse = new TaskResultDTO(
                    newTaskResult.getTaskCode(),
                    existingTaskResult.getScore(),
                    attempts,
                    existingTaskResult.getSuccessTime()
            );
            newTotalScore = currentTotalScore;
        }
        
        taskResults.put(newTaskResult.getTaskCode(), taskResultToUse);
        
        return ContestantResultDTO.builder(currentResult)
                .totalScore(newTotalScore)
                .taskResults(taskResults)
                .build();
    }

    private void addUpsolvingSubmission(Submission submission) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(submission.getContest().getId());
        ContestDTO contestDTO = ContestDTO.toDTO(contest);
        
        if (contestDTO.getUpsolvingStandings() == null) {
            contestDTO.setUpsolvingStandings(new ArrayList<>());
        }
        
        updateStandings(contestDTO.getUpsolvingStandings(), submission, contestDTO, false, contestantResultJpaRepository, contestRepository);
        
        contestManager.updateContest(contestDTO);
    }

    private static ContestantResultDTO findContestantResult(Collection<ContestantResultDTO> standings, long userId, long contestId) {
        return standings
                .stream()
                .filter(result -> Objects.equals(result.contestantId(), userId))
                .findFirst()
                .orElseGet(() -> ContestantResultDTO.builder()
                        .contestantId(userId)
                        .contestId(contestId)
                        .totalScore(0f)
                        .taskResults(new HashMap<>())
                        .build());
    }

    private static void updateStandings(Collection<ContestantResultDTO> standings,
                                        Submission submission,
                                        ContestDTO contestDTO,
                                        boolean isLiveContest,
                                        ContestantResultJpaRepository contestantResultJpaRepository,
                                        ContestJpaRepository contestRepository) {
        ContestantResultDTO contestantResult = findContestantResult(standings, submission.getUser().getId(), contestDTO.getId());
        standings.remove(contestantResult);

        TaskResultDTO existingTaskResult = contestantResult.getTaskResult(submission.getTask().getCode());
        
        // Calculate success time (relative for live contests, absolute for upsolving)
        Long successTime;
        if (isLiveContest) {
            successTime = getSuccessTime(submission, existingTaskResult, contestDTO);
        } else {
            successTime = submission.getSubmissionTime().getTime();
        }
        
        // Create new TaskResultDTO for the submission
        TaskResultDTO newTaskResult = new TaskResultDTO(
                submission.getTask().getCode(),
                submission.getScore(),
                existingTaskResult != null ? existingTaskResult.getAttempts() + 1 : 1,
                successTime
        );
        
        // Use the static method to update task result based on ScoringType
        ContestantResultDTO updatedContestantResult = updateTaskResult(
                contestantResult,
                newTaskResult,
                contestDTO.getScoringType(),
                successTime
        );
        Contest contest = contestRepository.getReferenceById(submission.getContest().getId());
        contestantResultJpaRepository.save(ContestantResultDTO.fromDTO(updatedContestantResult, contest));
        standings.add(updatedContestantResult);
    }

    @EventListener
    public void changeContest(ContestChangeEvent event) {
        ContestDTO contest = (ContestDTO) event.getSource();
        if (contest.getStatus() == ContestStatus.FUTURE) {
            deactivateContest(contest.getId());
            scheduleContestStart(contest);
            return;
        }
        if (contest.getStatus() == ContestStatus.LIVE) {
            LiveContestState state = liveContests.computeIfAbsent(contest.getId(),
                    id -> new LiveContestState(contest));
            state.merge(contest);
            scheduleContestEnd(contest);
            cancelStartSchedule(contest.getId());
            return;
        }
        if (contest.getStatus() == ContestStatus.PAST) {
            deactivateContest(contest.getId());
        }
    }

    private void safeContestStart(long contestId) {
        cancelStartSchedule(contestId);
        try {
            Contest contest = contestRepository.getReferenceById(contestId);
            ContestDTO contestDTO = ContestDTO.toDTO(contest);
            contestDTO.setStatus(ContestStatus.LIVE);
            ContestDTO activated = contestManager.updateContest(contestDTO);
            activateContest(activated);
            log.info("Contest [{}] has been started.", contestId);
        } catch (Exception ex) {
            log.error("Failed to start contest [{}]: {}", contestId, ex.getMessage(), ex);
        }
    }

    private void safeContestEnd(long contestId) {
        LiveContestState state = liveContests.remove(contestId);
        cancelEndSchedule(contestId);
        if (state == null) {
            return;
        }
        try {
            ContestDTO snapshot = state.snapshot();
            snapshot.setStatus(ContestStatus.PAST);
            if (snapshot.isUpsolvingAfterFinish()) {
                snapshot.setUpsolving(true);
            }
            contestManager.updateContest(snapshot);
            log.info("Contest [{}] has been finished.", contestId);
        } catch (Exception ex) {
            log.error("Failed to finish contest [{}]: {}", contestId, ex.getMessage(), ex);
        }
    }

    private void deactivateContest(long contestId) {
        liveContests.remove(contestId);
        cancelStartSchedule(contestId);
        cancelEndSchedule(contestId);
    }

    private void activateContest(ContestDTO contest) {
        contest.setStatus(ContestStatus.LIVE);
        LiveContestState state = new LiveContestState(contest);
        liveContests.put(contest.getId(), state);
        scheduleContestEnd(contest);
    }

    private void cancelStartSchedule(long contestId) {
        ScheduledFuture<?> future = startSchedules.remove(contestId);
        if (future != null) {
            future.cancel(false);
        }
    }

    private void cancelEndSchedule(long contestId) {
        ScheduledFuture<?> future = endSchedules.remove(contestId);
        if (future != null) {
            future.cancel(false);
        }
    }

    private static ContestDTO copyContest(ContestDTO source) {
        ContestDTO copy = new ContestDTO();
        copy.setId(source.getId());
        copy.setName(source.getName());
        copy.setRoomId(source.getRoomId());
        copy.setStartDate(source.getStartDate() == null ? null : new Date(source.getStartDate().getTime()));
        copy.setEndDate(source.getEndDate() == null ? null : new Date(source.getEndDate().getTime()));
        copy.setStatus(source.getStatus());
        copy.setTasks(source.getTasks() == null ? null : new ArrayList<>(source.getTasks()));
        copy.setParticipants(source.getParticipants() == null ? null : new ArrayList<>(source.getParticipants()));
        copy.setScoringType(source.getScoringType());
        copy.setUpsolving(source.isUpsolving());
        copy.setUpsolvingAfterFinish(source.isUpsolvingAfterFinish());
        copy.setVersion(source.getVersion());

        if (source.getStandings() != null) {
            TreeSet<ContestantResultDTO> standings = source.getStandings()
                    .stream()
                    .map(ContestantResultDTO::builder)
                    .map(ContestantResultDTO.Builder::build)
                    .collect(Collectors.toCollection(TreeSet::new));
            copy.setStandings(standings);
        } else {
            copy.setStandings(new TreeSet<>());
        }

        if (source.getUpsolvingStandings() != null) {
            copy.setUpsolvingStandings(source.getUpsolvingStandings()
                    .stream()
                    .map(ContestantResultDTO::builder)
                    .map(ContestantResultDTO.Builder::build)
                    .collect(Collectors.toList()));
        } else {
            copy.setUpsolvingStandings(new ArrayList<>());
        }
        return copy;
    }


    private class LiveContestState {
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        private ContestDTO contest;

        LiveContestState(ContestDTO contest) {
            this.contest = copyContest(contest);
        }

        ContestDTO snapshot() {
            lock.readLock().lock();
            try {
                return copyContest(contest);
            } finally {
                lock.readLock().unlock();
            }
        }

        List<ContestantResultDTO> getStandingsSnapshot() {
            lock.readLock().lock();
            try {
                return contest.getStandings()
                        .stream()
                        .map(ContestantResultDTO::builder)
                        .map(ContestantResultDTO.Builder::build)
                        .toList();
            } finally {
                lock.readLock().unlock();
            }
        }

        ContestDTO updateStandings(Submission submission) {
            lock.writeLock().lock();
            try {
                ensureStandingsInitialized();
                ContestService.updateStandings(contest.getStandings(), submission, contest, true, contestantResultJpaRepository, contestRepository);
                return copyContest(contest);
            } finally {
                lock.writeLock().unlock();
            }
        }

        void refresh(ContestDTO snapshot) {
            lock.writeLock().lock();
            try {
                this.contest = copyContest(snapshot);
            } finally {
                lock.writeLock().unlock();
            }
        }


        private void merge(ContestDTO snapshot) {
            lock.writeLock().lock();
            try {
                ContestDTO merged = copyContest(snapshot);
                if ((merged.getStandings() == null || merged.getStandings().isEmpty())
                        && contest.getStandings() != null) {
                    merged.setStandings(contest.getStandings()
                            .stream()
                            .map(ContestantResultDTO::builder)
                            .map(ContestantResultDTO.Builder::build)
                            .collect(Collectors.toCollection(TreeSet::new)));
                }
                if ((merged.getUpsolvingStandings() == null || merged.getUpsolvingStandings().isEmpty())
                        && contest.getUpsolvingStandings() != null) {
                    merged.setUpsolvingStandings(contest.getUpsolvingStandings()
                            .stream()
                            .map(ContestantResultDTO::builder)
                            .map(ContestantResultDTO.Builder::build)
                            .collect(Collectors.toList()));
                }
                this.contest = merged;
            } finally {
                lock.writeLock().unlock();
            }
        }

        Long getContestRoomId() {
            lock.readLock().lock();
            try {
                return contest.getRoomId();
            } finally {
                lock.readLock().unlock();
            }
        }

        private void ensureStandingsInitialized() {
            if (contest.getStandings() == null) {
                contest.setStandings(new TreeSet<>());
            }
        }
    }
}
