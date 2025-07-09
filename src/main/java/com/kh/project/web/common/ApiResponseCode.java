package com.kh.project.web.common;

import java.util.Arrays;

/**
 *  REST API 응답메세지 헤더에 사용되는 응답코드, 응답메세지 상수화
 */
public enum ApiResponseCode {
  // 성공 응답
  SUCCESS("S00", "Success"),

  // 공통 예외
  VALIDATION_ERROR("E01", "Validation error occurred"), // 유효성 검증 실패
  BUSINESS_ERROR("E02", "Business error occurred"), // 비즈니스 로직 예외
  ENTITY_NOT_FOUND("E03", "Entity not found"), // 엔티티 없음

  // 사용자 관련 예외
  USER_NOT_FOUND("U01", "User not found"), // 사용자 없음
  USER_ALREADY_EXISTS("U02", "User already exists"), // 사용자 중복
  INVALID_PASSWORD("U03", "Invalid password"), // 비밀번호 오류
  NICKNAME_ALREADY_EXISTS("U04", "Nickname already exists"), // 닉네임 중복
  BIZ_REG_NO_ALREADY_EXISTS("U05", "Business registration number already exists"), // 사업자번호 중복
  LOGIN_FAILED("U06", "Login failed"), // 로그인 실패 전용 코드

  // 시스템 예외
  INTERNAL_SERVER_ERROR("999","Internal server error"); // 서버 오류

  private final String rtcd; // 코드
  private final String rtmsg; // 메시지

  ApiResponseCode(String rtcd, String rtmsg) {
    this.rtcd = rtcd;
    this.rtmsg = rtmsg;
  }

  public String getRtcd() {
    return rtcd;
  }

  public String getRtmsg() {
    return rtmsg;
  }

  // 코드로 enum 조회
  public static ApiResponseCode of(String code) {
    return Arrays.stream(values())
        .filter(rc -> rc.getRtcd().equals(code))
        .findFirst()
        .orElse(INTERNAL_SERVER_ERROR);
  }
} 