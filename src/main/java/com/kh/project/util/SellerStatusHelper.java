package com.kh.project.util;

import com.kh.project.domain.entity.Seller;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 판매자 상태 조합 처리 유틸리티 (복합 키 기반 이커머스 표준)
 * 
 * 상태 조합:
 * - (status=1, serviceUsage=0): 가입완료/미판매 - 탈퇴 가능
 * - (status=1, serviceUsage=1): 가입완료/판매중 - 탈퇴 불가  
 * - (status=0, serviceUsage=0): 탈퇴완료/미판매 - 탈퇴 완료
 * - (status=0, serviceUsage=1): 탈퇴완료/판매중 - 불가능한 상태
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SellerStatusHelper {
    
    // 상태 상수
    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_WITHDRAWN = 0;
    public static final Integer SERVICE_USAGE_NONE = 0;
    public static final Integer SERVICE_USAGE_ACTIVE = 1;
    
    /**
     * 탈퇴 가능 여부 체크
     */
    public static boolean canWithdraw(Seller seller) {
        if (seller == null) return false;
        
        Integer status = seller.getStatus();
        Integer serviceUsage = seller.getServiceUsage();
        
        // null 체크 및 기본값 설정
        if (status == null) status = STATUS_ACTIVE;
        if (serviceUsage == null) serviceUsage = SERVICE_USAGE_NONE;
        
        // 활성 상태이면서 서비스 이용이 없는 경우에만 탈퇴 가능
        boolean canWithdraw = STATUS_ACTIVE.equals(status) && SERVICE_USAGE_NONE.equals(serviceUsage);
        
        log.debug("탈퇴 가능 여부 체크: sellerId={}, status={}, serviceUsage={}, canWithdraw={}", 
                seller.getSellerId(), status, serviceUsage, canWithdraw);
        
        return canWithdraw;
    }
    
    /**
     * 상태 조합 정보 반환
     */
    public static StatusCombination getStatusCombination(Seller seller) {
        if (seller == null) {
            return new StatusCombination("알 수 없음", "판매자 정보가 없습니다.", false);
        }
        
        Integer status = seller.getStatus();
        Integer serviceUsage = seller.getServiceUsage();
        
        // null 체크 및 기본값 설정
        if (status == null) status = STATUS_ACTIVE;
        if (serviceUsage == null) serviceUsage = SERVICE_USAGE_NONE;
        
        String combination = String.format("(status=%d, serviceUsage=%d)", status, serviceUsage);
        
        if (STATUS_ACTIVE.equals(status) && SERVICE_USAGE_NONE.equals(serviceUsage)) {
            return new StatusCombination(combination, "가입완료/미판매 - 탈퇴 가능", true);
        } else if (STATUS_ACTIVE.equals(status) && SERVICE_USAGE_ACTIVE.equals(serviceUsage)) {
            return new StatusCombination(combination, "가입완료/판매중 - 서비스 이용 완료 후 탈퇴 가능", false);
        } else if (STATUS_WITHDRAWN.equals(status) && SERVICE_USAGE_NONE.equals(serviceUsage)) {
            return new StatusCombination(combination, "탈퇴완료/미판매 - 탈퇴 완료", false);
        } else if (STATUS_WITHDRAWN.equals(status) && SERVICE_USAGE_ACTIVE.equals(serviceUsage)) {
            return new StatusCombination(combination, "탈퇴완료/판매중 - 비정상적인 상태", false);
        } else {
            return new StatusCombination(combination, "알 수 없는 상태 조합", false);
        }
    }
    
    /**
     * 상태 메시지 반환
     */
    public static String getStatusMessage(Seller seller) {
        return getStatusCombination(seller).getWithdrawStatus();
    }
    
    /**
     * 판매자 상태 로깅
     */
    public static void logSellerStatus(Seller seller, String action) {
        if (seller == null) {
            log.warn("판매자 상태 로그 - 판매자 정보가 null입니다. action={}", action);
            return;
        }
        
        StatusCombination combo = getStatusCombination(seller);
        log.info("판매자 상태 로그 - sellerId={}, action={}, 상태조합={}, 설명={}, 탈퇴가능={}", 
                seller.getSellerId(), action, combo.getCombination(), 
                combo.getWithdrawStatus(), combo.isCanWithdraw());
    }
    
    /**
     * 탈퇴 상태 설명 반환 (SellerSVCImpl에서 사용)
     */
    public static String getWithdrawStatusDescription(Seller seller) {
        StatusCombination combo = getStatusCombination(seller);
        return combo.getWithdrawStatus();
    }

    /**
     * 상태 조합 문자열 반환 (SellerSVCImpl에서 사용)
     */
    public static String getStatusCombinationString(Seller seller) {
        StatusCombination combo = getStatusCombination(seller);
        return combo.getCombination();
    }

    /**
     * 상태 정보 로그 출력 (SellerSVCImpl에서 사용)
     */
    public static void logStatusInfo(Seller seller) {
        logSellerStatus(seller, "상태조회");
    }
    
    /**
     * 단일 상태값에 대한 설명 반환
     */
    public static String getStatusDescription(Integer status) {
        if (status == null) return "알 수 없음";
        
        if (STATUS_ACTIVE.equals(status)) {
            return "활성";
        } else if (STATUS_WITHDRAWN.equals(status)) {
            return "탈퇴";
        } else {
            return "알 수 없음 (" + status + ")";
        }
    }
    
    /**
     * 서비스 이용현황에 대한 설명 반환
     */
    public static String getServiceUsageDescription(Integer serviceUsage) {
        if (serviceUsage == null) return "알 수 없음";
        
        if (SERVICE_USAGE_NONE.equals(serviceUsage)) {
            return "미이용";
        } else if (SERVICE_USAGE_ACTIVE.equals(serviceUsage)) {
            return "이용중";
        } else {
            return "알 수 없음 (" + serviceUsage + ")";
        }
    }
    
    /**
     * 상태 조합 내부 클래스
     */
    public static class StatusCombination {
        private final String combination;
        private final String withdrawStatus;
        private final boolean canWithdraw;
        
        public StatusCombination(String combination, String withdrawStatus, boolean canWithdraw) {
            this.combination = combination;
            this.withdrawStatus = withdrawStatus;
            this.canWithdraw = canWithdraw;
        }
        
        public String getCombination() {
            return combination;
        }
        
        public String getWithdrawStatus() {
            return withdrawStatus;
        }
        
        public boolean isCanWithdraw() {
            return canWithdraw;
        }
        
        @Override
        public String toString() {
            return String.format("StatusCombination{combination='%s', withdrawStatus='%s', canWithdraw=%s}", 
                    combination, withdrawStatus, canWithdraw);
        }
    }
} 