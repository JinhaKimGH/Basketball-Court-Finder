package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.entity.*;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.repository.ReviewRepository;
import com.basketballcourtfinder.repository.UserRepository;
import com.basketballcourtfinder.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@RestClientTest(ReviewService.class)
@AutoConfigureDataJpa
public class ReviewServiceTest {
    @Autowired
    private ReviewService reviewService;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @MockitoBean
    private VoteRepository voteRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private BasketballCourtService courtService;

    @Test
    public void testFindCourtReviews_Success() {
        // Mock data
        Long courtId = 123L;
        Long userId = 1L;

        Review review1 = new Review();
        review1.setReviewId(1L);
        review1.setBody("Great court!");
        review1.setTitle("Amazing Experience");
        review1.setEdited(false);
        review1.setRating(5);
        review1.setVoteCount(10);
        review1.setCreatedAt(new Date());

        User author = new User();
        author.setDisplayName("John Doe");
        review1.setUser(author);

        Review review2 = new Review();
        review2.setReviewId(2L);
        review2.setBody("Needs maintenance");
        review2.setTitle("Average");
        review2.setEdited(false);
        review2.setRating(3);
        review2.setVoteCount(5);
        review2.setCreatedAt(new Date());
        review2.setUser(author);

        Vote vote1 = new Vote();
        vote1.setType(VoteType.UPVOTE);
        vote1.setReview(review1);

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Arrays.asList(review1, review2));
        when(voteRepository.findByUserIdAndReview_ReviewIdIn(eq(userId), anyList()))
                .thenReturn(Collections.singletonList(vote1));

        // Call service method
        List<ReviewResponseDTO> response = reviewService.findCourtReviews(courtId, userId);

        // Verify response
        assertThat(response).hasSize(2);

        // Validate first review
        ReviewResponseDTO reviewResponse1 = response.get(0);
        assertThat(reviewResponse1.getReviewId()).isEqualTo(1L);
        assertThat(reviewResponse1.getContent()).isEqualTo("Great court!");
        assertThat(reviewResponse1.isUpvoted()).isTrue();
        assertThat(reviewResponse1.isDownvoted()).isFalse();
        assertThat(reviewResponse1.getAuthorDisplayName()).isEqualTo("John Doe");

        // Validate second review
        ReviewResponseDTO reviewResponse2 = response.get(1);
        assertThat(reviewResponse2.getReviewId()).isEqualTo(2L);
        assertThat(reviewResponse2.getContent()).isEqualTo("Needs maintenance");
        assertThat(reviewResponse2.isUpvoted()).isFalse();
        assertThat(reviewResponse2.isDownvoted()).isFalse();

