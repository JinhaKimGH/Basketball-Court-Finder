package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {
    ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("")
    public ResponseEntity<?> getReviews(@RequestBody long court_id) {
        List<Review> reviewList = reviewService.findCourtReviews(court_id);

        return ResponseEntity.status(HttpStatus.OK).body(reviewList);
    }
}
