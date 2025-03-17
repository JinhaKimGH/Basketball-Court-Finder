package com.basketballcourtfinder.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class ReviewResponseDTO {
    private Long reviewId;
    private String content;
    private int totalVotes;
    private String authorDisplayName;
    private double authorTrustScore;
    private boolean isUpvoted;
    private boolean isDownvoted;
    private Date createdAt;
    private boolean isEdited;
    private Integer rating;
}
