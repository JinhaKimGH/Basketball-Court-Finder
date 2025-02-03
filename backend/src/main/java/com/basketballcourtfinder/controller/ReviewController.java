package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.service.ReviewService;
import com.basketballcourtfinder.util.AuthUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("")
    public ResponseEntity<?> getReviews(@RequestBody long court_id) {
        List<Review> reviewList = reviewService.findCourtReviews(court_id);

        return ResponseEntity.status(HttpStatus.OK).body(reviewList);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> postReview(@RequestBody ReviewDTO review) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        try {
            reviewService.saveReview(review, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Review added successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateReview(@RequestBody ReviewDTO review) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        if (review.getCourtId() == null) {
            return ResponseEntity.badRequest().body("Court ID is required.");
        }

        try {
            if ((review.getTitle() != null && !review.getTitle().isEmpty()) ||
                    (review.getBody() != null && !review.getBody().isEmpty()) ||
                    (review.getRating() != null)) {
                if (review.getTitle() != null && !review.getTitle().isEmpty()) {
                    reviewService.updateReviewTitle(review.getCourtId(), userId, review.getTitle());
                }

                if (review.getBody() != null && !review.getBody().isEmpty()) {
                    reviewService.updateReviewBody(review.getCourtId(), userId, review.getBody());
                }

                if (review.getRating() != null) {
                    reviewService.updateReviewRating(review.getCourtId(), userId, review.getRating());
                }

                return ResponseEntity.ok("Review updated successfully.");
            } else {
                return ResponseEntity.badRequest().body("At least one field must not be null or blank.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteReview(@RequestBody ReviewDTO review) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        if (review.getCourtId() == null) {
            return ResponseEntity.badRequest().body("Court ID is required.");
        }

        try {
            reviewService.deleteReview(review.getCourtId(), userId);

            return ResponseEntity.ok("Review deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(e.getMessage());
        }
    }
}
