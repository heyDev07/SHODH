package com.shodh.contest.service;

import com.shodh.contest.model.Problem;

public interface CodeExecutionService {
    ExecutionResult executeCode(String code, Problem problem);
    ExecutionResult executeCode(String code, Problem problem, String language);
    
    class ExecutionResult {
        private com.shodh.contest.model.SubmissionStatus status;
        private String output;
        private String errorMessage;
        private int testCasesPassed;
        private int totalTestCases;

        public com.shodh.contest.model.SubmissionStatus getStatus() { return status; }
        public void setStatus(com.shodh.contest.model.SubmissionStatus status) { this.status = status; }
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
