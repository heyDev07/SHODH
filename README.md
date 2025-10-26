# Shodh-a-Code Contest Platform

A full-stack coding contest platform with live code judging, featuring real-time leaderboards and Dockerized code execution.

## Overview

This project implements a complete coding contest platform where users can join contests, solve problems, submit code solutions, and compete on a live leaderboard. The platform includes:

- **Backend (Spring Boot)**: RESTful API with Docker-based code execution engine
- **Frontend (Next.js + React)**: Modern, responsive UI with real-time updates
- **Docker Integration**: Secure, isolated code execution environment

## Architecture

### Backend Architecture

**Technology Stack:**
- Spring Boot 3.2.0 (Java 17)
- JPA/Hibernate for data persistence
- H2 in-memory database
- Docker for code execution
- Maven for dependency management

**Key Components:**

1. **Models** (`model/`): Entity classes for Contest, Problem, Submission, and SubmissionStatus
2. **Repositories** (`repository/`): Data access layer using Spring Data JPA
3. **Services** (`service/`):
   - `SubmissionService`: Handles submission processing asynchronously
   - `DockerExecutionService`: Orchestrates Docker containers for code execution
4. **Controller** (`controller/`): REST API endpoints
5. **DTOs** (`dto/`): Data transfer objects for API communication

### Frontend Architecture

**Technology Stack:**
- Next.js 14 (App Router)
- React 18 with TypeScript
- Tailwind CSS for styling
- Axios for HTTP requests

**Key Components:**

1. **Pages**:
   - `/`: Join page with contest ID and username input
   - `/contest`: Main contest page with problem view, code editor, and leaderboard
2. **API Client** (`lib/api.ts`): Centralized API communication layer
3. **Real-time Updates**: Polling mechanism for submission status and leaderboard

### Docker Integration

The Docker execution service creates isolated containers for each code submission:
- Uses `openjdk:17-slim` as the base image
- Imposes strict resource limits (memory, CPU, time)
- Runs compilation and execution inside the container
- Captures stdout and compares with expected output
- Automatically cleans up containers after execution

## Prerequisites

Before setting up the project, ensure you have:

