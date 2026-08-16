package ge.freeuni.informatics.judgeintegration;

import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.submission.TestStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskScoreType;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.judgeintegration.model.CallbackType;
import ge.freeuni.informatics.judgeintegration.model.KafkaCallback;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Judging progress lives in memory, so a restart leaves submissions with no tracking.
 * These cover both halves of the recovery: late callbacks, and the startup sweep.
 */
public class JudgeRecoveryTest {

    @Mock private KafkaProducerService kafkaProducerService;
    @Mock private SubmissionJpaRepository submissionRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private Logger log;
    @InjectMocks private JudgeIntegration judgeIntegration;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(judgeIntegration, "recoveryMaxAgeHours", 24L);
        ReflectionTestUtils.setField(judgeIntegration, "recoveryEnabled", true);
        clearStaticTracking();
    }

    @SuppressWarnings("unchecked")
    private void clearStaticTracking() {
        ((java.util.Map<Long, ?>) ReflectionTestUtils.getField(JudgeIntegration.class, "testCompletionMap")).clear();
        ((java.util.Map<Long, ?>) ReflectionTestUtils.getField(JudgeIntegration.class, "submissionLocks")).clear();
    }

    private Task task(String... testKeys) {
        Task t = new Task();
        t.setId(1L);
        t.setCode("ballmachine");
        t.setTaskScoreType(TaskScoreType.SUM);
        t.setTaskScoreParameter("1.0");
        t.setTimeLimitMillis(1000);
        t.setMemoryLimitMB(256);
        Contest c = new Contest();
        c.setId(1L);
        t.setContest(c);
        List<Testcase> tcs = new ArrayList<>();
        for (String k : testKeys) {
            Testcase tc = new Testcase();
            tc.setKey(k);
            tc.setInputFileAddress("/f/" + k + ".in");
            tc.setOutputFileAddress("/f/" + k + ".out");
            tcs.add(tc);
        }
        t.setTestCases(tcs);
        return t;
    }

    private Submission submission(long id, SubmissionStatus status, Task task, String... doneKeys) {
        Submission s = new Submission();
        s.setId(id);
        s.setStatus(status);
        s.setTask(task);
        s.setLanguage("CPP");
        s.setFileName("sol.cpp");
        s.setSubmissionTime(new Date());
        List<SubmissionTestResult> results = new ArrayList<>();
        for (String k : doneKeys) {
            SubmissionTestResult r = new SubmissionTestResult();
            r.setTestKey(k);
            r.setScore(1.0f);
            r.setTestStatus(TestStatus.CORRECT);
            results.add(r);
        }
        s.setSubmissionTestResults(results);
        return s;
    }

    private void deliverCallback(KafkaCallback callback) throws Exception {
        Method m = JudgeIntegration.class.getDeclaredMethod("listenToCompletionTopic", String.class);
        m.setAccessible(true);
        m.invoke(judgeIntegration, new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(callback));
    }

    // ---- score rounding ----

    @Test
    void roundsScoresToTwoDecimals() {
        assertEquals(62f, JudgeIntegration.roundScore(62.002f));
        assertEquals(62f, JudgeIntegration.roundScore(61.998f));
        assertEquals(61.99f, JudgeIntegration.roundScore(61.9949f));
        assertEquals(62.01f, JudgeIntegration.roundScore(62.005f));
    }

    @Test
    void roundingLeavesLegitimateFractionsAlone() {
        assertEquals(13.5f, JudgeIntegration.roundScore(13.5f));
        assertEquals(0.25f, JudgeIntegration.roundScore(0.25f));
        assertEquals(0f, JudgeIntegration.roundScore(0f));
        assertEquals(100f, JudgeIntegration.roundScore(100f));
    }

    // ---- 1. self-healing listener ----

    @Test
    void rebuildsTrackingForACallbackThatArrivesAfterRestart() throws Exception {
        Task t = task("01", "02");
        Submission s = submission(7L, SubmissionStatus.RUNNING, t, "01");
        when(submissionRepository.findById(7L)).thenReturn(Optional.of(s));

        // Tracking maps are empty, as they would be after a restart.
        deliverCallback(new KafkaCallback(7L, CallbackType.TEST_COMPLETED, "02", "ok",
                1.0, TestStatus.CORRECT, 0, 10L, 100L, ""));

        // The result was accepted rather than dropped, and the submission was finalised.
        assertEquals(2, s.getSubmissionTestResults().size());
        assertNotEquals(SubmissionStatus.RUNNING, s.getStatus());
        verify(submissionRepository, atLeastOnce()).save(s);
    }

    @Test
    void ignoresACallbackForAnAlreadyFinishedSubmission() throws Exception {
        Task t = task("01");
        Submission s = submission(8L, SubmissionStatus.CORRECT, t, "01");
        when(submissionRepository.findById(8L)).thenReturn(Optional.of(s));

        deliverCallback(new KafkaCallback(8L, CallbackType.TEST_COMPLETED, "01", "ok",
                1.0, TestStatus.CORRECT, 0, 10L, 100L, ""));

        assertEquals(1, s.getSubmissionTestResults().size(), "must not double-record a result");
        assertEquals(SubmissionStatus.CORRECT, s.getStatus());
    }

    // ---- 2. startup reconciliation ----

    @Test
    void reQueuesOnlyTheTestsThatNeverReported() {
        Task t = task("01", "02", "03");
        Submission s = submission(9L, SubmissionStatus.RUNNING, t, "01");
        when(submissionRepository.getAllByStatusIn(any())).thenReturn(List.of(s));

        judgeIntegration.recoverInFlightSubmissions();

        // Test messages are keyed per test so they hash across partitions and workers can share
        // the submission; an unkeyed burst would pile onto one partition and one worker.
        ArgumentCaptor<String> keys = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> published = ArgumentCaptor.forClass(String.class);
        verify(kafkaProducerService, times(2))
                .sendMessage(eq("submission-topic"), keys.capture(), published.capture());
        String all = String.join("\n", published.getAllValues());
        assertTrue(all.contains("\"testId\":\"02\""), all);
        assertTrue(all.contains("\"testId\":\"03\""), all);
        assertFalse(all.contains("\"testId\":\"01\""), "test 01 already had a result");
        assertEquals(List.of("9:02", "9:03"), keys.getAllValues());
    }

    @Test
    void finalisesASubmissionWhoseTestsAllReported() {
        Task t = task("01", "02");
        Submission s = submission(10L, SubmissionStatus.RUNNING, t, "01", "02");
        when(submissionRepository.getAllByStatusIn(any())).thenReturn(List.of(s));

        judgeIntegration.recoverInFlightSubmissions();

        verifyNoInteractions(kafkaProducerService);
        assertEquals(SubmissionStatus.CORRECT, s.getStatus());
        assertEquals(2.0f, s.getScore());
    }

    @Test
    void reQueuesFromCompilationWhenItNeverGotThatFar() {
        Task t = task("01", "02");
        Submission s = submission(11L, SubmissionStatus.IN_QUEUE, t);
        when(submissionRepository.getAllByStatusIn(any())).thenReturn(List.of(s));

        judgeIntegration.recoverInFlightSubmissions();

        ArgumentCaptor<String> published = ArgumentCaptor.forClass(String.class);
        verify(kafkaProducerService).sendMessage(eq("submission-topic"), published.capture());
        assertTrue(published.getValue().contains("COMPILATION"), published.getValue());
    }

    @Test
    void failsSubmissionsTooOldToReJudge() {
        Task t = task("01");
        Submission s = submission(12L, SubmissionStatus.RUNNING, t);
        s.setSubmissionTime(new Date(System.currentTimeMillis() - 48L * 3600_000L));
        when(submissionRepository.getAllByStatusIn(any())).thenReturn(List.of(s));

        judgeIntegration.recoverInFlightSubmissions();

        verifyNoInteractions(kafkaProducerService);
        assertEquals(SubmissionStatus.SYSTEM_ERROR, s.getStatus());
        assertEquals(0f, s.getScore());
    }

    @Test
    void doesNothingWhenDisabled() {
        ReflectionTestUtils.setField(judgeIntegration, "recoveryEnabled", false);

        judgeIntegration.recoverInFlightSubmissions();

        verifyNoInteractions(submissionRepository);
    }
}
