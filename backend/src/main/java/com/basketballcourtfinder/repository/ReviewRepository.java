package com.basketballcourtfinder.repository;

import com.basketballcourtfinder.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByCourt_Id(Long courtId);

    Optional<Review> findByCourt_idAndUser_id(Long court_id, Long user_id);
}
