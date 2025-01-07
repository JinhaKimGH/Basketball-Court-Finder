package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.CourtsDTO;
import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.service.BasketballCourtService;
import com.basketballcourtfinder.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/courts")
public class BasketballCourtController {
    BasketballCourtService courtService;
    UserService userService;

    public BasketballCourtController(BasketballCourtService courtService, UserService userService) {
        this.courtService = courtService;
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<BasketballCourt> getCourt(@RequestBody long court_id) {
        BasketballCourt court = courtService.getCourt(court_id);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(court);
    }

    @GetMapping("/around")
    public ResponseEntity<?> getCourts(@RequestBody CourtsDTO courtsDTO) {
        Set<BasketballCourt> courts = courtService.getCourtsInArea(courtsDTO.getLatitude(),
                courtsDTO.getLongitude(), courtsDTO.getRange());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(courts);
    }
}
