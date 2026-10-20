package com.khebre.payment.service;

import com.khebre.payment.model.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SimulatedPaymentGateway {

    public GatewayResult charge(Payment payment) {
        boolean successful = !payment.getMerchantReference().startsWith("FAIL-");
        return new GatewayResult(
                "SIM-" + UUID.randomUUID(),
                successful ? "00" : "05",
                successful
        );
    }

    public static final class GatewayResult {
        private final String transactionId;
        private final String responseCode;
        private final boolean successful;

        public GatewayResult(String transactionId, String responseCode, boolean successful) {
            this.transactionId = transactionId;
            this.responseCode = responseCode;
            this.successful = successful;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public String getResponseCode() {
            return responseCode;
        }

        public boolean isSuccessful() {
            return successful;
        }
    }
}
