package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.service.ReviewService;
import com.basketballcourtfinder.util.AuthUtil;
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

    @GetMapping("/rating")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getCourtRating(@RequestParam Long courtId) {
        return ResponseEntity.ok(reviewService.getCourtRating(courtId));
    }

    @GetMapping("/single")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getReview(@RequestParam Long courtId) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        return ResponseEntity.ok(reviewService.findCourtReview(courtId, userId));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getReviews(@RequestParam Long courtId) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        List<ReviewResponseDTO> reviews = reviewService.findCourtReviews(courtId, userId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> postReview(@RequestBody ReviewDTO review) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        try {
            reviewService.saveReview(review, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Review added successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        if (review.getCourtId() == null) {
            return ResponseEntity.badRequest().body("Court ID is required.");
        }

        try {
            if ((review.getBody() != null && !review.getBody().isEmpty()) ||
                    (review.getRating() != null)) {

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> deleteReview(@RequestParam Long courtId) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        if (courtId == null) {
            return ResponseEntity.badRequest().body("Court ID is required.");
        }

        try {
            reviewService.deleteReview(courtId, userId);

            return ResponseEntity.ok("Review deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
