package com.shodh.contest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardEntry {
    private String username;
    private Long totalSubmissions;
    private Long acceptedSubmissions;
    private Long totalProblemsSolved;
}
