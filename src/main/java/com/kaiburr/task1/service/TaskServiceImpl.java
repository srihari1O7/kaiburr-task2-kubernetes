package com.kaiburr.task1.service;

import com.kaiburr.task1.dto.TaskRequest;
import com.kaiburr.task1.exception.ResourceNotFoundException;
import com.kaiburr.task1.exception.ShellCommandException;
import com.kaiburr.task1.model.Task;
import com.kaiburr.task1.model.TaskExecution;
import com.kaiburr.task1.repository.TaskExecutionRepository;
import com.kaiburr.task1.repository.TaskRepository;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final CoreV1Api k8sApi;
    private final String K8S_NAMESPACE = "default";

    public TaskServiceImpl(TaskRepository taskRepository, TaskExecutionRepository taskExecutionRepository) {
        this.taskRepository = taskRepository;
        this.taskExecutionRepository = taskExecutionRepository;

        try {
             ApiClient client = ClientBuilder.standard().build();
            Configuration.setDefaultApiClient(client);
            this.k8sApi = new CoreV1Api();
            log.info("Kubernetes API client initialized successfully.");
        } catch (IOException e) {
            log.error("Failed to initialize Kubernetes API client", e);
            throw new RuntimeException("Could not configure Kubernetes client", e);
        }
    }

    @Override
    public Task createTask(TaskRequest taskRequest) {
        if (!ShellCommandValidator.isSafe(taskRequest.getCommand())) {
            throw new ShellCommandException("Validation failed: Command is not safe. Only 'echo' is allowed.");
        }
        Task task = new Task();
        task.setName(taskRequest.getName());
        task.setDescription(taskRequest.getDescription());
        task.setCommand(taskRequest.getCommand());
        task.setFramework(taskRequest.getFramework());
        task.setAssignedTo(taskRequest.getAssignedTo());
        return taskRepository.save(task);
    }

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public Task getTaskById(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    @Override
    public void deleteTask(String id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    @Override
    public List<Task> findTasksByName(String name) {
        return taskRepository.findByNameRegex(name);
    }

    @Override
    public TaskExecution executeTask(String id) {
        Task task = getTaskById(id);
        String commandToRun = task.getCommand();

        if (!ShellCommandValidator.isSafe(commandToRun)) {
             throw new ShellCommandException("Execution blocked: Command is not safe. Only 'echo' is allowed.");
        }

        String podName = "task-exec-" + task.getId() + "-" + System.currentTimeMillis();
        boolean success = false;
        String outputLog = "Pod execution initiated.";

        try {
            V1Pod pod = new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(new V1ObjectMeta().name(podName).namespace(K8S_NAMESPACE))
                .spec(new V1PodSpec()
                    .containers(Collections.singletonList(new V1Container()
                        .name("task-runner")
                        .image("busybox:latest")
                        .command(List.of("/bin/sh", "-c"))
                        .args(List.of(commandToRun))
                    ))
                    .restartPolicy("Never")
                );

            log.info("Creating Kubernetes pod: {}", podName);
            k8sApi.createNamespacedPod(K8S_NAMESPACE, pod, null, null, null, null);
            log.info("Pod {} created successfully.", podName);

            outputLog = waitForPodCompletion(podName);
            success = checkPodSuccess(podName);

        } catch (ApiException e) {
            log.error("Kubernetes API Exception creating pod {}: {} - {}", podName, e.getCode(), e.getResponseBody(), e);
            outputLog = "K8s API Error: " + e.getResponseBody();
            success = false;
        } catch (Exception e) {
             log.error("Error during pod execution for pod {}: {}", podName, e.getMessage(), e);
             outputLog = "Execution Error: " + e.getMessage();
             success = false;
        } finally {
            deletePod(podName);
        }

        TaskExecution executionLog = TaskExecution.builder()
                .taskId(task.getId())
                .commandRun(commandToRun)
                .output(outputLog)
                .success(success)
                .build();

        return taskExecutionRepository.save(executionLog);
    }

    private String waitForPodCompletion(String podName) throws ApiException, InterruptedException {
        log.info("Waiting for pod {} to complete...", podName);
        long startTime = System.currentTimeMillis();
        long timeoutMillis = TimeUnit.MINUTES.toMillis(2);

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            V1Pod currentPod = k8sApi.readNamespacedPod(podName, K8S_NAMESPACE, null);
            String phase = currentPod.getStatus() != null ? currentPod.getStatus().getPhase() : "Unknown";

            if ("Succeeded".equals(phase) || "Failed".equals(phase)) {
                log.info("Pod {} finished with phase: {}", podName, phase);
                TimeUnit.SECONDS.sleep(2);
                return getPodLogs(podName);
            }
            TimeUnit.SECONDS.sleep(5);
        }
        log.warn("Pod {} timed out waiting for completion.", podName);
        return "Pod execution timed out after 2 minutes.";
    }

    private boolean checkPodSuccess(String podName) {
        try {
            V1Pod finishedPod = k8sApi.readNamespacedPod(podName, K8S_NAMESPACE, null);
             if (finishedPod != null && finishedPod.getStatus() != null) {
                return "Succeeded".equals(finishedPod.getStatus().getPhase());
             }
        } catch (ApiException e) {
             if (e.getCode() != 404) {
                 log.error("API Exception checking pod success for {}: {} - {}", podName, e.getCode(), e.getResponseBody());
             }
        }
        return false;
    }

    private String getPodLogs(String podName) {
        try {
             log.info("Fetching logs for pod {}", podName);
             String podLogs = k8sApi.readNamespacedPodLog(
                 podName,
                 K8S_NAMESPACE,
                 null,
                 false,
                 null,
                 null,
                 null,
                 false,
                 null,
                 null,
                 true
             );
            return (podLogs != null && !podLogs.isEmpty()) ? podLogs.trim() : "No logs found or pod failed before logging.";
        } catch (ApiException e) {
            log.error("Kubernetes API Exception fetching logs for {}: {} - {}", podName, e.getCode(), e.getResponseBody());
            if (e.getCode() == 400 && e.getResponseBody() != null && e.getResponseBody().contains("must specify a container")) {
                 log.warn("Pod {} might still be initializing or has multiple containers.", podName);
                 return "Pod initializing or requires container specifier for logs.";
            }
            return "Error fetching logs: " + e.getResponseBody();
        } catch (Exception e) {
             log.error("Error fetching logs for pod {}: {}", podName, e.getMessage());
             return "Error fetching logs: " + e.getMessage();
        }
    }

    private void deletePod(String podName) {
        try {
            log.warn("Deleting pod {}", podName);
            k8sApi.deleteNamespacedPod(podName, K8S_NAMESPACE, null, null, 0, null, null, null);
            log.info("Pod {} delete request sent.", podName);
        } catch (ApiException e) {
             if (e.getCode() != 404) {
                log.error("Kubernetes API Exception deleting pod {}: {} - {}", podName, e.getCode(), e.getResponseBody());
             } else {
                 log.info("Pod {} already deleted or not found.", podName);
             }
        } catch (Exception e) {
             log.error("Error deleting pod {}: {}", podName, e.getMessage());
        }
    }
}