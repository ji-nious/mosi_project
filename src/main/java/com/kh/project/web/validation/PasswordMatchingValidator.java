package com.kh.project.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 비밀번호 일치 검증 Validator
 */
@Slf4j
public class PasswordMatchingValidator implements ConstraintValidator<PasswordMatching, Object> {
    
    private String passwordFieldName;
    private String passwordConfirmFieldName;
    private String message;
    
    @Override
    public void initialize(PasswordMatching constraintAnnotation) {
        this.passwordFieldName = constraintAnnotation.password();
        this.passwordConfirmFieldName = constraintAnnotation.passwordConfirm();
        this.message = constraintAnnotation.message();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            // Reflection을 사용하여 필드 값 가져오기
            Object password = getFieldValue(value, passwordFieldName);
            Object passwordConfirm = getFieldValue(value, passwordConfirmFieldName);
            
            // 둘 다 null인 경우는 valid (다른 검증에서 처리)
            if (password == null && passwordConfirm == null) {
                return true;
            }
            
            // 하나만 null인 경우는 invalid
            if (password == null || passwordConfirm == null) {
                addConstraintViolation(context);
                return false;
            }
            
            // 비밀번호 일치 여부 확인
            boolean isValid = password.equals(passwordConfirm);
            
            if (!isValid) {
                addConstraintViolation(context);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("비밀번호 일치 검증 중 오류 발생", e);
            return false;
        }
    }
    
    /**
     * Reflection을 사용하여 필드 값 가져오기
     */
    private Object getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }
    
    /**
     * 제약조건 위반 메시지 추가
     */
    private void addConstraintViolation(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
               .addPropertyNode(passwordConfirmFieldName)
               .addConstraintViolation();
    }
} 