package ge.freeuni.informatics.acceptance.util;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.contest.*;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.*;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.model.user.UserRole;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.repository.task.TestcaseRepository;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.utils.UserUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class TestDataFactory {

    @Autowired
    private UserJpaRepository userRepository;

    @Autowired
    private ContestRoomJpaRepository contestRoomRepository;

    @Autowired
    private ContestJpaRepository contestRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestcaseRepository testcaseRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @PersistenceContext
    private EntityManager entityManager;

    private int userCounter = 0;
    private int contestCounter = 0;
    private int taskCounter = 0;
    private long roomCounter = 100;

    @Transactional
    public ContestRoom createEmptyRoom(String name) {
        ContestRoom room = new ContestRoom();
        room.setId(++roomCounter);
        room.setName(name);
        room.setOpen(true);
        room.setTeachers(new HashSet<>());
        room.setParticipants(new HashSet<>());
        return contestRoomRepository.save(room);
    }

    @Transactional
    public void setRoomTeacher(ContestRoom room, User teacher) {
        room = contestRoomRepository.getReferenceById(room.getId());
        room.getTeachers().add(teacher);
        room.getParticipants().add(teacher);
        contestRoomRepository.save(room);
    }

    @Transactional
    public User createUser(String username, String password, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setFirstName("Test");
        user.setLastName("User" + (++userCounter));
        user.setPasswordSalt(UserUtils.getSalt());
        user.setPassword(UserUtils.getHash(password, user.getPasswordSalt()));
        user.setRole(role.name());
        user.setVersion(1);
        user.setRegistrationTime(new Date());
        return userRepository.save(user);
    }

    @Transactional
    public User createStudent(String username, String password) {
        return createUser(username, password, UserRole.STUDENT);
    }

    @Transactional
    public User createTeacher(String username, String password) {
        return createUser(username, password, UserRole.TEACHER);
    }

    @Transactional
    public User createAdmin(String username, String password) {
        return createUser(username, password, UserRole.ADMIN);
    }

    @Transactional
    public List<User> createStudents(int count, String usernamePrefix, String password) {
        List<User> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(createStudent(usernamePrefix + i, password));
        }
        return users;
    }

    @Transactional
    public ContestRoom createContestRoom(String name, User teacher, List<User> participants) {
        ContestRoom room = new ContestRoom();
        room.setId(++roomCounter);
        room.setName(name);
        room.setOpen(true);

        Set<User> teacherSet = new HashSet<>();
        teacherSet.add(teacher);
        room.setTeachers(teacherSet);

        Set<User> participantSet = new HashSet<>(participants);
        participantSet.add(teacher);
        room.setParticipants(participantSet);

        return contestRoomRepository.save(room);
    }

    @Transactional
    public ContestRoom createOpenRoom(String name, User teacher) {
        ContestRoom room = new ContestRoom();
        room.setId(++roomCounter);
        room.setName(name);
        room.setOpen(true);

        Set<User> teacherSet = new HashSet<>();
        teacherSet.add(teacher);
        room.setTeachers(teacherSet);
        room.setParticipants(new HashSet<>());

        return contestRoomRepository.save(room);
    }

    @Transactional
    public void addParticipantsToRoom(ContestRoom room, List<User> participants) {
        room = contestRoomRepository.getReferenceById(room.getId());
        room.getParticipants().addAll(participants);
        contestRoomRepository.save(room);
    }

    @Transactional
    public Contest createContest(String name, ContestRoom room, Date startDate, Date endDate,
                                  ScoringType scoringType, boolean upsolvingAfterFinish) {
        Contest contest = new Contest();
        contest.setName(name + (++contestCounter));
        contest.setRoomId(room.getId());
        contest.setStartDate(startDate);
        contest.setEndDate(endDate);
        contest.setScoringType(scoringType);
        contest.setUpsolvingAfterFinished(upsolvingAfterFinish);
        contest.setVersion(1);
        contest.setParticipants(new ArrayList<>());
        contest.setStandings(new ArrayList<>());
        contest.setUpsolvingStandings(new ArrayList<>());
        contest.setTasks(new ArrayList<>());
        return contestRepository.save(contest);
    }

    @Transactional
    public Contest createLiveContest(String name, ContestRoom room, int durationMinutes) {
        Date now = new Date();
        Date startDate = new Date(now.getTime() - 60000); // Started 1 minute ago
        Date endDate = new Date(now.getTime() + (durationMinutes * 60000L));
        Contest contest = createContest(name, room, startDate, endDate, ScoringType.BEST_SUBMISSION, true);
        
        // Publish event to activate the contest in ContestService
        ContestDTO contestDTO = ContestDTO.toDTO(contest);
        contestDTO.setStatus(ContestStatus.LIVE);
        eventPublisher.publishEvent(new ContestChangeEvent(contestDTO));
        
        return contest;
    }

    @Transactional
    public Contest createFutureContest(String name, ContestRoom room, int startInMinutes, int durationMinutes) {
        Date now = new Date();
        Date startDate = new Date(now.getTime() + (startInMinutes * 60000L));
        Date endDate = new Date(startDate.getTime() + (durationMinutes * 60000L));
        return createContest(name, room, startDate, endDate, ScoringType.BEST_SUBMISSION, true);
    }

    @Transactional
    public Contest createPastContest(String name, ContestRoom room, boolean upsolving) {
        Date now = new Date();
        Date startDate = new Date(now.getTime() - 7200000); // 2 hours ago
        Date endDate = new Date(now.getTime() - 3600000); // 1 hour ago
        Contest contest = createContest(name, room, startDate, endDate, ScoringType.BEST_SUBMISSION, upsolving);
        if (upsolving) {
            contest.setUpsolving(true);
            return contestRepository.save(contest);
        }
        return contest;
    }

    @Transactional
    public Task createTask(Contest contest, String code, String title, int numTestcases) {
        // Reload contest to ensure it's managed in this transaction
        contest = contestRepository.getReferenceById(contest.getId());
        
        Task task = new Task();
        task.setCode(code);
        task.setTitle(title);
        task.setContest(contest);
        task.setTaskType(TaskType.BATCH);
        task.setTaskScoreType(TaskScoreType.SUM);
        task.setTaskScoreParameter(String.valueOf(100.0 / numTestcases));
        task.setTimeLimitMillis(1000);
        task.setMemoryLimitMB(256);
        task.setCheckerType(CheckerType.TOKEN);
        task.setInputTemplate("test*.in");
        task.setOutputTemplate("test*.out");
        task.setOrder(++taskCounter);

        task = taskRepository.save(task);

        List<Testcase> testcases = new ArrayList<>();
        for (int i = 1; i <= numTestcases; i++) {
            Testcase testcase = new Testcase();
            testcase.setTaskId(task.getId());
            // Simple keys "1", "2", ... "10" for mock task
            // Input: n (1 <= n <= 10), Output: n
            testcase.setKey(String.valueOf(i));
            testcase.setInputFileAddress("/mock/test" + i + ".in");
            testcase.setOutputFileAddress("/mock/test" + i + ".out");
            testcase.setPublicTestcase(i == 1);
            testcases.add(testcaseRepository.save(testcase));
        }
        task.setTestCases(testcases);
        task = taskRepository.save(task);

        contest.getTasks().add(task);
        contestRepository.save(contest);

        return task;
    }

    @Transactional
    public void addStatement(Task task, Language language, String title, String content) {
        task = taskRepository.getReferenceById(task.getId());
        if (task.getStatements() == null) {
            task.setStatements(new HashMap<>());
        }
        // Task.statements is Map<Language, String> where String is the full statement content
        task.getStatements().put(language, content);
        taskRepository.save(task);
    }

    @Transactional
    public void registerUserForContest(Contest contest, User user) {
        contest = contestRepository.getReferenceById(contest.getId());
        if (contest.getParticipants() == null) {
            contest.setParticipants(new ArrayList<>());
        }
        if (contest.getStandings() == null) {
            contest.setStandings(new ArrayList<>());
        }

        if (contest.getParticipants().stream().noneMatch(u -> u.getId().equals(user.getId()))) {
            contest.getParticipants().add(user);

            ContestantResult result = new ContestantResult();
            result.setContest(contest);
            result.setContestant(user.getId());
            result.setTotalScore(0f);
            result.setTaskResults(new HashMap<>());
            contest.getStandings().add(result);

            contestRepository.save(contest);
        }
    }

    @Transactional
    public void registerUsersForContest(Contest contest, List<User> users) {
        for (User user : users) {
            registerUserForContest(contest, user);
        }
    }

    /**
     * Sets a live (contest-time) task score on the contest standings.
     * Use this to simulate frozen standings at contest end before testing upsolving.
     */
    @Transactional
    public void setLiveTaskScore(Contest contest, User user, String taskCode, float score) {
        contest = contestRepository.getReferenceById(contest.getId());
        if (contest.getStandings() == null) {
            return;
        }
        ContestantResult result = contest.getStandings().stream()
                .filter(r -> user.getId().equals(r.getContestantId()))
                .findFirst()
                .orElse(null);
        if (result == null) {
            return;
        }
        Map<String, TaskResult> taskResults = result.getTaskResults() != null
                ? new HashMap<>(result.getTaskResults())
                : new HashMap<>();
        TaskResult tr = new TaskResult();
        tr.setTaskCode(taskCode);
        tr.setScore(score);
        tr.setAttempts(1);
        tr.setSuccessTime(0L);
        taskResults.put(taskCode, tr);
        result.setTaskResults(taskResults);
        float total = taskResults.values().stream()
                .map(TaskResult::getScore)
                .reduce(0f, Float::sum);
        result.setTotalScore(total);
        contestRepository.save(contest);
    }

    public SubmissionRequest createSubmissionRequest(Long contestId, Long taskId, String code) {
        return new SubmissionRequest(contestId, taskId, CodeLanguage.CPP, code);
    }

    public record SubmissionRequest(Long contestId, Long taskId, CodeLanguage language, String code) {}

    @Transactional
    public void cleanup() {
        try {
            // First, clear Many-to-Many relationships
            List<ContestRoom> rooms = contestRoomRepository.findAll();
            for (ContestRoom room : rooms) {
                if (room.getTeachers() != null) {
                    room.getTeachers().clear();
                }
                if (room.getParticipants() != null) {
                    room.getParticipants().clear();
                }
                contestRoomRepository.save(room);
            }
            
            List<Contest> contests = contestRepository.findAll();
            for (Contest contest : contests) {
                if (contest.getParticipants() != null) {
                    contest.getParticipants().clear();
                    contestRepository.save(contest);
                }
            }
            
            // Flush to ensure relationship changes are persisted
            entityManager.flush();
            entityManager.clear();
            
            // Now delete entities in dependency order
            testcaseRepository.deleteAllInBatch();
            taskRepository.deleteAllInBatch();
            contestRepository.deleteAllInBatch();
            contestRoomRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();
            
            // Final flush
            entityManager.flush();
            entityManager.clear();
        } catch (Exception e) {
            // Log but don't fail - might be called when database is empty or tables don't exist yet
            System.err.println("Cleanup warning (this is normal on first run): " + e.getMessage());
        }
        
        // Always reset counters
        userCounter = 0;
        contestCounter = 0;
        taskCounter = 0;
        roomCounter = 100;
    }
}
