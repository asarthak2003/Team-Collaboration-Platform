package com.sarthak.teamcollab.exception;

// we use this whenever the user does something invalid (like trying to assign an inactive user)

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
