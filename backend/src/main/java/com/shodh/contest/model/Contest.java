package com.shodh.contest.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "contests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String contestId;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Problem> problems;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "contest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Submission> submissions;
}
