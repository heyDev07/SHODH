package com.shodh.contest.repository;

import com.shodh.contest.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByContestId(Long contestId);
    Optional<Problem> findByProblemId(String problemId);
}