        // Verify interactions
        verify(reviewRepository, times(1)).findByCourtId(courtId);
        verify(voteRepository, times(1)).findByUserIdAndReview_ReviewIdIn(eq(userId), anyList());
    }

    @Test
    public void testFindCourtReviews_NoReviews() {
        Long courtId = 123L;
        Long userId = 1L;

        when(reviewRepository.findByCourtId(courtId)).thenReturn(Collections.emptyList());

        // Act
        List<ReviewResponseDTO> result = reviewService.findCourtReviews(courtId, userId);

        // Assert
        assertThat(result.isEmpty());
        verify(reviewRepository).findByCourtId(courtId);
        verify(voteRepository, never()).findByUserIdAndReview_ReviewIdIn(anyLong(), anyList());
    }

    @Test
    public void testFindCourtReviews_NoVotes() {
        Long courtId = 123L;
        Long userId = 1L;

        Review review1 = new Review();
        review1.setReviewId(1L);
        review1.setBody("Great court!");
        review1.setTitle("Amazing Experience");
        review1.setEdited(false);
        review1.setRating(5);
        review1.setVoteCount(10);
        review1.setCreatedAt(new Date());

        User author = new User();
        author.setDisplayName("John Doe");
        review1.setUser(author);

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Collections.singletonList(review1));
        when(voteRepository.findByUserIdAndReview_ReviewIdIn(eq(userId), anyList()))
                .thenReturn(Collections.emptyList());

        // Call service method
        List<ReviewResponseDTO> response = reviewService.findCourtReviews(courtId, userId);

        // Verify response
        assertThat(response).hasSize(1);
        assertThat(response.get(0).isUpvoted()).isFalse();
        assertThat(response.get(0).isDownvoted()).isFalse();

        // Verify interactions
        verify(reviewRepository, times(1)).findByCourtId(courtId);
        verify(voteRepository, times(1)).findByUserIdAndReview_ReviewIdIn(eq(userId), anyList());
    }

    @Test
    public void testSaveReview() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBody("This is a test review");
        reviewDTO.setRating(5);
        reviewDTO.setCourtId(1L);

        User user = new User();
        BasketballCourt court = new BasketballCourt();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courtService.getCourt(1L)).thenReturn(court);
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(new Review());

        reviewService.saveReview(reviewDTO, 1L);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testSaveReview_UserNotFound() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBody("This is a test review");
        reviewDTO.setRating(5);
        reviewDTO.setCourtId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.saveReview(reviewDTO, 1L);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSaveReview_CourtNotFound() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBody("This is a test review");
        reviewDTO.setRating(5);
        reviewDTO.setCourtId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(courtService.getCourt(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.saveReview(reviewDTO, 1L);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSaveReview_ReviewExists() {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setBody("This is a test review");
        reviewDTO.setRating(5);
        reviewDTO.setCourtId(1L);

        User user = new User();
        BasketballCourt court = new BasketballCourt();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(courtService.getCourt(1L)).thenReturn(court);
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.of(new Review()));

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.saveReview(reviewDTO, 1L);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewTitle_Success() {
        Review review = new Review();
        review.setReviewId(1L);
        review.setTitle("Original Title");

        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.updateReviewTitle(1L, 1L, "New Title");

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewTitle_UserNotFound() {

        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewTitle(1L, 1L, "New Title");
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewTitle_CourtNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewTitle(1L, 1L, "New Title");
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewTitle_ReviewNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewTitle(1L, 1L, "New Title");
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewBody_Success() {
        Review review = new Review();
        review.setReviewId(1L);
        review.setBody("Original Body");

        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.updateReviewBody(1L, 1L, "New Body");

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewBody_UserNotFound() {

        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewBody(1L, 1L, "New Body");
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewBody_CourtNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewBody(1L, 1L, "New Body");
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewBody_ReviewNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewBody(1L, 1L, "New Body");
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewRating_Success() {
        Review review = new Review();
        review.setReviewId(1L);
        review.setRating(3);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.updateReviewRating(1L, 1L, 5);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewRating_UserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewRating(1L, 1L, 5);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewRating_CourtNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewRating(1L, 1L, 5);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testUpdateReviewRating_ReviewNotFound() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtService.getCourt(1L)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.updateReviewRating(1L, 1L, 5);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testDeleteReview_Success() {
        Long userId = 1L;
        Long courtId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(courtService.getCourt(courtId)).thenReturn(new BasketballCourt());
        Review review = new Review();
        review.setReviewId(1L);
        review.setUser(new User());
        review.setCourt(new BasketballCourt());

        when(reviewRepository.findByCourtIdAndUserId(courtId, userId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(courtId, userId);

        verify(reviewRepository).deleteById(1L);
    }

    @Test
    public void testDeleteReview_UserNotFound() {
        Long userId = 1L;
        Long courtId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.deleteReview(courtId, userId);
        });

        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteReview_CourtNotFound() {
        Long userId = 1L;
        Long courtId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(courtService.getCourt(courtId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.deleteReview(courtId, userId);
        });

        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteReview_ReviewNotFound() {
        Long userId = 1L;
        Long courtId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);
        when(courtService.getCourt(courtId)).thenReturn(new BasketballCourt());
        when(reviewRepository.findByCourtIdAndUserId(courtId, userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.deleteReview(courtId, userId);
        });

        verify(reviewRepository, never()).deleteById(anyLong());
    }
}
