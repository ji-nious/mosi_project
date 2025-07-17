package com.kh.project.web.common.response;

import com.kh.project.web.api.ApiResponseCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * REST API 응답메세지 표준 구현 - 최종수정반영
 */
@Slf4j
@Getter
@ToString
public class ApiResponse<T> {
  private final Header header;    // 응답헤더
  private final T body;           // 응답바디
  private final Paging paging;    // 페이지정보

  // 페이지 정보가 미포함된 표준 응답메세지 생성
  private ApiResponse(Header header, T body) {
    this.header = header;
    this.body = body;
    this.paging = null;
  }

  // 페이지 정보가 포함된 표준 응답메세지 생성
  private ApiResponse(Header header, T body, Paging paging) {
    this.header = header;
    this.body = body;
    this.paging = paging;
  }

  // 응답 헤더 클래스
  @Getter
  @ToString
  private static class Header {
    private final String rtcd;      // 응답코드
    private final String rtmsg;     // 응답메시지
    private final Map<String, String> details;  // 상세 메세지

    Header(String rtcd, String rtmsg, Map<String, String> details) {
      this.rtcd = rtcd;
      this.rtmsg = rtmsg;
      this.details = details;
    }
  }

  // 페이징 정보 클래스
  @Getter
  @ToString
  public static class Paging {
    private final int numOfRows;    // 레코드건수
    private final int pageNo;       // 요청페이지
    private final int totalCount;   // 총건수

    public Paging(int pageNo, int numOfRows, int totalCount) {
      this.pageNo = pageNo;
      this.numOfRows = numOfRows;
      this.totalCount = totalCount;
    }
  }

  /**
   * 기본 응답 생성 (상세 오류 미포함)
   */
  public static <T> ApiResponse<T> of(ApiResponseCode responseCode, T body) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), null), body);
  }

  /**
   * 페이징 포함 응답 생성
   */
  public static <T> ApiResponse<T> of(ApiResponseCode responseCode, T body, Paging paging) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), null), body, paging);
  }

  /**
   * 상세 오류 정보 포함 응답 생성
   */
  public static <T> ApiResponse<T> withDetails(ApiResponseCode responseCode, Map<String, String> details, T body) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), details), body);
  }

  /**
   * 상세 오류 정보 및 페이징 포함 응답 생성
   */
  public static <T> ApiResponse<T> withDetails(ApiResponseCode responseCode, Map<String, String> details, T body, Paging paging) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), details), body, paging);
  }

  // ============ 편의 메서드들 (간소화) ============

  /**
   * 성공 응답 (기본 메시지)
   */
  public static <T> ApiResponse<T> success(T body) {
    return ApiResponse.of(ApiResponseCode.SUCCESS, body);
  }

  /**
   * 성공 응답 (커스텀 메시지)
   */
  public static <T> ApiResponse<T> success(String message, T body) {
    return new ApiResponse<>(new Header(ApiResponseCode.SUCCESS.getRtcd(), message, null), body);
  }

  /**
   * 성공 응답 (페이징 포함)
   */
  public static <T> ApiResponse<T> success(String message, T body, Paging paging) {
    return new ApiResponse<>(new Header(ApiResponseCode.SUCCESS.getRtcd(), message, null), body, paging);
  }

  /**
   * 오류 응답 (ResponseCode 기반)
   */
  public static <T> ApiResponse<T> error(ApiResponseCode responseCode) {
    return ApiResponse.of(responseCode, null);
  }

  /**
   * 오류 응답 (커스텀 메시지)
   */
  public static <T> ApiResponse<T> error(ApiResponseCode responseCode, String message) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), message, null), null);
  }

  /**
   * 오류 응답 (상세 정보 포함)
   */
  public static <T> ApiResponse<T> error(ApiResponseCode responseCode, String message, Map<String, String> details) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), message, details), null);
  }

  /**
   * 검증 오류 응답 (필드별 오류 정보 포함)
   */
  public static <T> ApiResponse<T> validationError(String message, Map<String, String> fieldErrors) {
    return ApiResponse.withDetails(ApiResponseCode.VALIDATION_ERROR, fieldErrors, null);
  }

  /**
   * 비즈니스 로직 오류 응답
   */
  public static <T> ApiResponse<T> businessError(String message) {
    return new ApiResponse<>(new Header(ApiResponseCode.BUSINESS_ERROR.getRtcd(), message, null), null);
  }

  /**
   * 인증 필요 응답
   */
  public static <T> ApiResponse<T> loginRequired(String message) {
    return new ApiResponse<>(new Header(ApiResponseCode.LOGIN_REQUIRED.getRtcd(),
        message != null ? message : "로그인이 필요합니다.", null), null);
  }

  /**
   * 권한 없음 응답
   */
  public static <T> ApiResponse<T> accessDenied(String message) {
    return new ApiResponse<>(new Header(ApiResponseCode.ACCESS_DENIED.getRtcd(),
        message != null ? message : "접근 권한이 없습니다.", null), null);
  }

  /**
   * 서버 오류 응답
   */
  public static <T> ApiResponse<T> serverError(String message) {
    return new ApiResponse<>(new Header(ApiResponseCode.SERVER_ERROR.getRtcd(),
        message != null ? message : "서버 오류가 발생했습니다.", null), null);
  }

  /**
   * 성공 여부 확인
   */
  public boolean isSuccess() {
    return ApiResponseCode.SUCCESS.getRtcd().equals(this.header.rtcd);
  }

  /**
   * 오류 여부 확인
   */
  public boolean isError() {
    return !isSuccess();
  }

  /**
   * 페이징 정보 포함 여부 확인
   */
  public boolean hasPaging() {
    return this.paging != null;
  }

  /**
   * 상세 정보 포함 여부 확인
   */
  public boolean hasDetails() {
    return this.header.details != null && !this.header.details.isEmpty();
  }
}