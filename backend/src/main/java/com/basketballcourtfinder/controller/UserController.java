package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.LoginDTO;
import com.basketballcourtfinder.dto.UserDTO;
import com.basketballcourtfinder.exceptions.UserAlreadyExistsException;
import com.basketballcourtfinder.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@RequestBody UserDTO user) throws NoSuchAlgorithmException {
        try {
            service.saveUser(user.getEmail(), user.getPassword(), user.getDisplayName());
            return ResponseEntity.status(HttpStatus.CREATED).body("User added successfully.");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) throws NoSuchAlgorithmException {
        String token = service.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (token != null) {
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials!");
    }


}
