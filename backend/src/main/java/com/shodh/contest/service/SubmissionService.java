package com.shodh.contest.service;

import com.shodh.contest.dto.SubmissionRequest;
import com.shodh.contest.dto.SubmissionResponse;
import com.shodh.contest.model.*;
import com.shodh.contest.repository.ContestRepository;
import com.shodh.contest.repository.ProblemRepository;
import com.shodh.contest.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private DockerExecutionService dockerExecutionService;

    @Transactional
    public SubmissionResponse submitCode(SubmissionRequest request) {
        // Validate contest exists
        Contest contest = contestRepository.findByContestId(request.getContestId())
                .orElseThrow(() -> new RuntimeException("Contest not found: " + request.getContestId()));

        // Validate problem exists
        Problem problem = problemRepository.findByProblemId(request.getProblemId())
                .orElseThrow(() -> new RuntimeException("Problem not found: " + request.getProblemId()));

        // Create submission
        Submission submission = new Submission();
        submission.setSubmissionId(UUID.randomUUID().toString());
        submission.setContest(contest);
        submission.setProblem(problem);
        submission.setUsername(request.getUsername());
        submission.setCode(request.getCode());
        submission.setLanguage(request.getLanguage() != null ? request.getLanguage() : "java"); // Default to Java
        submission.setStatus(SubmissionStatus.PENDING);
        submission.setSubmittedAt(LocalDateTime.now());

        submission = submissionRepository.save(submission);

        logger.info("Created submission: {}", submission.getSubmissionId());

        // Process submission asynchronously
        processSubmissionAsync(submission.getId());

        return convertToResponse(submission);
    }

    @Async
    public void processSubmissionAsync(Long submissionId) {
        try {
            Submission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

            logger.info("Processing submission: {}", submission.getSubmissionId());

            submission.setStatus(SubmissionStatus.RUNNING);
            submissionRepository.save(submission);

            // Execute the code
            Problem problem = submission.getProblem();
            DockerExecutionService.ExecutionResult result = dockerExecutionService.executeCode(
                    submission.getCode(),
                    problem,
                    submission.getLanguage()
            );

            // Update submission with results
            submission.setStatus(result.getStatus());
            submission.setErrorMessage(result.getErrorMessage());
            submission.setTestCasesPassed(result.getTestCasesPassed());
            submission.setTotalTestCases(result.getTotalTestCases());
            submission.setProcessedAt(LocalDateTime.now());

            submissionRepository.save(submission);

            logger.info("Submission {} processed with status: {}", submission.getSubmissionId(), result.getStatus());

        } catch (Exception e) {
            logger.error("Error processing submission: " + submissionId, e);

            // Update submission with error status
            try {
                Submission submission = submissionRepository.findById(submissionId).orElse(null);
                if (submission != null) {
                    submission.setStatus(SubmissionStatus.RUNTIME_ERROR);
                    submission.setErrorMessage("Error processing submission: " + e.getMessage());
                    submission.setProcessedAt(LocalDateTime.now());
                    submissionRepository.save(submission);
                }
            } catch (Exception ex) {
                logger.error("Error updating submission status", ex);
            }
        }
    }

    public SubmissionResponse getSubmission(String submissionId) {
        Submission submission = submissionRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        return convertToResponse(submission);
    }

    @Transactional(readOnly = true)
    public List<com.shodh.contest.dto.LeaderboardEntry> getLeaderboard(String contestId) {
        Contest contest = contestRepository.findByContestId(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found: " + contestId));

        List<Submission> submissions = submissionRepository.findByContestIdOrderBySubmittedAtDesc(contest.getId());

        // Group by username and calculate stats
        return submissions.stream()
                .collect(java.util.stream.Collectors.groupingBy(Submission::getUsername))
                .entrySet().stream()
                .map(entry -> {
                    List<Submission> userSubmissions = entry.getValue();

                    // Count accepted submissions by problem
                    long uniqueProblemsSolved = userSubmissions.stream()
                            .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                            .map(s -> s.getProblem().getProblemId())
                            .distinct()
                            .count();

                    long totalSubmissions = userSubmissions.size();
                    long acceptedSubmissions = userSubmissions.stream()
                            .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                            .count();

                    return new com.shodh.contest.dto.LeaderboardEntry(
                            entry.getKey(),
                            totalSubmissions,
                            acceptedSubmissions,
                            uniqueProblemsSolved
                    );
                })
                .sorted((a, b) -> {
                    // Sort by problems solved (descending), then by accepted submissions (descending)
                    int compare = Long.compare(b.getTotalProblemsSolved(), a.getTotalProblemsSolved());
                    if (compare != 0) return compare;
                    return Long.compare(b.getAcceptedSubmissions(), a.getAcceptedSubmissions());
                })
                .collect(Collectors.toList());
    }

    private SubmissionResponse convertToResponse(Submission submission) {
        SubmissionResponse response = new SubmissionResponse();
        response.setSubmissionId(submission.getSubmissionId());
        response.setUsername(submission.getUsername());
        response.setProblemId(submission.getProblem().getProblemId());
        response.setLanguage(submission.getLanguage());
        response.setStatus(submission.getStatus());
        response.setErrorMessage(submission.getErrorMessage());
        response.setTestCasesPassed(submission.getTestCasesPassed());
        response.setTotalTestCases(submission.getTotalTestCases());
        response.setSubmittedAt(submission.getSubmittedAt());
        response.setProcessedAt(submission.getProcessedAt());
        return response;
    }
}
