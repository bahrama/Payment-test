package com.khebre.payment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(
        name = "payment_status_history",
        indexes = {
                @Index(name = "idx_payment_history_payment_created_at", columnList = "payment_id, created_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/*برای ثبت تاریخچه تغییر وضعیت هر پرداخت است*/
public class PaymentStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20, updatable = false)
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20, updatable = false)
    private PaymentStatus newStatus;

    @Column(name = "reason", length = 255, updatable = false)
    private String reason;

    public PaymentStatusHistory(Payment payment, PaymentStatus previousStatus,
                                PaymentStatus newStatus, String reason) {
        this.payment = java.util.Objects.requireNonNull(payment, "payment");
        this.previousStatus = previousStatus;
        this.newStatus = java.util.Objects.requireNonNull(newStatus, "newStatus");
        this.reason = reason;
    }

}
