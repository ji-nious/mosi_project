package com.kh.project.web.exception;

/**
 * 비즈니스 검증 예외 클래스들
 * 문자열 메시지 비교 대신 구체적인 예외 타입으로 필드별 에러 처리
 */
public class BusinessValidationException {
    
    /**
     * 이메일 중복 예외
     */
    public static class EmailDuplicateException extends BusinessException {
        public EmailDuplicateException(String message) {
            super(message);
        }
    }
    
    /**
     * 사업자등록번호 중복 예외  
     */
    public static class BizRegNoDuplicateException extends BusinessException {
        public BizRegNoDuplicateException(String message) {
            super(message);
        }
    }
    
    /**
     * 상호명 중복 예외
     */
    public static class ShopNameDuplicateException extends BusinessException {
        public ShopNameDuplicateException(String message) {
            super(message);
        }
    }
    
    /**
     * 닉네임 중복 예외
     */
    public static class NicknameDuplicateException extends BusinessException {
        public NicknameDuplicateException(String message) {
            super(message);
        }
    }
    
    /**
     * 대표자명 중복 예외
     */
    public static class NameDuplicateException extends BusinessException {
        public NameDuplicateException(String message) {
            super(message);
        }
    }
} 