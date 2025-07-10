package com.kh.project.web;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 공통 컨트롤러 (로그인/로그아웃/홈)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CommonController {

  /**
   * 메인 페이지
   */
  @GetMapping("/")
  public String home() {
    log.info("메인 페이지 요청");
    return "home";
  }

  /**
   * 공통 로그인 선택 페이지 (구매자/판매자 선택)
   */
  @GetMapping("/login")
  public String loginSelect() {
    log.info("로그인 유형 선택 페이지 요청");
    return "common/select_login";
  }

  /**
   * 회원가입 유형 선택 페이지 (구매자/판매자 선택)
   */
  @GetMapping("/signup")
  public String signupSelect() {
    log.info("회원가입 유형 선택 페이지 요청");
    return "common/select_signup";
  }

  /**
   * 로그아웃 처리 (GET/POST 모두 지원)
   */
  @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
  public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
    try {
      if (session != null) {
        Object loginMember = session.getAttribute("loginMember");
        session.invalidate();

        if (loginMember != null) {
          log.info("로그아웃 처리 완료 - 세션 무효화");
          redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");
        } else {
          log.info("이미 로그아웃된 상태");
          redirectAttributes.addFlashAttribute("message", "이미 로그아웃된 상태입니다.");
        }
      }

      return "redirect:/";

    } catch (Exception e) {
      log.error("로그아웃 처리 중 오류 발생", e);
      redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
      return "redirect:/";
    }
  }

  @GetMapping("/common/juso-popup")
  public String jusoPopup() {
    log.info("주소 검색 팝업 요청");
    return "common/juso_popup";
  }
}