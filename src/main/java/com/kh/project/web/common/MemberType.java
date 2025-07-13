package com.kh.project.web.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 타입 열거형
 */
@Getter
@RequiredArgsConstructor
public enum MemberType {
    
    BUYER("BUYER", "구매자", "개별 소비자로서 상품을 구매하는 회원"),
    SELLER("SELLER", "판매자", "농산물을 판매하는 사업자 회원");
    
    private final String code;
    private final String description;
    private final String detail;
    
    /**
     * 코드로 회원 타입 찾기
     */
    public static MemberType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (MemberType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 유효한 회원 타입 코드인지 확인
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
} 