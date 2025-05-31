package ge.freeuni.informatics.server.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FileManager {

    @Value("${ge.freeuni.informatics.Task.submissionDirectoryAddress}")
    private String submissionDirectory;

    public void saveTextSubmission(String fileName, String content) {
        // Implementation to save text file
    }
}
