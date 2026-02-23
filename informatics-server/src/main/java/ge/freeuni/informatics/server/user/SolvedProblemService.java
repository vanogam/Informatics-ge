package ge.freeuni.informatics.server.user;

import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.user.ProblemAttemptStatus;
import ge.freeuni.informatics.common.model.user.SolvedProblem;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.repository.user.SolvedProblemJpaRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Set;

@Service
public class SolvedProblemService {

    private static final Set<SubmissionStatus> FINAL_STATUSES = Set.of(
            SubmissionStatus.CORRECT,
            SubmissionStatus.FAILED,
            SubmissionStatus.WRONG_ANSWER,
            SubmissionStatus.TIME_LIMIT_EXCEEDED,
            SubmissionStatus.MEMORY_LIMIT_EXCEEDED,
            SubmissionStatus.RUNTIME_ERROR,
            SubmissionStatus.COMPILATION_ERROR,
            SubmissionStatus.SYSTEM_ERROR,
            SubmissionStatus.PARTIAL
    );

    private static final Set<SubmissionStatus> FAILED_STATUSES = Set.of(
            SubmissionStatus.FAILED,
            SubmissionStatus.WRONG_ANSWER,
            SubmissionStatus.TIME_LIMIT_EXCEEDED,
            SubmissionStatus.MEMORY_LIMIT_EXCEEDED,
            SubmissionStatus.RUNTIME_ERROR,
            SubmissionStatus.COMPILATION_ERROR,
            SubmissionStatus.SYSTEM_ERROR
    );

    private final SolvedProblemJpaRepository solvedProblemRepository;
    private final UserJpaRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public SolvedProblemService(SolvedProblemJpaRepository solvedProblemRepository,
                                 UserJpaRepository userRepository,
                                 TaskRepository taskRepository) {
        this.solvedProblemRepository = solvedProblemRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @EventListener
    @Transactional
    public void handleSubmissionEvent(SubmissionEvent event) {
        Submission submission = (Submission) event.getSource();
        
        if (!FINAL_STATUSES.contains(submission.getStatus())) {
            return;
        }

        Long userId = submission.getUser().getId();
        Long taskId = submission.getTask().getId();
        Date attemptTime = submission.getSubmissionTime() != null 
                ? submission.getSubmissionTime() 
                : new Date();

        ProblemAttemptStatus status;
        if (submission.getStatus() == SubmissionStatus.CORRECT) {
            status = ProblemAttemptStatus.SOLVED;
        } else {
            status = ProblemAttemptStatus.FAILED;
        }

        updateProblemAttempt(userId, taskId, status, attemptTime);
    }

    @Transactional
    public void updateProblemAttempt(Long userId, Long taskId, ProblemAttemptStatus status, Date attemptTime) {
        SolvedProblem solvedProblem = solvedProblemRepository
                .findByUserIdAndTaskId(userId, taskId)
                .orElse(new SolvedProblem());

        if (solvedProblem.getId() == null) {
            solvedProblem.setUser(userRepository.getReferenceById(userId));
            solvedProblem.setTask(taskRepository.getReferenceById(taskId));
        }

        solvedProblem.setStatus(status);
        solvedProblem.setLastAttemptAt(attemptTime);

        if (status == ProblemAttemptStatus.SOLVED && solvedProblem.getSolvedAt() == null) {
            solvedProblem.setSolvedAt(attemptTime);
        }

        solvedProblemRepository.save(solvedProblem);
    }
}

