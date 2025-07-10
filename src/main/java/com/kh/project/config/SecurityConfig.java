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
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /**
   * 비밀번호 인코더 설정
   * - !!주의!! 실제 운영 환경에서는 BCryptPasswordEncoder 등 보안 강도가 높은 인코더를 사용해야 합니다.
   * - 현재는 암호화 없이 비밀번호를 평문으로 비교합니다.
   * @return PasswordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CORS 설정 적용
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        // CSRF 비활성화
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스(CSS, JS, 이미지 등)는 인증 없이 항상 허용
            .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()
            
            // API, 주소검색 등 기능적인 경로는 인증 없이 허용
            .requestMatchers("/api/**", "/common/**", "/juso_popup").permitAll()
            
            // 로그인/회원가입 관련 페이지는 인증 없이 허용
            .requestMatchers("/login", "/signup", "/buyer/login", "/buyer/signup", "/seller/login", "/seller/signup").permitAll()
            
            // 메인 페이지는 인증 없이 허용
            .requestMatchers("/").permitAll()
            
            // 위에서 지정한 경로 외의 모든 요청은 반드시 인증을 거쳐야 함
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            // 로그인 페이지는 /login (CommonController가 처리)
            .loginPage("/login")
            .defaultSuccessUrl("/", true) // 로그인 성공 시 항상 메인 페이지로 리디렉션
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll());

    return http.build();
  }

  /**
   * CORS(Cross-Origin Resource Sharing) 설정
   * @return
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*")); // 모든 Origin 허용
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", configuration); // /api/ 경로에만 적용
    return source;
  }
}