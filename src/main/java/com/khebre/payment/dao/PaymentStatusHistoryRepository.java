package com.khebre.payment.dao;

import com.khebre.payment.model.PaymentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory, UUID> {

    List<PaymentStatusHistory> findByPayment_IdOrderByCreatedAtAsc(UUID paymentId);
}
