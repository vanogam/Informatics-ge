package ge.freeuni.informatics.controller.servlet.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.dto.AddTestcasesResult;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.controller.model.SetPublicTestcasesRequest;
import ge.freeuni.informatics.server.task.ITaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TestcaseControllerTest {

    @Mock
    private ITaskManager taskManager;

    @Mock
    private org.slf4j.Logger log;

    @InjectMocks
    private TestcaseController testcaseController;

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper;

    private File mockFile;

    @BeforeEach
    void setUp() throws IOException {
        objectMapper = new ObjectMapper();
        
        // Create standalone MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(testcaseController)
                .setMessageConverters(
                        new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper),
                        new org.springframework.http.converter.ResourceHttpMessageConverter()
                )
                .build();
        
        // Create a temporary file for download tests
        mockFile = File.createTempFile("test", ".zip");
        mockFile.deleteOnExit();
        Files.write(mockFile.toPath(), "test content".getBytes());
    }

    @Test
    void testAddTestcases_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testcases.zip",
                "application/zip",
                "test zip content".getBytes()
        );

        AddTestcasesResult result = new AddTestcasesResult();
        result.getSuccess().add("test01");
        result.getSuccess().add("test02");
        result.getUnmatched().add("unmatched.txt");

        when(taskManager.addTestcases(eq(1L), any(byte[].class))).thenReturn(result);

        mockMvc.perform(multipart("/api/task/1/testcases")
                        .file(file)
                        .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.success").isArray())
                .andExpect(jsonPath("$.result.success[0]").value("test01"))
                .andExpect(jsonPath("$.result.unmatched[0]").value("unmatched.txt"));

        verify(taskManager).addTestcases(eq(1L), any(byte[].class));
    }

    @Test
    void testAddTestcases_ServerException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testcases.zip",
                "application/zip",
                "test zip content".getBytes()
        );

        when(taskManager.addTestcases(eq(1L), any(byte[].class)))
                .thenThrow(InformaticsServerException.TASK_NOT_FOUND);

        mockMvc.perform(multipart("/api/task/1/testcases")
                        .file(file)
                        .param("taskId", "1"))
                .andExpect(status().isNotFound());
//                .andExpect(jsonPath("$.message").value("taskNotFound"));
    }

    @Test
    void testAddTestcases_IOException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testcases.zip",
                "application/zip",
                "test zip content".getBytes()
        );

        when(taskManager.addTestcases(eq(1L), any(byte[].class)))
                .thenThrow(InformaticsServerException.TEST_SAVE_EXCEPTION);

        mockMvc.perform(multipart("/api/task/1/testcases")
                        .file(file)
                        .param("taskId", "1"))
                .andExpect(status().isInternalServerError());
