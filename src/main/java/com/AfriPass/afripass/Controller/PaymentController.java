package com.AfriPass.afripass.Controller;

import com.AfriPass.afripass.DTOs.PaymentRequest;
import com.AfriPass.afripass.DTOs.TicketResponse;
import com.AfriPass.afripass.Services.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<List<TicketResponse>> processPayment(@Valid @RequestBody PaymentRequest request) throws StripeException {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
}
