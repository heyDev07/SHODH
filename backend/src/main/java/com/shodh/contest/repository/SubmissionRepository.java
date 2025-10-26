package com.shodh.contest.repository;

import com.shodh.contest.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findBySubmissionId(String submissionId);
    List<Submission> findByContestIdOrderBySubmittedAtDesc(Long contestId);
}
