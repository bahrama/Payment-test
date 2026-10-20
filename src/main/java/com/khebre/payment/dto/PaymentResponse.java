package com.khebre.payment.dto;

import com.khebre.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private UUID id;
    private String merchantId;
    private String merchantReference;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String gatewayTransactionId;
    private String gatewayResponseCode;
    private Instant createdAt;
    private Instant updatedAt;
}
