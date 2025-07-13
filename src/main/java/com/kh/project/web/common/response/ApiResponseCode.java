package com.kh.project.web.common.response;

import java.util.Arrays;

/**
 * API 응답 코드
 */
public enum ApiResponseCode {
  // 성공 응답
  SUCCESS("S00", "Success"),

  // 공통 예외
  VALIDATION_ERROR("E01", "Validation error occurred"),
  BUSINESS_ERROR("E02", "Business error occurred"),
  ENTITY_NOT_FOUND("E03", "Entity not found"),

  // 사용자 관련 예외
  USER_NOT_FOUND("U01", "User not found"),
  USER_ALREADY_EXISTS("U02", "User already exists"),
  INVALID_PASSWORD("U03", "Invalid password"),
  NICKNAME_ALREADY_EXISTS("U04", "Nickname already exists"),
  BIZ_REG_NO_ALREADY_EXISTS("U05", "Business registration number already exists"),
  LOGIN_FAILED("U06", "Login failed"),

  // 시스템 예외
  INTERNAL_SERVER_ERROR("999","Internal server error");

  private final String rtcd;
  private final String rtmsg;

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

  public static ApiResponseCode of(String code) {
    return Arrays.stream(values())
        .filter(rc -> rc.getRtcd().equals(code))
        .findFirst()
        .orElse(INTERNAL_SERVER_ERROR);
  }
} 