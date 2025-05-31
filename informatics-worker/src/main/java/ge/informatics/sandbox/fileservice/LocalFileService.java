package ge.informatics.sandbox.fileservice;

import ge.informatics.sandbox.Config;
import ge.informatics.sandbox.Sandbox;

import java.io.File;
import java.io.IOException;

import static ge.informatics.sandbox.Utils.compressFile;

public class LocalFileService implements FileService {

    @Override
    public void downloadFile(String remoteName, String destinationPath, Sandbox sandbox) {
        String directory = Config.get("fileStorageDirectory.url");
        File file = new File(directory + "/" + remoteName);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }
        try {
            sandbox.uploadTar(compressFile(file, remoteName), destinationPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void uploadFile(String containerPath, String localName, Sandbox sandbox) {
        String directory = Config.get("fileStorageDirectory.url");
        String localPath = directory + "/" + localName;
        sandbox.downloadFile(containerPath, localPath);
    }
}
