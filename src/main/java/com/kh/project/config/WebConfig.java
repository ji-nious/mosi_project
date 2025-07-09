package com.kh.project.config;

import com.kh.project.web.interceptor.ExecutionTimeInterceptor;
import com.kh.project.web.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Spring MVC 웹 설정
 */
@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  private final ExecutionTimeInterceptor executionTimeInterceptor;
  private final LoginCheckInterceptor loginCheckInterceptor;

  /**
   * 인터셉터 등록
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 실행 시간 측정 (모든 요청)
    registry.addInterceptor(executionTimeInterceptor)
        .addPathPatterns("/**");

    // 로그인 체크 인터셉터 활성화
    registry.addInterceptor(loginCheckInterceptor)
        // 인증 필요한 페이지 패턴
        .addPathPatterns("/buyer/mypage", "/buyer/update", "/buyer/withdraw")
        .addPathPatterns("/seller/mypage", "/seller/update", "/seller/withdraw")
        
        // 인증 필요한 API 패턴  
        .addPathPatterns("/api/buyers/info", "/api/buyers/update", "/api/buyers/withdraw")
        .addPathPatterns("/api/sellers/info", "/api/sellers/update", "/api/sellers/withdraw")
        
        // 제외 패턴 (공개 API)
        .excludePathPatterns("/api/buyers/join", "/api/buyers/login", "/api/buyers/check-*")
        .excludePathPatterns("/api/sellers/join", "/api/sellers/login", "/api/sellers/check-*")
        
        // 제외 패턴 (정적 리소스)
        .excludePathPatterns("/", "/login", "/signup", "/logout")
        .excludePathPatterns("/css/**", "/js/**", "/images/**", "/static/**");
  }

  /**
   * 정적 리소스 설정 (캐시 포함)
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // CSS, JS 파일 - 1시간 캐시
    registry.addResourceHandler("/css/**")
        .addResourceLocations("classpath:/static/css/")
        .setCachePeriod(3600);

    registry.addResourceHandler("/js/**")
        .addResourceLocations("classpath:/static/js/")
        .setCachePeriod(3600);

    // 이미지 파일 - 24시간 캐시
    registry.addResourceHandler("/images/**")
        .addResourceLocations("classpath:/static/images/")
        .setCachePeriod(86400);

    registry.addResourceHandler("/favicon.ico")
        .addResourceLocations("classpath:/static/images/")
        .setCachePeriod(86400);
  }

  /**
   * 단순 페이지 이동용 뷰 컨트롤러
   */
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    // /login은 HomeController가 처리하므로 주석 처리
    // registry.addViewController("/login").setViewName("common/select_login");
    registry.addViewController("/signup").setViewName("common/select_signup");
    registry.addViewController("/error/403").setViewName("error/403");
    registry.addViewController("/error/404").setViewName("error/404");
    registry.addViewController("/error/500").setViewName("error/500");
  }

  /**
   * API용 CORS 설정
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600);
  }
}