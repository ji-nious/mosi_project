package com.kh.project.config;

import com.kh.project.web.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 웹 설정
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final LoginCheckInterceptor loginCheckInterceptor;

  // 로그인 체크 인터셉터 등록
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 화이트리스트 전략
    registry.addInterceptor(loginCheckInterceptor)
        .order(2)
        .excludePathPatterns("/**")   // 루트부터 하위경로 모두 인터셉터 대상으로 미포함
        .addPathPatterns(
            "/products/**"           // 게시판(SSR)
        );
  }

  // 정적 리소스 핸들러 설정
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
} 