package com.kh.project.web.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * 비즈니스 로직 검증 실패 시 발생시키는 예외
 */
public class BusinessException extends RuntimeException {
    
    private final Map<String, String> details;

    public BusinessException(String message) {
        super(message);
        this.details = new HashMap<>();
    }

    public BusinessException(String message, Map<String, String> details) {
        super(message);
        this.details = details != null ? details : new HashMap<>();
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.details = new HashMap<>();
    }

    public Map<String, String> getDetails() {
        return details;
    }

    /**
     * 전역 오류 메시지 추가
     */
    public void addGlobalError(String key, String message) {
        details.put(key, message);
    }

    /**
     * 필드별 오류 메시지 추가
     */
    public void addFieldError(String fieldName, String message) {
        details.put(fieldName, message);
    }

    /**
     * 로그인 실패 예외
     */
    public static class LoginFailedException extends BusinessException {
        public LoginFailedException(String message) {
            super(message);
        }
    }

    /**
     * 이미 탈퇴한 회원 예외
     */
    public static class AlreadyWithdrawnException extends BusinessException {
        public AlreadyWithdrawnException(String message) {
            super(message);
        }
    }

    /**
     * 중복 회원 예외
     */
    public static class DuplicateMemberException extends BusinessException {
        public DuplicateMemberException(String message) {
            super(message);
        }
    }

    /**
     * 회원을 찾을 수 없는 예외
     */
    public static class MemberNotFoundException extends BusinessException {
        public MemberNotFoundException(String message) {
            super(message);
        }
    }
} 