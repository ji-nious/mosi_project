package com.kh.project.domain.entity;

import lombok.Getter;

/**
 * 회원 상태 표준화 enum
 */
@Getter
public enum MemberStatus {
    WITHDRAWN(0, "탈퇴"),
    ACTIVE(1, "활성화"), 
    INACTIVE(2, "비활성화"),
    SUSPENDED(3, "정지");
    
    private final int code;
    private final String description;
    
    MemberStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 코드로 상태 찾기
     */
    public static MemberStatus fromCode(int code) {
        for (MemberStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 상태 코드: " + code);
    }
    
    /**
     * 로그인 가능 여부
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }
    
    /**
     * 탈퇴 상태 여부  
     */
    public boolean isWithdrawn() {
        return this == WITHDRAWN;
    }
}

