package com.kh.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * 인증, 인가, CORS 설정 등 보안 관련 설정을 담당
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * 비밀번호 인코더 설정
   * 보안 경고: 현재 평문 저장
   * @return PasswordEncoder - 2차 프로젝트에서 BCryptPasswordEncoder 암호화 예정
   */
  @Bean
  @SuppressWarnings("deprecation")
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  /**
   * Spring Security 필터 체인 설정
   * 
   * @param http HttpSecurity 설정 객체
   * @return SecurityFilterChain - 구성된 보안 필터 체인
   * @throws Exception 설정 오류시
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CORS 설정 적용
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // CSRF 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스는 인증 없이 허용
            .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico", "/webjars/**").permitAll()
            // 에러 페이지 허용
            .requestMatchers("/error").permitAll()
            // API 및 공통 기능 허용
            .requestMatchers("/api/**", "/common/**").permitAll()
            // 로그인/회원가입/로그아웃 페이지 허용
            .requestMatchers("/login", "/signup", "/logout").permitAll()
            .requestMatchers("/buyer/login", "/buyer/signup", "/seller/login", "/seller/signup").permitAll()
            // 구매자/판매자 기능 허용 (자체 세션 인증 사용)
            .requestMatchers("/buyer/**", "/seller/**").permitAll()
            // 메인 페이지 허용
            .requestMatchers("/", "/home").permitAll()
            // 나머지는 인증 필요
            .anyRequest().authenticated()
        )
        .formLogin(AbstractHttpConfigurer::disable)
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll());

    return http.build();
  }

  /**
   * CORS 설정
   * @return CorsConfigurationSource - CORS 설정 소스
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }
}