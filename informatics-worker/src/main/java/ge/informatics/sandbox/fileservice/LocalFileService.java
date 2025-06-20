package ge.informatics.sandbox.fileservice;

import ge.informatics.sandbox.Config;
import ge.informatics.sandbox.Sandbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static ge.informatics.sandbox.Utils.compressFile;

public class LocalFileService implements FileService {

    @Override
    public void downloadFile(String remoteName, String destinationPath, String destinationName, Sandbox sandbox, boolean shouldArchive) {
        String directory = Config.get("fileStorageDirectory.url");
        File file = new File(directory + "/" + remoteName);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }
        try {
            if (shouldArchive) {
                sandbox.uploadTar(compressFile(file, destinationName), destinationPath);
            } else {
                sandbox.uploadTar(new FileInputStream(file), destinationPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void uploadFile(String containerPath, String remoteName, Sandbox sandbox) throws IOException {
        String rootdir = Config.get("fileStorageDirectory.url");
        String directory = rootdir + "worker/shared";
        Files.createDirectories(Path.of(directory));
        String remotePath = directory + "/" + remoteName;
        sandbox.downloadFile(containerPath, remotePath);
    }
}
