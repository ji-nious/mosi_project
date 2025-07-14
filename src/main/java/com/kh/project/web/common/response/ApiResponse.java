package com.kh.project.web.common.response;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

/**
 * REST API 응답메세지 표준 구현
 *
 */
@Slf4j
@Getter
@ToString
public class ApiResponse<T> {
  private final Header header;    //응답헤더
  private final T body;           //응답바디
  private final Paging paging;    //페이지정보

  //페이지 정보가 미포함된 표준 응답메세지 생성
  private ApiResponse(Header header, T body) {
    this.header = header;
    this.body = body;
    this.paging = null;
  }

  //페이지 정보가 포함된 표준 응답메세지 생성
  private ApiResponse(Header header, T body, Paging paging) {
    this.header = header;
    this.body = body;
    this.paging = paging;
  }

  // 1. 기본 헤더 (details가 없는 경우)
  @Getter
  @ToString
  private static class Header {
    private final String rtcd;      //응답코드
    private final String rtmsg;     //응답메시지
    private final Map<String, String> details;  //상세 메세지

    Header(String rtcd, String rtmsg, Map<String, String> details) {
      this.rtcd = rtcd;
      this.rtmsg = rtmsg;
      this.details = details;
    }
  }

  @Getter
  @ToString
  public static class Paging {
    private int numOfRows;    //레코드건수
    private int pageNo;       //요청페이지
    private int totalCount;   //총건수

    public Paging(int pageNo, int numOfRows,  int totalCount) {
      this.pageNo = pageNo;
      this.numOfRows = numOfRows;
      this.totalCount = totalCount;
    }
  }

  // API 응답 생성 메소드-상세 오류 미포함
  public static <T> ApiResponse<T> of(ApiResponseCode responseCode, T body) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), null), body);
  }

  public static <T> ApiResponse<T> of(ApiResponseCode responseCode, T body, Paging paging) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), null), body, paging);
  }

  // API 응답 생성 메소드-상세 오류 포함
  public static <T> ApiResponse<T> withDetails(ApiResponseCode responseCode, Map<String, String> details, T body) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), details), body);
  }

  public static <T> ApiResponse<T> withDetails(ApiResponseCode responseCode, Map<String, String> details, T body, Paging paging) {
    return new ApiResponse<>(new Header(responseCode.getRtcd(), responseCode.getRtmsg(), details), body, paging);
  }

  // ============== 현재 프로젝트에서 꼭 필요한 편의 메소드들 ==============
  
  /**
   * 성공 응답 생성
   */
  public static Map<String, Object> success(String message) {
    return success(message, null);
  }
  
  /**
   * 성공 응답 생성 (데이터 포함)
   */
  public static Map<String, Object> success(String message, Object data) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", message);
    response.put("timestamp", java.time.LocalDateTime.now().toString());
    
    if (data != null) {
      response.put("data", data);
    }
    
    log.debug("API 성공 응답: {}", message);
    return response;
  }
  
  /**
   * 실패 응답 생성
   */
  public static Map<String, Object> error(String message) {
    return error(message, null);
  }
  
  /**
   * 실패 응답 생성 (에러 데이터 포함)
   */
  public static Map<String, Object> error(String message, Object errorData) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("message", message);
    response.put("timestamp", java.time.LocalDateTime.now().toString());
    
    if (errorData != null) {
      response.put("error", errorData);
    }
    
    log.debug("API 실패 응답: {}", message);
    return response;
  }
  
  /**
   * 회원가입 성공 응답
   */
  public static Map<String, Object> joinSuccess(Object member, String memberType) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "회원가입이 완료되었습니다.");
    response.put("data", member);
    response.put("memberType", memberType);
    response.put("timestamp", java.time.LocalDateTime.now().toString());
    
    log.debug("회원가입 성공 응답: {}", memberType);
    return response;
  }
  
  /**
   * 로그인 성공 응답
   */
  public static Map<String, Object> loginSuccess(Object member, String memberType, boolean canLogin) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "로그인이 완료되었습니다.");
    response.put("data", member);
    response.put("memberType", memberType);
    response.put("canLogin", canLogin);
    response.put("timestamp", java.time.LocalDateTime.now().toString());
    
    log.debug("로그인 성공 응답: {}", memberType);
    return response;
  }
  
  /**
   * 엔티티 성공 응답
   */
  public static Map<String, Object> entitySuccess(String message, Object entity, Object additionalData) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", message);
    response.put("data", entity);
    response.put("timestamp", java.time.LocalDateTime.now().toString());
    
    if (additionalData != null) {
      response.put("additional", additionalData);
    }
    
    log.debug("엔티티 성공 응답: {}", message);
    return response;
  }
}
