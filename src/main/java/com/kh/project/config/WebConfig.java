package com.kh.project.config;

import com.kh.project.web.interceptor.ExecutionTimeInterceptor;
import com.kh.project.web.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 웹 설정
 * 인터셉터, CORS, 정적 리소스 등을 설정함.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginCheckInterceptor loginCheckInterceptor;
  private final ExecutionTimeInterceptor executionTimeInterceptor;

  /**
   * 인터셉터 등록
   * order가 낮을수록 먼저 실행됩니다.
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {

    // 1. 실행시간 측정 인터셉터 (모든 요청 대상)
    registry.addInterceptor(executionTimeInterceptor)
        .order(1)
        .addPathPatterns("/**");

    // 2. 로그인 체크 인터셉터 (블랙리스트 전략)
    registry.addInterceptor(loginCheckInterceptor)
        .order(2)
        .addPathPatterns("/**")  // 모든 경로에 적용
        .excludePathPatterns(    // 제외할 경로들
            // 메인 페이지
            "/", "/home",

            // 인증 관련 페이지  // 로그인/회원가입 페이지 추가
            "/login", "/signup", "/logout",
            "/buyer/login", "/buyer/signup",
            "/seller/login", "/seller/signup",

            // 공통 페이지
            "/common/**", "/error/**",

            // 정적 리소스
            "/css/**", "/js/**", "/images/**",
            "/webjars/**", "/favicon.ico",

            // API (별도 인증 방식)
            "/api/**",

            // 회원가입 완료, 탈퇴 완료 등
            "/withdraw-complete",
            "/signup-complete",
            "/juso-popup",          //  주소 검색 팝업 추가
            "/common/juso-popup"    //  주소 검색 팝업 추가
        );
  }

  /**
   * CORS 설정 (Cross-Origin Resource Sharing)
   * 프론트엔드와 API 통신을 위한 설정
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOriginPatterns("*")  // 개발환경에서는 모든 origin 허용
        // 또는 구체적으로 허용할 도메인들 지정
        // .allowedOrigins(
        //     "http://localhost:9080",    // 현재 서버
        //     "http://localhost:3000",    // React 개발 서버
        //     "http://localhost:5173"     // Vite 개발 서버
        // )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);  // preflight 요청 캐시 시간 (1시간)
  }

  /**
   * 정적 리소스 핸들러 설정
   * CSS, JS, 이미지 등의 정적 파일 서빙
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .setCachePeriod(3600);  // 캐시 기간 설정 (1시간)
  }
}