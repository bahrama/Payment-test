package com.khebre.payment.dao;

import com.khebre.payment.model.Payment;
import com.khebre.payment.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByMerchantIdAndIdempotencyKey(String merchantId, String idempotencyKey);

    Optional<Payment> findByIdAndMerchantId(UUID id, String merchantId);

    // Prevent two concurrent process/cancel operations on the same payment.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id and p.merchantId = :merchantId")
    Optional<Payment> findForUpdate(@Param("id") UUID id, @Param("merchantId") String merchantId);

    Page<Payment> findByMerchantId(String merchantId, Pageable pageable);

    Page<Payment> findByMerchantIdAndStatus(String merchantId, PaymentStatus status, Pageable pageable);
}
