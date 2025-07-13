package com.kh.project.domain.entity;

import lombok.Getter;

/**
 * 회원 상태
 */
@Getter
public enum MemberStatus {
    ACTIVE("활성화", "활성화"),
    INACTIVE("비활성화", "비활성화"),
    SUSPENDED("정지", "정지"),
    WITHDRAWN("탈퇴", "탈퇴");

    private final String code;
    private final String description;

    MemberStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static String getDescriptionByCode(String code) {
        if (code == null) return "알 수 없음";

        for (MemberStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDescription();
            }
        }
        return "알 수 없음";
    }
}
