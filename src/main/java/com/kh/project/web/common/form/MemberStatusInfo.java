package com.kh.project.web.common.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberStatusInfo {
    
    // 공통 필드
    @Builder.Default
    private boolean canWithdraw = true;     // 탈퇴 가능 여부 (1차에서는 기본 true)
    @Builder.Default
    private int activeOrders = 0;           // 진행 중인 주문
    @Builder.Default
    private int shippingOrders = 0;         // 배송 중인 주문
    
    // 구매자 전용 필드
    @Builder.Default
    private Integer points = 0;             // 적립금
    @Builder.Default
    private Integer coupons = 0;            // 쿠폰 수
    
    // 판매자 전용 필드  
    @Builder.Default
    private Integer totalProducts = 0;      // 등록된 상품 수
    @Builder.Default
    private Integer activeProducts = 0;     // 판매 중인 상품 수
    @Builder.Default
    private Integer preparingOrders = 0;    // 배송 준비 중인 주문
    @Builder.Default
    private Integer pendingAmount = 0;      // 정산 대기 금액
    @Builder.Default
    private Integer monthlyRevenue = 0;     // 이번 달 매출
    
    // 1차 프로젝트용 간단 생성 메서드
    public static MemberStatusInfo forBuyer(int points, int coupons) {
        return MemberStatusInfo.builder()
            .canWithdraw(true)
            .activeOrders(0)
            .shippingOrders(0)
            .points(points)
            .coupons(coupons)
            .build();
    }
    
    public static MemberStatusInfo forSeller(int totalProducts, int monthlyRevenue) {
        return MemberStatusInfo.builder()
            .canWithdraw(true)
            .activeOrders(0)
            .shippingOrders(0)
            .preparingOrders(0)
            .pendingAmount(0)
            .totalProducts(totalProducts)
            .activeProducts(totalProducts - 1) // 대부분 활성 상태로 가정
            .monthlyRevenue(monthlyRevenue)
            .build();
    }
    
    // 탈퇴 가능 여부 계산 (향후 확장용)
    public void calculateCanWithdraw() {
        // 1차 프로젝트에서는 단순 로직
        this.canWithdraw = (activeOrders == 0 && shippingOrders == 0 && 
                           (preparingOrders == null || preparingOrders == 0) &&
                           (pendingAmount == null || pendingAmount == 0));
    }
    
    // 기존 코드 호환성을 위한 Map 변환 메서드
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("canWithdraw", canWithdraw);
        map.put("activeOrderCount", activeOrders);
        map.put("shippingOrderCount", shippingOrders);
        map.put("pointBalance", points != null ? points : 0);
        map.put("coupons", coupons != null ? coupons : 0);
        map.put("totalProducts", totalProducts != null ? totalProducts : 0);
        map.put("activeProducts", activeProducts != null ? activeProducts : 0);
        map.put("preparingOrders", preparingOrders != null ? preparingOrders : 0);
        map.put("pendingAmount", pendingAmount != null ? pendingAmount : 0);
        map.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0);
        
        // 탈퇴 불가 사유 (기존 코드 호환성)
        List<String> withdrawBlockReasons = new ArrayList<>();
        if (activeOrders > 0) {
            withdrawBlockReasons.add("진행 중인 주문이 " + activeOrders + "건 있습니다.");
        }
        if (shippingOrders > 0) {
            withdrawBlockReasons.add("배송 중인 주문이 " + shippingOrders + "건 있습니다.");
        }
        if (preparingOrders != null && preparingOrders > 0) {
            withdrawBlockReasons.add("배송 준비 중인 주문이 " + preparingOrders + "건 있습니다.");
        }
        if (pendingAmount != null && pendingAmount > 0) {
            withdrawBlockReasons.add("정산 대기 금액이 " + pendingAmount + "원 있습니다.");
        }
        
        map.put("withdrawBlockReasons", withdrawBlockReasons);
        return map;
    }
} 