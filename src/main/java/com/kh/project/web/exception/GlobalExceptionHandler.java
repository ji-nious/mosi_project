package com.kh.project.web.exception;

import com.kh.project.web.common.response.ApiResponse;
import com.kh.project.web.common.response.ApiResponseCode;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice   // Controller에서 발생된 예외를 처리하는 클래스라는 것을 springboot에 알림
public class GlobalExceptionHandler {

    /**
     * 유효성 검증 실패 시 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> details = new HashMap<>();
        
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        log.error("Validation error: {}", details);
        
        Map<String, Object> response = ApiResponse.error("입력값을 확인해주세요.", details);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 비즈니스 유효성 검증 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
        BusinessException ex) {

        log.error("Business validation error: {}", ex.getMessage());

        Map<String, Object> response = ApiResponse.error(ex.getMessage(), ex.getDetails());

        return ResponseEntity.ok(response);
    }

    /**
     * 엔티티를 찾을 수 없을 때 처리
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(
            NoSuchElementException ex) {
        
        log.error("Entity not found: {}", ex.getMessage());
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("entity", ex.getMessage());

        Map<String, Object> response = ApiResponse.error("요청한 데이터를 찾을 수 없습니다.", errorDetails);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        
        Map<String, Object> response = ApiResponse.error("서버 내부 오류가 발생했습니다.");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

