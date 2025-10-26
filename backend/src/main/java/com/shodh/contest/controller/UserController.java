package com.shodh.contest.controller;

import com.shodh.contest.dto.UserDto;
import com.shodh.contest.model.Contest;
import com.shodh.contest.model.User;
import com.shodh.contest.repository.ContestRepository;
import com.shodh.contest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContestRepository contestRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setDisplayName(userDto.getDisplayName() != null ? userDto.getDisplayName() : userDto.getUsername());
        
        user = userRepository.save(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @PostMapping("/join-contest")
    public ResponseEntity<?> joinContest(@RequestParam String username, @RequestParam String contestId) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Contest> contestOpt = contestRepository.findByContestId(contestId);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        
        if (contestOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Contest not found"));
        }
        
        User user = userOpt.get();
        Contest contest = contestOpt.get();
        
        if (user.getJoinedContests() != null && user.getJoinedContests().contains(contest)) {
            return ResponseEntity.ok(Map.of("message", "User already joined this contest"));
        }
        
        user.getJoinedContests().add(contest);
        userRepository.save(user);
        
        return ResponseEntity.ok(Map.of("message", "Successfully joined contest"));
    }
}