package com.kh.project.web.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.exceptions.TemplateInputException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 표준화된 에러 메시지 매핑
  private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();
  
  static {
    ERROR_MESSAGES.put("TemplateInputException", "페이지를 불러오는 중 문제가 발생했습니다.");
    ERROR_MESSAGES.put("NullPointerException", "데이터 처리 중 오류가 발생했습니다.");
    ERROR_MESSAGES.put("ValidationException", "입력하신 정보를 다시 확인해 주세요.");
    ERROR_MESSAGES.put("AccessDeniedException", "해당 기능에 접근할 권한이 없습니다.");
    ERROR_MESSAGES.put("DataAccessException", "데이터베이스 연결에 문제가 있습니다.");
    ERROR_MESSAGES.put("BusinessException", "요청을 처리할 수 없습니다.");
    ERROR_MESSAGES.put("IllegalArgumentException", "잘못된 요청입니다.");
    ERROR_MESSAGES.put("default", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
  }

  /**
   * 템플릿 입력 예외 처리 (Thymeleaf 관련)
   */
  @ExceptionHandler(TemplateInputException.class)
  public ModelAndView handleTemplateInputException(TemplateInputException e, HttpServletRequest request) {
    log.error("템플릿 처리 오류 발생: {}", e.getMessage());
    
    return createErrorModelAndView(
      ERROR_MESSAGES.get("TemplateInputException"),
      "템플릿 파일에 문제가 있거나 존재하지 않습니다: " + e.getMessage(),
      request
    );
  }

  /**
   * 비즈니스 예외 처리
   */
  @ExceptionHandler(BusinessException.class)
  public ModelAndView handleBusinessException(BusinessException e, HttpServletRequest request) {
    log.warn("비즈니스 예외 발생: {}", e.getMessage());
    
    return createErrorModelAndView(
      e.getMessage(), // 비즈니스 예외는 사용자에게 직접 노출 가능한 메시지
      e.getMessage(),
      request
    );
  }

  /**
   * NullPointer 예외 처리
   */
  @ExceptionHandler(NullPointerException.class)
  public ModelAndView handleNullPointerException(NullPointerException e, HttpServletRequest request) {
    log.error("NullPointer 예외 발생", e);
    
    return createErrorModelAndView(
      ERROR_MESSAGES.get("NullPointerException"),
      "NullPointerException: " + e.getMessage(),
      request
    );
  }

  /**
   * IllegalArgument 예외 처리
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ModelAndView handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
    log.warn("잘못된 인자 예외 발생: {}", e.getMessage());
    
    return createErrorModelAndView(
      ERROR_MESSAGES.get("IllegalArgumentException"),
      "IllegalArgumentException: " + e.getMessage(),
      request
    );
  }

  /**
   * MethodArgumentTypeMismatch 예외 처리 (URL 파라미터 타입 변환 실패)
   */
  @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
  public ModelAndView handleMethodArgumentTypeMismatchException(
      org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e, 
      HttpServletRequest request) {
    log.warn("URL 파라미터 타입 변환 실패: parameter={}, value={}, requiredType={}", 
             e.getName(), e.getValue(), e.getRequiredType().getSimpleName());
    
    return createErrorModelAndView(
      "부적절한 요청입니다.",
      "MethodArgumentTypeMismatchException: " + e.getMessage(),
      request
    );
  }

  /**
   * 일반 예외 처리 (최종 catch-all)
   */
  @ExceptionHandler(Exception.class)
  public ModelAndView handleException(Exception e, HttpServletRequest request) {
    log.error("예상치 못한 서버 오류 발생", e);
    
    String userMessage = ERROR_MESSAGES.getOrDefault(
      e.getClass().getSimpleName(), 
      ERROR_MESSAGES.get("default")
    );
    
    return createErrorModelAndView(
      userMessage,
      e.getClass().getSimpleName() + ": " + e.getMessage(),
      request
    );
  }

  /**
   * 에러 ModelAndView 생성 유틸리티 메서드
   */
  private ModelAndView createErrorModelAndView(String userMessage, String technicalMessage, HttpServletRequest request) {
    ModelAndView modelAndView = new ModelAndView();
    modelAndView.setViewName("error/alert");
    modelAndView.addObject("errorMessage", technicalMessage); // 개발자용 상세 메시지
    modelAndView.addObject("userMessage", userMessage); // 사용자용 친화적 메시지
    
    // 적절한 리다이렉트 URL 결정
    String redirectUrl = determineRedirectUrl(request);
    modelAndView.addObject("redirectUrl", redirectUrl);
    
    return modelAndView;
  }

  /**
   * 적절한 리다이렉트 URL 결정
   */
  private String determineRedirectUrl(HttpServletRequest request) {
    String referer = request.getHeader("Referer");
    String requestUri = request.getRequestURI();
    
    // 로그인 관련 페이지에서 오류 발생시 메인으로
    if (requestUri != null && (requestUri.contains("/login") || requestUri.contains("/signup"))) {
      return "/";
    }
    
    // 관리자 페이지에서 오류 발생시 대시보드로
    if (requestUri != null && requestUri.contains("/admin")) {
      return "/admin/dashboard";
    }
    
    // 판매자 페이지에서 오류 발생시 판매자 대시보드로
    if (requestUri != null && requestUri.contains("/seller")) {
      return "/seller/dashboard";
    }
    
    // 구매자 페이지에서 오류 발생시 메인으로
    if (requestUri != null && requestUri.contains("/buyer")) {
      return "/";
    }
    
    // 기본적으로 이전 페이지 또는 메인 페이지
    return referer != null ? referer : "/";
  }
}

