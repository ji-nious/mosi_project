package com.kh.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 설정
 * 역할 분담:
 * - SecurityConfig: 기본 보안 설정 (CSRF, 로그아웃 등)
 * - LoginCheckInterceptor: 세션 기반 인증 처리
 * - 개발 단계에서는 모든 요청을 허용하고,
 * - 인증은 LoginCheckInterceptor가 담당.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * 비밀번호 암호화를 위한 PasswordEncoder
   * BCrypt 해싱 알고리즘을 사용하여 비밀번호를 안전하게 저장합니다.
   *
   * @return BCryptPasswordEncoder 인스턴스
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * HTTP 보안 필터 체인 설정
   *
   * 현재는 개발 단계로 모든 요청을 허용하고,
   * 실제 인증은 LoginCheckInterceptor에서 처리합니다.
   *
   * @param http HttpSecurity 설정 객체
   * @return 구성된 SecurityFilterChain
   * @throws Exception 설정 중 발생하는 예외
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 요청 권한 설정 - 현재는 모든 요청 허용
        .authorizeHttpRequests(authorize -> authorize
            // TODO: 운영 환경에서는 아래와 같이 세밀한 권한 설정 권장
            // .requestMatchers("/", "/home", "/login", "/signup").permitAll()
            // .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
            // .requestMatchers("/api/**").permitAll()
            // .requestMatchers("/buyer/**").hasRole("BUYER")
            // .requestMatchers("/seller/**").hasRole("SELLER")
            // .anyRequest().authenticated()

            .anyRequest().permitAll()  // 현재: 모든 요청 허용 (인터셉터가 인증 담당)
        )

        // Spring Security 기본 로그인 폼 비활성화
        // 직접 구현한 로그인 페이지를 사용하기 때문
        .formLogin(AbstractHttpConfigurer::disable)

        // 로그아웃 설정
        .logout(logout -> logout
            .logoutUrl("/logout")           // 로그아웃 처리 URL
            .logoutSuccessUrl("/")          // 로그아웃 성공 시 리다이렉트 URL
            .invalidateHttpSession(true)    // 세션 무효화
            .deleteCookies("JSESSIONID")    // 세션 쿠키 삭제
            .permitAll()
        )

        // CSRF(Cross-Site Request Forgery) 보호 비활성화
        // API 통신이나 Ajax 요청이 많은 경우 비활성화
        // 필요시 .csrf(csrf -> csrf.csrfTokenRepository(...)) 로 활성화 가능
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }
}