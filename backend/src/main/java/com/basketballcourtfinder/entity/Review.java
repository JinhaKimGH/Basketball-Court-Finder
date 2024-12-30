package com.basketballcourtfinder.entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long review_id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @ManyToMany
    @JoinColumn(name="court_id")
    private BasketballCourt basketballCourt;

    private String body;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private Date createdAt;

    private String title;

    public Review(long review_id, User user, BasketballCourt basketballCourt,
                  String body, int rating, Date createdAt, String title) {
        this.review_id = review_id;
        this.user = user;
        this.basketballCourt = basketballCourt;
        this.body = body;
        this.rating = rating;
        this.createdAt = createdAt;
        this.title = title;
    }

    public long getReview_id() {
        return review_id;
    }

    public void setReview_id(long review_id) {
        this.review_id = review_id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BasketballCourt getBasketballCourt() {
        return basketballCourt;
    }

    public void setBasketballCourt(BasketballCourt basketballCourt) {
        this.basketballCourt = basketballCourt;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
