package com.kh.project.web.common;

/**
 * 공통 상수 클래스
 */
public final class CommonConstants {
    
    // 세션 관련
    public static final String LOGIN_MEMBER_KEY = "loginMember";
    public static final int SESSION_TIMEOUT = 1800;
    
    // 회원 타입
    public static final String MEMBER_TYPE_BUYER = "BUYER";
    public static final String MEMBER_TYPE_SELLER = "SELLER";
    
    // 회원 상태
    public static final String STATUS_ACTIVE = "활성화";
    public static final String STATUS_WITHDRAWN = "탈퇴";
    
    private CommonConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
} 