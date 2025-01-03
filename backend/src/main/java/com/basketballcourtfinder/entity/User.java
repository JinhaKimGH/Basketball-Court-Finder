package com.basketballcourtfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(-100)
    @Max(100)
    private int trust = 0;

    public User(String email, String salt, String password, String displayName) {
        this.email = email;
        this.salt = salt;
        this.password = password;
        this.displayName = displayName;
    }

    public User() {
        this.trust = 0;
    }
}
