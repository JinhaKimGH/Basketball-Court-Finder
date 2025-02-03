package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.LoginDTO;
import com.basketballcourtfinder.dto.UserDTO;
import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.exceptions.UserAlreadyExistsException;
import com.basketballcourtfinder.exceptions.UserNotFoundException;
import com.basketballcourtfinder.service.UserService;
import com.basketballcourtfinder.util.AuthUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    /*
    * Gets the user's own information from the database
    * */
    @GetMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> get() {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        try {
            // Finds user from service, only returns non-sensitive parameters
            UserProjection user = service.get(userId);

            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(e.getMessage());
        }


    }

    /*
    * Signs the user up to the API.
    * */
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> create(@Valid @RequestBody UserDTO user, BindingResult result) throws NoSuchAlgorithmException {
        // Ensures valid email for UserDTO
        if (result.hasErrors()) {
            if (result.hasFieldErrors("email")) {
                return ResponseEntity.badRequest().body(result.getFieldError("email").getDefaultMessage());
            }
        }

        try {
            service.saveUser(user.getEmail(), user.getPassword(), user.getDisplayName());
            return ResponseEntity.status(HttpStatus.CREATED).body("User added successfully.");
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /*
    * Logs the user into the service and produces a token that must be sent to interact with other endpoints.
    * */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) throws NoSuchAlgorithmException {
        String token = service.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (token != null) {
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials!");
    }

    /*
    * Updates the user in the database.
    * */
    @PutMapping()
    public ResponseEntity<String> updateUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        // Ensures valid email for UserDTO
        if (result.hasErrors()) {
            if (result.hasFieldErrors("email")) {
                return ResponseEntity.badRequest().body(result.getFieldError("email").getDefaultMessage());
            }
        }

        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        // Updates attribute based on the parameter passed in
        try {
            if ((userDTO.getEmail() != null && !userDTO.getEmail().isEmpty())  |
                   (userDTO.getDisplayName() != null && !userDTO.getDisplayName().isEmpty()) |
                   (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty())) {
                if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
                    service.updateEmail(userId, userDTO.getEmail());
                }
                if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                    service.updatePassword(userId, userDTO.getPassword());
                }
                if (userDTO.getDisplayName() != null && !userDTO.getDisplayName().isEmpty()) {
                    service.updateDisplayName(userId, userDTO.getDisplayName());
                }
            }

            else {
                return ResponseEntity.badRequest().body("At least one field must not be null.");
            }

            return ResponseEntity.ok("User updated successfully");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Exception for when the new update doesn't result in any change
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body("Error updating user");
        }

    }

    /*
    * Deletes the user from the database.
    * */
    @DeleteMapping()
    public ResponseEntity<String> deleteUser() {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        // Delete user, returns true if successful
        if( service.deleteUser(userId) ) {
            return ResponseEntity.ok("User deleted successfully");
        }

        return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body("User not found");
    }


}
