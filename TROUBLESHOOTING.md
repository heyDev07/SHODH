# Troubleshooting Guide

This document addresses common issues encountered while setting up and running the Shodh-a-Code Contest Platform.

## Issue 1: Lombok Compilation Errors

### Symptoms
```
cannot find symbol
  symbol:   method getInputTestCases()
  location: variable problem of type com.shodh.contest.model.Problem
```
74 compilation errors related to missing getter/setter methods.

### Root Cause
Java 25 was being used instead of Java 17. Lombok annotation processor fails with Java 25.

### Solution
Set JAVA_HOME to Java 17 before running Maven:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
cd backend
mvn clean compile
```

**Permanent Fix (macOS):** Add to your `~/.zshrc` or `~/.bash_profile`:
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

## Issue 2: Contest Not Found (404 Error)

### Symptoms
Frontend shows "Contest not found" even though data was initialized.

### Root Cause
JPA lazy loading issue. The `problems` collection wasn't being loaded when the API returned the contest data.

### Solution
Added `@EntityGraph` annotation to eagerly load the problems:

```java
@EntityGraph(attributePaths = {"problems"})
@Query("SELECT c FROM Contest c WHERE c.contestId = :contestId")
Optional<Contest> findByContestIdWithProblems(@Param("contestId") String contestId);
```

## Issue 3: Maven Command Fails with "No POM found"

### Symptoms
```
[ERROR] The goal you specified requires a project to execute but there is no POM 
in this directory
```

### Root Cause
Running Maven from the wrong directory (root instead of backend).

### Solution
Ensure you're in the backend directory:
```bash
cd backend
mvn spring-boot:run
```

## Issue 4: Docker Build Fails

### Symptoms
```
ERROR: Cannot connect to the Docker daemon
```

### Root Cause
Docker Desktop is not running.

### Solution
1. Open Docker Desktop application
2. Wait for it to fully start
3. Run the build script again:
```bash
cd backend
./build-docker.sh
```

## Issue 5: Port Already in Use

### Symptoms
```
Port 8080 is already in use
```

### Solution

**Option 1:** Stop the existing process using port 8080:
```bash
# Find process
lsof -ti:8080

# Kill process
kill -9 <PID>
```

**Option 2:** Change the port in `application.properties`:
```properties
server.port=8081
```

## Issue 6: Frontend Can't Connect to Backend

### Symptoms
```
Network Error
ERR_CONNECTION_REFUSED
```

### Root Cause
Backend server is not running.

### Solution
1. Check if backend is running on port 8080
2. Start the backend:
```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run
```
3. Wait for "Started ContestApplication" message
4. Refresh the frontend

## Issue 7: Node Modules Installation Fails

### Symptoms
```
npm ERR! code ETIMEDOUT
npm ERR! errno ETIMEDOUT
```

### Solution
```bash
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install
```

## Quick Health Check

Run these commands to verify your setup:

```bash
# Check Java version
java -version  # Should be 17.x.x

# Check Maven
mvn -version

# Check Node.js
node -v  # Should be 18+

# Check Docker
docker --version
docker ps  # Should not error

# Check if backend is running
curl http://localhost:8080/api/contests/CONTEST-001
```

## Complete Fresh Start

If you need to start completely fresh:

```bash
# Stop all processes (Ctrl+C in terminal windows)

# Clean backend
cd backend
rm -rf target
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean compile

# Clean frontend
cd ../frontend
rm -rf node_modules .next
npm install

# Start services
cd ../backend
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn spring-boot:run &
sleep 10

cd ../frontend
npm run dev
```

## Getting Help

If you encounter issues not covered here:

1. Check the logs in the terminal where you ran `mvn spring-boot:run`
2. Check the browser console (F12) for frontend errors
3. Verify all prerequisites are installed correctly
4. Ensure you're using Java 17 (not 25)
5. Make sure Docker Desktop is running
