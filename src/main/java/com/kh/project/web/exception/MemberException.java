package com.kh.project.web.exception;

/**
 * 회원 관련 예외
 */
public class MemberException extends RuntimeException {
  protected MemberException(String message) {
    super(message);
  }

  public static class EmailDuplicationException extends MemberException {
    public EmailDuplicationException(String email) {
      super(String.format("이미 사용중인 이메일입니다: %s", email));
    }
  }

  public static class LoginFailedException extends MemberException {
    public LoginFailedException() {
      super("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
  }

  public static class AlreadyWithdrawnException extends MemberException {
    public AlreadyWithdrawnException() {
      super("이미 탈퇴한 회원입니다.");
    }
  }

  public static class MemberNotFoundException extends MemberException {
    public MemberNotFoundException() {
      super("존재하지 않는 회원입니다.");
    }
  }
}
