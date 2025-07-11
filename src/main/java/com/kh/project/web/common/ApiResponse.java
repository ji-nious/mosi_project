package com.kh.project.web.common;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * API 응답 표준화 클래스
 * 모든 REST API 응답의 일관된 구조를 제공
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
     * 성공 응답 생성 (데이터 없음)
     * 
     * @param message 성공 메시지
     * @return Map<String, Object> - 성공 응답 객체
     */
    public static Map<String, Object> success(String message) {
        return success(message, null);
    }
    
    /**
     * 성공 응답 생성 (데이터 포함)
     * 
     * @param message 성공 메시지
     * @param data 응답 데이터
     * @return Map<String, Object> - 성공 응답 객체
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
     * 실패 응답 생성 (에러 세부사항 없음)
     * 
     * @param message 에러 메시지
     * @return Map<String, Object> - 실패 응답 객체
     */
    public static Map<String, Object> error(String message) {
        return error(message, null);
    }
    
    /**
     * 실패 응답 생성 (에러 세부사항 포함)
     * 
     * @param message 에러 메시지
     * @param errorDetails 에러 세부사항
     * @return Map<String, Object> - 실패 응답 객체
     */
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
    
    /**
     * 엔티티 성공 응답 생성 (추가 데이터 포함)
     * 
     * @param message 성공 메시지
     * @param entity 주요 엔티티 객체
     * @param additionalData 추가 데이터
     * @return Map<String, Object> - 엔티티 포함 성공 응답 객체
     */
    public static Map<String, Object> entitySuccess(String message, Object entity, Map<String, Object> additionalData) {
        Map<String, Object> dataMap = new HashMap<>();
        
        if (entity != null) {
            dataMap.put("entity", entity);
        }
        
        if (additionalData != null) {
            dataMap.putAll(additionalData);
        }
        
        return success(message, dataMap);
    }
    
    /**
     * 로그인 성공 응답 생성
     * 
     * @param memberEntity 회원 엔티티 (Buyer 또는 Seller)
     * @param gubunName 회원 등급명
     * @param canLogin 로그인 가능 여부
     * @return Map<String, Object> - 로그인 성공 응답 객체
     */
    public static Map<String, Object> loginSuccess(Object memberEntity, String gubunName, boolean canLogin) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("gubunName", gubunName);
        additionalData.put("canLogin", canLogin);
        
        return entitySuccess("로그인 성공", memberEntity, additionalData);
    }
    
    /**
     * 회원가입 성공 응답 생성
     * 
     * @param memberEntity 회원 엔티티 (Buyer 또는 Seller)
     * @param gubunName 회원 등급명
     * @return Map<String, Object> - 회원가입 성공 응답 객체
     */
    public static Map<String, Object> joinSuccess(Object memberEntity, String gubunName) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("gubunName", gubunName);
        
        return entitySuccess("회원가입이 완료되었습니다.", memberEntity, additionalData);
    }
}
