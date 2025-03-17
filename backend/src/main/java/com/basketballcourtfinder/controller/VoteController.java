package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.entity.VoteType;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.service.VoteService;
import com.basketballcourtfinder.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vote")
public class VoteController {

    @Autowired
    private VoteService voteService;

    private ResponseEntity<?> addVote(Long reviewId, VoteType voteType) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        try {
            voteService.addVote(reviewId, userId, voteType);
            return ResponseEntity.ok("Vote added successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{reviewId}/upvote")
    public ResponseEntity<?> addUpvote(@PathVariable Long reviewId) {
        return addVote(reviewId, VoteType.UPVOTE);

    }

    @PostMapping("/{reviewId}/downvote")
    public ResponseEntity<?> addDownvote(@PathVariable Long reviewId) {
        return addVote(reviewId, VoteType.DOWNVOTE);
    }

    @DeleteMapping("/{reviewId}")
    private ResponseEntity<?> removeVote(@PathVariable Long reviewId) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        return voteService.removeVote(reviewId, userId);
    }
}
