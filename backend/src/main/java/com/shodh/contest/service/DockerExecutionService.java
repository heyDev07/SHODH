package com.shodh.contest.service;

import com.shodh.contest.model.SubmissionStatus;
import com.shodh.contest.model.Problem;
import com.shodh.contest.model.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DockerExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(DockerExecutionService.class);

    @Value("${docker.image.name:shodh/code-executor}")
    private String dockerImageName;

    @Value("${judge.max-execution-time:5000}")
    private int maxExecutionTime;

    @Value("${docker.memory.limit:256m}")
    private String memoryLimit;

    @Value("${docker.cpus:0.5}")
    private String cpuLimit;
    
    private static final Map<String, String> LANGUAGE_FILE_EXTENSIONS = new HashMap<>();
    private static final Map<String, String> LANGUAGE_COMPILE_COMMANDS = new HashMap<>();
    private static final Map<String, String> LANGUAGE_RUN_COMMANDS = new HashMap<>();
    
    {
        // Java
        LANGUAGE_FILE_EXTENSIONS.put("java", "Main.java");
        LANGUAGE_COMPILE_COMMANDS.put("java", "javac Main.java");
        LANGUAGE_RUN_COMMANDS.put("java", "java Main");
        
        // Python
        LANGUAGE_FILE_EXTENSIONS.put("python", "main.py");
        LANGUAGE_COMPILE_COMMANDS.put("python", "");  // No compilation needed
        LANGUAGE_RUN_COMMANDS.put("python", "python3 main.py");
        
        // JavaScript (Node.js)
        LANGUAGE_FILE_EXTENSIONS.put("javascript", "main.js");
        LANGUAGE_COMPILE_COMMANDS.put("javascript", "");  // No compilation needed
        LANGUAGE_RUN_COMMANDS.put("javascript", "node main.js");
        
        // C
        LANGUAGE_FILE_EXTENSIONS.put("c", "main.c");
        LANGUAGE_COMPILE_COMMANDS.put("c", "gcc -o main main.c");
        LANGUAGE_RUN_COMMANDS.put("c", "./main");
        
        // C++
        LANGUAGE_FILE_EXTENSIONS.put("cpp", "main.cpp");
        LANGUAGE_COMPILE_COMMANDS.put("cpp", "g++ -o main main.cpp");
        LANGUAGE_RUN_COMMANDS.put("cpp", "./main");
    }

    public ExecutionResult executeCode(String code, Problem problem) {
        return executeCode(code, problem, "java"); // Default to Java
    }
    
    public ExecutionResult executeCode(String code, Problem problem, String language) {
        String containerName = "executor-" + UUID.randomUUID().toString().substring(0, 8);
        Path tempDir = null;

        try {
            // Create temporary directory for the submission
            tempDir = Files.createTempDirectory("submission-" + UUID.randomUUID());
            
            // Get the appropriate file name for the language
            String fileName = LANGUAGE_FILE_EXTENSIONS.getOrDefault(language, "Main.java");
            Path codeFile = tempDir.resolve(fileName);
            Files.write(codeFile, code.getBytes(StandardCharsets.UTF_8));

            // Prepare test cases
            List<String> inputs = problem.getInputTestCases();
            List<String> expectedOutputs = problem.getExpectedOutputs();

            int testCasesPassed = 0;
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < inputs.size(); i++) {
                String input = inputs.get(i);
                String expectedOutput = expectedOutputs.get(i);

                // Execute the code with Docker
                ExecutionResult result = executeTestCase(codeFile, input, containerName + "-" + i, language);

                if (result.getStatus() == SubmissionStatus.ACCEPTED) {
                    // Normalize output (trim whitespace)
                    String normalizedActual = result.getOutput().trim();
                    String normalizedExpected = expectedOutput.trim();

                    if (normalizedActual.equals(normalizedExpected)) {
                        testCasesPassed++;
                    } else {
                        result.setStatus(SubmissionStatus.WRONG_ANSWER);
                        result.setErrorMessage("Test case " + (i + 1) + " failed.\nExpected: " + normalizedExpected + "\nGot: " + normalizedActual);
                        errors.add(result.getErrorMessage());
                    }
                } else {
                    errors.add("Test case " + (i + 1) + ": " + result.getErrorMessage());
                    // Return immediately on compilation error
                    if (result.getStatus() == SubmissionStatus.COMPILATION_ERROR) {
                        return result;
                    }
                }
            }

            // Clean up container
            cleanupContainer(containerName);

            // Determine final result
            ExecutionResult finalResult = new ExecutionResult();
            finalResult.setTestCasesPassed(testCasesPassed);
            finalResult.setTotalTestCases(inputs.size());

            if (testCasesPassed == inputs.size()) {
                finalResult.setStatus(SubmissionStatus.ACCEPTED);
                finalResult.setErrorMessage(null);
            } else if (errors.isEmpty()) {
                finalResult.setStatus(SubmissionStatus.WRONG_ANSWER);
                finalResult.setErrorMessage("Some test cases failed");
            } else {
                finalResult.setStatus(SubmissionStatus.WRONG_ANSWER);
                finalResult.setErrorMessage(String.join("\n", errors));
            }

            return finalResult;

        } catch (Exception e) {
            logger.error("Error executing code", e);
            ExecutionResult errorResult = new ExecutionResult();
            errorResult.setStatus(SubmissionStatus.RUNTIME_ERROR);
            errorResult.setErrorMessage("Error executing code: " + e.getMessage());
            return errorResult;
        } finally {
            // Clean up temporary directory
            if (tempDir != null) {
                cleanupDirectory(tempDir);
            }
        }
    }

    private ExecutionResult executeTestCase(Path codeFile, String input, String containerName) {
        return executeTestCase(codeFile, input, containerName, "java");
    }
    
    private ExecutionResult executeTestCase(Path codeFile, String input, String containerName, String language) {
        try {
            String compileCommand = LANGUAGE_COMPILE_COMMANDS.getOrDefault(language, "javac Main.java");
            String runCommand = LANGUAGE_RUN_COMMANDS.getOrDefault(language, "java Main");
            
            String executionCommand;
            if (compileCommand.isEmpty()) {
                // Languages that don't need compilation (Python, JavaScript)
                executionCommand = "timeout " + (maxExecutionTime / 1000) + " " + runCommand;
            } else {
                // Languages that need compilation (Java, C, C++)
                executionCommand = compileCommand + " && timeout " + (maxExecutionTime / 1000) + " " + runCommand;
            }
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "run",
                    "--rm",
                    "--name", containerName,
                    "--memory", memoryLimit,
                    "--cpus", cpuLimit,
                    "-v", codeFile.getParent().toString() + ":/workspace",
                    "-w", "/workspace",
                    "--network", "none",
                    "--timeout", String.valueOf(maxExecutionTime / 1000),
                    dockerImageName,
                    "bash", "-c",
                    executionCommand
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Write input to the process
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Read error output
            StringBuilder errorOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
            }

            boolean finished = process.waitFor(maxExecutionTime, java.util.concurrent.TimeUnit.MILLISECONDS);

            ExecutionResult result = new ExecutionResult();

            if (!finished) {
                process.destroyForcibly();
                result.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
                result.setErrorMessage("Time limit exceeded");
                cleanupContainer(containerName);
                return result;
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                if (errorOutput.toString().contains("error:")) {
                    result.setStatus(SubmissionStatus.COMPILATION_ERROR);
                    result.setErrorMessage(errorOutput.toString());
                } else {
                    result.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    result.setErrorMessage(errorOutput.toString());
                }
                result.setOutput(output.toString());
            } else {
                result.setStatus(SubmissionStatus.ACCEPTED);
                result.setOutput(output.toString());
            }

            cleanupContainer(containerName);
            return result;

        } catch (IOException | InterruptedException e) {
            logger.error("Error executing Docker container", e);
            ExecutionResult errorResult = new ExecutionResult();
            errorResult.setStatus(SubmissionStatus.RUNTIME_ERROR);
            errorResult.setErrorMessage("Error running code: " + e.getMessage());
            cleanupContainer(containerName);
            return errorResult;
        }
    }

    private void cleanupContainer(String containerName) {
        try {
            // Attempt to stop and remove the container if it exists
            new ProcessBuilder("docker", "rm", "-f", containerName)
                    .redirectErrorStream(true)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .start()
                    .waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (Exception e) {
            // Ignore cleanup errors
            logger.debug("Error cleaning up container: " + containerName, e);
        }
    }

    private void cleanupDirectory(Path directory) {
        try {
            Files.walk(directory)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.debug("Error deleting file: " + path, e);
                        }
                    });
        } catch (IOException e) {
            logger.debug("Error cleaning up directory: " + directory, e);
        }
    }

    public static class ExecutionResult {
        private SubmissionStatus status;
        private String output;
        private String errorMessage;
        private int testCasesPassed;
        private int totalTestCases;

        public SubmissionStatus getStatus() { return status; }
        public void setStatus(SubmissionStatus status) { this.status = status; }
        public String getOutput() { return output; }
        public void setOutput(String output) { this.output = output; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public int getTestCasesPassed() { return testCasesPassed; }
        public void setTestCasesPassed(int testCasesPassed) { this.testCasesPassed = testCasesPassed; }
        public int getTotalTestCases() { return totalTestCases; }
        public void setTotalTestCases(int totalTestCases) { this.totalTestCases = totalTestCases; }
    }
}
