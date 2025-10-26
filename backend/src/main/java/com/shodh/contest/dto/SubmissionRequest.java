package com.shodh.contest.dto;

import lombok.Data;

@Data
public class SubmissionRequest {
    private String contestId;
    private String problemId;
    private String username;
    private String code;
    private String language;
}
