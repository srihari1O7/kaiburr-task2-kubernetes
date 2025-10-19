# Kaiburr Assessment - Task 2: Kubernetes Deployment

This repository contains the solution for Task 2, demonstrating the deployment of the Task 1 Java REST API to a local Kubernetes cluster (Docker Desktop). It includes the necessary Dockerfile, Kubernetes manifests (YAML), and modifications to the API to interact with the Kubernetes API itself.

## Features

-   **Containerization:** A multi-stage `Dockerfile` builds a small, efficient, and secure image for the Java application.
-   **Kubernetes Manifests:** YAML files define deployments and services for both the API and a MongoDB database.
-   **Persistence:** MongoDB uses a `PersistentVolumeClaim` to ensure data is not lost when the pod restarts.
-   **Configuration:** The API connects to MongoDB using environment variables injected by Kubernetes, referencing the Kubernetes service name for MongoDB.
-   **Advanced Kubernetes Interaction:** The `/tasks/{id}/execute` endpoint has been modified to:
    -   Use the Kubernetes Java client library.
    -   Create a new Kubernetes `Pod` (using a `busybox` image) within the cluster.
    -   Run the task's command inside this new pod.
    -   Wait for the pod to complete, retrieve its logs (the command output), and then delete the pod.
-   **RBAC:** Includes necessary Role and RoleBinding to grant the API's service account permission to create, get, list, watch, and delete pods.

## Tech Stack

-   **Java 17 / Spring Boot 3.2** (Modified from Task 1)
-   **Docker / Docker Desktop**
-   **Kubernetes** (v1.34 via Docker Desktop)
-   **`kubectl`**
-   **Maven**
-   **MongoDB** (running as a container in Kubernetes)
-   **Kubernetes Java Client** (io.kubernetes:client-java)

## How to Run

### 1. Prerequisites

-   **Docker Desktop** installed and running.
-   **Kubernetes enabled** within Docker Desktop settings.
-   **`kubectl`** configured to point to `docker-desktop` context (this is usually automatic with Docker Desktop).
-   **Apache Maven** installed locally (needed for the Maven Wrapper).
-   **Docker Hub Account** (optional, but needed if you modify the image name).

### 2. Build the Docker Image

Clone the repository and build the Docker image using the provided `Dockerfile`. Replace `<your-dockerhub-username>` with your actual username if you change the image tag.

```sh
# Clone the repository
git clone <your-github-repo-url>
cd kaiburr-task2-kubernetes

# Add Maven Wrapper if missing (needed for Docker build)
# mvn -N wrapper:wrapper

# Build the v2 image (includes K8s client code)
# Replace ksrihari107 with your Docker Hub username if you change it
docker build -t ksrihari107/kaiburr-task1-api:v2 .

# Apply all YAML files in the k8s folder
# This will create deployments, services, PVC, and RBAC rules
kubectl apply -f k8s/

# Check pod status (wait until both are Running 1/1)
kubectl get pods

# Check service status (note the NodePort for api-service, e.g., 30083)
kubectl get services

## Testing the Kubernetes Interaction

Once both pods are running, test the API endpoints using Postman or `curl`. The API is accessible on `http://localhost:30083` because of the `NodePort` service.

*(**Your Name:** Srihari Kubenteran)*
*(**Date/Time:** October 19, 2025)*

---

### 1. GET /tasks (Verify API and DB Connection)

**Request:** `GET http://localhost:30083/tasks`

This confirms the API is running and can connect to the MongoDB instance within Kubernetes.

**Postman Screenshot:**

<img width="1044" height="849" alt="Screenshot 2025-10-19 at 11 46 09 PM" src="https://github.com/user-attachments/assets/e960df8b-f975-431d-b64d-c8a16fef3c2e" />




---

### 2. PUT /tasks/{id}/execute (Test Pod Creation)

First, create a task using `POST http://localhost:30083/tasks` with a command like `"echo Hello K8s Postman"`. Note the ID (e.g., `68f52b0dee490737f0f0045a`).

**Request:** `PUT http://localhost:30083/tasks/<your-task-id>/execute`

This tests the modified endpoint. It should create a `busybox` pod, run the echo command inside it, retrieve the logs, and return them in the response.

**Postman Screenshot:**

<img width="1043" height="844" alt="Screenshot 2025-10-19 at 11 50 14 PM" src="https://github.com/user-attachments/assets/e43dfcab-c6b2-46e2-bc23-e6962dc3a5dd" />












