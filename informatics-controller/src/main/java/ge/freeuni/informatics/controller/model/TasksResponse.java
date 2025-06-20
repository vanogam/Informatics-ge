package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.model.contest.ContestantResult;

import java.util.List;

public class TasksResponse extends InformaticsResponse {

    private List<TaskDTO> tasks;

    private ContestantResult contestantResult;

    public TasksResponse(String status, String message) {
        super(message);
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public ContestantResult getContestantResult() {
        return contestantResult;
    }

    public void setContestantResult(ContestantResult contestantResult) {
        this.contestantResult = contestantResult;
    }
}
