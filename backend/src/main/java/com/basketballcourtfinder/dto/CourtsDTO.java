package com.basketballcourtfinder.dto;

import lombok.Data;

@Data
public class CourtsDTO {
    double latitude;
    double longitude;
    int range;

    public CourtsDTO(double latitude, double longitude, int range) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.range = range;
    }
}
