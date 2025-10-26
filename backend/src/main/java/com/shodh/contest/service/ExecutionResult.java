package com.shodh.contest.service;

import com.shodh.contest.model.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    private SubmissionStatus status;
    private int testCasesPassed;
    private int totalTestCases;
    private String errorMessage;
    private List<String> outputs;
}