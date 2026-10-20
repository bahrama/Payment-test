package com.khebre.payment.exception;

import com.khebre.payment.model.PaymentStatus;

public class InvalidPaymentStateException extends RuntimeException {
    public InvalidPaymentStateException(String action, PaymentStatus currentStatus) {
        super("Cannot " + action + " payment in status " + currentStatus);
    }
}
