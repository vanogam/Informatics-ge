package ge.freeuni.informatics.cmsintegration.utils;

import ge.freeuni.informatics.model.Language;
import ge.freeuni.informatics.model.entity.task.Task;
import ge.freeuni.informatics.model.entity.task.TestCase;
import ge.freeuni.informatics.model.exception.InformaticsServerException;
import ge.freeuni.informatics.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class TaskConfigurationBuilder {


    @Value(value = "ge.freeuni.informatics.model.entity.task.Task.taskDirectoryAddress")
    private String taskDirectoryAddress;

    private final Task task;
    TaskConfigurationBuilder(Task task) {
        this.task = task;
    }

    public void build() throws InformaticsServerException {
        createInitialFolders();
        createTaskConfigurationYaml();
    }


    public void addTestCases() {

//        for (TestCase testCase : task.getTestCases()) {
//
//            Path inputPath = Paths.get("");
//
//        }
    }

    private void createInitialFolders() throws InformaticsServerException{
        if (createFolder(FileUtils.buildPath(Arrays.asList(taskDirectoryAddress, task.getCode())))) {
            throw new InformaticsServerException("Could not create task directory.");
        }
        if (createFolder(FileUtils.buildPath(Arrays.asList(taskDirectoryAddress, task.getCode(), "input")))) {
            throw new InformaticsServerException("Could not create input directory.");
        }
        if (createFolder(FileUtils.buildPath(Arrays.asList(taskDirectoryAddress, task.getCode(), "output")))) {
            throw new InformaticsServerException("Could not create output directory.");
        }
    }

    private void createTaskConfigurationYaml() throws InformaticsServerException {
        File configFile = new File(FileUtils.buildPath(Arrays.asList(taskDirectoryAddress, task.getCode(), "task.yaml")));
        if (configFile.exists()) {
            if (!configFile.delete()) {
                throw new InformaticsServerException("Could not delete previous yaml file.");
            }
        }

        try {
            if (!configFile.createNewFile()) {
                throw new InformaticsServerException("Could not create new yaml file.");
            }
            OutputStream outputStream = Files.newOutputStream(configFile.toPath());
            outputStream.write(getYamlKeyValue("name", task.getCode()).getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("title", task.getTitle().get(Language.EN.toString())).getBytes(StandardCharsets.UTF_8));
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
        } catch (IOException ex) {
            throw new InformaticsServerException("Could not create new yaml file.", ex);
        }
    }

    private boolean createFolder(String path) {
        return new File(path).mkdirs();
    }

    private String getTimeLimitSec(Integer timeLimitMillis) {
        float timeLimitSec = timeLimitMillis.floatValue() / 1000;
        return Float.toString(timeLimitSec);
    }

    private String getYamlKeyValue(String key, String value) {
        return key + ": " + value + "\n";
    }
}
