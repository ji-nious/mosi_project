package com.kh.project.web.exception;

import com.kh.project.web.api.ApiResponseCode;

/**
 * 사용자 관련 예외를 모아놓은 컨테이너 클래스
 */
public class UserException extends BusinessException {

  /**
   * 부모 생성자. 중첩 클래스에서 호출하여 상태 코드와 메시지를 설정합니다.
   * @param responseCode 응답 코드 Enum
   * @param message 응답 메시지
   */
  protected UserException(ApiResponseCode responseCode, String message) {
    super(responseCode, message);
  }

  /**
   * 로그인 필요 예외
   * - 원인: 인증이 필요한 요청에서 로그인하지 않은 상태
   * - 응답 코드: A01 (LOGIN_REQUIRED)
   */
  public static class LoginRequired extends UserException {
    public LoginRequired() {
      super(ApiResponseCode.LOGIN_REQUIRED, "로그인이 필요합니다.");
    }

    public LoginRequired(String message) {
      super(ApiResponseCode.LOGIN_REQUIRED, message);
    }
  }

  /**
   * 로그인 실패 예외
   * - 원인: 존재하지 않는 이메일 또는 잘못된 비밀번호 입력
   * - 응답 코드: U06 (LOGIN_FAILED)
   */
  public static class LoginFailed extends UserException {
    public LoginFailed() {
      super(ApiResponseCode.LOGIN_FAILED, "로그인에 실패했습니다.");
    }

    public LoginFailed(String message) {
      super(ApiResponseCode.LOGIN_FAILED, message);
    }
  }

  /**
   * 사용자를 찾을 수 없는 예외
   * - 원인: 존재하지 않는 사용자 ID나 이메일로 조회 시도
   * - 응답 코드: U01 (USER_NOT_FOUND)
   */
  public static class UserNotFound extends UserException {
    public UserNotFound() {
      super(ApiResponseCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다.");
    }

    public UserNotFound(String message) {
      super(ApiResponseCode.USER_NOT_FOUND, message);
    }
  }

  /**
   * 이메일 중복 예외
   * - 원인: 회원가입 시 이미 시스템에 등록된 이메일 주소 사용
   * - 응답 코드: U02 (USER_ALREADY_EXISTS)
   */
  public static class EmailAlreadyExists extends UserException {
    public EmailAlreadyExists() {
      super(ApiResponseCode.USER_ALREADY_EXISTS, "이미 사용중인 이메일입니다.");
    }

    public EmailAlreadyExists(String message) {
      super(ApiResponseCode.USER_ALREADY_EXISTS, message);
    }
  }

  /**
   * 닉네임 중복 예외
   * - 원인: 회원가입 시 이미 시스템에 등록된 닉네임 사용
   * - 응답 코드: U04 (NICKNAME_ALREADY_EXISTS)
   */
  public static class NicknameAlreadyExists extends UserException {
    public NicknameAlreadyExists() {
      super(ApiResponseCode.NICKNAME_ALREADY_EXISTS, "이미 사용중인 닉네임입니다.");
    }

    public NicknameAlreadyExists(String message) {
      super(ApiResponseCode.NICKNAME_ALREADY_EXISTS, message);
    }
  }

  /**
   * 사업자 등록번호 중복 예외
   * - 원인: 판매자 회원가입 시 이미 시스템에 등록된 사업자 번호 사용
   * - 응답 코드: U05 (BIZ_REG_NO_ALREADY_EXISTS)
   */
  public static class BizRegNoAlreadyExists extends UserException {
    public BizRegNoAlreadyExists() {
      super(ApiResponseCode.BIZ_REG_NO_ALREADY_EXISTS, "이미 등록된 사업자 등록번호입니다.");
    }

    public BizRegNoAlreadyExists(String message) {
      super(ApiResponseCode.BIZ_REG_NO_ALREADY_EXISTS, message);
    }
  }

  /**
   * 비밀번호 불일치 예외
   * - 원인: 회원 정보 수정, 회원 탈퇴 등 본인 확인 절차에서 현재 비밀번호를 잘못 입력
   * - 응답 코드: U03 (INVALID_PASSWORD)
   */
  public static class PasswordMismatched extends UserException {
    public PasswordMismatched() {
      super(ApiResponseCode.INVALID_PASSWORD, "비밀번호가 일치하지 않습니다.");
    }

    public PasswordMismatched(String message) {
      super(ApiResponseCode.INVALID_PASSWORD, message);
    }
  }

  /**
   * 업데이트 실패 예외
   * - 원인: 데이터 수정 작업이 실패한 경우
   * - 응답 코드: E04 (UPDATE_FAILED)
   */
  public static class UpdateFailed extends UserException {
    public UpdateFailed() {
      super(ApiResponseCode.UPDATE_FAILED, "정보 수정에 실패했습니다.");
    }

    public UpdateFailed(String message) {
      super(ApiResponseCode.UPDATE_FAILED, message);
    }
  }

  /**
   * 탈퇴 실패 예외
   * - 원인: 회원 탈퇴 처리가 실패한 경우
   * - 응답 코드: E05 (WITHDRAW_FAILED)
   */
  public static class WithdrawFailed extends UserException {
    public WithdrawFailed() {
      super(ApiResponseCode.WITHDRAW_FAILED, "탈퇴 처리에 실패했습니다.");
    }

    public WithdrawFailed(String message) {
      super(ApiResponseCode.WITHDRAW_FAILED, message);
    }
  }

  /**
   * 필수 입력값 누락 예외
   * - 원인: 필수 필드가 누락되거나 빈 값인 경우
   * - 응답 코드: E01 (VALIDATION_ERROR)
   */
  public static class ValidationError extends UserException {
    public ValidationError(String message) {
      super(ApiResponseCode.VALIDATION_ERROR, message);
    }
  }

  /**
   * 권한 없음 예외
   * - 원인: 접근 권한이 없는 리소스에 접근 시도
   * - 응답 코드: A01 (LOGIN_REQUIRED) - 권한 관련이므로 로그인 필요로 분류
   */
  public static class AccessDenied extends UserException {
    public AccessDenied() {
      super(ApiResponseCode.LOGIN_REQUIRED, "접근 권한이 없습니다.");
    }

    public AccessDenied(String message) {
      super(ApiResponseCode.LOGIN_REQUIRED, message);
    }
  }
}