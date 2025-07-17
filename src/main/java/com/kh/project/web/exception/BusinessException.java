package com.kh.project.web.exception;

import com.kh.project.web.api.ApiResponseCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {
  private final ApiResponseCode responseCode;
  private final Map<String, String> details = new HashMap<>();

  // 기존 생성자들
  public BusinessException(ApiResponseCode responseCode) {
    super(responseCode.getRtmsg());
    this.responseCode = responseCode;
  }

  public BusinessException(ApiResponseCode responseCode, String message) {
    super(message);
    this.responseCode = responseCode;
  }

  // 🔧 추가: String 메시지만으로 생성할 수 있는 생성자 (기본 BUSINESS_ERROR 사용)
  public BusinessException(String message) {
    super(message);
    this.responseCode = ApiResponseCode.BUSINESS_ERROR;
  }

  /**
   * 필드별 오류 정보 추가
   * @param field 필드명
   * @param message 오류 메시지
   * @return 메서드 체이닝을 위한 자기 자신 반환
   */
  public BusinessException addFieldError(String field, String message) {
    this.details.put(field, message);
    return this;
  }

  /**
   * 전역 오류 정보 추가
   * @param message 전역 오류 메시지
   * @return 메서드 체이닝을 위한 자기 자신 반환
   */
  public BusinessException addGlobalError(String message) {
    this.details.put("global", message);
    return this;
  }

  /**
   * 상세 정보가 있는지 확인
   * @return 상세 정보 존재 여부
   */
  public boolean hasDetails() {
    return !details.isEmpty();
  }
}