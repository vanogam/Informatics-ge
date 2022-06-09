package ge.freeuni.informatics.judgeintegration;

public interface IJudgeIntegration {

    void addTask();

    void addSubmission(String fileName, Integer taskId, Integer contestId);

    void editTask();


}
