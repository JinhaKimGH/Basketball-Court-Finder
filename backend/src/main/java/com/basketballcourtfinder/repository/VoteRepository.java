package com.basketballcourtfinder.repository;

import com.basketballcourtfinder.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Fetch all votes by a specific user for a list of review IDs
    List<Vote> findByUserIdAndReview_ReviewIdIn(Long userId, List<Long> reviewIds);

    Optional<Vote> findByUserIdAndReview_ReviewId(Long userId, Long reviewId);
}
