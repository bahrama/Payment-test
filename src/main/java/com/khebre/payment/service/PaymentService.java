package com.khebre.payment.service;

import com.khebre.payment.dto.CreatePaymentRequest;
import com.khebre.payment.dto.PaymentResponse;
import com.khebre.payment.dto.PaymentStatusHistoryResponse;
import com.khebre.payment.model.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse create(@NotNull @Valid CreatePaymentRequest request,
                           @NotBlank(message = "{payment.idempotencyKey.required}")
                           @Size(max = 100, message = "{payment.idempotencyKey.size}")
                           String idempotencyKey);

    PaymentResponse process(@NotNull UUID paymentId, @NotBlank String merchantId);

    PaymentResponse cancel(@NotNull UUID paymentId, @NotBlank String merchantId);

    PaymentResponse getById(@NotNull UUID paymentId, @NotBlank String merchantId);

    Page<PaymentResponse> list(@NotBlank String merchantId, PaymentStatus status,
                               @NotNull Pageable pageable);

    List<PaymentStatusHistoryResponse> getHistory(@NotNull UUID paymentId,
                                                  @NotBlank String merchantId);
}
