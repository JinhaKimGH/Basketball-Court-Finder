package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.entity.User;
import com.basketballcourtfinder.entity.Vote;
import com.basketballcourtfinder.entity.VoteType;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.repository.ReviewRepository;
import com.basketballcourtfinder.repository.UserRepository;
import com.basketballcourtfinder.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VoteService {
    @Autowired
    private final VoteRepository voteRepository;

    @Autowired
    private final ReviewRepository reviewRepository;

    @Autowired
    private final UserRepository userRepository;

    private final ConcurrentHashMap<Long, Object> lockMap = new ConcurrentHashMap<>();

    public VoteService(VoteRepository voteRepository, ReviewRepository reviewRepository, UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public void addVote(Long reviewId, Long userId, VoteType voteType) {
        // Obtain a unique lock for the reviewId
        Object lock = lockMap.computeIfAbsent(reviewId, k -> new Object());

        synchronized (lock) {
            try {
                // Check if user already voted this review
                Optional<Vote> existingVote = voteRepository.findByUserIdAndReview_ReviewId(userId, reviewId);

                if(existingVote.isPresent()) {
                    Vote vote = existingVote.get();
                    if (vote.getType() == voteType) {
                        throw new EntityAlreadyExistsException("You have already " + voteType.name().toLowerCase()
                                + "ed this review.");
                    } else {
                        updateUserAndReview(voteType, vote);

                        // Change vote type
                        vote.setType(voteType);
                        voteRepository.save(vote);
                        return;
                    }
                }

                // Fetch user and review
                User voter = userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("user", userId));
                Review review = reviewRepository.findById(reviewId)
                        .orElseThrow(() -> new EntityNotFoundException("review", reviewId));

                // Save upvote
                Vote vote = new Vote();
                vote.setReview(review);
                vote.setType(voteType);
                if (voteType == VoteType.UPVOTE) {
                    voter.setUpvoteCount(voter.getUpvoteCount() + 1);
                    review.incrementVoteCount();
                } else if (voteType == VoteType.DOWNVOTE) {
                    voter.setDownvoteCount(voter.getDownvoteCount() + 1);
                    review.decrementVoteCount();
                }
                vote.setUser(voter);
                voteRepository.save(vote);
                userRepository.save(voter);
                reviewRepository.save(review);
            } finally {
                lockMap.remove(reviewId);
            }
        }
    }

    private void updateUserAndReview(VoteType voteType, Vote vote) {
        User user = vote.getUser();
        Review review = vote.getReview();
        // Update counts based on the change in vote type
        if (vote.getType() == VoteType.UPVOTE && voteType == VoteType.DOWNVOTE) {
            user.setUpvoteCount(user.getUpvoteCount() - 1);
            user.setDownvoteCount(user.getDownvoteCount() + 1);
            review.decrementVoteCount(2);
        } else if (vote.getType() == VoteType.DOWNVOTE && voteType == VoteType.UPVOTE) {
            user.setDownvoteCount(user.getDownvoteCount() - 1);
            user.setUpvoteCount(user.getUpvoteCount() + 1);
            review.incrementVoteCount(2);
        }

        userRepository.save(user);
        reviewRepository.save(review);
    }

    public ResponseEntity<?> removeVote(Long reviewId, Long userId) {
        // Obtain a unique lock for the reviewId
        Object lock = lockMap.computeIfAbsent(reviewId, k -> new Object());

        synchronized (lock) {
            try {
                Optional<Vote> vote = voteRepository.findByUserIdAndReview_ReviewId(userId, reviewId);

                if (vote.isPresent()) {
                    Vote vote1 = vote.get();
                    Review review = vote1.getReview();
                    User user = vote1.getUser();
                    if (vote1.getType() == VoteType.UPVOTE) {
                        user.setUpvoteCount(user.getUpvoteCount() - 1);
                        review.setVoteCount(review.getVoteCount() - 1);
                    }
                    else if (vote1.getType() == VoteType.DOWNVOTE) {
                        user.setDownvoteCount(user.getDownvoteCount() - 1);
                        review.setVoteCount(review.getVoteCount() + 1);
                    }

                    updateUserAndReview(vote1.getType(), vote1);
                    voteRepository.delete(vote1);
                    return ResponseEntity.ok("Vote removed successfully.");
                }

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No vote found to remove.");
            } finally {
                lockMap.remove(reviewId);
            }
        }
    }


}
