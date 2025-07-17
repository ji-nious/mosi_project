package com.kh.project.web.api;

import java.util.Arrays;

public enum ApiResponseCode {
  // 성공 응답
  SUCCESS("S00", "Success"),

  // 공통 예외
  LOGIN_REQUIRED("A01", "Login required"),
  VALIDATION_ERROR("E01", "Validation error occurred"),
  BUSINESS_ERROR("E02", "Business error occurred"),
  ENTITY_NOT_FOUND("E03", "Entity not found"),
  UPDATE_FAILED("E04", "Update failed"),
  WITHDRAW_FAILED("E05", "Withdraw failed"),

  // 사용자 관련 예외
  USER_NOT_FOUND("U01", "User not found"),
  USER_ALREADY_EXISTS("U02", "User already exists"),
  INVALID_PASSWORD("U03", "Invalid password"),
  NICKNAME_ALREADY_EXISTS("U04", "Nickname already exists"),
  BIZ_REG_NO_ALREADY_EXISTS("U05", "Business registration number already exists"),
  LOGIN_FAILED("U06", "Login failed"),

  // 시스템 예외
  ACCESS_DENIED("A02", "Access denied"),
  SERVER_ERROR("S01", "Server error"),
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

  // 코드로 enum 조회
  public static ApiResponseCode of(String code) {
    return Arrays.stream(values())
        .filter(rc -> rc.getRtcd().equals(code))
        .findFirst()
        .orElse(INTERNAL_SERVER_ERROR);
  }

}