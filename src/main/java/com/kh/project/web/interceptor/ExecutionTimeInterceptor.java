package com.kh.project.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class ExecutionTimeInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;   // ms  : 1/1000 초

        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String className = handlerMethod.getBeanType().getSimpleName();  //컨트롤러 이름 추출
            String methodName = handlerMethod.getMethod().getName();        // 컨트롤러내의 메소드 추출

            String requestMethod = request.getMethod();             // 요청 method (get,post...)
            StringBuffer requestURL = request.getRequestURL();      // 요청 url

            //소요시간 로그 출력
            log.info("{}-{} : {}.{}() = {}ms",requestMethod, requestURL.toString(), className, methodName, duration );
        }

    }
}
