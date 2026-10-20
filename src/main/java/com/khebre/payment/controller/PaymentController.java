package com.khebre.payment.controller;

import com.khebre.payment.dto.CreatePaymentRequest;
import com.khebre.payment.dto.PaymentResponse;
import com.khebre.payment.dto.PaymentStatusHistoryResponse;
import com.khebre.payment.model.PaymentStatus;
import com.khebre.payment.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
public class PaymentController {

    private final PaymentService paymentService;

    //ایجاد یک پرداخت جدید
    //ولیدیشن ها برای مقدار های داخل هدر هست
    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request,
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "{payment.idempotencyKey.required}")
            @Size(max = 100, message = "{payment.idempotencyKey.size}")
            String idempotencyKey) {

        PaymentResponse payment = paymentService.create(request, idempotencyKey);
        // header --> Location: /api/v1/payments/2be43bd4-0a0d-4c40-b83d-457ce8140876
        return ResponseEntity.created(URI.create("/api/v1/payments/" + payment.getId()))
                .body(payment);
    }

    //پردازش پرداخت توسط درگاه
    @PostMapping("/{paymentId}/process")
    public ResponseEntity<PaymentResponse> process(
            @PathVariable UUID paymentId,
            @RequestParam("merchantId") @NotBlank(message = "{payment.merchantId.required}")
            @Size(max = 100, message = "{payment.merchantId.size}") String merchantId) {

        return ResponseEntity.ok(paymentService.process(paymentId, merchantId));
    }

    //لغو پرداخت
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancel(
            @PathVariable UUID paymentId,
            @RequestParam("merchantId") @NotBlank(message = "{payment.merchantId.required}")
            @Size(max = 100, message = "{payment.merchantId.size}") String merchantId) {

        return ResponseEntity.ok(paymentService.cancel(paymentId, merchantId));
    }


    //دریافت اطلاعات یک پرداخت
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getById(
            @PathVariable UUID paymentId,
            @RequestParam("merchantId") @NotBlank(message = "{payment.merchantId.required}")
            @Size(max = 100, message = "{payment.merchantId.size}") String merchantId) {

        return ResponseEntity.ok(paymentService.getById(paymentId, merchantId));
    }


    //دریافت فهرست پرداخت‌ها
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> list(
            @RequestParam("merchantId") @NotBlank(message = "{payment.merchantId.required}")
            @Size(max = 100, message = "{payment.merchantId.size}") String merchantId,
            @RequestParam(required = false) PaymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(paymentService.list(merchantId, status, pageable));
    }


    //مشاهده تاریخچه وضعیت پرداخت
    @GetMapping("/{paymentId}/history")
    public ResponseEntity<List<PaymentStatusHistoryResponse>> getHistory(
            @PathVariable UUID paymentId,
            @RequestParam("merchantId") @NotBlank(message = "{payment.merchantId.required}")
            @Size(max = 100, message = "{payment.merchantId.size}") String merchantId) {

        return ResponseEntity.ok(paymentService.getHistory(paymentId, merchantId));
    }
}