//                .andExpect(jsonPath("$.code").value("fileUploadError"));
    }

    @Test
    void testGetSingleTestcase_Success() throws Exception {
        when(taskManager.getTestcaseZip(eq(1L), eq("test01"))).thenReturn(mockFile);

        mockMvc.perform(get("/api/task/1/testcase/test01"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + mockFile.getName() + "\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(taskManager).getTestcaseZip(1L, "test01");
    }

    @Test
    void testGetSingleTestcase_NotFound() throws Exception {
        when(taskManager.getTestcaseZip(eq(1L), eq("test01")))
                .thenThrow(InformaticsServerException.TEST_NOT_FOUND);

        mockMvc.perform(get("/api/task/1/testcase/test01"))
                .andExpect(status().isNotFound());

        verify(taskManager).getTestcaseZip(1L, "test01");
    }

    @Test
    void testGetSingleTestcase_IOException() throws Exception {
        when(taskManager.getTestcaseZip(eq(1L), eq("test01")))
                .thenThrow(InformaticsServerException.UNEXPECTED_ERROR);

        mockMvc.perform(get("/api/task/1/testcase/test01"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetTestcases_Success() throws Exception {
        when(taskManager.getTestcasesZip(eq(1L))).thenReturn(mockFile);

        mockMvc.perform(get("/api/task/1/testcases"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + mockFile.getName() + "\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(taskManager).getTestcasesZip(1L);
    }

    @Test
    void testGetTestcases_ServerException() throws Exception {
        when(taskManager.getTestcasesZip(eq(1L)))
                .thenThrow(InformaticsServerException.TASK_NOT_FOUND);

        mockMvc.perform(get("/api/task/1/testcases"))
                .andExpect(status().isNotFound());

        verify(taskManager).getTestcasesZip(1L);
    }

    @Test
    void testAddSingleTestcase_Success() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "inputFile",
                "test01.in",
                "text/plain",
                "input content".getBytes()
        );
        MockMultipartFile outputFile = new MockMultipartFile(
                "outputFile",
                "test01.out",
                "text/plain",
                "output content".getBytes()
        );

        AddTestcasesResult result = new AddTestcasesResult();
        result.getSuccess().add("test01");

        when(taskManager.addTestcase(eq(1L), any(byte[].class), any(byte[].class), eq("test01.in"), eq("test01.out")))
                .thenReturn(result);

        mockMvc.perform(multipart("/api/task/1/testcase")
                        .file(inputFile)
                        .file(outputFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.success[0]").value("test01"));

        verify(taskManager).addTestcase(eq(1L), any(byte[].class), any(byte[].class), eq("test01.in"), eq("test01.out"));
    }

    @Test
    void testAddSingleTestcase_ServerException() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "inputFile",
                "test01.in",
                "text/plain",
                "input content".getBytes()
        );
        MockMultipartFile outputFile = new MockMultipartFile(
                "outputFile",
                "test01.out",
                "text/plain",
                "output content".getBytes()
        );

        when(taskManager.addTestcase(eq(1L), any(byte[].class), any(byte[].class), any(), any()))
                .thenThrow(InformaticsServerException.UNEXPECTED_ERROR);

        mockMvc.perform(multipart("/api/task/1/testcase")
                        .file(inputFile)
                        .file(outputFile))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testAddSingleTestcase_IOException() throws Exception {
        MockMultipartFile inputFile = new MockMultipartFile(
                "inputFile",
                "test01.in",
                "text/plain",
                "input content".getBytes()
        );
        MockMultipartFile outputFile = new MockMultipartFile(
                "outputFile",
                "test01.out",
                "text/plain",
                "output content".getBytes()
        );

        when(taskManager.addTestcase(eq(1L), any(byte[].class), any(byte[].class), any(), any()))
                .thenThrow(InformaticsServerException.UNEXPECTED_ERROR);

        mockMvc.perform(multipart("/api/task/1/testcase")
                        .file(inputFile)
                        .file(outputFile))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testSetPublicTestcases_Success() throws Exception {
        SetPublicTestcasesRequest request = new SetPublicTestcasesRequest(true);

        doNothing().when(taskManager).setPublicTestcase(eq(1L), eq("test01"), eq(true));

        mockMvc.perform(put("/api/task/1/testcases/test01/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(taskManager).setPublicTestcase(1L, "test01", true);
    }

    @Test
    void testSetPublicTestcases_ServerException() throws Exception {
        SetPublicTestcasesRequest request = new SetPublicTestcasesRequest(true);

        doThrow(new InformaticsServerException("testcaseNotFound"))
                .when(taskManager).setPublicTestcase(eq(1L), eq("test01"), eq(true));

        mockMvc.perform(put("/api/task/1/testcases/test01/public")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("testcaseNotFound"));

        verify(log).error(eq("Error during setting public testcases"), any(InformaticsServerException.class));
    }

    @Test
    void testDeleteSingleTestcase_Success() throws Exception {
        doNothing().when(taskManager).removeTestCase(eq(1L), eq("test01"));

        mockMvc.perform(delete("/api/task/1/testcase/test01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isEmpty());

        verify(taskManager).removeTestCase(1L, "test01");
    }

    @Test
    void testDeleteSingleTestcase_Error() throws Exception {
        doThrow(new InformaticsServerException("testcaseNotFound"))
                .when(taskManager).removeTestCase(eq(1L), eq("test01"));

        mockMvc.perform(delete("/api/task/1/testcase/test01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("testcaseNotFound"));

        verify(log).error(eq("Error during deleting testcase"), any(InformaticsServerException.class));
    }
}

