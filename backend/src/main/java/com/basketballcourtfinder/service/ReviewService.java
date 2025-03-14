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

    private ReviewResponseDTO mapToReviewDTO(Review review, Map<Long, VoteType> userVoteMap) {
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
    }

    public Map<String, Object> findCourtReviews(Long courtId,
                                                Long userId,
                                                Integer page,
                                                Integer reviewPerPage,
                                                SortMethod sortMethod
    ) {
        List<Review> reviews = reviewRepository.findByCourtId(courtId);

        if (reviews.isEmpty()) {
            return Map.of("userReview", Optional.empty(), "otherReviews", Collections.emptyList());
        }

        // Sort reviews based on the selected sort method
        switch (sortMethod) {
            case NEWEST:
                reviews.sort(Comparator.comparing(Review::getCreatedAt).reversed()); // Sort by newest date
                break;
            case HIGHEST:
                reviews.sort(Comparator.comparing(Review::getRating).reversed()); // Sort by highest rating
                break;
            case LOWEST:
                reviews.sort(Comparator.comparing(Review::getRating)); // Sort by lowest rating
                break;
        }

        // Fetch user's votes for these reviews
        List<Vote> userVotes = voteRepository.findByUserIdAndReview_ReviewIdIn(userId,
                reviews.stream().map(Review::getReviewId).collect(Collectors.toList()));
        Map<Long, VoteType> userVoteMap = userVotes.stream()
                .collect(Collectors.toMap(v -> v.getReview().getReviewId(), Vote::getType));

        // Identify user's own review
        Review userReview = reviews.stream()
                .filter(review -> review.getUser().getId() == userId)
                .findFirst()
                .orElse(null);

        // Map user review if it exists
        ReviewResponseDTO userReviewDTO = (userReview != null) ? mapToReviewDTO(userReview, userVoteMap) : null;

        // Map other reviews
        List<ReviewResponseDTO> otherReviews = reviews.stream()
                .filter(review -> userReview == null || !(review.getReviewId() == userReview.getReviewId()))
                .map(review -> mapToReviewDTO(review, userVoteMap))
                .toList();

        // Paginate reviews
        int start = (page - 1) * reviewPerPage;
        int end = Math.min(start + reviewPerPage, otherReviews.size());
        otherReviews = otherReviews.subList(start, end);

        Map<String, Object> map = new HashMap<>(Map.of("otherReviews", otherReviews));

        if (userReviewDTO != null) {
            map.put("userReview", userReviewDTO);
        }

        return map;
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

    public Review partialUpdate(Long reviewId, Long userId, ReviewResponseDTO updates) throws Exception {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("review", reviewId));

        if (userId == existingReview.getUser().getId()) {
            if (updates.getContent() != null) {
                existingReview.setBody(updates.getContent());
            }

            if (updates.getRating() != null) {
                if (updates.getRating() < 0 || updates.getRating() > 5) {
                    throw new IllegalArgumentException("Rating must be between 0 to 5 inclusive.");
                } else {
                    existingReview.setRating(updates.getRating());
                }
            }

            return reviewRepository.save(existingReview);
        } else {
            throw new Exception("You are not the author of this review.");
        }
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
