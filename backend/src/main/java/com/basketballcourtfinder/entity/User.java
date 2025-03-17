package com.basketballcourtfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String displayName;

    @Column(nullable = false)
    private int upvoteCount;

    @Column(nullable = false)
    private int downvoteCount;

    public User(String email, String salt, String password, String displayName) {
        this.email = email;
        this.salt = salt;
        this.password = password;
        this.displayName = displayName;
        this.upvoteCount = 0;
        this.downvoteCount = 0;
    }

    public User() {
        this.upvoteCount = 0;
        this.downvoteCount = 0;
    }

    public double getTrustScore() {
        // Calculate the trust score dynamically
        if (upvoteCount + downvoteCount < 10) {
            return 0; // Small number of votes == neutral trust
        }
        return (double) ((upvoteCount - downvoteCount) / (upvoteCount + downvoteCount)) * 100;
    }
}
