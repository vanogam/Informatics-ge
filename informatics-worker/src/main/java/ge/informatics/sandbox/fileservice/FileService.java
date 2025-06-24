package ge.informatics.sandbox.fileservice;

import ge.informatics.sandbox.Sandbox;

import java.io.IOException;

public interface FileService {
    static FileService getInstance(String type) {
        if (type.equals("local")) {
            return new LocalFileService();
        } else if (type.equals("http")) {
            return new HttpFileService();
        } else {
            throw new IllegalArgumentException("Unknown file service type: " + type);
        }
    }
    void downloadFile(String remoteName, String destinationPath, String destinationName, Sandbox sandbox, boolean shouldArchive) throws IOException;
    void uploadFile(String localPath, String remoteName, Sandbox sandbox) throws IOException;
}
