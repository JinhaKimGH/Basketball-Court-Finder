package com.basketballcourtfinder.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityName, Long entityId) {
        super(entityName + " with ID " + entityId + " not found");
    }
}

