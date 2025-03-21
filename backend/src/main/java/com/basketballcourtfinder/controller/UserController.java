package com.basketballcourtfinder.controller;

import com.basketballcourtfinder.dto.LoginDTO;
import com.basketballcourtfinder.dto.UserDTO;
import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.exceptions.EntityAlreadyExistsException;
import com.basketballcourtfinder.exceptions.EntityNotFoundException;
import com.basketballcourtfinder.service.UserService;
import com.basketballcourtfinder.util.AuthUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

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
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> get() {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        try {
            // Finds user from service, only returns non-sensitive parameters
            UserProjection user = service.get(userId);

            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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
            if (result.hasFieldErrors("password")) {
                return ResponseEntity.badRequest().body(result.getFieldError("password").getDefaultMessage());
            }
            if (result.hasFieldErrors("reenterPassword")) {
                return ResponseEntity.badRequest().body(result.getFieldError("reenterPassword").getDefaultMessage());
            }
            if (result.hasFieldErrors("displayName")) {
                return ResponseEntity.badRequest().body(result.getFieldError("displayName").getDefaultMessage());
            }
        }

        if (!user.getPassword().equals(user.getReenterPassword())) {
            return ResponseEntity.badRequest().body("Passwords are not the same.");
        }

        try {
            service.saveUser(user.getEmail(), user.getPassword(), user.getDisplayName());
            return ResponseEntity.status(HttpStatus.CREATED).body("User added successfully.");
        } catch (EntityAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /*
    * Logs the user into the service and produces a token that must be sent to interact with other endpoints.
    * */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException {
        Map<String, String> map = service.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (map != null) {
            String token = map.get("token");

            // Create cookie with JWT token
            Cookie cookie = new Cookie("BCourtFindertoken", token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(86400);
            cookie.setPath("/");
            if (AuthUtil.isLocal(request)) {
                cookie.setAttribute("SameSite", "Lax");
                cookie.setSecure(false);
            } else {
                cookie.setAttribute("SameSite", "None");
                cookie.setSecure(true);
            }

            response.addCookie(cookie);
            map.remove("token");
            return ResponseEntity.ok(map);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials!");
    }

    /*
     * Logs out user, immediately expires the cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = new Cookie("BCourtFindertoken", "deleted");
        cookie.setHttpOnly(true);

        if (AuthUtil.isLocal(request)) {
            cookie.setAttribute("SameSite", "Lax");
            cookie.setSecure(false);
        } else {
            cookie.setAttribute("SameSite", "None");
            cookie.setSecure(true);
        }
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email")
    public ResponseEntity<String> updateUserEmail(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // Updates attribute based on the parameter passed in
        try {
            if ((userDTO.getEmail() != null && !userDTO.getEmail().isEmpty())) {
                service.updateEmail(userId, userDTO.getEmail());
            }

            else {
                return ResponseEntity.badRequest().body("Email field must not be null.");
            }

            return ResponseEntity.ok("User email updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Exception for when the new update doesn't result in any change
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user email.");
        }
    }

    @PutMapping("/displayName")
    public ResponseEntity<String> updateUserDisplayName(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        // Ensures valid email for UserDTO
        if (result.hasErrors()) {
            if (result.hasFieldErrors("displayName")) {
                return ResponseEntity.badRequest().body(result.getFieldError("displayName").getDefaultMessage());
            }
        }

        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // Updates attribute based on the parameter passed in
        try {
            if ((userDTO.getDisplayName() != null && !userDTO.getDisplayName().isEmpty())) {
                service.updateDisplayName(userId, userDTO.getDisplayName());
            }

            else {
                return ResponseEntity.badRequest().body("Display name field must not be null.");
            }

            return ResponseEntity.ok("User display name updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Exception for when the new update doesn't result in any change
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user display name.");
        }
    }

    @PutMapping("/password")
    public ResponseEntity<String> updateUserPassword(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        // Ensures valid email for UserDTO
        if (result.hasErrors()) {
            if (result.hasFieldErrors("password")) {
                return ResponseEntity.badRequest().body(result.getFieldError("password").getDefaultMessage());
            }
        }

        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // Updates attribute based on the parameter passed in
        try {
            if ((userDTO.getPassword() != null && !userDTO.getPassword().isEmpty())) {
                service.updatePassword(userId, userDTO.getPassword());
            }

            else {
                return ResponseEntity.badRequest().body("Password field must not be null.");
            }

            return ResponseEntity.ok("User password updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            // Exception for when the new update doesn't result in any change
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user password.");
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        // Delete user, returns true if successful
        if( service.deleteUser(userId) ) {
            return ResponseEntity.ok("User deleted successfully");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

    @GetMapping("/stats")
    public ResponseEntity<?>  getUserStats() {
        // User ID Found from Token
        Long userId;
        try {
            userId = AuthUtil.getAuthenticatedUserId();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

        try {
            Map<String, String> map = service.findUserStats(userId);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
