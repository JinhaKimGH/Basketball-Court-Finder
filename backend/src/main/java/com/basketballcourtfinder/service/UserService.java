package com.basketballcourtfinder.service;

import com.basketballcourtfinder.entity.User;
import com.basketballcourtfinder.repository.UserRepository;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User findUser(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public boolean verifyUser(String email, String password) throws NoSuchAlgorithmException {
        Optional<User> exists = repository.findByEmail(email);
        if(exists.isPresent()) {
            User user = exists.get();
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((user.getSalt().concat(password)).getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getEncoder().encodeToString(hash);

            return Objects.equals(encoded, user.getPassword());

        } else {
            return false;
        }
    }

    public User saveUser(String email, String password, String displayName) throws NoSuchAlgorithmException {
        String salt = KeyGenerators.string().generateKey();

        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((salt.concat(password)).getBytes(StandardCharsets.UTF_8));
        String encoded = Base64.getEncoder().encodeToString(hash);

        User user = new User(email, salt, encoded, displayName);

        return repository.save(user);
    }
}
