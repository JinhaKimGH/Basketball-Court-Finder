package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.enums.SortMethod;
import com.basketballcourtfinder.service.ReviewService;
import com.basketballcourtfinder.util.AuthUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getReviews(
            @RequestParam Long courtId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer reviewsPerPage,
            @RequestParam(defaultValue = "NEWEST") SortMethod sortMethod) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            userId = null;
        }

        return ResponseEntity.ok(reviewService.findCourtReviews(courtId, userId, page, reviewsPerPage, sortMethod));
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

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateReview(@PathVariable long id, @RequestBody ReviewResponseDTO updates) {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        try {
            Review updatedReview = reviewService.partialUpdate(id, userId, updates);
            return ResponseEntity.status(HttpStatus.OK).body(updatedReview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
