package com.kh.project.domain.entity;

import lombok.Getter;

/**
 * 서비스 이용현황
 */
@Getter
public enum ServiceUsage {
    NONE(0, "이용현황 없음"),
    USING(1, "이용현황 있음");
    
    private final int code;
    private final String description;
    
    ServiceUsage(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 코드로 이용현황 찾기
     */
    public static ServiceUsage fromCode(int code) {
        for (ServiceUsage usage : values()) {
            if (usage.code == code) {
                return usage;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 이용현황 코드: " + code);
    }
    
    /**
     * 탈퇴 가능 여부 (이용현황이 없어야 탈퇴 가능)
     */
    public boolean canWithdraw() {
        return this == NONE;
    }
} 