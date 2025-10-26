package com.shodh.contest.dto;

import com.shodh.contest.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private String submissionId;
    private String username;
    private String problemId;
    private String language;
    private SubmissionStatus status;
    private String errorMessage;
    private Integer testCasesPassed;
    private Integer totalTestCases;
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
}
