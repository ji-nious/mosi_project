package com.kh.project.config;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Spring Security 설정 클래스
 * 인증, 인가, CORS 설정 등 보안 관련 설정을 담당
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
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
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스 및 공통 페이지
            .requestMatchers(
                "/", "/home", 
                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
            ).permitAll()
            // 회원가입 및 로그인 관련
            .requestMatchers(
                "/login", "/signup",
                "/common/select_login", "/common/select-login",
                "/common/select_signup",
                "/common/juso-popup", "/error/**"
            ).permitAll()
            // 구매자 관련
            .requestMatchers(
                "/buyer/login",
                "/buyer/signup", "/buyer/join"
            ).permitAll()
            // 판매자 관련
            .requestMatchers(
                "/seller/login",
                "/seller/signup", "/seller/join"
            ).permitAll()
            // API 경로
            .requestMatchers("/api/**").permitAll()
            .anyRequest().authenticated() 
        )
        .formLogin(form -> form
            .loginPage("/common/select_login") 
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        )
        .csrf(csrf -> csrf.disable()); 
            
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