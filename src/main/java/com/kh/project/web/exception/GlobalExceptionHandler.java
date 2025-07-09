package com.kh.project.web.exception;

import com.kh.project.web.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 강화??글로벌 ?�외 ?�들??(?��??�된 ?�답)
 * ?� ?�로?�트 ?�체?�서 ?��????�러 처리 ?�공
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation ?�패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("?�력값이 ?�바르�? ?�습?�다.");

        log.warn("?�력�?검�??�패: URI={} | ?�러={}", request.getRequestURI(), errorMessage);
        
        Map<String, Object> response = ApiResponse.error(errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 비즈?�스 ?�외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        
        log.warn("비즈?�스 ?�외: URI={} | ?�러={}", request.getRequestURI(), e.getMessage());
        
        Map<String, Object> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 보안 ?�외 처리 (?�근 거�?)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        
        log.warn("?�근 거�?: URI={} | IP={} | ?�러={}", 
            request.getRequestURI(), 
            getClientIP(request), 
            e.getMessage());
        
        Map<String, Object> response = ApiResponse.error("?�근 권한???�습?�다.");
        return ResponseEntity.status(403).body(response);
    }
    
    /**
     * ?�원 관???�외 처리
     */
    @ExceptionHandler(MemberException.class)
    public ResponseEntity<Map<String, Object>> handleMemberException(
            MemberException e, HttpServletRequest request) {
        
        log.warn("?�원 관???�외: URI={} | ?�러={}", request.getRequestURI(), e.getMessage());
        
        Map<String, Object> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * ?�반 ?�외 처리 (보안 ?�보 ?�출 방�?)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception e, HttpServletRequest request) {
        
        // ?�세 ?�러??로그?�만 기록 (보안???�라?�언?�에???�반 메시지)
        log.error("?�상?��? 못한 ?�버 ?�류: URI={} | IP={} | ?�러={}", 
            request.getRequestURI(), 
            getClientIP(request), 
            e.getMessage(), e);
        
        Map<String, Object> response = ApiResponse.error("?�시?�인 ?�버 ?�류가 발생?�습?�다. ?�시 ???�시 ?�도?�주?�요.");
        return ResponseEntity.internalServerError().body(response);
    }
    
    /**
     * ?�라?�언??IP 추출 (?�록??고려)
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