/**
 * 커스텀 에러 컨트롤러 (HTTP 상태 코드 기반 에러 처리)
 */
@Slf4j
@Controller
class CustomErrorController implements ErrorController {

  // HTTP 상태 코드별 사용자 친화적 메시지
  private static final Map<Integer, String> STATUS_MESSAGES = new HashMap<>();
  
  static {
    STATUS_MESSAGES.put(400, "잘못된 요청입니다. 입력 내용을 확인해 주세요.");
    STATUS_MESSAGES.put(401, "로그인이 필요합니다.");
    STATUS_MESSAGES.put(403, "해당 페이지에 접근할 권한이 없습니다.");
    STATUS_MESSAGES.put(404, "요청하신 페이지를 찾을 수 없습니다.");
    STATUS_MESSAGES.put(405, "허용되지 않은 요청 방식입니다.");
    STATUS_MESSAGES.put(500, "서버에 일시적인 문제가 발생했습니다.");
    STATUS_MESSAGES.put(502, "서버 연결에 문제가 있습니다.");
    STATUS_MESSAGES.put(503, "서비스를 일시적으로 사용할 수 없습니다.");
  }

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
    
    String userMessage = "알 수 없는 오류가 발생했습니다.";
    String technicalMessage = "";
    
    if (status != null) {
      int statusCode = Integer.parseInt(status.toString());
      userMessage = STATUS_MESSAGES.getOrDefault(statusCode, 
        "오류가 발생했습니다. (상태 코드: " + statusCode + ")");
      technicalMessage = "HTTP " + statusCode + " Error";
      
      log.error("HTTP 에러 발생: 상태코드={}, URI={}", statusCode, request.getRequestURI());
    }
    
    if (exception != null && exception instanceof Exception) {
      Exception ex = (Exception) exception;
      technicalMessage += " - " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
      log.error("에러 상세 정보", ex);
    }
    
    model.addAttribute("errorMessage", technicalMessage);
    model.addAttribute("userMessage", userMessage);
    model.addAttribute("redirectUrl", determineRedirectUrl(request));
    
    return "error/alert";
  }

  /**
   * 적절한 리다이렉트 URL 결정 (GlobalExceptionHandler와 동일한 로직)
   */
  private String determineRedirectUrl(HttpServletRequest request) {
    String referer = request.getHeader("Referer");
    String requestUri = request.getRequestURI();
    
    if (requestUri != null && (requestUri.contains("/login") || requestUri.contains("/signup"))) {
      return "/";
    }
    
    if (requestUri != null && requestUri.contains("/admin")) {
      return "/admin/dashboard";
    }
    
    if (requestUri != null && requestUri.contains("/seller")) {
      return "/seller/dashboard";
    }
    
    if (requestUri != null && requestUri.contains("/buyer")) {
      return "/";
    }
    
    return referer != null ? referer : "/";
  }
}
