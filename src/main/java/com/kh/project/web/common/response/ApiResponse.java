package com.kh.project.web.common.response;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * API 응답 표준화
 */
@Slf4j
@Getter
@ToString
public class ApiResponse {
    
    private final boolean success;
    private final String message;
    private final Object data;
    private final String timestamp;
    
    private ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }
    
    /**
     * 성공 응답 생성
     */
    public static Map<String, Object> success(String message) {
        return success(message, null);
    }
    
    /**
     * 성공 응답 생성 (데이터 포함)
     */
    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (data != null) {
            response.put("data", data);
        }
        
        log.debug("API 성공 응답: {}", message);
        return response;
    }
    
    /**
     * 실패 응답 생성
     */
    public static Map<String, Object> error(String message) {
        return error(message, null);
    }
    
    /**
     * 실패 응답 생성 (에러 데이터 포함)
     */
    public static Map<String, Object> error(String message, Object errorData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (errorData != null) {
            response.put("error", errorData);
        }
        
        log.debug("API 실패 응답: {}", message);
        return response;
    }
    
    /**
     * 회원가입 성공 응답
     */
    public static Map<String, Object> joinSuccess(Object member, String memberType) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "회원가입이 완료되었습니다.");
        response.put("data", member);
        response.put("memberType", memberType);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        log.debug("회원가입 성공 응답: {}", memberType);
        return response;
    }
    
    /**
     * 로그인 성공 응답
     */
    public static Map<String, Object> loginSuccess(Object member, String memberType, boolean canLogin) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그인이 완료되었습니다.");
        response.put("data", member);
        response.put("memberType", memberType);
        response.put("canLogin", canLogin);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        log.debug("로그인 성공 응답: {}", memberType);
        return response;
    }
    
    /**
     * 엔티티 성공 응답
     */
    public static Map<String, Object> entitySuccess(String message, Object entity, Object additionalData) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", entity);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (additionalData != null) {
            response.put("additional", additionalData);
        }
        
        log.debug("엔티티 성공 응답: {}", message);
        return response;
    }
}
