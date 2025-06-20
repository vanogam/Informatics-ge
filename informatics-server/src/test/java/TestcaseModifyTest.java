import ge.freeuni.informatics.common.dto.AddTestcasesResult;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.repository.task.TestcaseRepository;
import ge.freeuni.informatics.server.task.TaskManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestcaseModifyTest {

    private Task task;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private Logger log;

    @Mock
    private ContestJpaRepository contestRepository;

    @Mock
    private TestcaseRepository testcaseRepository;

    @InjectMocks
    private TaskManager taskManager;

    private long index = 1;

    private final HashMap<Long, TestCase> testCases = new HashMap<>();

    @BeforeEach
    public void setUp() throws IOException, NoSuchFieldException, IllegalAccessException {
        task = new Task();
        task.setId(1L);
        task.setCode("TASK_CODE");
        task.setInputTemplate("test*.in");
        task.setOutputTemplate("test*.out");

        MockitoAnnotations.openMocks(this);
        setUpTestDirField();
        setUpTempDirField();

        when(taskRepository.save(any())).thenAnswer((ob) -> ob.getArgument(0));
        when(testcaseRepository.save(any())).thenAnswer((ob) -> {
            ((TestCase)ob.getArgument(0)).setId(index++);
            testCases.put(index, ob.getArgument(0));
            return ob.getArgument(0);
        });
    }

    private void setUpTestDirField() throws IOException, IllegalAccessException, NoSuchFieldException {
        Field testsDirField = TaskManager.class.getDeclaredField("testsDirectoryAddress");
        testsDirField.setAccessible(true);
        File fileRoot = new File(getClass().getClassLoader().getResource("").getFile() + "/testfileroot");
        fileRoot.mkdirs();
        FileUtils.cleanDirectory(fileRoot);
        testsDirField.set(taskManager, fileRoot.getPath());
    }

    private void setUpTempDirField() throws IOException, IllegalAccessException, NoSuchFieldException {
        Field testsDirField = TaskManager.class.getDeclaredField("tempDirectoryAddress");
        testsDirField.setAccessible(true);
        File fileRoot = new File(getClass().getClassLoader().getResource("").getFile() + "/tempfileroot");
        fileRoot.mkdirs();
        FileUtils.cleanDirectory(fileRoot);
        testsDirField.set(taskManager, fileRoot.getPath());
    }

    @Test
    public void testAddTestcase() throws InformaticsServerException, IOException {
        when(taskRepository.getReferenceById(any())).thenAnswer(ignored -> task);
        Task persistedTask = addTestcase("0", task, "input 1", "output 1");
        TestCase testCase = persistedTask.getTestCases().get(0);
        assertEquals("test0.in", getTestFileName(testCase.getInputFileAddress()));
        assertEquals("test0.out", getTestFileName(testCase.getOutputFileAddress()));
        File inputFile = new File(testCase.getInputFileAddress());
        File outputFile = new File(testCase.getOutputFileAddress());
        assertTrue(inputFile.exists());
        assertTrue(outputFile.exists());
        assertEquals("input 1", Files.readString(inputFile.toPath()));
        assertEquals("output 1", Files.readString(outputFile.toPath()));
    }

    @Test
    public void testTestcaseOrder() throws InformaticsServerException, IOException {
        when(taskRepository.getReferenceById(any())).thenAnswer(ignored -> task);

        addTestcase("11", task, "input 11", "output 11");
        addTestcase("10", task,"input 10", "output 10");
        addTestcase("04", task,"input 4", "output 4");
        addTestcase("03", task,"input 3", "output 3");
        addTestcase("02", task,"input 2", "output 2");
        addTestcase("01", task,"input 1", "output 1");

        addTestcase("09", task, "input 9", "output 9");
        addTestcase("08", task, "input 8", "output 8");
        addTestcase("07", task, "input 7", "output 7");
        addTestcase("06", task, "input 6", "output 6");
        addTestcase("05", task, "input 5", "output 5");

        for (int i = 0; i <= 10; i++) {
            TestCase testCase = task.getTestCases().get(i);
            assertEquals("test" + addLeadingZeros(i + 1, 2) + ".in", getTestFileName(testCase.getInputFileAddress()));
            assertEquals("test" + addLeadingZeros(i + 1, 2) + ".out", getTestFileName(testCase.getOutputFileAddress()));
            assertEquals("input " + (i + 1), Files.readString(new File(testCase.getInputFileAddress()).toPath()));
            assertEquals("output " + (i + 1), Files.readString(new File(testCase.getOutputFileAddress()).toPath()));
        }

    }

    @Test
    public void testAddTestcasesZip() throws IOException, InformaticsServerException {
        task.setInputTemplate("box.I*");
        task.setOutputTemplate("box.O*");

        when(taskRepository.getReferenceById(any())).thenAnswer(ignored -> task);
        AddTestcasesResult result = taskManager.addTestcases(task.getId(), Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("test.zip")).readAllBytes());

        assertEquals(10, task.getTestCases().size());
        String[] inputPrefix = new String[]{
                "12 20 47\r\n18 7 42\r\n\r\n",
                "12 20 47\r\n40 18 42\r\n\r\n",
                "34 46 89\r\n98 50 42\r\n",
                "10 10 10\r\n100 100 100",
                "37 37 37\r\n37 37 37",
                "10 100 100\r\n20 30 60",
                "20 30 60\r\n10 100 100",
                "100 100 99\r\n99 99 99",
                "63 63 63\r\n63 63 63",
                "90 87 99\r\n88 100 91"
        };
        String[] outputPrefix = new String[]{
                "YRS",
                "NO",
                "YES",
                "NO",
                "NO",
                "NO",
                "NO",
                "NO",
                "NO",
                "YES"
        };
        for (int i = 1; i <=10; i++) {
            TestCase testCase = task.getTestCases().get(i - 1);
            assertEquals("box.I" + addLeadingZeros(i, 2), getTestFileName(testCase.getInputFileAddress()));
            assertEquals("box.O" + addLeadingZeros(i, 2), getTestFileName(testCase.getOutputFileAddress()));
            assertEquals(inputPrefix[i - 1], Files.readString(new File(testCase.getInputFileAddress()).toPath()));
            assertEquals(outputPrefix[i - 1], Files.readString(new File(testCase.getOutputFileAddress()).toPath()));
        }
    }

    @Test
    public void testAddTestcasesTwice() throws InformaticsServerException, IOException {
        testAddTestcasesZip();
        testAddTestcasesZip();
    }

    public String addLeadingZeros(int number, int totalLength) {
        return String.format("%0" + totalLength + "d", number);
    }

    private Task addTestcase(String key, Task task, String input, String output) throws InformaticsServerException {
        byte[] inputContent = input.getBytes(StandardCharsets.UTF_8);
        byte[] outputContent = output.getBytes(StandardCharsets.UTF_8);

        return taskManager.addTestcase(task.getId(), inputContent, outputContent,
                task.getInputTemplate().replace("*", key),
                task.getOutputTemplate().replace("*", key));
    }

    private String getTestFileName(String testFileAddress) {
        return testFileAddress.substring(testFileAddress.lastIndexOf("/") + 1);
    }
}