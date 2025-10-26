package com.shodh.contest.controller;

import com.shodh.contest.dto.ContestDto;
import com.shodh.contest.dto.LeaderboardEntry;
import com.shodh.contest.dto.ProblemDto;
import com.shodh.contest.dto.SubmissionRequest;
import com.shodh.contest.dto.SubmissionResponse;
import com.shodh.contest.model.Contest;
import com.shodh.contest.model.Problem;
import com.shodh.contest.repository.ContestRepository;
import com.shodh.contest.service.SubmissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ContestController {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private SubmissionService submissionService;

    @GetMapping("/contests/{contestId}")
    public ResponseEntity<?> getContest(@PathVariable String contestId) {
        Optional<Contest> contestOpt = contestRepository.findByContestIdWithProblems(contestId);
        
        if (contestOpt.isPresent()) {
            Contest contest = contestOpt.get();
            ContestDto dto = new ContestDto();
            BeanUtils.copyProperties(contest, dto);
            
            if (contest.getProblems() != null) {
                List<ProblemDto> problemDtos = contest.getProblems().stream()
                        .map(problem -> {
                            ProblemDto problemDto = new ProblemDto();
                            BeanUtils.copyProperties(problem, problemDto);
                            return problemDto;
                        })
                        .collect(Collectors.toList());
                dto.setProblems(problemDtos);
            }
            
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Contest not found");
        }
    }

    @PostMapping("/submissions")
    public ResponseEntity<?> submitCode(@RequestBody SubmissionRequest request) {
        try {
            SubmissionResponse response = submissionService.submitCode(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<?> getSubmission(@PathVariable String submissionId) {
        try {
            SubmissionResponse response = submissionService.getSubmission(submissionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable String contestId) {
        try {
            List<LeaderboardEntry> leaderboard = submissionService.getLeaderboard(contestId);
            return ResponseEntity.ok(leaderboard);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/contests/{contestId}/problems")
    public ResponseEntity<?> getProblems(@PathVariable String contestId) {
        Optional<Contest> contestOpt = contestRepository.findByContestIdWithProblems(contestId);
        
        if (contestOpt.isPresent()) {
            Contest contest = contestOpt.get();
            if (contest.getProblems() != null) {
                List<ProblemDto> problemDtos = contest.getProblems().stream()
                        .map(problem -> {
                            ProblemDto problemDto = new ProblemDto();
                            BeanUtils.copyProperties(problem, problemDto);
                            return problemDto;
                        })
                        .collect(Collectors.toList());
                return ResponseEntity.ok(problemDtos);
            }
            return ResponseEntity.ok(List.of());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Contest not found");
        }
    }
}
