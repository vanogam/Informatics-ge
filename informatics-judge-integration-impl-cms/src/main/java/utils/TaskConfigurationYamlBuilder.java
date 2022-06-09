package utils;

import ge.freeuni.informatics.model.Language;
import ge.freeuni.informatics.model.entity.task.Task;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class TaskConfigurationYamlBuilder {


    @Value("ge.freeuni.informatics.model.entity.task.Task.taskDirectory")
    private static String taskDirectory;

    private static final String configName = "task.yaml";

    public static void createTaskConfigurationYaml(Task task) throws IOException {
        File configFile = new File(FileUtils.buildPath(Arrays.asList(taskDirectory, task.getCode(), configName)));
        boolean ignored = configFile.mkdirs();
        if (configFile.exists()) {
            ignored = configFile.delete();
        }
        ignored = configFile.createNewFile();

        OutputStream outputStream = Files.newOutputStream(configFile.toPath());
        outputStream.write(getYamlKeyValue("name", task.getCode()).getBytes(StandardCharsets.UTF_8));
        outputStream.write(getYamlKeyValue("title", task.getTitle().get(Language.EN)).getBytes(StandardCharsets.UTF_8));
        outputStream.write(getYamlKeyValue("n_input", "0").getBytes(StandardCharsets.UTF_8));
        outputStream.write(getYamlKeyValue("score_mode", "max_subtask").getBytes(StandardCharsets.UTF_8));
        outputStream.write(getYamlKeyValue("time_limit", getTimeLimitSec(task.getTimeLimitMillis())).getBytes(StandardCharsets.UTF_8));
        outputStream.write(getYamlKeyValue("memory_limit", task.getMemoryLimitMB().toString()).getBytes());
        outputStream.write(getYamlKeyValue("public_testcases", "all").getBytes());
        outputStream.write(getYamlKeyValue("total_value", "100.0").getBytes());
        outputStream.write(getYamlKeyValue("score_type", task.getTaskScoreType().getCode()).getBytes());
        outputStream.write(getYamlKeyValue("feedback_level", "full").getBytes());
        outputStream.write(getYamlKeyValue("infile", "\"\"").getBytes());
        outputStream.write(getYamlKeyValue("outfile", "\"\"").getBytes());
        outputStream.write(getYamlKeyValue("task_type", "Batch").getBytes());
        outputStream.close();
    }

    private static String getTimeLimitSec(Integer timeLimitMillis) {
        float timeLimitSec = timeLimitMillis.floatValue() / 1000;
        return Float.toString(timeLimitSec);
    }

    private static String getYamlKeyValue(String key, String value) {
        return key + ": " + value + "\n";
    }
}
