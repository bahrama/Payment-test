package com.khebre.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    @NotBlank(message = "{payment.merchantId.required}")
    @Size(max = 100, message = "{payment.merchantId.size}")
    private String merchantId;

    @NotBlank(message = "{payment.merchantReference.required}")
    @Size(max = 100, message = "{payment.merchantReference.size}")
    private String merchantReference;

    @NotNull(message = "{payment.amount.required}")
    @DecimalMin(value = "0.01", message = "{payment.amount.min}")
    @Digits(integer = 17, fraction = 2, message = "{payment.amount.digits}")
    private BigDecimal amount;

    @NotBlank(message = "{payment.currency.required}")
    @Pattern(regexp = "[A-Z]{3}", message = "{payment.currency.format}")
    private String currency;
}
