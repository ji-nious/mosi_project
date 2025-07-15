package com.kh.project.web.exception;

import com.kh.project.web.common.response.ApiResponse;
import com.kh.project.web.common.response.ApiResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global Exception Handler
 * 모든 컨트롤러에서 발생하는 예외를 통합 처리
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 유효성 검증 실패 시 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        log.error("Validation error occurred", ex);
        
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        Map<String, Object> response = ApiResponse.error("입력값 검증에 실패했습니다.", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 비즈니스 유효성 검증 예외 처리
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessValidationException(
            BusinessValidationException ex) {
        
        log.error("Business validation error occurred", ex);
        
        Map<String, Object> response = ApiResponse.error(ex.getMessage(), ex.getDetails());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 권한 관련 예외 처리
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
        
        log.error("Security error occurred", ex);
        
        Map<String, Object> response = ApiResponse.error("권한이 없습니다.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 데이터 조회 실패 시 처리
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(NoSuchElementException ex) {
        
        log.error("Data not found error occurred", ex);
        
        Map<String, Object> response = ApiResponse.error("요청한 데이터를 찾을 수 없습니다.");
        return ResponseEntity.notFound().build();
    }

    /**
     * 데이터베이스 접근 예외 처리
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
        
        log.error("Database error occurred", ex);
        
        Map<String, Object> response = ApiResponse.error("데이터베이스 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        
        log.error("Unexpected error occurred", ex);
        
        Map<String, Object> response = ApiResponse.error("서버 내부 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

