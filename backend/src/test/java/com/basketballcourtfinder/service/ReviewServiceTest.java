package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.entity.*;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
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
import static org.junit.jupiter.api.Assertions.*;
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

        Review userReview = new Review();
        userReview.setReviewId(1L);
        userReview.setBody("Great court!");
        userReview.setEdited(false);
        userReview.setRating(5);
        userReview.setVoteCount(10);
        userReview.setCreatedAt(new Date());

        User author = new User();
        author.setId(userId);
        author.setDisplayName("John Doe");
        userReview.setUser(author);

        Review otherReview = new Review();
        otherReview.setReviewId(2L);
        otherReview.setBody("Needs maintenance");
        otherReview.setEdited(false);
        otherReview.setRating(3);
        otherReview.setVoteCount(5);
        otherReview.setCreatedAt(new Date());

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setDisplayName("Jane Doe");
        otherReview.setUser(otherUser);

        Vote vote = new Vote();
        vote.setType(VoteType.UPVOTE);
        vote.setReview(userReview);

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Arrays.asList(userReview, otherReview));
        when(voteRepository.findByUserIdAndReview_ReviewIdIn(eq(userId), anyList()))
                .thenReturn(Collections.singletonList(vote));

        // Call service method
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId);

        // Validate user review
        ReviewResponseDTO userReviewDTO = (ReviewResponseDTO) response.get("userReview");
        assertThat(userReviewDTO).isNotNull();
        assertThat(userReviewDTO.getReviewId()).isEqualTo(1L);
        assertThat(userReviewDTO.getContent()).isEqualTo("Great court!");
        assertThat(userReviewDTO.isUpvoted()).isTrue();
        assertThat(userReviewDTO.isDownvoted()).isFalse();
        assertThat(userReviewDTO.getAuthorDisplayName()).isEqualTo("John Doe");

        // Validate other reviews list
        List<ReviewResponseDTO> otherReviews = (List<ReviewResponseDTO>) response.get("otherReviews");
        assertThat(otherReviews).hasSize(1);

        ReviewResponseDTO otherReviewDTO = otherReviews.get(0);
        assertThat(otherReviewDTO.getReviewId()).isEqualTo(2L);
        assertThat(otherReviewDTO.getContent()).isEqualTo("Needs maintenance");
        assertThat(otherReviewDTO.isUpvoted()).isFalse();
        assertThat(otherReviewDTO.isDownvoted()).isFalse();
        assertThat(otherReviewDTO.getAuthorDisplayName()).isEqualTo("Jane Doe");

        // Verify repository calls
        verify(reviewRepository, times(1)).findByCourtId(courtId);
        verify(voteRepository, times(1)).findByUserIdAndReview_ReviewIdIn(eq(userId), anyList());
    }

    @Test
    public void testFindCourtReviews_NoReviews() {
        Long courtId = 123L;
        Long userId = 1L;

        when(reviewRepository.findByCourtId(courtId)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = reviewService.findCourtReviews(courtId, userId);
        Object userReviewObj = result.get("userReview");
        Optional<ReviewResponseDTO> userReview = userReviewObj instanceof Optional
                ? (Optional<ReviewResponseDTO>) userReviewObj
                : Optional.empty();

        // Assert
        assertThat(userReview).isEmpty();
        assertThat(((List<?>) result.get("otherReviews")).isEmpty()).isTrue();

        verify(reviewRepository).findByCourtId(courtId);
        verify(voteRepository, never()).findByUserIdAndReview_ReviewIdIn(anyLong(), anyList());
    }

    @Test
    public void testFindCourtReviews_NoVotes() {
        Long courtId = 123L;
        Long userId = 1L;

        Review userReview = new Review();
        userReview.setReviewId(1L);
        userReview.setBody("Great court!");
        userReview.setEdited(false);
        userReview.setRating(5);
        userReview.setVoteCount(10);
        userReview.setCreatedAt(new Date());

        User author = new User();
        author.setId(userId);
        author.setDisplayName("John Doe");
        userReview.setUser(author);

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Collections.singletonList(userReview));
        when(voteRepository.findByUserIdAndReview_ReviewIdIn(eq(userId), anyList()))
                .thenReturn(Collections.emptyList());

        // Call service method
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId);

        // Validate user review
        ReviewResponseDTO userReviewDTO = (ReviewResponseDTO) response.get("userReview");
        assertThat(userReviewDTO).isNotNull();
        assertThat(userReviewDTO.isUpvoted()).isFalse();
        assertThat(userReviewDTO.isDownvoted()).isFalse();

        // Validate other reviews list
        List<ReviewResponseDTO> otherReviews = (List<ReviewResponseDTO>) response.get("otherReviews");
        assertThat(otherReviews.isEmpty()).isTrue();

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

        assertThrows(EntityAlreadyExistsException.class, () -> {
            reviewService.saveReview(reviewDTO, 1L);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testPatchReviewBody_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        Review review = new Review();
        review.setUser(user);
        review.setReviewId(1L);
        review.setBody("Original Body");

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO updates = new ReviewResponseDTO();
        updates.setContent("New Body");

        reviewService.partialUpdate(1L, 1L, updates);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testPatchReviewBody_ReviewNotFound() throws Exception {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewResponseDTO updates = new ReviewResponseDTO();
        updates.setContent("New Body");

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.partialUpdate(1L, 1L, updates);
        });

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testPatchReviewRating_Success() throws Exception {
        User user = new User();
        user.setId(1L);
        Review review = new Review();
        review.setUser(user);
        review.setReviewId(1L);
        review.setRating(3);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponseDTO updates = new ReviewResponseDTO();
        updates.setRating(5);

        reviewService.partialUpdate(1L, 1L, updates);

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testPatchReviewRating_ReviewNotFound() throws Exception {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        ReviewResponseDTO updates = new ReviewResponseDTO();
        updates.setRating(5);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.partialUpdate(1L, 1L, updates);
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

    @Test
    public void testGetCourtRating_MultipleRatings() {
        // Mock data
        Long courtId = 123L;

        Review review1 = new Review();
        review1.setReviewId(1L);
        review1.setBody("Great court!");
        review1.setEdited(false);
        review1.setRating(5);
        review1.setVoteCount(10);
        review1.setCreatedAt(new Date());

        Review review2 = new Review();
        review2.setReviewId(2L);
        review2.setBody("Needs maintenance");
        review2.setEdited(false);
        review2.setRating(3);
        review2.setVoteCount(5);
        review2.setCreatedAt(new Date());

        when(reviewRepository.findByCourtId(courtId)).thenReturn(List.of(review1, review2));


        Map map = Map.of("rating", 4.0, "reviews", 2);

        assert(Objects.equals(reviewService.getCourtRating(courtId), map));
    }

    @Test
    public void testGetCourtRating_NoRatings() {
        // Mock data
        Long courtId = 123L;

        when(reviewRepository.findByCourtId(courtId)).thenReturn(List.of());
        Map map = Map.of("rating", 0.0, "reviews", 0);

        assert(Objects.equals(reviewService.getCourtRating(courtId), map));
    }
}
