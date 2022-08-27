package ge.freeuni.informatics.cmsintegration.utils;

import ge.freeuni.informatics.cmsintegration.manager.CmsCommunicationManager;
import ge.freeuni.informatics.cmsintegration.repository.ICmsIntegrationRepository;
import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.common.model.submission.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CMSIntegration implements IJudgeIntegration {

    @Autowired
    ICmsIntegrationRepository cmsIntegrationRepository;

    @Autowired
    CmsCommunicationManager cmsCommunicationManager;

    @Autowired
    TaskConfigurationBuilder builder;

    @Override
    public void addTask(TaskDTO taskDTO) throws InformaticsServerException {
        Task task = TaskDTO.fromDTO(taskDTO);
        String configFolder = builder.name(taskDTO.getCode())
            .title(getTitle(taskDTO))
            .timeLimit(taskDTO.getTimeLimitMillis())
            .memoryLimit(taskDTO.getMemoryLimitMB())
            .scoreType(taskDTO.getTaskScoreType().getCode())
            .scoreTypeParameter(taskDTO.getTaskScoreParameter())
            .scoreMode(taskDTO.getTaskScoreParameter())
            .build();

        task.setConfigAddress(configFolder);
        task = cmsIntegrationRepository.updateTask(task);
        cmsCommunicationManager.addTask(task);
    }

    @Override
    public void setTestcases(Task task) throws InformaticsServerException {
        cmsCommunicationManager.addTestcases(task);
    }

    @Override
    public void addSubmission(Task task, Submission submission) throws InformaticsServerException {
        cmsCommunicationManager.addSubmission(submission, task);
    }

    @Override
    public void editTask() {

    }

    private String getTitle(TaskDTO taskDTO) {
        if (taskDTO.getTitle().containsKey(Language.EN.name())) {
            return taskDTO.getTitle().get(Language.EN.name());
        }
        return taskDTO.getCode();
    }
}
