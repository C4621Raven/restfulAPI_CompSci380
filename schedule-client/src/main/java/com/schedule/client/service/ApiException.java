package com.schedule.client.service;

/**
 * Thrown when the Schedule API returns a non-2xx response or is unreachable.
 */
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
    }

    /** HTTP status code, or -1 if the server was unreachable. */
    public int getStatusCode() {
        return statusCode;
    }
}
