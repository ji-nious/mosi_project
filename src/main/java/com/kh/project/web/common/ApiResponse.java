package com.kh.project.web.common;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 간단한 API 응답 표준화 (DTO 없이 Entity 직접 사용)
 * 팀 프로젝트에서 모든 도메인이 사용할 수 있는 공통 응답 구조
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
    
    // 성공 응답 생성 메서드들
    public static Map<String, Object> success(String message) {
        return success(message, null);
    }
    
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
    
    // 실패 응답 생성 메서드들
    public static Map<String, Object> error(String message) {
        return error(message, null);
    }
    
    public static Map<String, Object> error(String message, Object errorDetails) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        
        if (errorDetails != null) {
            response.put("errorDetails", errorDetails);
        }
        
        log.warn("API 실패 응답: {}", message);
        return response;
    }
    
    // Entity 전용 성공 응답 (구매자/판매자 등)
    public static Map<String, Object> entitySuccess(String message, Object entity, Map<String, Object> additionalData) {
        Map<String, Object> dataMap = new HashMap<>();
        
        // Entity 직접 포함
        if (entity != null) {
            dataMap.put("entity", entity);
        }
        
        // 추가 데이터 병합
        if (additionalData != null) {
            dataMap.putAll(additionalData);
        }
        
        return success(message, dataMap);
    }
    
    // 로그인 전용 응답
    public static Map<String, Object> loginSuccess(Object memberEntity, String gubunName, boolean canLogin) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("gubunName", gubunName);
        additionalData.put("canLogin", canLogin);
        
        return entitySuccess("로그인 성공", memberEntity, additionalData);
    }
    
    // 회원가입 전용 응답
    public static Map<String, Object> joinSuccess(Object memberEntity, String gubunName) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("gubunName", gubunName);
        
        return entitySuccess("회원가입이 완료되었습니다.", memberEntity, additionalData);
    }
}
