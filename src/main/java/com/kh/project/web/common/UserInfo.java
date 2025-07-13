package com.kh.project.web.common;

import lombok.Builder;
import lombok.Data;

/**
 * 공통 사용자 정보 DTO
 */
@Data
@Builder
public class UserInfo {
    
    private Long id;
    private String email;
    private String name;
    private String nickname;
    private String memberType;
    
    /**
     * 구매자 여부 확인
     */
    public boolean isBuyer() {
        return MemberType.BUYER.getCode().equals(memberType);
    }
    
    /**
     * 판매자 여부 확인
     */
    public boolean isSeller() {
        return MemberType.SELLER.getCode().equals(memberType);
    }
} 