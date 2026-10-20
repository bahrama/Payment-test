package com.khebre.payment.dto;

import com.khebre.payment.model.PaymentStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PaymentStatusHistoryMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    PaymentStatusHistoryResponse toResponse(PaymentStatusHistory history);

    List<PaymentStatusHistoryResponse> toResponses(List<PaymentStatusHistory> histories);
}
