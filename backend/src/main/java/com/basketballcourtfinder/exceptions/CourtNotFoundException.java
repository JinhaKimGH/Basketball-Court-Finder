package com.basketballcourtfinder.exceptions;

public class CourtNotFoundException extends RuntimeException {
    public CourtNotFoundException(Long userId) {
        super("Court with ID " + userId + " not found");
    }
}
