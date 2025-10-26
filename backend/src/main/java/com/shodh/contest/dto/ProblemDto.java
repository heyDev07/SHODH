package com.shodh.contest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDto {
    private Long id;
    private String problemId;
    private String title;
    private String description;
    private List<String> inputTestCases;
    private List<String> expectedOutputs;
    private Integer timeLimitSeconds;
    private Integer memoryLimitMB;
}
