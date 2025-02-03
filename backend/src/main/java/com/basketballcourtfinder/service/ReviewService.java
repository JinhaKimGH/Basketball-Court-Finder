package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.entity.Review;
import com.basketballcourtfinder.entity.User;
import com.basketballcourtfinder.exceptions.CourtNotFoundException;
import com.basketballcourtfinder.exceptions.UserNotFoundException;
import com.basketballcourtfinder.repository.BasketballCourtRepository;
import com.basketballcourtfinder.repository.ReviewRepository;
import com.basketballcourtfinder.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final BasketballCourtRepository courtRepository;

    private final UserRepository userRepository;

    private final BasketballCourtService courtService;

    public ReviewService(ReviewRepository reviewRepository, BasketballCourtRepository courtRepository, UserRepository userRepository, BasketballCourtService courtService) {
        this.reviewRepository = reviewRepository;
        this.courtRepository = courtRepository;
        this.userRepository = userRepository;
        this.courtService = courtService;
    }

    public List<Review> findCourtReviews(Long courtId) {
        return reviewRepository.findByCourtId(courtId);
    }

    public Review findUserCourtReview(Long courtId, Long userId) {
        return reviewRepository.findByCourtIdAndUserId(courtId, userId).orElse(null);
    }

    public void saveReview(ReviewDTO reviewDTO, Long userId) {

        // Fetch user and court entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        BasketballCourt court = courtService.getCourt(reviewDTO.getCourtId());

        if (court == null) {
            throw new CourtNotFoundException(reviewDTO.getCourtId());
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(reviewDTO.getCourtId(), userId);

        if (found.isPresent()) {
            throw new IllegalArgumentException("Review already exists.");
        }

        // Create a new Review entity
        Review review = new Review();
        review.setUser(user);
        review.setCourt(court);
        review.setBody(reviewDTO.getBody());
        review.setRating(reviewDTO.getRating());
        review.setTitle(reviewDTO.getTitle());
        review.setCreatedAt(new Date());  // Automatically set creation date
        review.setPoints(0);  // Default value for points

        reviewRepository.save(review);
    }

    public void updateReviewTitle(Long courtId, Long userId, String title) {
        // Fetch user and court entities
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }

        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new CourtNotFoundException(courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new IllegalArgumentException("Review does not exist.");
        }

        Review review = found.get();
        review.setTitle(title);
        review.setEdited(true);
        reviewRepository.save(review);
    }

    public void updateReviewBody(Long courtId, Long userId, String body) {
        // Fetch user and court entities
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }

        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new CourtNotFoundException(courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new IllegalArgumentException("Review does not exist.");
        }

        Review review = found.get();
        review.setBody(body);
        review.setEdited(true);
        reviewRepository.save(review);
    }

    public void updateReviewRating(Long courtId, Long userId, Integer rating) {
        if (! (rating > 0 && rating <= 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 inclusive.");
        }

        // Fetch user and court entities
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }

        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new CourtNotFoundException(courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new IllegalArgumentException("Review does not exist.");
        }

        Review review = found.get();
        review.setRating(rating);
        review.setEdited(true);
        reviewRepository.save(review);
    }

    public void deleteReview(Long courtId, Long userId) {
        // Fetch user and court entities
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }
        
        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new CourtNotFoundException(courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new IllegalArgumentException("Review does not exist.");
        }

        Review review = found.get();
        reviewRepository.deleteById(review.getReviewId());
    }
}