1. **Java 17 or higher** - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or use OpenJDK
2. **Maven** - Download from [Apache Maven](https://maven.apache.org/download.cgi) or install via:
   - macOS: `brew install maven`
   - Ubuntu/Debian: `sudo apt-get install maven`
   - Windows: Download installer from Maven website
3. **Node.js 18+ and npm** - Download from [Node.js website](https://nodejs.org/)
4. **Docker** - Download from [Docker Desktop](https://www.docker.com/products/docker-desktop)

Verify installations:
```bash
java -version
mvn -version
node -v
npm -v
docker --version
```

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- Docker installed and running

### Step 1: Build Docker Image

First, build the Docker image for code execution:

```bash
cd backend
./build-docker.sh
```

This creates the `shodh/code-executor` image used for running user submissions.

### Step 2: Start Backend

**Important:** Set JAVA_HOME to Java 17 before running:

```bash
# macOS/Linux
export JAVA_HOME=$(/usr/libexec/java_home -v 17)  # macOS only
cd backend
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**Note:** If you have Java 25 installed, you MUST set JAVA_HOME to Java 17 for the project to compile successfully.

### Step 3: Start Frontend

In a new terminal:

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:3000`

### Step 4: Access the Application

1. Open `http://localhost:3000` in your browser
2. Enter contest ID: `CONTEST-001` (pre-populated sample contest)
3. Enter your username
4. Click "Join Contest"

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Endpoints

#### 1. Get Contest
```
GET /api/contests/{contestId}
```
**Response:**
```json
{
  "id": 1,
  "contestId": "CONTEST-001",
  "name": "Shodh Coding Challenge",
  "description": "Test your coding skills...",
  "problems": [...],
  "startTime": "2024-01-01T00:00:00",
  "endTime": "2024-01-08T00:00:00"
}
```

#### 2. Submit Code
```
POST /api/submissions
```
**Request Body:**
```json
{
  "contestId": "CONTEST-001",
  "problemId": "SUM-001",
  "username": "john_doe",
  "code": "public class Main { ... }"
}
```
**Response:**
```json
{
  "submissionId": "uuid",
  "username": "john_doe",
  "problemId": "SUM-001",
  "status": "PENDING",
  "submittedAt": "2024-01-01T12:00:00"
}
```

#### 3. Get Submission Status
```
GET /api/submissions/{submissionId}
```
**Response:**
```json
{
  "submissionId": "uuid",
  "status": "ACCEPTED",
  "testCasesPassed": 3,
  "totalTestCases": 3,
  "processedAt": "2024-01-01T12:00:15"
}
```

#### 4. Get Leaderboard
```
GET /api/contests/{contestId}/leaderboard
```
**Response:**
```json
[
  {
    "username": "alice",
    "totalSubmissions": 5,
    "acceptedSubmissions": 4,
    "totalProblemsSolved": 3
  }
]
```

## Design Choices & Justification

### Backend Design Decisions

1. **Asynchronous Processing**
   - Submissions are queued and processed asynchronously using `@Async`
   - Allows the API to respond immediately with a submission ID
   - Frontend polls for status updates (2-second interval)
   - Justification: Prevents API timeout on long-running executions

2. **Docker-based Execution**
   - Each submission runs in an isolated container
   - Resource limits applied (256MB memory, 0.5 CPUs, 5s timeout)
   - Network disabled for security
   - Justification: Prevents malicious code from affecting the host system

3. **Service Layer Separation**
   - `SubmissionService`: Business logic for submissions
   - `DockerExecutionService`: Infrastructure concerns for execution
   - Justification: Clear separation of concerns, easier testing

4. **Repository Pattern**
   - Spring Data JPA repositories provide clean data access
   - Type-safe query methods
   - Justification: Reduces boilerplate, improves maintainability

### Frontend Design Decisions

1. **Polling vs WebSockets**
   - Chose polling for simplicity
   - Submission status: 2-second interval
   - Leaderboard: 20-second interval
   - Justification: Easier to implement, sufficient for prototype

2. **Component Structure**
   - Single-page contest view with split layout
   - Left: Problems and code editor
   - Right: Live leaderboard
   - Justification: Allows users to code while watching rankings

3. **State Management**
   - React `useState` and `useEffect` for local state
   - No global state library (Redux/Zustand)
   - Justification: Simple state needs, avoids unnecessary complexity

### Docker Challenges & Trade-offs

1. **Container Cleanup**
   - Challenge: Containers must be cleaned up even on failure
   - Solution: Try-finally blocks and timeout-based cleanup
   - Trade-off: Longer cleanup time vs resource leaks

2. **Input/Output Handling**
   - Challenge: Passing test case input and capturing output
   - Solution: ProcessBuilder with stdin/stdout redirection
   - Trade-off: Synchronous execution vs complexity of async pipes

3. **Resource Limits**
   - Challenge: Preventing infinite loops and excessive memory usage
   - Solution: Docker's `--memory` and `--cpus` flags, Java timeout
   - Trade-off: Stricter limits vs false TLE on edge cases

## Sample Problems

The platform comes with 3 pre-populated problems:

1. **Sum of Two Numbers**: Basic I/O with Scanner
2. **Find Maximum**: Conditional logic
3. **Reverse String**: String manipulation

## Testing the Platform

1. Join the contest with ID `CONTEST-001`
2. Select "Sum of Two Numbers"
3. Write the solution:
```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int a = scanner.nextInt();
        int b = scanner.nextInt();
        System.out.println(a + b);
    }
}
```
4. Click "Submit Code"
5. Watch the status update in real-time
6. Check your ranking on the leaderboard

## Future Enhancements

- WebSocket support for true real-time updates
- Support for multiple languages (Python, C++, etc.)
- Test case visibility toggle
- Problem difficulty levels
- User profiles and submission history
- Contest scheduling and registration
- Administrator dashboard

## License

This project is created for assessment purposes.
# SHODH
