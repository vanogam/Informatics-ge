package ge.freeuni.informatics.cmsintegration.utils;

import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.common.model.submission.Submission;
import org.springframework.stereotype.Component;

@Component
public class CMSIntegration implements IJudgeIntegration {

    @Override
    public void addTask(TaskDTO taskDTO) {

    }

    @Override
    public void addSubmission(String fileName, Submission submission) {

    }

    @Override
    public void editTask() {

    }
}
