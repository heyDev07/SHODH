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
public class LocalExecutionService implements CodeExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(LocalExecutionService.class);

    @Value("${judge.max-execution-time:5000}")
    private int maxExecutionTime;
    
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

    public CodeExecutionService.ExecutionResult executeCode(String code, Problem problem) {
        return executeCode(code, problem, "java"); // Default to Java
    }
    
    public CodeExecutionService.ExecutionResult executeCode(String code, Problem problem, String language) {
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

                // Execute the code locally
                CodeExecutionService.ExecutionResult result = executeTestCase(codeFile, input, language);

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

            // Determine final result
            CodeExecutionService.ExecutionResult finalResult = new CodeExecutionService.ExecutionResult();
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
            CodeExecutionService.ExecutionResult errorResult = new CodeExecutionService.ExecutionResult();
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
    
    private CodeExecutionService.ExecutionResult executeTestCase(Path codeFile, String input, String language) {
        try {
            String compileCommand = LANGUAGE_COMPILE_COMMANDS.getOrDefault(language, "javac Main.java");
            String runCommand = LANGUAGE_RUN_COMMANDS.getOrDefault(language, "java Main");
            
            // Change to the directory containing the code file
            Path workingDir = codeFile.getParent();
            
            // Compile if needed
            if (!compileCommand.isEmpty()) {
                ProcessBuilder compileBuilder = new ProcessBuilder("bash", "-c", compileCommand);
                compileBuilder.directory(workingDir.toFile());
                compileBuilder.redirectErrorStream(true);
                
                Process compileProcess = compileBuilder.start();
                
                StringBuilder compileOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(compileProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        compileOutput.append(line).append("\n");
                    }
                }
                
                boolean compileFinished = compileProcess.waitFor(maxExecutionTime, TimeUnit.MILLISECONDS);
                
                if (!compileFinished) {
                    compileProcess.destroyForcibly();
                    CodeExecutionService.ExecutionResult result = new CodeExecutionService.ExecutionResult();
                    result.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
                    result.setErrorMessage("Compilation time limit exceeded");
                    return result;
                }
                
                int compileExitCode = compileProcess.exitValue();
                if (compileExitCode != 0) {
                    CodeExecutionService.ExecutionResult result = new CodeExecutionService.ExecutionResult();
                    result.setStatus(SubmissionStatus.COMPILATION_ERROR);
                    result.setErrorMessage(compileOutput.toString());
                    return result;
                }
            }
            
            // Run the program
            ProcessBuilder runBuilder = new ProcessBuilder("bash", "-c", "timeout " + (maxExecutionTime / 1000) + " " + runCommand);
            runBuilder.directory(workingDir.toFile());
            runBuilder.redirectErrorStream(true);
            
            Process runProcess = runBuilder.start();
            
            // Write input to the process
            try (OutputStream stdin = runProcess.getOutputStream()) {
                stdin.write(input.getBytes(StandardCharsets.UTF_8));
                stdin.flush();
            }
            
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(runProcess.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            boolean finished = runProcess.waitFor(maxExecutionTime, TimeUnit.MILLISECONDS);

            CodeExecutionService.ExecutionResult result = new CodeExecutionService.ExecutionResult();

            if (!finished) {
                runProcess.destroyForcibly();
                result.setStatus(SubmissionStatus.TIME_LIMIT_EXCEEDED);
                result.setErrorMessage("Time limit exceeded");
                return result;
            }

            int exitCode = runProcess.exitValue();

            if (exitCode != 0) {
                result.setStatus(SubmissionStatus.RUNTIME_ERROR);
                result.setErrorMessage("Runtime error (exit code: " + exitCode + ")");
                result.setOutput(output.toString());
            } else {
                result.setStatus(SubmissionStatus.ACCEPTED);
                result.setOutput(output.toString());
            }

            return result;

        } catch (IOException | InterruptedException e) {
            logger.error("Error executing code", e);
            CodeExecutionService.ExecutionResult errorResult = new CodeExecutionService.ExecutionResult();
            errorResult.setStatus(SubmissionStatus.RUNTIME_ERROR);
            errorResult.setErrorMessage("Error running code: " + e.getMessage());
            return errorResult;
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

}
