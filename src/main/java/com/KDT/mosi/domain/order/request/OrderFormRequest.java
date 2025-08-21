package com.KDT.mosi.domain.order.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderFormRequest {
    
    @NotEmpty(message = "주문할 상품을 선택해주세요")
    private List<Long> cartItemIds;
    
    @Size(max = 50, message = "요청사항은 50자 이내로 입력해주세요")
    private String specialRequest;

    @NotNull(message = "결제 방법을 선택해주세요")
    private String paymentMethod;

    @NotNull(message = "결제 금액은 필수입니다")
    @Positive(message = "결제 금액은 0보다 커야 합니다")
    private Long amount;
}
