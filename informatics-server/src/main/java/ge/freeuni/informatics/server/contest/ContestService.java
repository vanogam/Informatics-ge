package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.TaskResultDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contest.ScoringType;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubtaskScores;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contest.ContestantResultJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
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

    @Autowired
    private SubmissionJpaRepository submissionRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final Duration SYNC_INTERVAL = Duration.ofSeconds(30);

    private final ConcurrentHashMap<Long, LiveContestState> liveContests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> startSchedules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> endSchedules = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> syncSchedules = new ConcurrentHashMap<>();

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
                .map(ContestService::toFullDTO)
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
                .map(ContestService::toFullDTO)
                .toList();
        scheduleFutureContests(futureContests);
        manageLiveContests(liveContests);
    }

    /**
     * DTO conversion for a contest whose tasks/participants/standings/upsolvingStandings were all
     * just Hibernate.initialize'd by the caller (e.g. the load-flagged findContests overload).
     */
    private static ContestDTO toFullDTO(Contest contest) {
        return ContestDTO.toDTO(contest, contest.getTasks(), contest.getParticipants(),
                contest.getStandings(), contest.getUpsolvingStandings());
    }

    public List<ContestantResultDTO> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        LiveContestState state = liveContests.get(contestId);
        if (state != null) {
            boolean viewerIsAdmin = userManager.isAdmin(userManager.getAuthenticatedUserIdOrAnonymous());
            List<ContestantResultDTO> standings = state.getStandingsSnapshot().stream()
                    .filter(r -> viewerIsAdmin || !userManager.isAdmin(r.contestantId()))
                    .toList();
            return ArrayUtils.getPage(standings, offset, size);
        } else {
            return contestManager.getStandings(contestId, offset, size)
                    .stream()
                    .map(res -> ContestantResultDTO.toDTO(res, getUsername(res.getContestantId())))
                    .toList();
        }
    }

    /**
     * The total number of standings rows {@link #getStandings} would page through, ignoring
     * offset/size - what a pagination control needs to compute the last page.
     */
    public long getStandingsCount(long contestId) throws InformaticsServerException {
        LiveContestState state = liveContests.get(contestId);
        if (state != null) {
            boolean viewerIsAdmin = userManager.isAdmin(userManager.getAuthenticatedUserIdOrAnonymous());
            return state.getStandingsSnapshot().stream()
                    .filter(r -> viewerIsAdmin || !userManager.isAdmin(r.contestantId()))
                    .count();
        } else {
            return contestManager.getStandingsCount(contestId);
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
        // Admin scores are still recorded - getStandings only hides them from non-admin viewers -
        // so an admin trying a task out shows up in standings for other staff, not for contestants.
        long contestId = submission.getContest().getId();
        LiveContestState state = liveContests.get(contestId);
        if (state == null) {
            addUpsolvingSubmission(submission, event.isRejudged());
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

        // Update in-memory standings only; DB sync happens periodically
        if (event.isRejudged()) {
            state.replayStandings(submission, scoredSubmissionsForReplay(submission));
        } else {
            state.updateStandings(submission);
        }
    }

    /**
     * Every scored submission this contestant has made to this task, oldest first - the input a
     * replay needs.
     *
     * <p>The re-judged submission itself is read from the list rather than from the event, so the
     * replay works from one consistent set of rows; it has just been saved, so the list already
     * carries its new score.
     */
    private List<Submission> scoredSubmissionsForReplay(Submission submission) {
        return submissionRepository.findScoredForReplay(
                submission.getUser().getId(), submission.getTask().getId());
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
        
        // SUBTASK_MAX keeps the contestant's best award on every subtask rather than the best
        // single submission, so it merges vectors instead of comparing totals.
        if (scoringType == ScoringType.SUBTASK_MAX) {
            List<Float> merged = SubtaskScores.merge(
                    existingTaskResult == null ? null : SubtaskScores.parse(existingTaskResult.getSubtaskScores()),
                    SubtaskScores.parse(newTaskResult.getSubtaskScores()));

            // No merge is possible when either side has no breakdown - the first submission for
            // this task, one that never compiled - or when the two vectors disagree on length,
            // which means the task's subtasks were re-configured mid-contest and there is no
            // honest way to line the old entries up with the new. Both fall back on comparing
            // totals, exactly as BEST_SUBMISSION does.
            if (merged != null) {
                float mergedScore = SubtaskScores.total(merged);
                TaskResultDTO mergedResult = new TaskResultDTO(
                        newTaskResult.getTaskCode(),
                        mergedScore,
                        SubtaskScores.format(merged),
                        attempts,
                        // The accumulated score can only rise, so the moment it was reached is
                        // this submission's whenever it added anything at all.
                        mergedScore > initialScore ? successTime : existingTaskResult.getSuccessTime()
                );
                taskResults.put(newTaskResult.getTaskCode(), mergedResult);
                return ContestantResultDTO.builder(currentResult)
                        .totalScore(currentTotalScore + mergedScore - initialScore)
                        .taskResults(taskResults)
                        .build();
            }
            if (existingTaskResult != null && SubtaskScores.parse(newTaskResult.getSubtaskScores()).isEmpty()
                    && !SubtaskScores.parse(existingTaskResult.getSubtaskScores()).isEmpty()) {
                // Static context: the injected `log` belongs to the instance.
                org.slf4j.LoggerFactory.getLogger(ContestService.class).warn(
                        "Submission for task {} carries no subtask breakdown; keeping the accumulated one",
                        newTaskResult.getTaskCode());
            }
        }

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
                    newTaskResult.getSubtaskScores(),
                    attempts,
                    successTime
            );
            newTotalScore = currentTotalScore + newTaskResult.getScore() - initialScore;
        } else {
            // Keep existing score but increment attempts
            taskResultToUse = new TaskResultDTO(
                    newTaskResult.getTaskCode(),
                    existingTaskResult.getScore(),
                    existingTaskResult.getSubtaskScores(),
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

    private void addUpsolvingSubmission(Submission submission, boolean rejudged) throws InformaticsServerException {
        // Loads upsolvingStandings inside its own transaction so the collection is genuinely
        // initialized here, rather than toDTO silently treating an un-fetched association as
        // empty - which would then get saved back and, via orphanRemoval, delete every other
        // contestant's upsolving results for this contest.
        //
        // `standings` is deliberately left untouched (still an uninitialized lazy proxy): a
        // ContestantResult row created for upsolving carries *both* the contest and
        // upsolvingContest FK, so it is (by FK) a member of the `standings` collection too. Were
        // that collection loaded and then saved back containing only the DTO's live-only subset,
        // Hibernate would read the missing upsolving rows as orphaned and delete them - or, the
        // other way around, saving it as null would orphan the live rows instead. Never reading
        // it at all is what keeps this save from touching it either way.
        Contest contest = contestRepository.getById(submission.getContest().getId(), false, false, false, true);
        ContestDTO contestDTO = ContestDTO.toDTO(contest, null, null, null, contest.getUpsolvingStandings());

        if (rejudged) {
            replayTaskIntoStandings(contestDTO.getUpsolvingStandings(), scoredSubmissionsForReplay(submission),
                    submission.getTask().getCode(), submission.getUser().getId(), contestDTO, false);
        } else {
            applySubmissionToStandings(contestDTO.getUpsolvingStandings(), submission, contestDTO, false);
        }

        List<ContestantResult> updatedUpsolvingResults = contestDTO.getUpsolvingStandings().stream()
                .map(res -> ContestantResultDTO.fromDTO(res, contest))
                .toList();
        updatedUpsolvingResults.forEach(r -> r.setUpsolvingContest(contest));

        // contest is a managed entity, and upsolvingStandings has orphanRemoval - Hibernate
        // requires the collection instance it already loaded to be mutated in place (not replaced
        // with a new List), or it treats the original as dereferenced and refuses to flush.
        contest.getUpsolvingStandings().clear();
        contest.getUpsolvingStandings().addAll(updatedUpsolvingResults);

        // Upsolving persists immediately since there's no concurrency concern
        contestRepository.saveAndPublish(contest, eventPublisher);
    }

    private void syncContestStandings(long contestId) {
        LiveContestState state = liveContests.get(contestId);
        if (state == null) return;
        syncContestStandings(contestId, state, false);
    }

    private void syncContestStandings(long contestId, LiveContestState state, boolean force) {
        List<ContestantResultDTO> standings = force
                ? state.getAllStandings()
                : state.getDirtyStandings();
        if (standings == null || standings.isEmpty()) return;

        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        Map<Long, Long> newIds = txTemplate.execute(status -> {
            Contest contestRef = contestRepository.getReferenceById(contestId);
            List<ContestantResult> entities = new ArrayList<>();
            for (ContestantResultDTO dto : standings) {
                entities.add(ContestantResultDTO.fromDTO(dto, contestRef));
            }
            List<ContestantResult> saved = contestantResultJpaRepository.saveAll(entities);

            Map<Long, Long> ids = new HashMap<>();
            for (ContestantResult entity : saved) {
                ids.put(entity.getContestantId(), entity.getId());
            }
            return ids;
        });

        if (newIds != null && !newIds.isEmpty()) {
            state.updateIds(newIds);
        }
        log.info("Synced standings to DB for contest [{}], {} entries", contestId, standings.size());
    }

    private void safeSyncStandings(long contestId) {
        try {
            syncContestStandings(contestId);
        } catch (Exception ex) {
            log.error("Failed to sync standings for contest [{}]: {}", contestId, ex.getMessage(), ex);
        }
    }

    private void scheduleSyncTask(long contestId) {
        cancelSyncSchedule(contestId);
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                () -> safeSyncStandings(contestId),
                Instant.now().plus(SYNC_INTERVAL),
                SYNC_INTERVAL
        );
        if (future != null) {
            syncSchedules.put(contestId, future);
        }
    }

    private void cancelSyncSchedule(long contestId) {
        ScheduledFuture<?> future = syncSchedules.remove(contestId);
        if (future != null) {
            future.cancel(false);
        }
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

    private static void applySubmissionToStandings(Collection<ContestantResultDTO> standings,
                                                    Submission submission,
                                                    ContestDTO contestDTO,
                                                    boolean isLiveContest) {
        ContestantResultDTO contestantResult = findContestantResult(standings, submission.getUser().getId(), contestDTO.getId());
        standings.remove(contestantResult);
        standings.add(foldSubmission(contestantResult, submission, contestDTO, isLiveContest));
    }

    /**
     * Rebuilds one contestant's result for one task by replaying every submission they made to it,
     * in place of folding a single new submission in.
     *
     * <p>Folding is one-directional - a new submission can raise a score but the scoring types
     * never lower one - which is right for a contestant submitting, and wrong after a re-judge:
     * a corrected grader may well have taken points away, and the same submission would otherwise
     * be counted as a second attempt. Replaying from nothing gives the result the contestant would
     * have had if the task had been correct all along.
     *
     * @param submissions every scored submission for this contestant and task, oldest first
     */
    private static void replayTaskIntoStandings(Collection<ContestantResultDTO> standings,
                                                List<Submission> submissions,
                                                String taskCode,
                                                long contestantId,
                                                ContestDTO contestDTO,
                                                boolean isLiveContest) {
        ContestantResultDTO contestantResult = findContestantResult(standings, contestantId, contestDTO.getId());
        standings.remove(contestantResult);

        // Clear the task away first - including its share of the total - so the replay builds it
        // up from nothing rather than on top of the figures it is replacing.
        TaskResultDTO stale = contestantResult.getTaskResult(taskCode);
        Map<String, TaskResultDTO> withoutTask = contestantResult.taskResults() != null
                ? new HashMap<>(contestantResult.taskResults())
                : new HashMap<>();
        withoutTask.remove(taskCode);
        float totalWithoutTask = (contestantResult.totalScore() != null ? contestantResult.totalScore() : 0f)
                - (stale != null && stale.getScore() != null ? stale.getScore() : 0f);

        ContestantResultDTO rebuilt = ContestantResultDTO.builder(contestantResult)
                .totalScore(totalWithoutTask)
                .taskResults(withoutTask)
                .build();
        for (Submission submission : submissions) {
            rebuilt = foldSubmission(rebuilt, submission, contestDTO, isLiveContest);
        }
        standings.add(rebuilt);
    }

    /**
     * Folds one scored submission into a contestant's result - the single place the scoring types
     * are applied, so a replay and a live submission cannot drift apart.
     */
    private static ContestantResultDTO foldSubmission(ContestantResultDTO contestantResult,
                                                      Submission submission,
                                                      ContestDTO contestDTO,
                                                      boolean isLiveContest) {
        TaskResultDTO existingTaskResult = contestantResult.getTaskResult(submission.getTask().getCode());

        Long successTime;
        if (isLiveContest) {
            // Under SUBTASK_MAX the submission's own total says nothing about whether the
            // contestant improved - a submission scoring less than the accumulated total can
            // still win a subtask. So hand over this submission's time and let the merge decide
            // whether it is the moment the standing total was reached.
            successTime = contestDTO.getScoringType() == ScoringType.SUBTASK_MAX
                    ? submission.getSubmissionTime().getTime() - contestDTO.getStartDate().getTime()
                    : getSuccessTime(submission, existingTaskResult, contestDTO);
        } else {
            successTime = submission.getSubmissionTime().getTime();
        }

        TaskResultDTO newTaskResult = new TaskResultDTO(
                submission.getTask().getCode(),
                submission.getScore(),
                submission.getSubtaskScores(),
                existingTaskResult != null ? existingTaskResult.getAttempts() + 1 : 1,
                successTime
        );

        return updateTaskResult(
                contestantResult,
                newTaskResult,
                contestDTO.getScoringType(),
                successTime
        );
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
            scheduleSyncTask(contest.getId());
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
        cancelSyncSchedule(contestId);
        LiveContestState state = liveContests.remove(contestId);
        cancelEndSchedule(contestId);
        if (state == null) {
            return;
        }
        try {
            // Final sync of standings directly to contestant_result table
            syncContestStandings(contestId, state, true);

            // Update contest metadata (upsolving flag) if needed
            ContestDTO snapshot = state.snapshot();
            if (snapshot.isUpsolvingAfterFinish()) {
                TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                txTemplate.executeWithoutResult(status -> {
                    Contest contest = contestRepository.getReferenceById(contestId);
                    contest.setUpsolving(true);
                    contestRepository.save(contest);
                });
            }
            log.info("Contest [{}] has been finished.", contestId);
        } catch (Exception ex) {
            log.error("Failed to finish contest [{}]: {}", contestId, ex.getMessage(), ex);
        }
    }

    private void deactivateContest(long contestId) {
        liveContests.remove(contestId);
        cancelStartSchedule(contestId);
        cancelEndSchedule(contestId);
        cancelSyncSchedule(contestId);
    }

    private void activateContest(ContestDTO contest) {
        contest.setStatus(ContestStatus.LIVE);
        LiveContestState state = new LiveContestState(contest);
        liveContests.put(contest.getId(), state);
        scheduleSyncTask(contest.getId());
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
        private volatile boolean dirty = false;

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

        void updateStandings(Submission submission) {
            lock.writeLock().lock();
            try {
                ensureStandingsInitialized();
                ContestService.applySubmissionToStandings(contest.getStandings(), submission, contest, true);
                dirty = true;
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Rebuilds the re-judged submission's task from its whole history, under the same write
         * lock an ordinary update takes, so a standings snapshot never observes the half-cleared
         * row the replay starts from.
         */
        void replayStandings(Submission submission, List<Submission> submissionsForTask) {
            lock.writeLock().lock();
            try {
                ensureStandingsInitialized();
                ContestService.replayTaskIntoStandings(contest.getStandings(), submissionsForTask,
                        submission.getTask().getCode(), submission.getUser().getId(), contest, true);
                dirty = true;
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Returns a copy of standings if dirty, resetting the flag atomically.
         * Returns null if standings have not changed since last sync.
         */
        List<ContestantResultDTO> getDirtyStandings() {
            lock.writeLock().lock();
            try {
                if (!dirty) return null;
                dirty = false;
                return new ArrayList<>(contest.getStandings());
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Returns a copy of all standings regardless of dirty flag (for final sync).
         */
        List<ContestantResultDTO> getAllStandings() {
            lock.writeLock().lock();
            try {
                dirty = false;
                if (contest.getStandings() == null || contest.getStandings().isEmpty()) {
                    return null;
                }
                return new ArrayList<>(contest.getStandings());
            } finally {
                lock.writeLock().unlock();
            }
        }

        /**
         * Updates entity IDs in the in-memory standings after DB sync.
         */
        void updateIds(Map<Long, Long> contestantIdToEntityId) {
            lock.writeLock().lock();
            try {
                TreeSet<ContestantResultDTO> updated = new TreeSet<>();
                for (ContestantResultDTO dto : contest.getStandings()) {
                    Long entityId = contestantIdToEntityId.get(dto.contestantId());
                    if (entityId != null && !entityId.equals(dto.id())) {
                        updated.add(ContestantResultDTO.builder(dto)
                                .id(entityId)
                                .build());
                    } else {
                        updated.add(dto);
                    }
                }
                contest.setStandings(updated);
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
