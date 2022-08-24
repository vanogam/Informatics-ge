package ge.freeuni.informatics.cmsintegration.utils;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.utils.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
public class TaskConfigurationBuilder {

    @Autowired
    Logger logger;

    @Value("${ge.freeuni.informatics.Task.taskDirectoryAddress}")
    private String taskDirectoryAddress;

    private String name;

    private String title;

    private Integer nInput = 0;

    private String scoreMode = "max";

    private Boolean tokenMode = false;

    private Integer timeLimit;

    private Integer memoryLimit;

    private String publicTestcases = "all";

    private String totalValue = "100";

    private String scoreType;

    private String scoreTypeParameter;

    private String feedbackLevel = "full";

    private String infile = "\"\"";

    private String outfile = "\"\"";

    private String taskType = "Batch";


    public TaskConfigurationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public TaskConfigurationBuilder title(String title) {
        this.title = title;
        return this;
    }

    public TaskConfigurationBuilder nInput(Integer nInput) {
        this.nInput = nInput;
        return this;
    }

    public TaskConfigurationBuilder scoreMode(String scoreMode) {
        this.scoreMode = scoreMode;
        return this;
    }

    public TaskConfigurationBuilder tokenMode(Boolean tokenMode) {
        this.tokenMode = tokenMode;
        return this;
    }

    public TaskConfigurationBuilder timeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
        return this;
    }

    public TaskConfigurationBuilder memoryLimit(Integer memoryLimit) {
        this.memoryLimit = memoryLimit;
        return this;
    }

    public TaskConfigurationBuilder publicTestcases(String publicTestcases) {
        this.publicTestcases = publicTestcases;
        return this;
    }

    public TaskConfigurationBuilder totalValue(String totalValue) {
        this.totalValue = totalValue;
        return this;
    }

    public TaskConfigurationBuilder scoreType(String scoreType) {
        this.scoreType = scoreType;
        return this;
    }

    public TaskConfigurationBuilder scoreTypeParameter(String scoreTypeParameter) {
        this.scoreTypeParameter = scoreTypeParameter;
        return this;
    }


    public TaskConfigurationBuilder feedbackLevel(String feedbackLevel) {
        this.feedbackLevel = feedbackLevel;
        return this;
    }

    public TaskConfigurationBuilder infile(String infile) {
        this.infile = infile;
        return this;
    }

    public TaskConfigurationBuilder outfile(String outfile) {
        this.outfile = outfile;
        return this;
    }

    public TaskConfigurationBuilder taskType(String taskType) {
        this.taskType = taskType;
        return this;
    }

    public String build() throws InformaticsServerException {
        String configFolder = createInitialFolders();
        createTaskConfigurationYaml();
        return configFolder;
    }


    public void addTestCases() {

//        for (TestCase testCase : task.getTestCases()) {
//
//            Path inputPath = Paths.get("");
//
//        }
    }

    private String createInitialFolders() throws InformaticsServerException {
        try {
            createFolder(FileUtils.buildPath(taskDirectoryAddress, name));
            createFolder(FileUtils.buildPath(taskDirectoryAddress, name, "input"));
            createFolder(FileUtils.buildPath(taskDirectoryAddress, name, "output"));
        } catch (IOException ex) {
            logger.error("Could not create output directory.", ex);
            throw new InformaticsServerException("Could not create output directory.", ex);
        }
        return FileUtils.buildPath(taskDirectoryAddress, name);
    }

    private void createTaskConfigurationYaml() throws InformaticsServerException {
        File configFile = new File(FileUtils.buildPath(taskDirectoryAddress, name, "task.yaml"));
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
            outputStream.write(getYamlKeyValue("name", name).getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("title", title).getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("n_input", nInput.toString()).getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("score_mode", scoreMode).getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("token_mode", tokenMode ? "enabled" : "disabled").getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("time_limit", getTimeLimitSec(timeLimit)).getBytes(StandardCharsets.UTF_8));
            outputStream.write(getYamlKeyValue("memory_limit", memoryLimit.toString()).getBytes());
            outputStream.write(getYamlKeyValue("public_testcases", publicTestcases).getBytes());
            outputStream.write(getYamlKeyValue("total_value", totalValue).getBytes());
            outputStream.write(getYamlKeyValue("score_type", scoreType).getBytes());
            outputStream.write(getYamlKeyValue("score_type_parameters", scoreTypeParameter).getBytes());
            outputStream.write(getYamlKeyValue("feedback_level", feedbackLevel).getBytes());
            outputStream.write(getYamlKeyValue("infile", infile).getBytes());
            outputStream.write(getYamlKeyValue("outfile", outfile).getBytes());
            outputStream.write(getYamlKeyValue("task_type", taskType).getBytes());
            outputStream.close();
        } catch (IOException ex) {
            throw new InformaticsServerException("Could not create new yaml file.", ex);
        }
    }

    private void createFolder(String path) throws IOException {
        Files.createDirectories(new File(path).toPath());
    }

    private String getTimeLimitSec(Integer timeLimitMillis) {
        float timeLimitSec = timeLimitMillis.floatValue() / 1000;
        return Float.toString(timeLimitSec);
    }

    private String getYamlKeyValue(String key, String value) {
        return key + ": " + value + "\n";
    }
}
