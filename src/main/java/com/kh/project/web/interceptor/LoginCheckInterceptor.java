package com.kh.project.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    //리다이렉트 Url
    String redirectUrl = null;

    log.info("handler={}", handler.getClass());
    //요청 URI    ex) GET http://localhost:9080/products?a=1&b=2 상품관리

    String requestURI = request.getRequestURI();    //   /products
//    log.info("requestURI={}",request.getRequestURI());   // /products
//    log.info("queryString="+request.getQueryString());   // a=1&b=2
//    log.info("queryString="+request.getRequestURL());   // http://localhost:9080/products
//    log.info("queryString="+request.getMethod());   // GET,POST

    //세션조회
    HttpSession session = request.getSession(false);

    // 로그인전 : 세션이 없거나 loginMember정보가 없는경우 로그인 화면으로 리다이렉트
    if(session == null || session.getAttribute("loginMember") == null){
      log.info("로그인되지 않은 사용자의 접근: {}", requestURI);
      response.sendRedirect("/login");   // 302 GET http://localhost:9080/login
      return false; // 요청 처리 중단
    }
    
    log.info("로그인된 사용자의 접근 허용: {}", requestURI);
    return true;
  }
}
