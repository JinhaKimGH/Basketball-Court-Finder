package com.basketballcourtfinder.repository;

import com.basketballcourtfinder.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourtId(Long courtId);

    Optional<Review> findByCourtIdAndUserId(Long court_id, Long user_id);
}
