package ge.freeuni.informatics.repository.task;

import ge.freeuni.informatics.common.model.task.TaskFile;
import ge.freeuni.informatics.common.model.task.TaskFileKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskFileRepository extends JpaRepository<TaskFile, Long> {

    List<TaskFile> findByTaskIdOrderByKindAscFileNameAsc(Long taskId);

    List<TaskFile> findByTaskIdAndKindOrderByFileNameAsc(Long taskId, TaskFileKind kind);

    List<TaskFile> findByTaskIdAndVisibleToContestantsOrderByFileNameAsc(Long taskId, boolean visibleToContestants);

    TaskFile findFirstByTaskIdAndKindAndFileName(Long taskId, TaskFileKind kind, String fileName);
}