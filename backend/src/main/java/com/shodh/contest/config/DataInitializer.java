package com.shodh.contest.config;

import com.shodh.contest.model.Contest;
import com.shodh.contest.model.Problem;
import com.shodh.contest.repository.ContestRepository;
import com.shodh.contest.repository.ProblemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (contestRepository.count() > 0) {
            return;
        }

        // Create sample contest
        Contest contest = new Contest();
        contest.setContestId("CONTEST-001");
        contest.setName("Shodh Coding Challenge");
        contest.setDescription("Test your coding skills with our curated problems!");
        contest.setStartTime(LocalDateTime.now());
        contest.setEndTime(LocalDateTime.now().plusDays(7));

        contest = contestRepository.save(contest);

        // Problem 1: Sum of Two Numbers
        Problem problem1 = new Problem();
        problem1.setContest(contest);
        problem1.setProblemId("SUM-001");
        problem1.setTitle("Sum of Two Numbers");
        problem1.setDescription("Write a Java program that takes two integers as input and returns their sum.\n\n" +
                "Input Format:\n" +
                "Two integers separated by a space.\n\n" +
                "Output Format:\n" +
                "The sum of the two integers.\n\n" +
                "Example:\n" +
                "Input: 5 3\n" +
                "Output: 8");
        problem1.setInputTestCases(Arrays.asList(
                "5 3",
                "10 20",
                "-5 7"
        ));
        problem1.setExpectedOutputs(Arrays.asList(
                "8",
                "30",
                "2"
        ));
        problem1.setTimeLimitSeconds(5);
        problem1.setMemoryLimitMB(256);

        // Problem 2: Find Maximum
        Problem problem2 = new Problem();
        problem2.setContest(contest);
        problem2.setProblemId("MAX-001");
        problem2.setTitle("Find Maximum");
        problem2.setDescription("Write a Java program that takes three integers as input and returns the maximum of the three.\n\n" +
                "Input Format:\n" +
                "Three integers separated by spaces.\n\n" +
                "Output Format:\n" +
                "The maximum of the three integers.\n\n" +
                "Example:\n" +
                "Input: 10 5 8\n" +
                "Output: 10");
        problem2.setInputTestCases(Arrays.asList(
                "10 5 8",
                "3 3 3",
                "-1 -5 -2"
        ));
        problem2.setExpectedOutputs(Arrays.asList(
                "10",
                "3",
                "-1"
        ));
        problem2.setTimeLimitSeconds(5);
        problem2.setMemoryLimitMB(256);

        // Problem 3: Reverse String
        Problem problem3 = new Problem();
        problem3.setContest(contest);
        problem3.setProblemId("REV-001");
        problem3.setTitle("Reverse String");
        problem3.setDescription("Write a Java program that takes a string as input and returns it reversed.\n\n" +
                "Input Format:\n" +
                "A single string.\n\n" +
                "Output Format:\n" +
                "The reversed string.\n\n" +
                "Example:\n" +
                "Input: hello\n" +
                "Output: olleh");
        problem3.setInputTestCases(Arrays.asList(
                "hello",
                "Shodh",
                "12345"
        ));
        problem3.setExpectedOutputs(Arrays.asList(
                "olleh",
                "hdohS",
                "54321"
        ));
        problem3.setTimeLimitSeconds(5);
        problem3.setMemoryLimitMB(256);

        // Save problems
        problemRepository.save(problem1);
        problemRepository.save(problem2);
        problemRepository.save(problem3);

        System.out.println("Sample data initialized successfully!");
        System.out.println("Contest ID: CONTEST-001");
        System.out.println("Contest Name: " + contest.getName());
    }
}
