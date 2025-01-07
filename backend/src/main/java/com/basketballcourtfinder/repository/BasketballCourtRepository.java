package com.basketballcourtfinder.repository;

import com.basketballcourtfinder.entity.BasketballCourt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasketballCourtRepository extends JpaRepository<BasketballCourt, Long> {
    Optional<BasketballCourt> findById(long id);

    List<BasketballCourt> findByIdIn(List<Long> ids);
}
