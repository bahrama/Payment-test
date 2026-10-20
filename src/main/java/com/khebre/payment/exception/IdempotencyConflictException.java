package com.khebre.payment.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
        super("Idempotency-Key was previously used with different request data");
    }
}
