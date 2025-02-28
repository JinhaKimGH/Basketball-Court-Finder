package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        List<ReviewResponseDTO> reviews = new ArrayList<>();
        ReviewResponseDTO review = new ReviewResponseDTO();
        review.setReviewId(1L);
        review.setRating(5);
        review.setContent("Great court!");
        review.setTitle("Great court!");
        review.setTotalVotes(10);
        review.setAuthorDisplayName("Test User");
        review.setAuthorTrustScore(100);
        review.setUpvoted(true);
        review.setDownvoted(false);
        review.setCreatedAt(new Date());
        review.setEdited(false);
        reviews.add(review);

        when(reviewService.findCourtReviews(courtId, userId)).thenReturn(reviews);

        mockMvc.perform(get("/api/review")
                .param("courtId", String.valueOf(courtId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reviewId").value(1L))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].content").value("Great court!"));
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
        review.setTitle("Great court!");

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
        review.setTitle("Great court!");

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
        review.setTitle("Great court!");

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
        review.setTitle("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doThrow(new EntityNotFoundException("review", 1L)).when(reviewService).saveReview(review, courtId);

        mockMvc.perform(post("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("review with ID 1 not found"));
    }

    @Test
    public void testPutReviewSuccessAll() throws Exception {
        setup_authUser();
        long courtId = 1L;
        long userId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);
        review.setBody("Great court!");
        review.setTitle("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doNothing().when(reviewService).updateReviewTitle(courtId, userId, "Great court!");
        doNothing().when(reviewService).updateReviewBody(courtId, userId, "Great court!");
        doNothing().when(reviewService).updateReviewRating(courtId, userId, 5);

        mockMvc.perform(put("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testPutReviewSuccessTitle() throws Exception {
        setup_authUser();
        long courtId = 1L;
        long userId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setTitle("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doNothing().when(reviewService).updateReviewTitle(courtId, userId, "Great court!");

        mockMvc.perform(put("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isOk());

        verify(reviewService).updateReviewTitle(courtId, userId, "Great court!");
        verify(reviewService, never()).updateReviewBody(courtId, userId, "Great court!");
        verify(reviewService, never()).updateReviewRating(courtId, userId, 5);
    }

    @Test
    public void testPutReviewSuccessBody() throws Exception {
        setup_authUser();
        long courtId = 1L;
        long userId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doNothing().when(reviewService).updateReviewBody(courtId, userId, "Great court!");

        mockMvc.perform(put("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isOk());

        verify(reviewService).updateReviewBody(courtId, userId, "Great court!");
        verify(reviewService, never()).updateReviewTitle(courtId, userId, "Great court!");
        verify(reviewService, never()).updateReviewRating(courtId, userId, 5);
    }

    @Test
    public void testPutReviewSuccessRating() throws Exception {
        setup_authUser();
        long courtId = 1L;
        long userId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setRating(5);

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doNothing().when(reviewService).updateReviewRating(courtId, userId, 5);

        mockMvc.perform(put("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isOk());

        verify(reviewService).updateReviewRating(courtId, userId, 5);
        verify(reviewService, never()).updateReviewTitle(courtId, userId, "Great court!");
        verify(reviewService, never()).updateReviewBody(courtId, userId, "Great court!");
    }

    @Test
    public void testPutReviewBadRequest() throws Exception {
        setup_authUser();

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        mockMvc.perform(put("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("At least one field must not be null or blank."));

        verify(reviewService, never()).updateReviewRating(anyLong(), anyLong(), anyInt());
        verify(reviewService, never()).updateReviewTitle(anyLong(), anyLong(), anyString());
        verify(reviewService, never()).updateReviewBody(anyLong(), anyLong(), anyString());
    }

    @Test
    public void testPutReviewFailedNotFound() throws Exception {
        setup_authUser();
        long courtId = 1L;
        long userId = 1L;

        ReviewDTO review = new ReviewDTO();
        review.setCourtId(1L);
        review.setBody("Great court!");

        String reviewJson = new ObjectMapper().writeValueAsString(review);

        doThrow(new EntityNotFoundException("review", 1L)).when(reviewService).updateReviewBody(courtId, userId,
                "Great court!");

        mockMvc.perform(put("/api/review")
                .contentType("application/json")
                .content(reviewJson))
                .andExpect(status().isNotFound())
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
        when(reviewService.getCourtRating(mockCourtId)).thenReturn(5.0);

        mockMvc.perform(get("/api/review/rating")
                .param("courtId", String.valueOf(mockCourtId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("rating").value(5));
    }

}
