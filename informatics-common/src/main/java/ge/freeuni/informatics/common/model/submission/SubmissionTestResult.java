package ge.freeuni.informatics.common.model.submission;

import jakarta.persistence.Embeddable;

@Embeddable
public class SubmissionTestResult {

    /**
     * Final score for the test case from 0.0 to 1.0.
     */
    private Float score;

    /**
     * First 1000 symbols of contestant output for the test case.
     */
    private String outcome;

    /**
     * Compilers message.
     */
    private String message;

    /**
     * Time in milliseconds taken to run the test case.
     */
    private Integer time;

    /**
     * Memory in kilobytes used to run the test case.
     */
    private Integer memory;

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
}
