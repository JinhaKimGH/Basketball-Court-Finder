package com.basketballcourtfinder.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reviewId;

    @ManyToOne
    @JoinColumn(name="user_id")
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name="court_id")
    @ToString.Exclude
    private BasketballCourt court;

    private String body;

    @Min(1) @Max(5)
    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private Date createdAt;

    private String title;

    @Min(0)
    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private boolean isEdited = false;

    private int voteCount = 0;

    // Increment vote count
    public void incrementVoteCount() {
        this.voteCount++;
    }

    public void incrementVoteCount(int count) {
        this.voteCount = this.voteCount + count;
    }

    // Decrement vote count
    public void decrementVoteCount() {
        this.voteCount--;
    }

    public void decrementVoteCount(int count) {
        this.voteCount = this.voteCount - count;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }

}
