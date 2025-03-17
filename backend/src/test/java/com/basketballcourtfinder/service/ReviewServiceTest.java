package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.entity.*;
import com.basketballcourtfinder.enums.SortMethod;
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

    private Long courtId;
    private Long userId;
    private Review userReview;
    private User author;
    private User otherUser;
    private User otherUser2;
    private Review higherNewer;
    private Review lowerOlder;

    @BeforeEach
    public void setup() {
        courtId = 123L;
        userId = 1l;

        userReview = new Review();
        userReview.setReviewId(1L);
        userReview.setBody("Great court!");
        userReview.setEdited(false);
        userReview.setRating(5);
        userReview.setVoteCount(10);
        userReview.setCreatedAt(new Date());

        higherNewer = new Review();
        higherNewer.setReviewId(2L);
        higherNewer.setBody("Newest Higher review");
        higherNewer.setEdited(false);
        higherNewer.setRating(5);
        higherNewer.setVoteCount(10);
        higherNewer.setCreatedAt(new Date(2000L));

        lowerOlder = new Review();
        lowerOlder.setReviewId(3L);
        lowerOlder.setBody("Older review");
        lowerOlder.setEdited(false);
        lowerOlder.setRating(1);
        lowerOlder.setVoteCount(5);
        lowerOlder.setCreatedAt(new Date(1000L));

        author = new User();
        author.setId(userId);
        author.setDisplayName("John Doe");
        userReview.setUser(author);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setDisplayName("Jane Doe");
        higherNewer.setUser(otherUser);

        otherUser2 = new User();
        otherUser2.setId(3L);
        otherUser2.setDisplayName("John Smith");
        lowerOlder.setUser(otherUser2);
    }

    @Test
    public void testFindCourtReviews_Success() {
        Vote vote = new Vote();
        vote.setType(VoteType.UPVOTE);
        vote.setReview(userReview);

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Arrays.asList(userReview, higherNewer));
        when(voteRepository.findByUserIdAndReview_ReviewIdIn(eq(userId), anyList()))
                .thenReturn(Collections.singletonList(vote));

        // Call service method
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.NEWEST);

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

        // Verify repository calls
        verify(reviewRepository, times(1)).findByCourtId(courtId);
        verify(voteRepository, times(1)).findByUserIdAndReview_ReviewIdIn(eq(userId), anyList());
    }

    @Test
    public void testFindCourtReviews_NoReviews() {

        when(reviewRepository.findByCourtId(courtId)).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.NEWEST);
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

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Collections.singletonList(userReview));
        when(voteRepository.findByUserIdAndReview_ReviewIdIn(eq(userId), anyList()))
                .thenReturn(Collections.emptyList());

        // Call service method
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.NEWEST);

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
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.deleteReview(courtId, userId);
        });

        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteReview_CourtNotFound() {
        when(userRepository.existsById(userId)).thenReturn(true);
        when(courtService.getCourt(courtId)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            reviewService.deleteReview(courtId, userId);
        });

        verify(reviewRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteReview_ReviewNotFound() {
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

        when(reviewRepository.findByCourtId(courtId)).thenReturn(List.of(higherNewer, lowerOlder));


        Map map = Map.of("rating", 3.0, "reviews", 2);

        assert(Objects.equals(reviewService.getCourtRating(courtId), map));
    }

    @Test
    public void testGetCourtRating_NoRatings() {
        when(reviewRepository.findByCourtId(courtId)).thenReturn(List.of());
        Map map = Map.of("rating", 0.0, "reviews", 0);

        assert(Objects.equals(reviewService.getCourtRating(courtId), map));
    }

    @Test
    public void testFindCourtReviews_SortByNewest() {

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Arrays.asList(higherNewer, lowerOlder));

        // Call service method with SortMethod.NEWEST
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.NEWEST);

        List<ReviewResponseDTO> otherReviews = (List<ReviewResponseDTO>) response.get("otherReviews");

        // Ensure reviews are sorted by date, with the newest first
        assertThat(otherReviews.get(0).getReviewId()).isEqualTo(2L);
        assertThat(otherReviews.get(1).getReviewId()).isEqualTo(3L);

        verify(reviewRepository, times(1)).findByCourtId(courtId);
    }

    @Test
    public void testFindCourtReviews_SortByHighestRated() {

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Arrays.asList(higherNewer, lowerOlder));

        // Call service method with SortMethod.HIGHEST_RATED
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.HIGHEST);

        List<ReviewResponseDTO> otherReviews = (List<ReviewResponseDTO>) response.get("otherReviews");

        // Ensure reviews are sorted by rating, with the highest rated first
        assertThat(otherReviews.get(0).getReviewId()).isEqualTo(2L);
        assertThat(otherReviews.get(1).getReviewId()).isEqualTo(3L);

        verify(reviewRepository, times(1)).findByCourtId(courtId);
    }

    @Test
    public void testFindCourtReviews_SortByLowestRated() {

        // Mock repository behavior
        when(reviewRepository.findByCourtId(courtId)).thenReturn(Arrays.asList(higherNewer, lowerOlder));

        // Call service method with SortMethod.LOWEST_RATED
        Map<String, Object> response = reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.LOWEST);

        List<ReviewResponseDTO> otherReviews = (List<ReviewResponseDTO>) response.get("otherReviews");

        // Ensure reviews are sorted by rating, with the lowest rated first
        assertThat(otherReviews.get(0).getReviewId()).isEqualTo(3L);
        assertThat(otherReviews.get(1).getReviewId()).isEqualTo(2L);

        verify(reviewRepository, times(1)).findByCourtId(courtId);
    }

}
