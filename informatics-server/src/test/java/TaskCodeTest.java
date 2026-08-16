import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.server.task.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** The code a teacher types is the task's identifier and must survive being saved. */
public class TaskCodeTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ContestJpaRepository contestRepository;
    @Mock private Logger log;
    @InjectMocks private TaskManager taskManager;

    private Contest contest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        contest = new Contest();
        contest.setId(1L);
        contest.setTasks(new ArrayList<>());
        when(contestRepository.getReferenceById(any())).thenReturn(contest);
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    private TaskDTO dto(Long id, String code) {
        return new TaskDTO(id, 1L, code, "Ball Machine", null, null, null,
                1000, 256, null, 1, "*.in", "*.out", new HashMap<>(), new ArrayList<>(), null);
    }

    @Test
    void keepsTheCodeTheTeacherTyped() throws Exception {
        when(taskRepository.findFirstByCode("ballgame")).thenReturn(Optional.empty());

        TaskDTO saved = taskManager.addTask(1L, dto(null, "ballgame"));

        assertEquals("ballgame", saved.code());
    }

    @Test
    void generatesACodeOnlyWhenNoneWasGiven() throws Exception {
        TaskDTO saved = taskManager.addTask(1L, dto(null, "  "));

        assertNotNull(saved.code());
        assertFalse(saved.code().isBlank());
    }

    @Test
    void refusesACodeAlreadyUsedByAnotherTask() {
        Task other = new Task();
        other.setId(99L);
        other.setCode("ballgame");
        when(taskRepository.findFirstByCode("ballgame")).thenReturn(Optional.of(other));

        InformaticsServerException ex = assertThrows(InformaticsServerException.class,
                () -> taskManager.addTask(1L, dto(null, "ballgame")));
        assertEquals("taskCodeAlreadyExists", ex.getCode());
    }

    @Test
    void letsAnExistingGeneratedCodeThroughUnchanged() throws Exception {
        // Generated codes are base64 and may contain '+' or '=', which the charset rule rejects.
        // Editing a task must not be blocked by its own pre-existing code.
        Task existing = new Task();
        existing.setId(5L);
        existing.setCode("aB+c/dE=fg");
        contest.getTasks().add(existing);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(existing));
        existing.setContest(contest);

        TaskDTO saved = taskManager.addTask(1L, dto(5L, "aB+c/dE=fg"));

        assertEquals("aB+c/dE=fg", saved.code());
    }

    @Test
    void stillRefusesAnUnsafeCodeOnAnExistingTask() {
        Task existing = new Task();
        existing.setId(6L);
        existing.setCode("aB+c/dE=fg");
        existing.setContest(contest);
        when(taskRepository.findById(6L)).thenReturn(Optional.of(existing));

        assertThrows(InformaticsServerException.class,
                () -> taskManager.addTask(1L, dto(6L, "../../etc/passwd")));
    }

    @Test
    void refusesACodeThatIsNotFileNameSafe() {
        // The code is used to name downloaded archives.
        InformaticsServerException ex = assertThrows(InformaticsServerException.class,
                () -> taskManager.addTask(1L, dto(null, "../../etc/passwd")));
        assertEquals("invalidTaskCode", ex.getCode());
    }
}
