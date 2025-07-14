package com.kh.project.web.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BusinessValidationException extends RuntimeException {
  private final Map<String, String> details;

  public BusinessValidationException(String message) {
    super(message);
    this.details = new HashMap<>();
  }

  public BusinessValidationException(String message, Map<String, String> details) {
    super(message);
    this.details = details;
  }

  // 글로벌 에러를 추가하는 편의 메서드
  public void addGlobalError(String message) {
    details.put("global", message);
  }

  // 필드 에러를 추가하는 편의 메서드
  public void addFieldError(String field, String message) {
    details.put(field, message);
  }

  // ============== 현재 프로젝트에서 꼭 필요한 하위 클래스들 ==============

  /**
   * 로그인 실패 예외
   */
  public static class LoginFailedException extends BusinessValidationException {
    public LoginFailedException(String message) {
      super(message);
    }
  }

  /**
   * 이미 탈퇴한 회원 예외
   */
  public static class AlreadyWithdrawnException extends BusinessValidationException {
    public AlreadyWithdrawnException(String message) {
      super(message);
    }
  }

  /**
   * 중복 회원 예외
   */
  public static class DuplicateMemberException extends BusinessValidationException {
    public DuplicateMemberException(String message) {
      super(message);
    }
  }

  /**
   * 회원을 찾을 수 없는 예외
   */
  public static class MemberNotFoundException extends BusinessValidationException {
    public MemberNotFoundException(String message) {
      super(message);
    }
  }
} 