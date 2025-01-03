package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.User;
import com.basketballcourtfinder.exceptions.UserAlreadyExistsException;
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

    public User findUser(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public String login(String email, String password) throws NoSuchAlgorithmException {
        Optional<User> exists = repository.findByEmail(email);
        if(exists.isPresent()) {
            User user = exists.get();

            String encoded = hashPassword(password, user.getSalt());

            if (user.getPassword().equals(encoded)) {
                return generateToken(user);
            }

        }

        return null;
    }

    public void saveUser(String email, String password, String displayName) throws NoSuchAlgorithmException {
        Optional<User> emailExists = repository.findByEmail(email);
        Optional<User> displayNameExists = repository.findByDisplayName(displayName);

        if (emailExists.isPresent()) {
            throw new UserAlreadyExistsException("Email is already registered");
        } else if (displayNameExists.isPresent()) {
            throw new UserAlreadyExistsException("Display name is already taken");
        }

        String salt = KeyGenerators.string().generateKey();

        String encoded = hashPassword(password, salt);

        User user = new User(email, salt, encoded, displayName);

        repository.save(user);

    }

    private String generateToken(User user) {
        // Secret string to key object
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));;

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((salt.concat(password)).getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
