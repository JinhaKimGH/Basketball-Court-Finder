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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RestClientTest(VoteService.class)
@AutoConfigureDataJpa
public class VoteServiceTest {
    @Autowired
    private VoteService voteService;

    @MockitoBean
    private VoteRepository voteRepository;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @MockitoBean
    private UserRepository userRepository;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUpvoteCount(0);
        user.setDownvoteCount(0);

        review = new Review();
        review.setReviewId(1L);
        review.setVoteCount(0);
    }

    @Test
    void testAddVote_NewVote() {
        // Mocking repository behavior
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(voteRepository.findByUserIdAndReview_ReviewId(1L, 1L)).thenReturn(Optional.empty());

        // Adding new vote
        voteService.addVote(1L, 1L, VoteType.UPVOTE);

        // Verifying changes
        assertEquals(1, user.getUpvoteCount());
        assertEquals(1, review.getVoteCount());
        verify(voteRepository).save(any(Vote.class));
        verify(userRepository).save(user);
        verify(reviewRepository).save(review);
    }

    @Test
    void testAddVote_ChangeVoteType() {
        // Mocking repository behavior
        Vote existingVote = new Vote();
        user.setUpvoteCount(1);
        existingVote.setUser(user);
        review.setVoteCount(1);
        existingVote.setReview(review);
        existingVote.setType(VoteType.UPVOTE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(voteRepository.findByUserIdAndReview_ReviewId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Changing vote type from UPVOTE to DOWNVOTE
        voteService.addVote(1L, 1L, VoteType.DOWNVOTE);

        // Verifying changes
        assertEquals(1, existingVote.getUser().getDownvoteCount());
        assertEquals(0, existingVote.getUser().getUpvoteCount());
        assertEquals(-1, review.getVoteCount());
        verify(voteRepository).save(any(Vote.class));
        verify(userRepository).save(user);
        verify(reviewRepository).save(review);
    }

    @Test
    void testAddVote_AlreadyVoted() {
        // Mocking repository behavior
        Vote existingVote = new Vote();
        existingVote.setUser(user);
        existingVote.setReview(review);
        existingVote.setType(VoteType.UPVOTE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(voteRepository.findByUserIdAndReview_ReviewId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Attempting to add the same vote type again (should throw an exception)
        assertThrows(EntityAlreadyExistsException.class, () -> {
            voteService.addVote(1L, 1L, VoteType.UPVOTE);
        });
    }

    @Test
    void testAddVote_NonExistentUser() {
        // Mocking repository behavior
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // Attempting to add vote for non-existent user (should throw an exception)
        assertThrows(EntityNotFoundException.class, () -> {
            voteService.addVote(1L, 1L, VoteType.UPVOTE);
        });
    }

    @Test
    void testAddVote_NonExistentReview() {
        // Mocking repository behavior
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        // Attempting to add vote for non-existent review (should throw an exception)
        assertThrows(EntityNotFoundException.class, () -> {
            voteService.addVote(1L, 1L, VoteType.UPVOTE);
        });
    }

    @Test
    void testRemoveVote_VoteExists() {
        // Mocking repository behavior
        Vote existingVote = new Vote();
        user.setUpvoteCount(1);
        existingVote.setUser(user);
        review.setVoteCount(1);
        existingVote.setReview(review);
        existingVote.setType(VoteType.UPVOTE);

        when(voteRepository.findByUserIdAndReview_ReviewId(1L, 1L)).thenReturn(Optional.of(existingVote));

        // Removing vote
        ResponseEntity<?> response = voteService.removeVote(1L, 1L);

        // Verifying changes
        assertEquals("Vote removed successfully.", response.getBody());
        assertEquals(0, user.getUpvoteCount());
        assertEquals(0, review.getVoteCount());
        verify(voteRepository).delete(existingVote);
        verify(userRepository).save(user);
        verify(reviewRepository).save(review);
    }

    @Test
    void testRemoveVote_VoteNotFound() {
        // Mocking repository behavior
        when(voteRepository.findByUserIdAndReview_ReviewId(1L, 1L)).thenReturn(Optional.empty());

        // Attempting to remove non-existent vote (should return NOT_FOUND)
        ResponseEntity<?> response = voteService.removeVote(1L, 1L);

        // Verifying response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("No vote found to remove.", response.getBody());
    }

}
