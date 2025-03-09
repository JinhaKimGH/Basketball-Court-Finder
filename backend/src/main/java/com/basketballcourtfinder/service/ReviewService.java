package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.ReviewDTO;
import com.basketballcourtfinder.dto.ReviewResponseDTO;
import com.basketballcourtfinder.entity.*;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.repository.ReviewRepository;
import com.basketballcourtfinder.repository.UserRepository;
import com.basketballcourtfinder.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    private final VoteRepository voteRepository;

    private final UserRepository userRepository;

    private final BasketballCourtService courtService;

    public ReviewService(ReviewRepository reviewRepository, VoteRepository voteRepository, UserRepository userRepository, BasketballCourtService courtService) {
        this.reviewRepository = reviewRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.courtService = courtService;
    }

    public Map<String, ?> getCourtRating(Long courtId) {
        List<Review> reviews = reviewRepository.findByCourtId(courtId);

        double rating = reviews.stream()
                .mapToInt(Review::getRating)
                .average().orElse(0.0);

        return Map.of("rating", rating, "reviews", reviews.size());
    }

    public ReviewResponseDTO findCourtReview(Long courtId, Long userId) {
        Review review = reviewRepository.findByCourtIdAndUserId(courtId, userId).orElse(null);
        if (review == null) {
            return null;
        }

        User author = review.getUser();

        ReviewResponseDTO response = new ReviewResponseDTO();
        response.setReviewId(review.getReviewId());
        response.setContent(review.getBody());
        response.setEdited(review.isEdited());
        response.setRating(review.getRating());
        response.setTotalVotes(review.getVoteCount());
        response.setAuthorDisplayName(author.getDisplayName());
        response.setAuthorTrustScore(author.getTrustScore());

        return response;
    }

    public List<ReviewResponseDTO> findCourtReviews(Long courtId, Long userId) {
        List<Review> reviews = reviewRepository.findByCourtId(courtId);

        if (reviews.isEmpty()) {
            return Collections.emptyList(); // Return early if no reviews
        }

        // Fetch all votes by the user for these reviews in one query
        List<Vote> userVotes = voteRepository.findByUserIdAndReview_ReviewIdIn(userId,
                reviews.stream().map(Review::getReviewId).collect(Collectors.toList()));
        Map<Long, VoteType> userVoteMap = userVotes.stream()
                .collect(Collectors.toMap(v -> v.getReview().getReviewId(), Vote::getType));

        // Map reviews to ReviewResponse with additional details
        return reviews.stream().map(review -> {
            User author = review.getUser();

            ReviewResponseDTO response = new ReviewResponseDTO();
            response.setReviewId(review.getReviewId());
            response.setContent(review.getBody());
            response.setEdited(review.isEdited());
            response.setRating(review.getRating());
            response.setTotalVotes(review.getVoteCount());
            response.setAuthorDisplayName(author.getDisplayName());
            response.setAuthorTrustScore(author.getTrustScore());
            response.setUpvoted(userVoteMap.getOrDefault(review.getReviewId(), null) == VoteType.UPVOTE);
            response.setDownvoted(userVoteMap.getOrDefault(review.getReviewId(), null) == VoteType.DOWNVOTE);
            response.setCreatedAt(review.getCreatedAt());

            return response;
        }).collect(Collectors.toList());
    }

    public void saveReview(ReviewDTO reviewDTO, Long userId) {

        // Fetch user and court entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("user", userId));
        BasketballCourt court = courtService.getCourt(reviewDTO.getCourtId());

        if (court == null) {
            throw new EntityNotFoundException("court", reviewDTO.getCourtId());
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(reviewDTO.getCourtId(), userId);

        if (found.isPresent()) {
            throw new EntityAlreadyExistsException("You already have an existing review.");
        }

        // Create a new Review entity
        Review review = new Review();
        review.setUser(user);
        review.setCourt(court);
        review.setBody(reviewDTO.getBody());
        review.setRating(reviewDTO.getRating());
        review.setCreatedAt(new Date());  // Automatically set creation date
        review.setPoints(0);  // Default value for points

        reviewRepository.save(review);
    }

    public void updateReviewBody(Long courtId, Long userId, String body) {
        // Fetch user and court entities
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new EntityNotFoundException("user", userId);
        }

        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new EntityNotFoundException("court", courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new EntityNotFoundException("review", courtId, userId);
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
            throw new EntityNotFoundException("user", userId);
        }

        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new EntityNotFoundException("court", courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new EntityNotFoundException("review", courtId, userId);
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
            throw new EntityNotFoundException("user", userId);
        }
        
        BasketballCourt court = courtService.getCourt(courtId);

        if (court == null) {
            throw new EntityNotFoundException("court", courtId);
        }

        // Check if review already exists
        Optional<Review> found = reviewRepository.findByCourtIdAndUserId(courtId, userId);

        if (found.isEmpty()) {
            throw new EntityNotFoundException("review", courtId, userId);
        }

        Review review = found.get();
        reviewRepository.deleteById(review.getReviewId());
    }
}
