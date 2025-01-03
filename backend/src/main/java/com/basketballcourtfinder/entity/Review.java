package com.basketballcourtfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long review_id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name="court_id")
    private BasketballCourt court;

    private String body;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private int rating;

    @Column(nullable = false)
    private Date createdAt;

    private String title;

    @Column(nullable=false)
    @Min(0)
    private int points;
}
