package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.CourtsDTO;
import com.basketballcourtfinder.entity.BasketballCourt;
import com.basketballcourtfinder.service.BasketballCourtService;
import com.basketballcourtfinder.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<BasketballCourt> getCourt(@RequestParam long court_id) {
        BasketballCourt court = courtService.getCourt(court_id);

        return ResponseEntity.ok().body(court);
    }

    @GetMapping("/around")
    public ResponseEntity<?> getCourts(@ModelAttribute CourtsDTO courtsDTO) {
        Set<BasketballCourt> courts = courtService.getCourtsInArea(courtsDTO.getLatitude(),
                courtsDTO.getLongitude(), courtsDTO.getRange());

        return ResponseEntity.ok(courts);
    }
}
