package ge.informatics.sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicLong;

public class HeartbeatSender {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatSender.class);
    private final String serverUrl;
    private final String workerId;
    private final String workerUsername;
    private final String workerPassword;
    private final AtomicLong jobsProcessed;
    private volatile boolean running = true;
    private volatile boolean isWorking = false;

    public HeartbeatSender(String serverUrl, String workerId, String workerUsername, String workerPassword) {
        this.serverUrl = serverUrl;
        this.workerId = workerId;
        this.workerUsername = workerUsername;
        this.workerPassword = workerPassword;
        this.jobsProcessed = new AtomicLong(0);
    }

    public void incrementJobsProcessed() {
        jobsProcessed.incrementAndGet();
    }

    public void setWorking(boolean working) {
        this.isWorking = working;
    }

    public boolean isWorking() {
        return isWorking;
    }

    public void start() {
        Thread heartbeatThread = new Thread(() -> {
            while (running) {
                try {
                    sendHeartbeat();
                    Thread.sleep(30000); // 30 seconds
                } catch (InterruptedException e) {
                    log.info("Heartbeat thread interrupted, stopping...");
                    running = false;
                    break;
                } catch (Exception e) {
                    log.error("Error sending heartbeat", e);
                    // Continue trying even if there's an error
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                        log.info("Heartbeat thread interrupted, stopping...");
                        running = false;
                        break;
                    }
                }
            }
        });
        heartbeatThread.setDaemon(true);
        heartbeatThread.setName("HeartbeatSender");
        heartbeatThread.start();
    }

    public void stop() {
        running = false;
    }

    private void sendHeartbeat() {
        try {
            String urlString = serverUrl + "/api/admin/workers/" + workerId + "/heartbeat";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            
            // Add HTTP Basic Authentication
            if (workerUsername != null && workerPassword != null) {
                String auth = workerUsername + ":" + workerPassword;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            }
            
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            ObjectMapper mapper = new ObjectMapper();
            HeartbeatRequest request = new HeartbeatRequest();
            request.setJobsProcessed(jobsProcessed.get());
            request.setWorking(isWorking);
            String jsonBody = mapper.writeValueAsString(request);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log.debug("Heartbeat sent successfully for worker: {}", workerId);
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                log.error("Heartbeat authentication failed for worker: {} (response code: {})", workerId, responseCode);
            } else {
                log.warn("Heartbeat failed with response code: {} for worker: {}", responseCode, workerId);
            }
        } catch (Exception e) {
            log.error("Failed to send heartbeat for worker: {}", workerId, e);
        }
    }

    private static class HeartbeatRequest {
        private Long jobsProcessed;
        private Boolean working;

        public Long getJobsProcessed() {
            return jobsProcessed;
        }

        public void setJobsProcessed(Long jobsProcessed) {
            this.jobsProcessed = jobsProcessed;
        }

        public Boolean getWorking() {
            return working;
        }

        public void setWorking(Boolean working) {
            this.working = working;
        }
    }
}

