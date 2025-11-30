package com.example.hrms.exceptions;

public class TokenRefreshException extends RuntimeException{
    private final String token;

    public TokenRefreshException(String token, String message) {
        super(message);
        this.token = token;
    }

    public TokenRefreshException(String token, String message, Throwable cause) {
        super(message, cause);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
