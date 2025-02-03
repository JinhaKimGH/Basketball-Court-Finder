package com.basketballcourtfinder.service;

import com.basketballcourtfinder.dto.UserProjection;
import com.basketballcourtfinder.entity.User;
import com.basketballcourtfinder.exceptions.UserAlreadyExistsException;
import com.basketballcourtfinder.exceptions.UserNotFoundException;
import com.basketballcourtfinder.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /*
    * Retrieves the users information
    * */
    public UserProjection get(Long userId) {
        UserProjection user = repository.findProjectedById(userId).orElse(null);

        if (user == null) {
            throw new UserNotFoundException(userId);
        }

        return user;
    }

    /*
    * Logs the user in and returns generated token
    * */
    public String login(String email, String password) throws NoSuchAlgorithmException {
        // Checks if user exists first
        Optional<User> exists = repository.findByEmail(email);
        if(exists.isPresent()) {
            User user = exists.get();

            // Encodes the input with a hash + salt
            String encoded = hashPassword(password, user.getSalt());

            // Compares the hashed input with the hashed password and generates token if matched
            if (user.getPassword().equals(encoded)) {
                return generateToken(user);
            }

        }

        return null;
    }

    /*
    * Saves the user to the database
    * */
    public void saveUser(String email, String password, String displayName) throws NoSuchAlgorithmException {
        // Checks if email and display name exists, both should be unique values
        Optional<User> emailExists = repository.findByEmail(email);
        Optional<User> displayNameExists = repository.findByDisplayName(displayName);

        if (emailExists.isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered");
        } else if (displayNameExists.isPresent()) {
            throw new UserAlreadyExistsException("Display name is already taken");
        }

        // Generates a random salt
        String salt = KeyGenerators.string().generateKey();

        // Encodes the password by adding a salt plus hashing
        String encoded = hashPassword(password, salt);

        // Creates the new user and saves it to the database
        User user = new User(email, salt, encoded, displayName);

        repository.save(user);

    }

    /*
    * Deletes user by ID and returns true if user exists in the database.
    * */
    public boolean deleteUser(long id) {
        Optional<User> user = repository.findById(id);

        if (user.isPresent()) {
            repository.deleteById(id);
            return true;
        }

        return false;
    }

    /*
     * Updates the email of the user
     * */
    public void updateEmail(Long userId, String email) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (Objects.equals(email, user.getEmail())) {
            throw new IllegalArgumentException("The new email address must be different from the current email address.");
        }
        user.setEmail(email);
        repository.save(user);
    }

    /*
     * Updates the user's password
     * */
    public void updatePassword(Long userId, String password) throws NoSuchAlgorithmException {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (Objects.equals(hashPassword(password, user.getSalt()), user.getPassword())) {
            throw new IllegalArgumentException("The new password must be different from the current password.");
        }

        // Generates a random salt
        String salt = KeyGenerators.string().generateKey();

        // Encodes the password by adding a salt plus hashing
        String encoded = hashPassword(password, salt);

        // Sets the new password and salt
        user.setPassword(encoded);
        user.setSalt(salt);

        repository.save(user);
    }

    /*
     * Updates the user's display name.
     * */
    public void updateDisplayName(Long userId, String displayName) {
        User user = repository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (Objects.equals(displayName, user.getDisplayName())) {
            throw new IllegalArgumentException("The new display name must be different from the current display name.");
        }

        user.setDisplayName(displayName);
        repository.save(user);
    }

    /*
    * Generates a token for a logged-in user.
    * */
    private String generateToken(User user) {
        // Secret string to key object
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));;

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userID", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /*
    * Hashes/encodes a password
    * */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((salt.concat(password)).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
