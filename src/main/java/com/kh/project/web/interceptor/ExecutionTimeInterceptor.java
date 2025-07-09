package com.kh.project.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 강화된 성능 모니터링 인터셉터
 * 팀 프로젝트 전체에서 사용할 수 있는 상세한 성능 분석 제공
 */
@Slf4j
@Component
public class ExecutionTimeInterceptor implements HandlerInterceptor {
  
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    long startTime = System.currentTimeMillis();
    request.setAttribute("startTime", startTime);
    
    // 요청 시작 로깅 (개발환경에서만)
    if (log.isDebugEnabled()) {
      String userAgent = request.getHeader("User-Agent");
      String clientIP = getClientIP(request);
      
      log.debug("요청 시작: {} {} | IP: {} | Agent: {}", 
          request.getMethod(), 
          request.getRequestURI(), 
          clientIP,
          userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "Unknown"
      );
    }
    
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    long startTime = (Long) request.getAttribute("startTime");
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;

    if(handler instanceof HandlerMethod) {
      HandlerMethod handlerMethod = (HandlerMethod) handler;
      String className = handlerMethod.getBeanType().getSimpleName();
      String methodName = handlerMethod.getMethod().getName();
      String requestMethod = request.getMethod();
      String requestURI = request.getRequestURI();
      
      // 성능 등급 분류
      String performanceGrade = getPerformanceGrade(duration);
      String logMessage = String.format(
          "%s %s-%s : %s.%s() = %dms [%s]",
          performanceGrade, requestMethod, requestURI, className, methodName, duration, getResponseStatus(response)
      );
      
      // 성능에 따른 로그 레벨 차등화
      if (duration > 1000) {
        log.warn("SLOW " + logMessage + " - 성능 개선 필요!");
      } else if (duration > 500) {
        log.info("WARN " + logMessage + " - 주의 필요");
      } else {
        log.info("OK " + logMessage);
      }
      
      // 세션 및 보안 관련 추가 모니터링
      if (requestURI.contains("/login") || requestURI.contains("/api/")) {
        logSecurityMetrics(request, duration);
      }
    }
  }
  
  /**
   * 성능 등급 분류
   */
  private String getPerformanceGrade(long duration) {
    if (duration < 100) return "FAST";
    if (duration < 300) return "GOOD";
    if (duration < 500) return "SLOW";
    if (duration < 1000) return "WARN";
    return "CRITICAL";
  }
  
  /**
   * 응답 상태 정보
   */
  private String getResponseStatus(HttpServletResponse response) {
    int status = response.getStatus();
    if (status >= 200 && status < 300) return "SUCCESS";
    if (status >= 400 && status < 500) return "CLIENT_ERROR";
    if (status >= 500) return "SERVER_ERROR";
    return "OTHER";
  }
  
  /**
   * 클라이언트 IP 추출 (프록시 고려)
   */
  private String getClientIP(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    
    String xRealIP = request.getHeader("X-Real-IP");
    if (xRealIP != null && !xRealIP.isEmpty()) {
      return xRealIP;
    }
    
    return request.getRemoteAddr();
  }
  
  /**
   * 보안 관련 메트릭 로깅
   */
  private void logSecurityMetrics(HttpServletRequest request, long duration) {
    if (log.isDebugEnabled()) {
      String sessionId = request.getSession(false) != null ? 
          request.getSession().getId().substring(0, 8) + "..." : "NO_SESSION";
      
      log.debug("보안 메트릭: URI={} | 세션={} | 처리시간={}ms | IP={}", 
          request.getRequestURI(),
          sessionId,
          duration,
          getClientIP(request)
      );
    }
  }
}
