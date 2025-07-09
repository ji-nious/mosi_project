package com.kh.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            // 완전 공개 페이지
            .requestMatchers("/", "/home", "/index.html").permitAll()
            .requestMatchers("/login", "/signup", "/logout").permitAll()
            .requestMatchers("/common/**").permitAll()
            
            // 회원가입/로그인 관련 공개 API
            .requestMatchers("/api/buyers/join", "/api/buyers/login").permitAll()
            .requestMatchers("/api/sellers/join", "/api/sellers/login").permitAll()
            .requestMatchers("/api/buyers/check-*", "/api/sellers/check-*").permitAll()
            
            // 인증 필요한 페이지 (인터셉터와 이중 보안)
            .requestMatchers("/buyer/mypage", "/buyer/update", "/buyer/withdraw").authenticated()
            .requestMatchers("/seller/mypage", "/seller/update", "/seller/withdraw").authenticated()
            
            // 인증 필요한 API (인터셉터와 이중 보안)
            .requestMatchers("/api/buyers/info", "/api/buyers/update", "/api/buyers/withdraw").authenticated()
            .requestMatchers("/api/sellers/info", "/api/sellers/update", "/api/sellers/withdraw").authenticated()
            
            // 관리자 전용
            .requestMatchers("/admin/**").hasRole("ADMIN")
            
            // 정적 리소스
            .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
            .requestMatchers("/webjars/**", "/resources/**").permitAll()
            
            // 나머지 요청은 인터셉터에서 판단
            .anyRequest().permitAll()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers("/api/**") // API는 CSRF 비활성화
        )
        // Spring Security 6.x 새로운 헤더 설정 방식
        .headers(headers -> headers
            .frameOptions(frameOptions -> frameOptions.deny())
            .contentTypeOptions(Customizer.withDefaults())
            .httpStrictTransportSecurity(hsts -> hsts
                .maxAgeInSeconds(31536000)
                .includeSubDomains(true)
            )
        );

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }
}