package com.kh.project.domain.entity;

/**
 * 회원 유형
 */
public enum MemberType {

    BUYER("B", "구매자"),
    SELLER("S", "판매자");

    private final String code;
    private final String description;

    MemberType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }
}
