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
        String requestURI = request.getRequestURI();
        log.debug("로그인 체크 인터셉터 실행: {}", requestURI);

        // 세션 조회 (새 세션 생성하지 않음)
        HttpSession session = request.getSession(false);

        // 로그인하지 않은 경우: 세션이 없거나 loginMember 정보가 없는 경우
        if (session == null || session.getAttribute("loginMember") == null) {
            log.info("미인증 사용자 요청: {}", requestURI);

            // 로그인 후 원래 페이지로 돌아가기 위해 요청 URI 저장
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("redirectURL", requestURI);

            // 로그인 페이지로 리다이렉트
            response.sendRedirect("/login");
            return false;
        }

        log.debug("인증된 사용자 요청: {}", requestURI);
        return true;  // 요청 계속 진행
    }
}