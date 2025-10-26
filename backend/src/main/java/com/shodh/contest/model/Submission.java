package com.shodh.contest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String submissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(nullable = false)
    private String username;

    @Column(columnDefinition = "TEXT")
    private String code;

    private String language;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer testCasesPassed = 0;
    private Integer totalTestCases = 0;

    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
}
