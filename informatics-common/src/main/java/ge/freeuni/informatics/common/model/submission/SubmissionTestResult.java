package ge.freeuni.informatics.common.model.submission;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class SubmissionTestResult {

    /**
     * Final score for the test case from 0.0 to 1.0.
     */
    private Float score;

    @Column(length = 512)
    private String testKey;

    /**
     * First symbols of contestant output for the test case.
     */
    @Column(length = 4000)
    private String outcome;

    /**
     * Compiler message.
     */
    @Column(length = 4000)
    private String message;

    /**
     * Time in milliseconds taken to run the test case.
     */
    private Integer time;

    /**
     * Memory in kilobytes used to run the test case.
     */
    private Integer memory;

    private TestStatus testStatus;

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }

    public TestStatus getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }
}
