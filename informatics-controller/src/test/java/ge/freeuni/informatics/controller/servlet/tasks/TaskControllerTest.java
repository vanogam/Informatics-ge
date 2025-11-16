package ge.freeuni.informatics.controller.servlet.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.dto.TestcaseDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.task.*;
import ge.freeuni.informatics.controller.model.*;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private ITaskManager taskManager;

    @Mock
    private org.slf4j.Logger log;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;

    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Use reflection to set the defaultLanguage field
        try {
            java.lang.reflect.Field field = TaskController.class.getDeclaredField("defaultLanguage");
            field.setAccessible(true);
            field.set(taskController, Language.KA.name());
        } catch (Exception e) {
            // Ignore if reflection fails
        }
        
        // Create standalone MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setMessageConverters(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper))
                .build();
        
        testTaskDTO = new TaskDTO(
                1L,
                1L,
                "TASK001",
                "Test Task",
                TaskType.BATCH,
                TaskScoreType.SUM,
                "1.0",
                1000,
                256,
                CheckerType.TOKEN,
                "test*.in",
                "test*.out",
                new HashMap<>(),
                new ArrayList<>()
        );
    }

    @Test
    void testGetTasks_Success() throws Exception {
        List<TaskInfo> taskInfos = new ArrayList<>();
        taskInfos.add(new TaskInfo(testTaskDTO, 0.0f));

        when(taskManager.getUpsolvingTasks(eq(1L), any(), any())).thenReturn(taskInfos);

        mockMvc.perform(get("/api/room/1/tasks")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks[0].task.code").value("TASK001"));

        verify(taskManager).getUpsolvingTasks(1L, 0, 10);
    }

    @Test
    void testGetTasks_WithNullPaging() throws Exception {
        List<TaskInfo> taskInfos = new ArrayList<>();
        when(taskManager.getUpsolvingTasks(eq(1L), any(), any())).thenReturn(taskInfos);

        mockMvc.perform(get("/api/room/1/tasks"))
                .andExpect(status().isOk());

        verify(taskManager).getUpsolvingTasks(eq(1L), any(), any());
    }

    @Test
    void testGetTasks_Error() throws Exception {
        when(taskManager.getUpsolvingTasks(eq(1L), any(), any()))
                .thenThrow(new InformaticsServerException("permissionDenied"));

        mockMvc.perform(get("/api/room/1/tasks")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("permissionDenied"));
    }

    @Test
    void testGetContestTasks_Success() throws Exception {
        List<TaskInfo> taskInfos = new ArrayList<>();
        taskInfos.add(new TaskInfo(testTaskDTO, 85.5f));

        when(taskManager.getContestTasks(eq(1L), anyInt(), anyInt())).thenReturn(taskInfos);

        mockMvc.perform(get("/api/contest/1/tasks")
                        .param("offset", "0")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tasks").isArray())
                .andExpect(jsonPath("$.tasks[0].score").value(85.5));
    }

    @Test
    void testGetContestTaskNames_Success() throws Exception {
        List<String> taskNames = List.of("Task 1", "Task 2", "Task 3");
        when(taskManager.getTaskNames(eq(1L), eq(Language.KA.name()))).thenReturn(taskNames);

        TaskNamesRequest request = new TaskNamesRequest();
        request.setLanguage(Language.KA.name());

        mockMvc.perform(get("/api/contest/1/task-names")
                        .param("language", "KA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskNames").isArray())
                .andExpect(jsonPath("$.taskNames[0]").value("Task 1"));
    }

    @Test
    void testGetContestTaskNames_DefaultLanguage() throws Exception {
        List<String> taskNames = List.of("Task 1");
        when(taskManager.getTaskNames(eq(1L), eq(Language.KA.name()))).thenReturn(taskNames);

        mockMvc.perform(get("/api/contest/1/task-names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskNames[0]").value("Task 1"));
    }

    @Test
    void testGetTask_Success() throws Exception {
        Task task = TaskDTO.fromDTO(testTaskDTO);
        Contest testContest = new Contest();
        testContest.setId(1L);
        task.setContest(testContest);
        when(taskManager.getTask(1L)).thenReturn(task);

        mockMvc.perform(get("/api/task/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK001"))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testSaveTask_Success() throws Exception {
        AddTaskRequest request = new AddTaskRequest(
                1L,
                1,
                "Test Task",
                "STANDARD",
                TaskType.BATCH,
                TaskScoreType.SUM,
                "1.0",
                1000,
                256,
                CheckerType.TOKEN,
                "test*.in",
                "test*.out"
        );

        when(taskManager.addTask(eq(1L), any(TaskDTO.class))).thenReturn(testTaskDTO);

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TASK001"));

        verify(taskManager).addTask(eq(1L), any(TaskDTO.class));
    }

    @Test
    void testSaveTask_Error() throws Exception {
        AddTaskRequest request = new AddTaskRequest(
                1L,
                1,
                "Test Task",
                "STANDARD",
                TaskType.BATCH,
                TaskScoreType.SUM,
                "1.0",
                1000,
                256,
                CheckerType.TOKEN,
                "test*.in",
                "test*.out"
        );

        when(taskManager.addTask(eq(1L), any(TaskDTO.class)))
                .thenThrow(new InformaticsServerException("contestNotFound"));

        mockMvc.perform(post("/api/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetStatement_Success() throws Exception {
        Statement statement = new Statement("title", "statement content", "input", "output", "");
        List<TestcaseDTO> publicTestcases = new ArrayList<>();

        when(taskManager.getStatement(eq(1L), eq(Language.KA))).thenReturn(statement);
        when(taskManager.getPublicTestcases(1L)).thenReturn(publicTestcases);

        mockMvc.perform(get("/api/task/1/statement/KA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statement").exists());

        verify(taskManager).getStatement(1L, Language.KA);
        verify(taskManager).getPublicTestcases(1L);
    }

    @Test
    void testGetStatement_NotFound() throws Exception {
        when(taskManager.getStatement(eq(1L), eq(Language.KA)))
                .thenThrow(InformaticsServerException.TASK_NOT_FOUND);

        mockMvc.perform(get("/api/task/1/statement/KA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUploadStatement_Success() throws Exception {
        AddStatementRequest request = new AddStatementRequest("statement content", Language.KA);

        doNothing().when(taskManager).addStatement(eq(1L), eq("statement content"), eq(Language.KA));

        mockMvc.perform(post("/api/task/1/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(taskManager).addStatement(1L, "statement content", Language.KA);
    }
}