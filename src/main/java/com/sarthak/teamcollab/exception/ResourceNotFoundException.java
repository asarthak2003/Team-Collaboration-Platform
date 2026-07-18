package com.sarthak.teamcollab.exception;

// we use this whenever a database entity isn't found
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
