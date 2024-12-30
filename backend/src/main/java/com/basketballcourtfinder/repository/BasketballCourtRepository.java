package com.basketballcourtfinder.repository;

import com.basketballcourtfinder.entity.BasketballCourt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketballCourtRepository extends JpaRepository<BasketballCourt, Long> {

}
