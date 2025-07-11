package com.kh.project.config;

import com.kh.project.web.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 MVC 설정 클래스
 * 인터셉터, 정적 리소스 등 웹 관련 설정을 담당
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginCheckInterceptor loginCheckInterceptor;

  /**
   * 정적 리소스 핸들러 설정
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");
    registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");
    registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/");
    registry.addResourceHandler("/favicon.ico")
            .addResourceLocations("classpath:/static/images/");
  }

  /**
   * 인터셉터 등록
   * 로그인 체크 인터셉터를 특정 패턴에 적용
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(loginCheckInterceptor)
            .addPathPatterns("/**") // 모든 경로에 적용
            .excludePathPatterns("/", "/api/**", "/login", "/signup", "/buyer/**", "/seller/**", 
                               "/css/**", "/js/**", "/images/**", "/common/**", "/favicon.ico", "/error"); // 제외 경로
  }
} 