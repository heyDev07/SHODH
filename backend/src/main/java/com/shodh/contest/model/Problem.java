package com.shodh.contest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    @JsonIgnore
    private Contest contest;

    @Column(nullable = false)
    private String problemId;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "problem_input_test_cases", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "input_case")
    private List<String> inputTestCases;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "problem_expected_outputs", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "expected_output")
    private List<String> expectedOutputs;

    private Integer timeLimitSeconds = 5;
    private Integer memoryLimitMB = 256;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Submission> submissions;
}
