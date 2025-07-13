package com.kh.project.web.interceptor;

import com.kh.project.web.common.LoginMember;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 체크 인터셉터
 */
@Slf4j
@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        log.info("인증 체크 인터셉터 실행: {}", requestURI);

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loginMember") == null) {
            log.info("미인증 사용자 요청: {}", requestURI);
            response.sendRedirect("/login");
            return false;
        }

        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        String memberType = loginMember.getMemberType();

        log.info("인증된 사용자 요청: {}, 회원타입: {}", requestURI, memberType);
        return true;
    }
}
