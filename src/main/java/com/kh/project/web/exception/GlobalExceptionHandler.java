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
 * ê°•í™”??ê¸€ë¡œë²Œ ?ˆì™¸ ?¸ë“¤??(?œì??”ëœ ?‘ë‹µ)
 * ?€ ?„ë¡œ?íŠ¸ ?„ì²´?ì„œ ?¼ê????ëŸ¬ ì²˜ë¦¬ ?œê³µ
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation ?¤íŒ¨ ì²˜ë¦¬
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fieldError -> fieldError.getDefaultMessage())
            .orElse("?…ë ¥ê°’ì´ ?¬ë°”ë¥´ì? ?ŠìŠµ?ˆë‹¤.");

        log.warn("?…ë ¥ê°?ê²€ì¦??¤íŒ¨: URI={} | ?ëŸ¬={}", request.getRequestURI(), errorMessage);
        
        Map<String, Object> response = ApiResponse.error(errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * ë¹„ì¦ˆ?ˆìŠ¤ ?ˆì™¸ ì²˜ë¦¬
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        
        log.warn("ë¹„ì¦ˆ?ˆìŠ¤ ?ˆì™¸: URI={} | ?ëŸ¬={}", request.getRequestURI(), e.getMessage());
        
        Map<String, Object> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * ë³´ì•ˆ ?ˆì™¸ ì²˜ë¦¬ (?‘ê·¼ ê±°ë?)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {
        
        log.warn("?‘ê·¼ ê±°ë?: URI={} | IP={} | ?ëŸ¬={}", 
            request.getRequestURI(), 
            getClientIP(request), 
            e.getMessage());
        
        Map<String, Object> response = ApiResponse.error("?‘ê·¼ ê¶Œí•œ???†ìŠµ?ˆë‹¤.");
        return ResponseEntity.status(403).body(response);
    }
    
    /**
     * ?Œì› ê´€???ˆì™¸ ì²˜ë¦¬
     */
    @ExceptionHandler(MemberException.class)
    public ResponseEntity<Map<String, Object>> handleMemberException(
            MemberException e, HttpServletRequest request) {
        
        log.warn("?Œì› ê´€???ˆì™¸: URI={} | ?ëŸ¬={}", request.getRequestURI(), e.getMessage());
        
        Map<String, Object> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * ?¼ë°˜ ?ˆì™¸ ì²˜ë¦¬ (ë³´ì•ˆ ?•ë³´ ?¸ì¶œ ë°©ì?)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception e, HttpServletRequest request) {
        
        // ?ì„¸ ?ëŸ¬??ë¡œê·¸?ë§Œ ê¸°ë¡ (ë³´ì•ˆ???´ë¼?´ì–¸?¸ì—???¼ë°˜ ë©”ì‹œì§€)
        log.error("?ˆìƒ?˜ì? ëª»í•œ ?œë²„ ?¤ë¥˜: URI={} | IP={} | ?ëŸ¬={}", 
            request.getRequestURI(), 
            getClientIP(request), 
            e.getMessage(), e);
        
        Map<String, Object> response = ApiResponse.error("?¼ì‹œ?ì¸ ?œë²„ ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤. ? ì‹œ ???¤ì‹œ ?œë„?´ì£¼?¸ìš”.");
        return ResponseEntity.internalServerError().body(response);
    }
    
    /**
     * ?´ë¼?´ì–¸??IP ì¶”ì¶œ (?„ë¡??ê³ ë ¤)
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
