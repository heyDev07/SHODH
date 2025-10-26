package com.shodh.contest.repository;

import com.shodh.contest.model.Contest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    Optional<Contest> findByContestId(String contestId);
    
    @EntityGraph(attributePaths = {"problems"})
    @Query("SELECT c FROM Contest c WHERE c.contestId = :contestId")
    Optional<Contest> findByContestIdWithProblems(@Param("contestId") String contestId);
}
