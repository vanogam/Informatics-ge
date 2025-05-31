package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.TaskResultDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contest.ContestantResultJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.server.task.ITaskManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope("singleton")
public class ContestService {

    @Autowired
    Logger log;

    @Autowired
    private IContestManager contestManager;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ITaskManager taskManager;

    @Autowired
    private ContestJpaRepository contestRepository;

    @Autowired
    private ContestRoomJpaRepository contestRoomJpaRepository;

    @Autowired
    private ContestantResultJpaRepository contestantResultJpaRepository;

    private final ConcurrentHashMap<Long, ContestDTO> liveContests = new ConcurrentHashMap<>();

    @PostConstruct
    public void startup() {
        List<ContestDTO> futureContests = contestRepository.findContests(null, null, new Date(), null, null, null, null, null)
                .stream()
                .map(ContestDTO::toDTO)
                .toList();
        List<ContestDTO> liveContests = contestRepository.findContests(null, null, null, new Date(), new Date(), null, null, null)
                .stream()
                .map(ContestDTO::toDTO)
                .toList();
        scheduleFutureContests(futureContests);
        manageLiveContests(liveContests);
    }

    public List<ContestantResultDTO> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        if (liveContests.containsKey(contestId)) {
            return ArrayUtils.getPage(liveContests.get(contestId).getStandings().stream().toList(), offset, size);
        } else {
            return contestManager.getStandings(contestId, offset, size)
                    .stream()
                    .map(ContestantResultDTO::toDTO)
                    .toList();
        }
    }

    public List<Long> getLiveContests() {
        return new ArrayList<>(liveContests.keySet());
    }

    private void scheduleFutureContests(List<ContestDTO> futureContests) {
        for (ContestDTO contest : futureContests) {
            scheduleContestStart(contest);
        }
    }

    private void manageLiveContests(List<ContestDTO> futureContests) {
        for (ContestDTO contest : futureContests) {
            liveContests.put(contest.getId(), contest);
            scheduleContestEnd(contest);
        }
    }

    private void scheduleContestStart(ContestDTO contest) {
        if (new Date().after(contest.getStartDate())) {
            new ContestStartThread(contest).run();
        } else {
            taskScheduler.schedule(new ContestStartThread(contest), contest.getStartDate());
        }

    }

    private void scheduleContestEnd(ContestDTO contest) {
        if (new Date().after(contest.getEndDate())) {
            new ContestEndThread(contest).run();
        } else {
            taskScheduler.schedule(new ContestEndThread(contest), contest.getEndDate());
        }
    }

    @EventListener
    public void addSubmission(SubmissionEvent event) throws InformaticsServerException {
        Submission submission = (Submission) event.getSource();
        ContestDTO contest = liveContests.get(submission.getContest().getId());
        ContestRoom room = contestRoomJpaRepository.getReferenceById(contest.getId());
        if (!room.isMember(submission.getUser().getId())) {
            log.info("User {} is not a member of contest room {}", submission.getUser().getId(), room.getId());
            throw new InformaticsServerException("permissionDenied");
        }
        if (contest == null) {
            addUpsolvingSubmission(submission);
            return;
        }
        for (ContestantResultDTO contestantResult : contest.getStandings()) {
            if (contestantResult.getContestantId() == submission.getUser().getId()) {
                TaskResultDTO taskResult = contestantResult.getTaskResults().get(submission.getTask().getCode());
                TaskResultDTO newTaskResult = new TaskResultDTO(submission.getTask().getCode(),
                        submission.getScore(),
                        taskResult == null ? 1 : taskResult.attempts() + 1,
                        getSuccessTime(submission, taskResult, contest));
                contestantResult.setTaskResult(newTaskResult, contest.getScoringType());
            }
        }
        contestManager.updateContest(contest);
    }

    private Long getSuccessTime(Submission submission, TaskResultDTO taskResult, ContestDTO contestDTO) {
        Long newTime = submission.getSubmissionTime().getTime() - contestDTO.getStartDate().getTime();
        if (taskResult == null) {
            return newTime;
        }
        if (taskResult.score() < submission.getScore()) {
            return newTime;
        }
        if (taskResult.score().equals(submission.getScore())) {
            return Math.min(newTime, taskResult.successTime());
        }
        return taskResult.successTime();
    }

    private void addUpsolvingSubmission(Submission submission) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(submission.getContest().getId());

        long numChanged = contest.getUpsolvingStandings().stream()
                .filter(result -> result.getContestantId() == submission.getUser().getId())
                .map(result -> {
                    float newScore = Math.max(submission.getScore(), result.getTaskResults().get(submission.getTask().getCode()).getScore());
                    float diff = submission.getScore() - newScore;
                    result.setTotalScore(result.getTotalScore() + diff);
                    result.getTaskResults().put(submission.getTask().getCode(), newScore);
                    contestantResultJpaRepository.save(result);
                    return result;
                }).count();
        if (numChanged > 0) {
            return;
        }
        ContestantResultDTO newContestantResult = new ContestantResult(contest.getScoringType(), (int) submission.getUser().getId());
        contest.getUpsolvingStandings().add(newContestantResult);
        newContestantResult.setTaskScore(taskManager.getTask(submission.getTaskId()).getCode(), submission.getScore());
        contestManager.updateContest(contest);
    }

    @EventListener
    public void changeContest(ContestChangeEvent event) {
        ContestDTO contest = ContestDTO.toDTO((Contest) event.getSource());
        if (contest.getStatus() == ContestStatus.FUTURE) {
            scheduleContestStart(contest);
        } else if (contest.getStatus() == ContestStatus.LIVE) {
            ContestDTO liveContest = liveContests.get(contest.getId());
            if (liveContest == null) {
                liveContests.put(contest.getId(), contest);
                liveContest = contest;
            }

            contest.setStandings(liveContest.getStandings());
            contest.setUpsolvingStandings(liveContest.getUpsolvingStandings());
            liveContests.put(contest.getId(), contest);
            if (!liveContest.getEndDate().equals(contest.getEndDate())) {
                scheduleContestEnd(contest);
            }
        } else {
            if (contest.isUpsolving()) {
                manageUpsolvingOn(contest);
            } else {
                manageUpsolvingOff(contest);
            }
        }
    }

    private void manageUpsolvingOn(ContestDTO contest) {
        if (upsolvingContests.containsKey(contest.getId())) {
            return;
        }
        if (contest.getUpsolvingStandings() == null) {
            contest.setUpsolvingStandings(contest.getStandings().stream().toList());
        }
        upsolvingContests.put(contest.getId(), contest);
        contestManager.updateContest(contest);
    }

    private void manageUpsolvingOff(ContestDTO contest) {
        if (!upsolvingContests.containsKey(contest.getId())) {
            return;
        }
        upsolvingContests.remove(contest.getId());
    }

    private class ContestStartThread implements Runnable {

        private ContestDTO contest;

        public ContestStartThread(ContestDTO contest) {
            super();
            this.contest = contest;
        }

        @Override
        public void run() {
            ContestDTO upToDateContest = ContestDTO.toDTO(contestRepository.getReferenceById(contest.getId()));
            if (!contest.getVersion().equals(upToDateContest.getVersion())) {
                return;
            }

            contest.setStatus(ContestStatus.LIVE);
            contest = contestManager.updateContest(contest);
            log.info("Contest [{}] has been started.", contest.getId());
            scheduleContestEnd(contest);
        }
    }

    private class ContestEndThread implements Runnable {

        private final ContestDTO contest;

        public ContestEndThread(ContestDTO contest) {
            super();
            this.contest = contest;
        }

        @Override
        public void run() {
            if (!liveContests.containsKey(contest.getId())) {
                return;
            }
            if (!liveContests.get(contest.getId()).getVersion().equals(contest.getVersion())) {
                return;
            }

            contest.setStatus(ContestStatus.PAST);
            if (contest.isUpsolvingAfterFinish()) {
                contest.setUpsolving(true);
            }
            liveContests.remove(contest.getId());
            contestManager.updateContest(contest);
            log.info("Contest [{}] has been finished.", contest.getId());

        }
    }
}
