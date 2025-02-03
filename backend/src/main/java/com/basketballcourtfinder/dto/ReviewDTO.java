package com.basketballcourtfinder.dto;

import lombok.Data;

@Data
public class ReviewDTO {
    private Long courtId;
    private String body;
    private Integer rating;
    private String title;
}
