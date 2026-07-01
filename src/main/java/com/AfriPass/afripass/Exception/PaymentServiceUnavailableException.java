package com.AfriPass.afripass.Exception;

public class PaymentServiceUnavailableException extends RuntimeException {
    public PaymentServiceUnavailableException(String message) {
        super(message);
    }
}
