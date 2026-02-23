# Acceptance Tests Architecture

## Overview

The acceptance tests use **real** components with embedded infrastructure to provide realistic end-to-end testing.

## Components

### 1. **Real Application Components**
- **JudgeIntegration** (real): Sends Kafka messages for compilation and testing
- **SubmissionManager** (real): Manages submission lifecycle
- **All other Spring components**: Real implementations

### 2. **Embedded Infrastructure**
- **H2 Database**: In-memory database with PostgreSQL compatibility mode
- **Embedded Kafka**: Spring Kafka Test's embedded broker
  - Topics: `submission-topic`, `submission-callback`
  - Port: 9092 (embedded)
  - Auto-configured via `EmbeddedKafkaConfig`

### 3. **Mock Worker**
- **MockKafkaWorker**: Simulates the worker/judge behavior
  - Listens to `submission-topic`
  - Sends callbacks to `submission-callback`
  - Simulates compilation (always succeeds)
  - Simulates test execution based on configured scores

## Test Flow

```
Test → SubmissionController → SubmissionManager → Real JudgeIntegration
                                                           ↓
                                                    [Kafka: submission-topic]
                                                           ↓
                                                    MockKafkaWorker
                                                           ↓
                                                    [Kafka: submission-callback]
                                                           ↓
                                                    Real JudgeIntegration
                                                           ↓
                                                    Updates Submission in DB
```

## Mock Task Model

Tests use a simple mock task model:
- **Input**: n (where 1 ≤ n ≤ 10)
- **Output**: n
- **10 testcases** with keys "1" through "10"

### Score Calculation

MockKafkaWorker determines test pass/fail based on score percentage:
- **100%**: All tests 1-10 pass
- **60%**: Tests 1-6 pass, tests 7-10 fail
- **40%**: Tests 1-4 pass, tests 5-10 fail
- **0%**: All tests fail

Example usage:
```java
Long submissionId = submitSolution("student1", taskA.getId());
mockKafkaWorker.setSubmissionScore(submissionId, 60); // 60% score
waitForSubmissionsToComplete();
```

## Database Lifecycle

- **DDL Strategy**: `create-drop`
- **Lifecycle**: `@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)`
- **Result**: Fresh database for each test method

## Configuration Files

### Test Configuration
- **AcceptanceTestConfig.java**: Main test configuration
  - Imports `InformaticsApplication` (main app config)
  - Imports `EmbeddedKafkaConfig` (Kafka setup)
  - Configures async executor

### Properties
- **application-acceptance.properties**: Test-specific properties
  - H2 database configuration
  - Embedded Kafka bootstrap servers
  - File storage in `/tmp/informatics-test/`

### Logging
- **logback-test.xml**: Test logging configuration
  - `mock-worker.log`: MockKafkaWorker activity
  - `judge-integration.log`: Real JudgeIntegration activity
  - `submissions.log`: Submission processing
  - `acceptance-tests.log`: General test logs

## Disabled Components

- **MockJudgeIntegration**: Commented out `@Component` and `@Primary`
  - Previously bypassed Kafka for faster tests
  - Now replaced with real Kafka flow for more realistic testing

## Running Tests

### Via Maven
```bash
# Run all acceptance tests
mvn verify -Pacceptance-tests

# Run specific test
mvn test -Dtest=ContestAcceptanceTest
```

### Via IDE
Just run the test class normally. Spring will:
1. Start embedded Kafka
2. Create H2 database
3. Load all Spring beans
4. Execute tests

## Benefits of This Architecture

1. **Realistic**: Tests actual Kafka message flow
2. **Isolated**: Each test gets fresh database and context
3. **Fast**: Embedded infrastructure starts quickly
4. **Maintainable**: Real components mean less mock code to maintain
5. **Reliable**: Catches integration issues that unit tests miss

## Troubleshooting

### Kafka Connection Issues
- Ensure no other Kafka is running on port 9092
- Check logs in `target/test-logs/judge-integration.log`

### Database Issues
- Check H2 mode is PostgreSQL
- Verify `create-drop` is working (check logs)
- Ensure `@DirtiesContext` is on test class

### Submission Timeout
- Check `mock-worker.log` for worker activity
- Verify MockKafkaWorker is receiving messages
- Increase timeout in `waitForSubmissionsToComplete()`
