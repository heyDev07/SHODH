package com.shodh.contest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @OneToMany(mappedBy = "username", fetch = FetchType.LAZY)
    private List<Submission> submissions;

    // Track which contests the user has joined
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_contests",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "contest_id")
    )
    private List<Contest> joinedContests = new java.util.ArrayList<>();
}