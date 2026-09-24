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
        File file = new File(remoteName);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + file.getAbsolutePath());
        }
        try {
            if (shouldArchive) {
                sandbox.uploadTar(compressFile(file, destinationName), destinationPath);
            } else {
                // uploadTar() PUTs the stream synchronously and returns only once the request
                // is done, so it's safe to close right after - docker-java's own exec never
                // closes the stream it's handed, and without this every test run of a
                // non-output submission leaked one file descriptor.
                try (FileInputStream fis = new FileInputStream(file)) {
                    sandbox.uploadTar(fis, destinationPath);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress file: " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void uploadFile(String containerPath, String remoteName, Sandbox sandbox) throws IOException {
        String remotePath = Config.get("sharedDirectory.url") + "/" + remoteName;
        sandbox.downloadFile(containerPath, remotePath);
    }
}
