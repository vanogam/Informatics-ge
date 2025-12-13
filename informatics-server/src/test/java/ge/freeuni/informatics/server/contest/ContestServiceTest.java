package ge.freeuni.informatics.server.contest;

import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.TaskResultDTO;
import ge.freeuni.informatics.common.events.ContestChangeEvent;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ScoringType;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.contest.ContestantResultJpaRepository;
import ge.freeuni.informatics.repository.contestroom.ContestRoomJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContestServiceTest {

    @Mock
    private IContestManager contestManager;

    @Mock
    private TaskScheduler taskScheduler;

    @Mock
    private ContestJpaRepository contestRepository;

    @Mock
    private ContestRoomJpaRepository contestRoomJpaRepository;

    @Mock
    private ContestantResultJpaRepository contestantResultJpaRepository;

    @Mock
    private Logger log;

    @InjectMocks
    private ContestService contestService;

    private ContestDTO testContest;
    private Contest testContestEntity;
    private ContestRoom testRoom;
    private User testUser;
    private Task testTask;
    private Submission testSubmission;

    @BeforeEach
    void setUp() {
        // Create test contest DTO
        testContest = new ContestDTO();
        testContest.setId(1L);
        testContest.setName("Test Contest");
        testContest.setRoomId(1L);
        testContest.setStartDate(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago
        testContest.setEndDate(new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now
        testContest.setStatus(ContestStatus.LIVE);
        testContest.setScoringType(ScoringType.BEST_SUBMISSION);
        testContest.setUpsolvingAfterFinish(true);
        testContest.setVersion(1);
        testContest.setStandings(new TreeSet<>());
        testContest.setUpsolvingStandings(new ArrayList<>());

        // Create test contest entity
        testContestEntity = new Contest();
        testContestEntity.setId(1L);
        testContestEntity.setName("Test Contest");
        testContestEntity.setRoomId(1L);
        testContestEntity.setStartDate(testContest.getStartDate());
        testContestEntity.setEndDate(testContest.getEndDate());
        // Status is calculated from dates, so we set appropriate dates
        testContestEntity.setScoringType(ScoringType.BEST_SUBMISSION);
        testContestEntity.setUpsolvingAfterFinished(true);
        testContestEntity.setVersion(1);

        // Create test room
        testRoom = new ContestRoom();
        testRoom.setId(1L);
        testRoom.setOpen(true);
        testRoom.setTeachers(new HashSet<>());
        testRoom.setParticipants(new HashSet<>());

        // Create test user
        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testRoom.getParticipants().add(testUser);

        // Create test task
        testTask = new Task();
        testTask.setId(200L);
        testTask.setCode("TASK1");
        testTask.setTitle("Test Task");

        // Create test submission
        testSubmission = new Submission();
        testSubmission.setId(1L);
        testSubmission.setUser(testUser);
        testSubmission.setTask(testTask);
        testSubmission.setContest(testContestEntity);
        testSubmission.setScore(100.0f);
        testSubmission.setSubmissionTime(new Date());

        ArgumentCaptor<ContestDTO> contestCaptor = ArgumentCaptor.forClass(ContestDTO.class);
        Mockito.lenient().when(contestManager.updateContest(contestCaptor.capture())).thenAnswer(invocation -> {
            testContest = invocation.getArgument(0);
            return testContest;
        });
    }

    @Test
    void testAddSubmission_UpdatesStandings_FirstSubmission() throws InformaticsServerException {
        // Arrange
        ContestantResultDTO contestantResult = ContestantResultDTO.builder()
                .contestantId(100L)
                .totalScore(0.0f)
                .taskResults(new HashMap<>())
                .build();
        testContest.getStandings().add(contestantResult);

        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        when(contestRoomJpaRepository.getReferenceById(1L)).thenReturn(testRoom);

        SubmissionEvent event = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event);

        verify(contestManager).updateContest(any(ContestDTO.class));
        
        ContestantResultDTO updatedResult = testContest.getStandings().stream()
                .filter(r -> r.contestantId() == 100L)
                .findFirst()
                .orElse(null);
        
        assertNotNull(updatedResult);
        assertEquals(100.0f, updatedResult.totalScore());
        assertNotNull(updatedResult.taskResults().get("TASK1"));
        TaskResultDTO taskResult = updatedResult.taskResults().get("TASK1");
        assertEquals(100.0f, taskResult.getScore());
        assertEquals(1, taskResult.getAttempts());
        assertNotNull(taskResult.getSuccessTime());
    }

    @Test
    void testAddSubmission_UpdatesStandings_MultipleSubmissions_BetterScore() throws InformaticsServerException {
        // Arrange
        Map<String, TaskResultDTO> taskResults = new HashMap<>();
        taskResults.put("TASK1", new TaskResultDTO("TASK1", 50.0f, 1, 1000L));
        ContestantResultDTO contestantResult = ContestantResultDTO.builder()
                .contestantId(100L)
                .totalScore(50.0f)
                .taskResults(taskResults)
                .build();
        testContest.getStandings().add(contestantResult);

        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        when(contestRoomJpaRepository.getReferenceById(1L)).thenReturn(testRoom);

        // Act - submit better score
        testSubmission.setScore(100.0f);
        SubmissionEvent event = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event);

        // Assert
        ContestantResultDTO updatedResult = testContest.getStandings().stream()
                .filter(r -> r.contestantId() == 100L)
                .findFirst()
                .orElse(null);
        
        assertNotNull(updatedResult);
        assertEquals(100.0f, updatedResult.totalScore(), "Total score should be updated to best score");
        TaskResultDTO taskResult = updatedResult.taskResults().get("TASK1");
        assertEquals(100.0f, taskResult.getScore(), "Task score should be updated");
        assertEquals(2, taskResult.getAttempts(), "Attempts should be incremented");
    }

    @Test
    void testAddSubmission_UpdatesStandings_MultipleSubmissions_WorseScore() throws InformaticsServerException {
        // Arrange
        Map<String, TaskResultDTO> taskResults = new HashMap<>();
        taskResults.put("TASK1", new TaskResultDTO("TASK1", 100.0f, 1, 1000L));
        ContestantResultDTO contestantResult = ContestantResultDTO.builder()
                .contestantId(100L)
                .totalScore(100.0f)
                .taskResults(taskResults)
                .build();
        testContest.getStandings().add(contestantResult);

        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        when(contestRoomJpaRepository.getReferenceById(1L)).thenReturn(testRoom);

        // Act - submit worse score
        testSubmission.setScore(50.0f);
        SubmissionEvent event = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event);

        // Assert
        ContestantResultDTO updatedResult = testContest.getStandings().stream()
                .filter(r -> r.contestantId() == 100L)
                .findFirst()
                .orElse(null);
        
        assertNotNull(updatedResult);
        assertEquals(100.0f, updatedResult.totalScore(), "Total score should keep best score with BEST_SUBMISSION");
        TaskResultDTO taskResult = updatedResult.taskResults().get("TASK1");
        assertEquals(100.0f, taskResult.getScore(), "Task score should keep best score");
        assertEquals(2, taskResult.getAttempts(), "Attempts should be incremented");
        assertEquals(1000L, taskResult.getSuccessTime(), "Success time should remain the original (better score)");
    }

    @Test
    void testAddSubmission_UpdatesStandings_MultipleUsers() throws InformaticsServerException {
        // Arrange
        ContestantResultDTO contestantResult1 = ContestantResultDTO.builder()
                .contestantId(100L)
                .totalScore(0.0f)
                .taskResults(new HashMap<>())
                .build();
        
        ContestantResultDTO contestantResult2 = ContestantResultDTO.builder()
                .contestantId(101L)
                .totalScore(0.0f)
                .taskResults(new HashMap<>())
                .build();
        
        testContest.getStandings().add(contestantResult1);
        testContest.getStandings().add(contestantResult2);

        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        when(contestRoomJpaRepository.getReferenceById(1L)).thenReturn(testRoom);

        // Act - user 100 submits
        testSubmission.setScore(100.0f);
        SubmissionEvent event1 = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event1);

        // Act - user 101 submits
        User user2 = new User();
        user2.setId(101L);
        testRoom.getParticipants().add(user2);
        Submission submission2 = new Submission();
        submission2.setUser(user2);
        submission2.setTask(testTask);
        submission2.setContest(testContestEntity);
        submission2.setScore(80.0f);
        submission2.setSubmissionTime(new Date());
        
        SubmissionEvent event2 = new SubmissionEvent(submission2);
        contestService.addSubmission(event2);

        // Assert
        ContestantResultDTO result1 = testContest.getStandings().stream()
                .filter(r -> r.contestantId() == 100L)
                .findFirst()
                .orElse(null);
        ContestantResultDTO result2 = testContest.getStandings().stream()
                .filter(r -> r.contestantId() == 101L)
                .findFirst()
                .orElse(null);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(100.0f, result1.totalScore());
        assertEquals(80.0f, result2.totalScore());
    }

    @Test
    void testAddSubmission_StandingsIntegrity_MaintainsOrder() throws InformaticsServerException {
        ContestantResultDTO contestantResult1 = ContestantResultDTO.builder()
                .contestantId(100L)
                .totalScore(0.0f)
                .taskResults(new HashMap<>())
                .build();
        
        ContestantResultDTO contestantResult2 = ContestantResultDTO.builder()
                .contestantId(101L)
                .totalScore(0.0f)
                .taskResults(new HashMap<>())
                .build();
        
        testContest.getStandings().add(contestantResult1);
        testContest.getStandings().add(contestantResult2);

        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        when(contestRoomJpaRepository.getReferenceById(1L)).thenReturn(testRoom);

        // Act - user 101 submits first to get 80.0f score
        User user2 = new User();
        user2.setId(101L);
        testRoom.getParticipants().add(user2);
        Submission submission2 = new Submission();
        submission2.setUser(user2);
        submission2.setTask(testTask);
        submission2.setContest(testContestEntity);
        submission2.setScore(80.0f);
        submission2.setSubmissionTime(new Date());
        
        SubmissionEvent event2 = new SubmissionEvent(submission2);
        contestService.addSubmission(event2);

        // Act - user 100 submits to get 50.0f score initially
        testSubmission.setScore(50.0f);
        SubmissionEvent event1 = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event1);

        // Act - user 100 submits better score (100.0f) that should move them to first place
        testSubmission.setScore(100.0f);
        SubmissionEvent event = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event);
        
        // Assert - standings should be sorted by totalScore descending
        // Get the final state from the last captured contest (which is a new object returned by the mock)
        List<ContestantResultDTO> standingsList = new ArrayList<>(testContest.getStandings());
        assertEquals(100.0f, standingsList.get(0).totalScore(), "First place should have highest score");
        assertEquals(80.0f, standingsList.get(1).totalScore(), "Second place should have lower score");
    }

    @Test
    void testAddSubmission_ThrowsException_WhenUserNotMember() {
        // Arrange
        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        ContestRoom roomWithDifferentMembers = new ContestRoom();
        roomWithDifferentMembers.setId(1L);
        roomWithDifferentMembers.setParticipants(new HashSet<>()); // No members
        roomWithDifferentMembers.setTeachers(new HashSet<>()); // No members

        when(contestRoomJpaRepository.getReferenceById(1L)).thenReturn(roomWithDifferentMembers);

        // Act & Assert
        SubmissionEvent event = new SubmissionEvent(testSubmission);
        assertThrows(InformaticsServerException.class, () -> contestService.addSubmission(event));
    }

    @Test
    void testAddSubmission_UpsolvingSubmission_WhenContestNotLive() throws InformaticsServerException {
        // Arrange - contest is not in liveContests (already ended)
        // Ensure liveContests map remains empty (do not activate)

        when(contestRepository.getReferenceById(1L)).thenReturn(testContestEntity);
        when(contestantResultJpaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        testSubmission.setScore(100.0f);
        SubmissionEvent event = new SubmissionEvent(testSubmission);
        contestService.addSubmission(event);

        // Assert
        verify(contestManager).updateContest(any(ContestDTO.class));
        verify(contestantResultJpaRepository).save(any());
    }

    @Test
    void testContestEnd_TransfersTasksToArchive_WithUpsolving() {
        // Arrange
        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        ArgumentCaptor<ContestDTO> contestCaptor = ArgumentCaptor.forClass(ContestDTO.class);

        // Act - simulate contest end (past date means immediate execution)
        testContest.setEndDate(new Date(System.currentTimeMillis() - 1000)); // Past date
        ReflectionTestUtils.invokeMethod(contestService, "scheduleContestEnd", testContest);

        // Verify contest end was executed immediately
        verify(contestManager).updateContest(any(ContestDTO.class));
        
        // Verify contest status and upsolving settings
        assertEquals(ContestStatus.PAST, testContest.getStatus());
        assertTrue(testContest.isUpsolving(), "Upsolving should be enabled after contest ends");
        
        // Verify contest is removed from liveContests
        @SuppressWarnings("unchecked")
        Map<Long, ?> liveContestsAfterEnd = (Map<Long, ?>) 
                ReflectionTestUtils.getField(contestService, "liveContests");
        assertNotNull(liveContestsAfterEnd);
        assertFalse(liveContestsAfterEnd.containsKey(1L), "Contest should be removed from liveContests");
    }

    @Test
    void testContestEnd_TransfersTasksToArchive_WithoutUpsolving() {
        // Arrange
        testContest.setUpsolvingAfterFinish(false);
        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        // Act
        testContest.setEndDate(new Date(System.currentTimeMillis() - 1000));
        ReflectionTestUtils.invokeMethod(contestService, "scheduleContestEnd", testContest);

        // Verify
        verify(contestManager).updateContest(any(ContestDTO.class));
        assertEquals(ContestStatus.PAST, testContest.getStatus());
        assertFalse(testContest.isUpsolving(), "Upsolving should not be enabled");
    }

    @Test
    void testContestStart_SchedulesFutureContest() {
        // Arrange
        Date futureDate = new Date(System.currentTimeMillis() + 3600000); // 1 hour from now
        testContest.setStartDate(futureDate);
        testContest.setStatus(ContestStatus.FUTURE);

        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn((ScheduledFuture) mockFuture);

        // Act
        ReflectionTestUtils.invokeMethod(contestService, "scheduleContestStart", testContest);

        // Assert
        verify(taskScheduler).schedule(any(Runnable.class), eq(futureDate.toInstant()));
    }

    @Test
    void testContestStart_StartsImmediately_WhenPast() {
        // Arrange
        Date pastDate = new Date(System.currentTimeMillis() - 3600000); // 1 hour ago
        testContest.setStartDate(pastDate);
        testContest.setStatus(ContestStatus.FUTURE);
        // End date is in the future (set in setUp), so it should be scheduled

        when(contestRepository.getReferenceById(1L)).thenReturn(testContestEntity);

        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        when(taskScheduler.schedule(any(Runnable.class), instantCaptor.capture())).thenReturn((ScheduledFuture) mockFuture);

        // Act
        ReflectionTestUtils.invokeMethod(contestService, "scheduleContestStart", testContest);

        // Assert - start should not be scheduled (runs immediately), but end should be scheduled
        verify(taskScheduler, times(1)).schedule(any(Runnable.class), any(Instant.class));

        // Verify the scheduled time is for the end date
        Instant capturedInstant = instantCaptor.getValue();
        long expectedEndTime = testContest.getEndDate().toInstant().toEpochMilli();
        assertTrue(Math.abs(capturedInstant.toEpochMilli() - expectedEndTime) < 1000,
                "Contest end should be scheduled at the correct time");

        // Verify contest was updated to LIVE status
        ArgumentCaptor<ContestDTO> contestCaptor = ArgumentCaptor.forClass(ContestDTO.class);
        verify(contestManager, timeout(1000)).updateContest(contestCaptor.capture());
        assertEquals(ContestStatus.LIVE, contestCaptor.getValue().getStatus());
    }

    @Test
    void testContestStart_OnTime_ScheduledCorrectly() {
        // Arrange
        long scheduledTime = System.currentTimeMillis() + 5000; // 5 seconds from now
        Date startDate = new Date(scheduledTime);
        testContest.setStartDate(startDate);
        testContest.setStatus(ContestStatus.FUTURE);

        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Instant> dateCaptor = ArgumentCaptor.forClass(Instant.class);
        when(taskScheduler.schedule(any(Runnable.class), dateCaptor.capture())).thenReturn((ScheduledFuture) mockFuture);

        // Act
        ReflectionTestUtils.invokeMethod(contestService, "scheduleContestStart", testContest);

        // Assert
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        // Allow some tolerance for test execution time
        assertTrue(Math.abs(dateCaptor.getValue().toEpochMilli() - scheduledTime) < 1000,
                "Contest should be scheduled at the correct time");
    }

    @Test
    void testContestEnd_ScheduledCorrectly() {
        // Arrange
        long scheduledTime = System.currentTimeMillis() + 5000; // 5 seconds from now
        Date endDate = new Date(scheduledTime);
        testContest.setEndDate(endDate);

        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        when(taskScheduler.schedule(any(Runnable.class), instantCaptor.capture())).thenReturn((ScheduledFuture) mockFuture);

        // Act
        ReflectionTestUtils.invokeMethod(contestService, "scheduleContestEnd", testContest);

        // Assert
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
        Instant capturedInstant = instantCaptor.getValue();
        assertTrue(Math.abs(capturedInstant.toEpochMilli() - scheduledTime) < 1000,
                "Contest end should be scheduled at the correct time");
    }

    @Test
    void testChangeContest_SchedulesStart_ForFutureContest() {
        // Arrange
        testContest.setStatus(ContestStatus.FUTURE);
        testContest.setStartDate(new Date(System.currentTimeMillis() + 3600000));

        ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn((ScheduledFuture) mockFuture);

        // Act
        ContestChangeEvent event = new ContestChangeEvent(testContest);
        contestService.changeContest(event);

        // Assert
        verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void testChangeContest_UpdatesLiveContest_ReschedulesEnd_WhenEndDateChanges() {
        // Arrange
        // Activate contest to populate liveContests with LiveContestState
        ScheduledFuture<?> initialMockFuture = mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn((ScheduledFuture) initialMockFuture);
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        Date newEndDate = new Date(System.currentTimeMillis() + 7200000); // 2 hours from now
        testContest.setEndDate(newEndDate);

        ScheduledFuture<?> newMockFuture = mock(ScheduledFuture.class);
        when(taskScheduler.schedule(any(Runnable.class), any(Instant.class))).thenReturn((ScheduledFuture) newMockFuture);

        // Act
        // Status is calculated from dates, so set dates appropriately
        ContestChangeEvent event = new ContestChangeEvent(testContest);
        contestService.changeContest(event);

        // Assert - should schedule the new end date (verify was called at least once for the new schedule)
        verify(taskScheduler, atLeastOnce()).schedule(any(Runnable.class), any(Instant.class));
    }

    @Test
    void testGetStandings_ReturnsLiveContestStandings() throws InformaticsServerException {
        // Arrange
        ContestantResultDTO contestantResult = ContestantResultDTO.builder()
                .contestantId(100L)
                .totalScore(100.0f)
                .taskResults(new HashMap<>())
                .build();
        testContest.getStandings().add(contestantResult);

        // Activate contest to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);

        // Act
        List<ContestantResultDTO> result = contestService.getStandings(1L, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().contestantId());
        verify(contestManager, never()).getStandings(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testGetStandings_ReturnsFromContestManager_WhenNotLive() throws InformaticsServerException {
        // Arrange
        // Ensure liveContests map remains empty (do not activate)

        when(contestManager.getStandings(1L, 0, 10)).thenReturn(new ArrayList<>());

        // Act
        List<ContestantResultDTO> result = contestService.getStandings(1L, 0, 10);

        // Assert
        assertNotNull(result);
        verify(contestManager).getStandings(1L, 0, 10);
    }

    @Test
    void testGetLiveContests() {
        // Arrange
        // Activate two contests to populate liveContests with LiveContestState
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", testContest);
        ContestDTO anotherContest = new ContestDTO();
        anotherContest.setId(2L);
        anotherContest.setName("Test Contest 2");
        anotherContest.setRoomId(1L);
        anotherContest.setStartDate(new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago
        anotherContest.setEndDate(new Date(System.currentTimeMillis() + 3600000)); // 1 hour from now
        anotherContest.setStatus(ContestStatus.LIVE);
        anotherContest.setVersion(1);
        anotherContest.setStandings(new TreeSet<>());
        anotherContest.setUpsolvingStandings(new ArrayList<>());
        ReflectionTestUtils.invokeMethod(contestService, "activateContest", anotherContest);

        // Act
        List<Long> result = contestService.getLiveContests();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }
}
