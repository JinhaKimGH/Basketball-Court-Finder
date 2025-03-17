package com.basketballcourtfinder.exceptions;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityName, Long entityId) {
        super(entityName + " with ID " + entityId + " not found");
    }

    public EntityNotFoundException(String entityName, Long entityId1, Long entityId2) {
        super(entityName + " with IDs " + entityId1 + ", " + entityId2 + " not found");
    }
}

