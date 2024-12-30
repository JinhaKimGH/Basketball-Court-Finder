package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.repository.BasketballCourtRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BasketballCourtService {
    private final BasketballCourtRepository repository;

    public BasketballCourtService(BasketballCourtRepository repository) {
        this.repository = repository;
    }

    public List<BasketballCourt> getAllCourts() {
        return repository.findAll();
    }

    public BasketballCourt saveCourt(BasketballCourt court) {
        return repository.save(court);
    }
}
