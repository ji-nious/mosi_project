package com.KDT.mosi.domain.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    
    @NotEmpty(message = "주문할 상품을 선택해주세요")
    private List<Long> cartItemIds;
    
    @Size(max = 50, message = "요청사항은 50자 이내로 입력해주세요")
    private String specialRequest;
    
    private String paymentMethod;
}
