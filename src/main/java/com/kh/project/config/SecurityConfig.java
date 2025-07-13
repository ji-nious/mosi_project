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
 * 보안 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  /**
   * 비밀번호 인코더 설정
   */
  @Bean
  @SuppressWarnings("deprecation")
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  /**
   * Security 필터 체인 설정
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/home", 
                "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico"
            ).permitAll()
            .requestMatchers(
                "/login", "/signup",
                "/common/select_login", "/common/select-login",
                "/common/select_signup", "/common/signup-complete",
                "/common/juso-popup", "/error/**"
            ).permitAll()
            .requestMatchers("/buyer/**").permitAll()
            .requestMatchers("/seller/**").permitAll()
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
            .clearAuthentication(true)
            .permitAll()
        )
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()));

    return http.build();
  }

  /**
   * CORS 설정
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}