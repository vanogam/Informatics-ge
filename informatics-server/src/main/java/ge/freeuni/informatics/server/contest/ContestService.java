package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.SubmissionEvent;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContestService {

    @Autowired
    private IContestManager contestManager;

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private ITaskManager taskManager;

    private final ConcurrentHashMap<Long, ContestDTO> liveContests = new ConcurrentHashMap<>();

    @PostConstruct
    public void startup() {
        List<ContestDTO> futureContests = contestManager.getContests(null, null, Collections.singletonList(ContestStatus.FUTURE), null, null);
        List<ContestDTO> liveContests = contestManager.getContests(null, null, Collections.singletonList(ContestStatus.LIVE), null, null);

        scheduleFutureContests(futureContests);
        manageLiveContests(liveContests);
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(contest.getStartDate());
        calendar.add(Calendar.SECOND, contest.getDurationInSeconds());
        if (calendar.getTime().before(new Date())) {
            contest.setStatus(ContestStatus.PAST);
            contestManager.updateContest(contest);
        }

        if (new Date().after(calendar.getTime())) {
            new ContestEndThread(contest).run();
        } else {
            taskScheduler.schedule(new ContestEndThread(contest), calendar.getTime());
        }


    }

    @EventListener
    public void addSubmission(SubmissionEvent event) {
        Submission submission = (Submission) event.getSource();
        ContestDTO contest = liveContests.get(submission.getContestId());
        if (contest == null) {
            return;
        }
        for (ContestantResult contestantResult : contest.getStandings().getStandings()) {
            if (contestantResult.getContestantId() == submission.getUserId()) {
                contestantResult.setTaskScore(taskManager.getTask(submission.getTaskId()).getCode(), submission.getScore());
            }
        }
    }

    private class ContestStartThread implements Runnable {

        private final ContestDTO contest;

        public ContestStartThread(ContestDTO contest) {
            super();
            this.contest = contest;
        }

        @Override
        public void run() {
            contest.setStatus(ContestStatus.LIVE);
            liveContests.put(contest.getId(), contest);
            contestManager.updateContest(contest);
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
            contest.setStatus(ContestStatus.PAST);
            contestManager.updateContest(contest);
        }
    }
}
