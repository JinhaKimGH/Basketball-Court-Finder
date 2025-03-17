package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.enums.SortMethod;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Mock
    Authentication authentication;

    public void setup_authUser() {
        // Create a mocked Authentication object
        Long mockUserId = 1L;
        authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal()).thenReturn(mockUserId);

        // Mock the SecurityContextHolder
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testGetReview() throws Exception {
        setup_authUser();

        long courtId = 1L;
        long userId = 1L;

        // Create a mock user review
        ReviewResponseDTO userReview = new ReviewResponseDTO();
        userReview.setReviewId(1L);
        userReview.setRating(5);
        userReview.setContent("Great court!");
        userReview.setTotalVotes(10);
        userReview.setAuthorDisplayName("Test User");
        userReview.setAuthorTrustScore(100);
        userReview.setUpvoted(true);
        userReview.setDownvoted(false);
        userReview.setCreatedAt(new Date());
        userReview.setEdited(false);

        // Create a list for other reviews
        List<ReviewResponseDTO> otherReviews = new ArrayList<>();
        ReviewResponseDTO otherReview = new ReviewResponseDTO();
        otherReview.setReviewId(2L);
        otherReview.setRating(4);
        otherReview.setContent("Decent court.");
        otherReview.setTotalVotes(5);
        otherReview.setAuthorDisplayName("Another User");
        otherReview.setAuthorTrustScore(80);
        otherReview.setUpvoted(false);
        otherReview.setDownvoted(false);
        otherReview.setCreatedAt(new Date());
        otherReview.setEdited(false);
        otherReviews.add(otherReview);

        // Mock the service response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("userReview", userReview);
        responseMap.put("otherReviews", otherReviews);

        when(reviewService.findCourtReviews(courtId, userId, 1, 10, SortMethod.NEWEST)).thenReturn(responseMap);

        mockMvc.perform(get("/api/review")
                        .param("courtId", String.valueOf(courtId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userReview.reviewId").value(1L))
                .andExpect(jsonPath("$.userReview.rating").value(5))
                .andExpect(jsonPath("$.userReview.content").value("Great court!"))
                .andExpect(jsonPath("$.otherReviews[0].reviewId").value(2L))
                .andExpect(jsonPath("$.otherReviews[0].rating").value(4))
                .andExpect(jsonPath("$.otherReviews[0].content").value("Decent court."));
    }

    @Test
    public void testGetReviewWithFailedAuthorization() throws Exception {

        long courtId = 1L;

        mockMvc.perform(get("/api/review")
                .param("courtId", String.valueOf(courtId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPostReviewSuccess() throws Exception {
        setup_authUser();
        long courtId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doNothing().when(reviewService).saveReview(review, courtId);

        mockMvc.perform(post("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Review added successfully."));
    }

    @Test
    public void testPostReviewFailedAuthorization() throws Exception {
        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        mockMvc.perform(post("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPostReviewFailedConflict() throws Exception {
        setup_authUser();
        long courtId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doThrow(new IllegalArgumentException("Review already exists.")).when(reviewService).saveReview(review, courtId);

        mockMvc.perform(post("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isConflict())
                .andExpect(content().string("Review already exists."));
    }

    @Test
    public void testPostReviewFailedNotFound() throws Exception {
        setup_authUser();
        long courtId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doThrow(new EntityNotFoundException("court", 1L)).when(reviewService).saveReview(review, courtId);

        mockMvc.perform(post("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("court with ID 1 not found"));
    }

    @Test
    public void testPatchReviewSuccessAll() throws Exception {
        setup_authUser();
        long reviewId = 1L;
        long userId = 1L;

        // Creating the ReviewDTO with both body and rating
        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        // Mocking the service method to simulate partial update behavior
        doReturn(new Review()).when(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));

        // Performing the PATCH request
        mockMvc.perform(patch("/api/review/{reviewId}", reviewId)
                        .contentType("application/json")
                        .content(reviewJson))
                .andExpect(status().isOk());

        // Verifying interactions with the service
        verify(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));
    }

    @Test
    public void testPatchReviewSuccessBody() throws Exception {
        setup_authUser();
        long reviewId = 1L;
        long userId = 1L;

        // Creating the ReviewDTO with only body
        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        // Mocking the service method to simulate partial update behavior
        doReturn(new Review()).when(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));

        // Performing the PATCH request
        mockMvc.perform(patch("/api/review/{reviewId}", reviewId)
                        .contentType("application/json")
                        .content(reviewJson))
                .andExpect(status().isOk());

        // Verifying interactions with the service
        verify(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));
    }

    @Test
    public void testPatchReviewSuccessRating() throws Exception {
        setup_authUser();
        long reviewId = 1L;
        long userId = 1L;

        // Creating the ReviewDTO with only rating
        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        // Mocking the service method to simulate partial update behavior
        doReturn(new Review()).when(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));

        // Performing the PATCH request
        mockMvc.perform(patch("/api/review/{reviewId}", reviewId)
                        .contentType("application/json")
                        .content(reviewJson))
                .andExpect(status().isOk());

        // Verifying interactions with the service
        verify(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));
    }

    @Test
    public void testPatchReviewFailedNotFound() throws Exception {
        setup_authUser();
        long reviewId = 1L;
        long userId = 1L;

        // Creating the ReviewDTO with both body and rating
        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setBody("Great court!");
        review.setRating(5);

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        // Mocking the service method to throw an exception when review is not found
        doThrow(new EntityNotFoundException("review", reviewId)).when(reviewService).partialUpdate(eq(reviewId), eq(userId), any(ReviewResponseDTO.class));

        // Performing the PATCH request
        mockMvc.perform(patch("/api/review/{reviewId}", reviewId)
                        .contentType("application/json")
                        .content(reviewJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("review with ID 1 not found"));
    }


    @Test
    public void testDeleteReviewSuccess() throws Exception {
        setup_authUser();
        long courtId = 1L;
        long userId = 1L;

        doNothing().when(reviewService).deleteReview(courtId, userId);

        mockMvc.perform(delete("/api/review")
                .param("courtId", String.valueOf(courtId)))
                .andExpect(status().isOk());

        verify(reviewService).deleteReview(courtId, userId);
    }

    @Test
    public void testDeleteReviewAuthenticationFailed() throws Exception {
        long courtId = 1L;

        mockMvc.perform(delete("/api/review")
                .param("courtId", String.valueOf(courtId)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteReviewFailedNotFound() throws Exception {
        setup_authUser();
        long courtId = 1L;

        doThrow(new EntityNotFoundException("review", 1L)).when(reviewService).deleteReview(courtId, 1L);

        mockMvc.perform(delete("/api/review")
                .param("courtId", String.valueOf(courtId)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("review with ID 1 not found"));
    }

    @Test
    public void testDeleteReviewFailedBadRequest() throws Exception {
        setup_authUser();

        mockMvc.perform(delete("/api/review"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteReviewFailedBadRequestService() throws Exception {
        setup_authUser();

        doThrow(new IllegalArgumentException("Invalid request")).when(reviewService).deleteReview(1L, 1L);

        mockMvc.perform(delete("/api/review")
                .param("courtId", String.valueOf(1L)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request"));
    }

    @Test
    public void testRatingRetrieval() throws Exception {
        setup_authUser();
        Long mockCourtId = 1L;
        Map map = Map.of("rating", 5.0, "reviews", 1);

        when(reviewService.getCourtRating(mockCourtId)).thenReturn(map);

        mockMvc.perform(get("/api/review/rating")
                .param("courtId", String.valueOf(mockCourtId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("rating").value(5))
                .andExpect(jsonPath("reviews").value(1));
    }
}
