package com.khebre.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payments_merchant_idempotency_key",
                        columnNames = {"merchant_id", "idempotency_key"}
                )
        },
        indexes = {
                @Index(name = "idx_payments_status_created_at", columnList = "status, created_at"),
                @Index(name = "idx_payments_merchant_reference", columnList = "merchant_id, merchant_reference")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // create protected constructor
public class Payment extends BaseEntity {

    @Setter
    @Column(name = "merchant_id", nullable = false, length = 100)
    private String merchantId;

    //شناسه مرجع پذیرنده که پذیرنده تولید میکنه
    @Setter
    @Column(name = "merchant_reference", nullable = false, length = 100)
    private String merchantReference;

    @Setter
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Setter
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /*فرض کن کاربر روی دکمه پرداخت کلیک می‌کند، اما پاسخ سرور به دلیل اختلال شبکه به او نمی‌رسد. برنامه دوباره همان درخواست را ارسال می‌کند.
    اگر PayFlow درخواست دوم را یک پرداخت جدید در نظر بگیرد، ممکن است دو پرداخت برای یک عملیات ایجاد شود.
    برای جلوگیری از این مشکل، کلاینت یک کلید تولید می‌کند:*/
    @Setter
    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Setter
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    /*این شناسه را معمولاً درگاه پرداخت تولید می‌کند*/
    @Setter
    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    /*این فیلد کدی را نگه می‌دارد که درگاه برای توضیح نتیجه پردازش برمی‌گرداند.*/
    @Setter
    @Column(name = "gateway_response_code", length = 50)
    private String gatewayResponseCode;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /** Create a new payment; technical fields are set by the server only. */
    public static Payment create(String merchantId, String merchantReference,
                                 BigDecimal amount, String currency,
                                 String idempotencyKey, String requestHash) {
        Payment payment = new Payment();
        payment.merchantId = java.util.Objects.requireNonNull(merchantId);
        payment.merchantReference = java.util.Objects.requireNonNull(merchantReference);
        payment.amount = java.util.Objects.requireNonNull(amount);
        payment.currency = java.util.Objects.requireNonNull(currency);
        payment.idempotencyKey = java.util.Objects.requireNonNull(idempotencyKey);
        payment.requestHash = java.util.Objects.requireNonNull(requestHash);
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

}
