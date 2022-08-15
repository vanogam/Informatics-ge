package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class ContestService {

    @Autowired
    private IContestManager contestManager;

    @Autowired
    private TaskScheduler taskScheduler;

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> futureContestsMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, ScheduledFuture<?>> liveContestsMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void startup() {
        List<ContestDTO> futureContests = contestManager.getContests(null, null, Arrays.asList(ContestStatus.FUTURE), null, null);
        List<ContestDTO> liveContests = contestManager.getContests(null, null, Arrays.asList(ContestStatus.LIVE, ContestStatus.FUTURE), null, new Date());

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
            scheduleContestEnd(contest);
        }
    }

    private void scheduleContestStart(ContestDTO contest) {
        ScheduledFuture<?> future = taskScheduler.schedule(new ContestStartThread(contest), contest.getStartDate());
        futureContestsMap.put(contest.getId(), future);

    }

    private void scheduleContestEnd(ContestDTO contest) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(contest.getStartDate());
        calendar.add(Calendar.SECOND, contest.getDurationInSeconds());
        if (calendar.getTime().before(new Date())) {
            contest.setStatus(ContestStatus.PAST);
            contestManager.updateContest(contest);
        }

        ScheduledFuture<?> future = taskScheduler.schedule(new ContestEndThread(contest), calendar.getTime());
        futureContestsMap.put(contest.getId(), future);

    }

    private static class ContestStartThread implements Runnable {

        private final ContestDTO contest;

        public ContestStartThread(ContestDTO contest) {
            super();
            this.contest = contest;
        }

        @Override
        public void run() {
            contest.setStatus(ContestStatus.LIVE);
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
