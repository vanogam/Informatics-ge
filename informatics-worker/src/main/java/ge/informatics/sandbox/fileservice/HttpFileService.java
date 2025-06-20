package ge.informatics.sandbox.fileservice;


import ge.informatics.sandbox.Config;
import ge.informatics.sandbox.Sandbox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpFileService implements FileService {
    private static final Logger log = LogManager.getLogger(HttpFileService.class);

    public void downloadFile(String remoteName, String destinationPath, String destinationName, Sandbox sandbox, boolean shouldArchive) throws IOException {
        String domain = Config.get("fileServerDomain.url");
        String user = Config.get("fileServerDomain.user");
        String password = Config.get("fileServerDomain.password");
        String url = domain + "/download/" + remoteName;
        HttpURLConnection connection = null;
        String localUrl = "/tmp/" + remoteName;
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(localUrl)) {

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            String encodedAuth = java.util.Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to download file: HTTP " + connection.getResponseCode());
            }

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
            File file = new File(localUrl);
            sandbox.uploadTar(new FileInputStream(file), destinationPath);
            file.delete();
            log.info("File downloaded successfully");
        } catch (IOException e) {
            log.error("Error downloading file from {}", remoteName, e);
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void uploadFile(String localPath, String remoteName, Sandbox sandbox) {
        String domain = Config.get("fileServerDomain.url");
        String user = Config.get("fileServerDomain.user");
        String password = Config.get("fileServerDomain.password");
        String url = domain + "/upload";
        HttpURLConnection connection = null;

        try {
            File file = new File(localPath);
            if (!file.exists()) {
                throw new FileNotFoundException("File not found: " + localPath);
            }

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8)));
            connection.setRequestProperty("Content-Type", "application/octet-stream");
            connection.setRequestProperty("name", remoteName);

            try (OutputStream os = connection.getOutputStream();
                 FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Failed to upload file: HTTP " + connection.getResponseCode());
            }
            log.info("File uploaded successfully");
        } catch (IOException e) {
            log.error("Error uploading file to {}", remoteName, e);
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }
}