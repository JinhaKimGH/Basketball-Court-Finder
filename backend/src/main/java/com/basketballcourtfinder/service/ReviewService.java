package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository repository;

    public ReviewService(ReviewRepository repository) {
        this.repository = repository;
    }

    public List<Review> findCourtReviews(Long court_id) {
        return repository.findByCourtId(court_id);
    }

    public Review findUserCourtReview(Long court_id, Long user_id) {
        return repository.findByCourtIdUserId(court_id, user_id).orElse(null);
    }
}
