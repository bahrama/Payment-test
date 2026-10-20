package com.khebre.payment.dto;

import com.khebre.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusHistoryResponse {
    private UUID id;
    private UUID paymentId;
    private PaymentStatus previousStatus;
    private PaymentStatus newStatus;
    private String reason;
    private Instant createdAt;
}
