package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.repository.contest.IContestRepository;
import ge.freeuni.informatics.repository.user.IUserRepository;
import ge.freeuni.informatics.server.task.ITaskManager;
import ge.freeuni.informatics.utils.ArrayUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private IContestRepository contestRepository;

    @Autowired
    IUserRepository userRepository;

    private final ConcurrentHashMap<Long, ContestDTO> liveContests = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ContestDTO> upsolvingContests = new ConcurrentHashMap<>();

    @PostConstruct
    public void startup() {
        List<ContestDTO> futureContests = ContestDTO.toDTOs(contestRepository.getContests(null, null, Collections.singletonList(ContestStatus.FUTURE), null, null, null));
        List<ContestDTO> liveContests = ContestDTO.toDTOs(contestRepository.getContests(null, null, Collections.singletonList(ContestStatus.LIVE), null, null, null));

        scheduleFutureContests(futureContests);
        manageLiveContests(liveContests);
    }

    public List<ContestantResultDTO> getStandings(long contestId, Integer offset, Integer size) throws InformaticsServerException {
        List<ContestantResult> standings = contestManager.getStandings(contestId, offset, size);
        if (liveContests.containsKey(contestId)) {
            standings = liveContests.get(contestId).getStandings().getStandings();
        }
        List<ContestantResultDTO> standingsDTO = new ArrayList<>();
        for (ContestantResult result : standings) {
            ContestantResultDTO contestantResultDTO = ContestantResultDTO.toDTO(result);
            try {
                contestantResultDTO.setUsername(userRepository.getUser(result.getContestantId()).getUsername());
                standingsDTO.add(contestantResultDTO);
            } catch (Exception ignored) {

            }
        }
        return ArrayUtils.getPage(standingsDTO, offset, size);
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
        Date endTime = getEndTime(contest);

        if (new Date().after(endTime)) {
            new ContestEndThread(contest).run();
        } else {
            taskScheduler.schedule(new ContestEndThread(contest), endTime);
        }
    }

    @EventListener
    public void addSubmission(SubmissionEvent event) {
        Submission submission = (Submission) event.getSource();
        ContestDTO contest = liveContests.get(submission.getContestId());
        if (contest == null) {
            if (upsolvingContests.containsKey(submission.getContestId())) {
                addUpsolvingSubmission(submission);
            }
            return;
        }
        for (ContestantResult contestantResult : contest.getStandings().getStandings()) {
            if (contestantResult.getContestantId() == submission.getUserId()) {
                contestantResult.setTaskScore(taskManager.getTask(submission.getTaskId()).getCode(), submission.getScore());
            }
        }
        contestManager.updateContest(contest);
    }

    public void addUpsolvingSubmission(Submission submission) {
        ContestDTO contest = upsolvingContests.get(submission.getContestId());

        for (ContestantResult contestantResult : contest.getUpsolvingStandings().getStandings()) {
            if (contestantResult.getContestantId() == submission.getUserId()) {
                contestantResult.setTaskScore(taskManager.getTask(submission.getTaskId()).getCode(), submission.getScore());
                contestManager.updateContest(contest);
                return;
            }
        }
        ContestantResult newContestantResult = new ContestantResult(contest.getScoringType(), (int) submission.getUserId());
        contest.getUpsolvingStandings().getStandings().add(newContestantResult);
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
            if (!getEndTime(liveContest).equals(getEndTime(contest))) {
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
            contest.setUpsolvingStandings(contest.getStandings());
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

    private Date getEndTime(ContestDTO contest) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(contest.getStartDate());
        calendar.add(Calendar.SECOND, contest.getDurationInSeconds());
        return calendar.getTime();
    }

    private class ContestStartThread implements Runnable {

        private ContestDTO contest;

        public ContestStartThread(ContestDTO contest) {
            super();
            this.contest = contest;
        }

        @Override
        public void run() {
            ContestDTO upToDateContest = ContestDTO.toDTO(contestRepository.getContest(contest.getId()));
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
