package com.kh.project.web.exception;

import com.kh.project.web.common.ApiResponse;
import com.kh.project.web.common.ApiResponseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * GlobalExceptionHandler 포괄적 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler 포괄적 테스트")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getMethod()).thenReturn("POST");
    }

    // ==================== BusinessException 테스트 ====================

    @Test
    @DisplayName("BusinessException 처리 - 일반 비즈니스 예외")
    void handleBusinessException() {
        // given
        String errorMessage = "비즈니스 로직 오류가 발생했습니다.";
        BusinessException exception = new BusinessException(errorMessage);

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.BAD_REQUEST.getCode(), body.getCode());
        assertEquals(errorMessage, body.getMessage());
        assertNull(body.getData());
    }

    @Test
    @DisplayName("BusinessException 처리 - null 메시지")
    void handleBusinessException_null_message() {
        // given
        BusinessException exception = new BusinessException(null);

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.BAD_REQUEST.getCode(), body.getCode());
        assertEquals("비즈니스 로직 오류", body.getMessage()); // 기본 메시지
    }

    // ==================== MemberException 테스트 ====================

    @Test
    @DisplayName("MemberException.EmailDuplicationException 처리")
    void handleEmailDuplicationException() {
        // given
        String email = "test@example.com";
        MemberException.EmailDuplicationException exception = 
            new MemberException.EmailDuplicationException(email);

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleMemberException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.EMAIL_DUPLICATION.getCode(), body.getCode());
        assertTrue(body.getMessage().contains(email));
    }

    @Test
    @DisplayName("MemberException.LoginFailedException 처리")
    void handleLoginFailedException() {
        // given
        MemberException.LoginFailedException exception = 
            new MemberException.LoginFailedException();

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleMemberException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.LOGIN_FAILED.getCode(), body.getCode());
        assertEquals("로그인에 실패했습니다.", body.getMessage());
    }

    @Test
    @DisplayName("MemberException.MemberNotFoundException 처리")
    void handleMemberNotFoundException() {
        // given
        MemberException.MemberNotFoundException exception = 
            new MemberException.MemberNotFoundException();

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleMemberException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.MEMBER_NOT_FOUND.getCode(), body.getCode());
        assertEquals("회원을 찾을 수 없습니다.", body.getMessage());
    }

    @Test
    @DisplayName("MemberException.AlreadyWithdrawnException 처리")
    void handleAlreadyWithdrawnException() {
        // given
        MemberException.AlreadyWithdrawnException exception = 
            new MemberException.AlreadyWithdrawnException();

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleMemberException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.ALREADY_WITHDRAWN.getCode(), body.getCode());
        assertEquals("이미 탈퇴한 회원입니다.", body.getMessage());
    }

    // ==================== Validation Exception 테스트 ====================

    @Test
    @DisplayName("MethodArgumentNotValidException 처리")
    void handleMethodArgumentNotValidException() {
        // given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        FieldError fieldError1 = new FieldError("buyer", "email", "이메일은 필수입니다.");
        FieldError fieldError2 = new FieldError("buyer", "password", "비밀번호는 8자 이상이어야 합니다.");
        
        when(exception.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleValidationException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.VALIDATION_FAILED.getCode(), body.getCode());
        assertTrue(body.getMessage().contains("validation failed"));
        
        // 검증 오류 정보 확인
        assertNotNull(body.getData());
    }

    @Test
    @DisplayName("BindException 처리")
    void handleBindException() {
        // given
        BindException exception = new BindException("target", "objectName");
        exception.addError(new FieldError("buyer", "nickname", "닉네임은 필수입니다."));

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleValidationException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.VALIDATION_FAILED.getCode(), body.getCode());
    }

    // ==================== Generic Exception 테스트 ====================

    @Test
    @DisplayName("일반 Exception 처리")
    void handleGenericException() {
        // given
        Exception exception = new RuntimeException("예상치 못한 오류가 발생했습니다.");

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleGenericException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.INTERNAL_SERVER_ERROR.getCode(), body.getCode());
        assertEquals("서버 내부 오류가 발생했습니다.", body.getMessage());
    }

    @Test
    @DisplayName("NullPointerException 처리")
    void handleNullPointerException() {
        // given
        NullPointerException exception = new NullPointerException("Null pointer 오류");

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleGenericException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.INTERNAL_SERVER_ERROR.getCode(), body.getCode());
        assertEquals("서버 내부 오류가 발생했습니다.", body.getMessage());
    }

    // ==================== IllegalArgumentException 테스트 ====================

    @Test
    @DisplayName("IllegalArgumentException 처리")
    void handleIllegalArgumentException() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("잘못된 매개변수입니다.");

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleIllegalArgumentException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.BAD_REQUEST.getCode(), body.getCode());
        assertEquals("잘못된 매개변수입니다.", body.getMessage());
    }

    // ==================== 로깅 검증 테스트 ====================

    @Test
    @DisplayName("예외 발생시 로그 기록 확인 - BusinessException")
    void verifyLoggingForBusinessException() {
        // given
        BusinessException exception = new BusinessException("테스트 비즈니스 예외");

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(exception, mockRequest);

        // then
        assertNotNull(response);
        // 로그 기록 여부는 실제 로그 프레임워크를 통해 확인해야 하지만,
        // 여기서는 응답이 정상적으로 생성되는지만 확인
        verify(mockRequest, atLeastOnce()).getRequestURI();
    }

    // ==================== Edge Case 테스트 ====================

    @Test
    @DisplayName("매우 긴 오류 메시지 처리")
    void handleVeryLongErrorMessage() {
        // given
        String longMessage = "오류".repeat(1000); // 매우 긴 메시지
        BusinessException exception = new BusinessException(longMessage);

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(longMessage, body.getMessage());
    }

    @Test
    @DisplayName("특수문자 포함 오류 메시지 처리")
    void handleSpecialCharactersInErrorMessage() {
        // given
        String specialMessage = "오류 메시지 @#$%^&*()_+ []{}|;':\",./<>?";
        BusinessException exception = new BusinessException(specialMessage);

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(specialMessage, body.getMessage());
    }

    @Test
    @DisplayName("다중 필드 검증 오류 처리")
    void handleMultipleFieldValidationErrors() {
        // given
        BindException exception = new BindException("target", "objectName");
        exception.addError(new FieldError("buyer", "email", "이메일 형식이 올바르지 않습니다."));
        exception.addError(new FieldError("buyer", "password", "비밀번호는 8자 이상이어야 합니다."));
        exception.addError(new FieldError("buyer", "nickname", "닉네임은 필수입니다."));
        exception.addError(new FieldError("buyer", "tel", "전화번호 형식이 올바르지 않습니다."));

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleValidationException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(ApiResponseCode.VALIDATION_FAILED.getCode(), body.getCode());
        assertNotNull(body.getData());
    }

    @Test
    @DisplayName("중첩된 예외 처리")
    void handleNestedException() {
        // given
        RuntimeException rootCause = new RuntimeException("근본 원인");
        BusinessException exception = new BusinessException("비즈니스 오류", rootCause);

        // when
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleBusinessException(exception, mockRequest);

        // then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ApiResponse<Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("비즈니스 오류", body.getMessage());
    }
} 