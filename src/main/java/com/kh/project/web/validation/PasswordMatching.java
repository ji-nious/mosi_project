package com.kh.project.web.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 비밀번호와 비밀번호 확인이 일치하는지 검증하는 Custom Validation Annotation
 */
@Documented
@Constraint(validatedBy = PasswordMatchingValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatching {
    
    String message() default "비밀번호가 일치하지 않습니다.";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 비밀번호 필드명 (기본값: "password")
     */
    String password() default "password";
    
    /**
     * 비밀번호 확인 필드명 (기본값: "passwordConfirm")
     */
    String passwordConfirm() default "passwordConfirm";
} 