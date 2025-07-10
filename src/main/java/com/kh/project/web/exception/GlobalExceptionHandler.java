package com.kh.project.web.exception;

import com.kh.project.web.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

/**
 * 강화된 글로벌 예외 핸들러 (API 응답용)
 * 이 프로젝트 전체의 RESTful API 에러 처리를 제공
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("입력값 검증 실패: URI={} | 에러={}", request.getRequestURI(), errorMessage);

        Map<String, Object> response = ApiResponse.error(errorMessage);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 404 Not Found 예외 처리
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404 Not Found: URI={} | 메소드={}", request.getRequestURI(), request.getMethod());
        Map<String, Object> response = ApiResponse.error("요청하신 리소스를 찾을 수 없습니다.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {

        log.warn("비즈니스 예외: URI={} | 에러={}", request.getRequestURI(), e.getMessage());

        Map<String, Object> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 보안 예외 처리 (접근 거부)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException e, HttpServletRequest request) {

        log.warn("접근 거부: URI={} | IP={} | 에러={}",
                request.getRequestURI(),
                getClientIP(request),
                e.getMessage());

        Map<String, Object> response = ApiResponse.error("접근 권한이 없습니다.");
        return ResponseEntity.status(403).body(response);
    }

    /**
     * 회원 관련 예외 처리
     */
    @ExceptionHandler(MemberException.class)
    public ResponseEntity<Map<String, Object>> handleMemberException(
            MemberException e, HttpServletRequest request) {

        log.warn("회원 관련 예외: URI={} | 에러={}", request.getRequestURI(), e.getMessage());

        Map<String, Object> response = ApiResponse.error(e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 일반 예외 처리 (보안 정보 노출 방지)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(
            Exception e, HttpServletRequest request) {

        log.error("예상치 못한 서버 오류: URI={} | IP={} | 에러={}",
                request.getRequestURI(),
                getClientIP(request),
                e.getMessage(), e);

        Map<String, Object> response = ApiResponse.error("일시적인 서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 클라이언트 IP 추출 (프록시 환경 고려)
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
