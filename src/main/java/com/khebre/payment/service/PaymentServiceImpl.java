package com.khebre.payment.service;

import com.khebre.payment.dao.PaymentRepository;
import com.khebre.payment.dao.PaymentStatusHistoryRepository;
import com.khebre.payment.dto.CreatePaymentRequest;
import com.khebre.payment.dto.PaymentMapper;
import com.khebre.payment.dto.PaymentResponse;
import com.khebre.payment.dto.PaymentStatusHistoryMapper;
import com.khebre.payment.dto.PaymentStatusHistoryResponse;
import com.khebre.payment.exception.IdempotencyConflictException;
import com.khebre.payment.exception.InvalidPaymentStateException;
import com.khebre.payment.exception.PaymentNotFoundException;
import com.khebre.payment.model.Payment;
import com.khebre.payment.model.PaymentStatus;
import com.khebre.payment.model.PaymentStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository historyRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentStatusHistoryMapper historyMapper;
    private final SimulatedPaymentGateway gateway;

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request, String idempotencyKey) {
        String key = idempotencyKey.trim();
        String requestHash = hashRequest(request);
        var existing = paymentRepository.findByMerchantIdAndIdempotencyKey(request.getMerchantId(), key);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            if (!payment.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException();
            }
            return paymentMapper.toResponse(payment);
        }

        Payment payment = Payment.create(
                request.getMerchantId(),
                request.getMerchantReference(),
                request.getAmount(),
                request.getCurrency(),
                key,
                requestHash
        );
        payment = paymentRepository.saveAndFlush(payment);
        recordHistory(payment, null, PaymentStatus.PENDING, "Payment created");
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse process(UUID paymentId, String merchantId) {
        Payment payment = findForUpdate(paymentId, merchantId);
        requireStatus(payment, PaymentStatus.PENDING, "process");

        moveTo(payment, PaymentStatus.PROCESSING, "Gateway processing started");
        // This gateway runs in-memory. Do not call a real remote gateway inside
        // a database transaction; a real integration needs a separate workflow.
        SimulatedPaymentGateway.GatewayResult result = gateway.charge(payment);
        payment.setGatewayTransactionId(result.getTransactionId());
        payment.setGatewayResponseCode(result.getResponseCode());
        moveTo(payment, result.isSuccessful() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                result.isSuccessful() ? "Gateway approved payment" : "Gateway declined payment");
        paymentRepository.flush();
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancel(UUID paymentId, String merchantId) {
        Payment payment = findForUpdate(paymentId, merchantId);
        requireStatus(payment, PaymentStatus.PENDING, "cancel");
        moveTo(payment, PaymentStatus.CANCELLED, "Cancelled by merchant");
        paymentRepository.flush();
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getById(UUID paymentId, String merchantId) {
        return paymentMapper.toResponse(findPayment(paymentId, merchantId));
    }

    @Override
    public Page<PaymentResponse> list(String merchantId, PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = (status == null)
                ? paymentRepository.findByMerchantId(merchantId, pageable)
                : paymentRepository.findByMerchantIdAndStatus(merchantId, status, pageable);
        return payments.map(paymentMapper::toResponse);
    }

    @Override
    public List<PaymentStatusHistoryResponse> getHistory(UUID paymentId, String merchantId) {
        // Scope access to merchant before reading history of the payment.
        findPayment(paymentId, merchantId);
        return historyMapper.toResponses(historyRepository.findByPayment_IdOrderByCreatedAtAsc(paymentId));
    }

    private Payment findPayment(UUID id, String merchantId) {
        return paymentRepository.findByIdAndMerchantId(id, merchantId)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private Payment findForUpdate(UUID id, String merchantId) {
        return paymentRepository.findForUpdate(id, merchantId)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }

    private static void requireStatus(Payment payment, PaymentStatus expected, String action) {
        if (payment.getStatus() != expected) {
            throw new InvalidPaymentStateException(action, payment.getStatus());
        }
    }

    private void moveTo(Payment payment, PaymentStatus nextStatus, String reason) {
        PaymentStatus previous = payment.getStatus();
        payment.setStatus(nextStatus);
        recordHistory(payment, previous, nextStatus, reason);
    }

    private void recordHistory(Payment payment, PaymentStatus previous,
                               PaymentStatus next, String reason) {
        historyRepository.save(new PaymentStatusHistory(payment, previous, next, reason));
    }

    private static String hashRequest(CreatePaymentRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String canonical = joinField(request.getMerchantId())
                    + joinField(request.getMerchantReference())
                    + joinField(request.getAmount().setScale(2, RoundingMode.UNNECESSARY).toPlainString())
                    + joinField(request.getCurrency());
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static String joinField(String value) {
        Objects.requireNonNull(value, "hash field");
        return value.length() + ":" + value;
    }
}
