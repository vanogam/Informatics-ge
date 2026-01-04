package ge.freeuni.informatics.server.worker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import ge.freeuni.informatics.common.dto.WorkerDTO;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.common.model.user.UserRole;
import ge.freeuni.informatics.common.model.worker.Worker;
import ge.freeuni.informatics.common.model.worker.WorkerStatus;
import ge.freeuni.informatics.repository.user.UserJpaRepository;
import ge.freeuni.informatics.repository.worker.WorkerRepository;
import ge.freeuni.informatics.utils.UserUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class WorkerManager implements IWorkerManager {

    private static final Logger log = LoggerFactory.getLogger(WorkerManager.class);
    private static final long HEARTBEAT_TIMEOUT_MS = 60000;

    @Value("${ge.freeuni.informatics.worker.initialCount:0}")
    private int initialWorkerCount;

    @Value("${ge.freeuni.informatics.worker.idPrefix:main}")
    private String workerIdPrefix;

    @Value("${ge.freeuni.informatics.worker.dockerImage:informatics/worker:latest}")
    private String workerDockerImage;

    @Value("${ge.freeuni.informatics.worker.dockerNetwork:informatics_dev}")
    private String workerDockerNetwork;

    @Value("${ge.freeuni.informatics.worker.filesPath:/home/informatics/dev/files}")
    private String workerFilesPath;

    @Value("${ge.freeuni.informatics.worker.logsPath:/home/informatics/dev/logs/worker}")
    private String workerLogsPath;

    @Value("${ge.freeuni.informatics.worker.sharedPath:/home/informatics/dev/files/worker/shared}")
    private String workerSharedPath;

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaBootstrapServers;

    @Value("${ge.freeuni.informatics.worker.serverUrl:http://core:8080}")
    private String serverUrl;

    @Value("${SPRING_DATASOURCE_URL:jdbc:postgresql://db:5432/informatics}")
    private String jdbcUrl;

    @Value("${SPRING_DATASOURCE_USERNAME:informatics}")
    private String jdbcUsername;

    @Value("${SPRING_DATASOURCE_PASSWORD:}")
    private String jdbcPassword;

    @Value("${ge.freeuni.informatics.worker.workerUsername:worker}")
    private String workerUsername;

    @Value("${ge.freeuni.informatics.worker.workerPassword:worker_password}")
    private String workerPassword;

    final WorkerRepository workerRepository;
    final UserJpaRepository userRepository;
    private DockerClient dockerClient;

    @Autowired
    public WorkerManager(WorkerRepository workerRepository, UserJpaRepository userRepository) {
        this.workerRepository = workerRepository;
        this.userRepository = userRepository;
        try {
            this.dockerClient = createDockerClient();
        } catch (Exception e) {
            log.warn("Failed to initialize Docker client. Worker container management will be disabled.", e);
            this.dockerClient = null;
        }
    }

    private DockerClient createDockerClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        return DockerClientImpl.getInstance(config, httpClient);
    }

    @PostConstruct
    @Transactional
    public void initializeWorkers() {
        try {
            User workerUser = null;
            try {
                workerUser = userRepository.getFirstByUsername(workerUsername);
            } catch (Exception e) {
                // User doesn't exist, will create it
            }
            
            if (workerUser == null) {
                // Create worker user if it doesn't exist
                workerUser = new User();
                workerUser.setUsername(workerUsername);
                workerUser.setEmail(workerUsername + "@informatics.local");
                workerUser.setFirstName("Worker");
                workerUser.setLastName("Service");
                workerUser.setRole(UserRole.WORKER.name());
                workerUser.setVersion(1);
                log.info("Creating worker user: {}", workerUsername);
            } else {
                log.info("Worker user already exists: {}", workerUsername);
            }
            
            // Update password from config (for both new and existing users)
            workerUser.setPasswordSalt(UserUtils.getSalt());
            workerUser.setPassword(UserUtils.getHash(workerPassword, workerUser.getPasswordSalt()));
            userRepository.save(workerUser);
            log.info("Worker user password set from configuration");
        } catch (Exception e) {
            log.error("Failed to create/update worker user", e);
        }
        
        log.info("Initializing workers from configuration. Initial count: {}", initialWorkerCount);
        
        if (initialWorkerCount > 0) {
            long existingCount = workerRepository.count();
            
            if (existingCount == 0) {
                for (int i = 1; i <= initialWorkerCount; i++) {
                    try {
                        WorkerDTO worker = createWorkerInstance();
                        log.info("Created initial worker instance: {}", worker.workerId());
                    } catch (Exception e) {
                        log.error("Failed to create initial worker instance", e);
                    }
                }
            } else {
                log.info("Workers already exist ({} instances). Skipping initial creation.", existingCount);
            }
        } else {
            log.info("Initial worker count is 0, skipping worker creation");
        }
    }

    @PreDestroy
    @Transactional
    public void cleanupWorkers() {
        log.info("Shutting down - stopping all worker instances");
        stopAllWorkers();
    }


    @Override
    @Transactional
    public void updateHeartbeat(String workerId, Long jobsProcessed, Boolean isWorking) {
        Optional<Worker> workerOpt = workerRepository.findByWorkerId(workerId);
        Worker worker;
        
        if (workerOpt.isPresent()) {
            worker = workerOpt.get();
            worker.setLastHeartbeat(new Date());
            if (isWorking != null && isWorking) {
                worker.setStatus(WorkerStatus.WORKING);
            } else {
                worker.setStatus(WorkerStatus.ONLINE);
            }
            if (jobsProcessed != null) {
                worker.setJobsProcessed(jobsProcessed);
            }
        } else {
            worker = new Worker();
            worker.setWorkerId(workerId);
            worker.setLastHeartbeat(new Date());
            worker.setStartTime(new Date());
            if (isWorking != null && isWorking) {
                worker.setStatus(WorkerStatus.WORKING);
            } else {
                worker.setStatus(WorkerStatus.ONLINE);
            }
            worker.setJobsProcessed(jobsProcessed != null ? jobsProcessed : 0L);
        }
        
        workerRepository.save(worker);
    }

    @Override
    @Transactional
    public List<WorkerDTO> getAllWorkers() {
        Date now = new Date();
        List<Worker> workers = workerRepository.findAll();
        
        for (Worker worker : workers) {
            long timeSinceLastHeartbeat = now.getTime() - worker.getLastHeartbeat().getTime();
            if (timeSinceLastHeartbeat > HEARTBEAT_TIMEOUT_MS) {
                worker.setStatus(WorkerStatus.OFFLINE);
                workerRepository.save(worker);
            }
        }
        
        workers = workerRepository.findAll();
        
        return workers.stream()
                .map(worker -> {
                    long uptimeSeconds = (now.getTime() - worker.getStartTime().getTime()) / 1000;
                    return WorkerDTO.toDTO(worker, uptimeSeconds);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WorkerDTO createWorkerInstance() {
        Worker worker = new Worker();
        worker.setLastHeartbeat(new Date());
        worker.setWorkerId("unassigned");
        worker.setStartTime(new Date());
        worker.setStatus(WorkerStatus.OFFLINE);
        worker.setJobsProcessed(0L);
        
        worker = workerRepository.save(worker);
        
        String workerId = workerIdPrefix + "-" + worker.getId();
        worker.setWorkerId(workerId);
        worker = workerRepository.save(worker);
        
        String containerId = startWorkerContainer(workerId);
        
        log.info("Created worker instance {} with Docker container {}", workerId, containerId);
        
        long uptimeSeconds = 0; // Just created
        return WorkerDTO.toDTO(worker, uptimeSeconds);
    }

    private String startWorkerContainer(String workerId) {
        if (dockerClient == null) {
            log.warn("Docker client not available. Skipping container creation for worker: {}", workerId);
            return null;
        }

        try {
            String containerName = "informatics-worker-" + workerId;
            
            // Check if container already exists and remove it
            List<Container> existingContainers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .withNameFilter(Collections.singletonList(containerName))
                    .exec();
            
            for (Container container : existingContainers) {
                try {
                    if (container.getState().equals("running")) {
                        dockerClient.stopContainerCmd(container.getId()).exec();
                    }
                    dockerClient.removeContainerCmd(container.getId()).exec();
                    log.info("Removed existing container: {}", containerName);
                } catch (Exception e) {
                    log.warn("Error removing existing container: {}", containerName, e);
                }
            }

            Map<String, String> envVars = new HashMap<>();
            envVars.put("APP_ID", workerId);
            envVars.put("JDBC_URL", jdbcUrl);
            envVars.put("JDBC_USERNAME", jdbcUsername);
            envVars.put("JDBC_PASSWORD", jdbcPassword);
            envVars.put("KAFKA_BOOTSTRAP_SERVERS", kafkaBootstrapServers);
            envVars.put("SERVER_URL", serverUrl);
            envVars.put("WORKER_USERNAME", workerUsername);
            envVars.put("WORKER_PASSWORD", workerPassword);

            List<String> envList = envVars.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.toList());

            List<Bind> binds = new ArrayList<>();
            binds.add(new Bind(workerFilesPath + "/tasks", new Volume("/files")));
            binds.add(new Bind(workerSharedPath, new Volume("/shared")));
            binds.add(new Bind(workerLogsPath + "/" + workerId, new Volume("/logs")));
            binds.add(new Bind("/var/run/docker.sock", new Volume("/var/run/docker.sock")));

            HostConfig hostConfig = HostConfig.newHostConfig()
                    .withBinds(binds)
                    .withNetworkMode(workerDockerNetwork);

            CreateContainerResponse container = dockerClient.createContainerCmd(workerDockerImage)
                    .withName(containerName)
                    .withEnv(envList)
                    .withHostConfig(hostConfig)
                    .exec();

            dockerClient.startContainerCmd(container.getId()).exec();
            
            log.info("Started Docker container {} for worker {}", containerName, workerId);
            return container.getId();
        } catch (Exception e) {
            log.error("Failed to start Docker container for worker: {}", workerId, e);
            throw new RuntimeException("Failed to start Docker container for worker: " + workerId, e);
        }
    }

    @Override
    @Transactional
    public void deleteWorkerInstance(String workerId) {
        stopWorkerContainer(workerId);

        Optional<Worker> workerOpt = workerRepository.findByWorkerId(workerId);
        if (workerOpt.isPresent()) {
            workerRepository.delete(workerOpt.get());
            log.info("Deleted worker instance: {}", workerId);
        } else {
            throw new RuntimeException("Worker instance with ID " + workerId + " not found");
        }
    }

    private void stopWorkerContainer(String workerId) {
        if (dockerClient == null) {
            log.warn("Docker client not available. Skipping container stop for worker: {}", workerId);
            return;
        }

        try {
            String containerName = "informatics-worker-" + workerId;
            List<Container> containers = dockerClient.listContainersCmd()
                    .withShowAll(true)
                    .withNameFilter(Collections.singletonList(containerName))
                    .exec();

            for (Container container : containers) {
                try {
                    if (container.getState().equals("running")) {
                        dockerClient.stopContainerCmd(container.getId()).exec();
                        log.info("Stopped Docker container: {}", containerName);
                    }
                    dockerClient.removeContainerCmd(container.getId()).exec();
                    log.info("Removed Docker container: {}", containerName);
                } catch (Exception e) {
                    log.warn("Error stopping/removing container: {}", containerName, e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to stop Docker container for worker: {}", workerId, e);
        }
    }

    @Override
    @Transactional
    public List<WorkerDTO> addWorkerInstances(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }

        List<WorkerDTO> createdWorkers = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            try {
                WorkerDTO worker = createWorkerInstance();
                createdWorkers.add(worker);
                log.info("Created worker instance: {}", worker.workerId());
            } catch (Exception e) {
                log.error("Failed to create worker instance", e);
            }
        }
        
        return createdWorkers;
    }

    @Override
    @Transactional
    public void stopAllWorkers() {
        List<Worker> workers = workerRepository.findAll();
        for (Worker worker : workers) {
            try {
                stopWorkerContainer(worker.getWorkerId());
                
                worker.setStatus(WorkerStatus.OFFLINE);
                workerRepository.save(worker);
                log.info("Stopped worker instance: {}", worker.getWorkerId());
            } catch (Exception e) {
                log.error("Error stopping worker instance: {}", worker.getWorkerId(), e);
            }
        }
        log.info("Stopped {} worker instances", workers.size());
    }

    @Override
    public int getCurrentWorkerCount() {
        return (int) workerRepository.count();
    }
}

