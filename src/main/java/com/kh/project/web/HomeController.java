package com.kh.project.web;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.entity.LoginMember;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final SessionService sessionService;

    /**
     * 홈페이지(메인 화면)
     */
    @GetMapping({"/", "/home"})
    public String home(HttpSession session, Model model) {
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        return "home";
    }

    /**
     * 로그인 선택 페이지
     */
    @GetMapping({"/login", "/common/select-login"})
    public String selectLogin() {
        log.info("로그인 선택 페이지 호출");
        return "common/select_login";
    }

    /**
     * 회원가입 선택 페이지
     */
    @GetMapping({"/signup", "/common/select-signup"})
    public String selectSignup() {
        log.info("회원가입 선택 페이지 호출");
        return "common/select_signup";
    }

    /**
     * 로그아웃 처리
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("로그아웃 처리 시작");

        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember != null) {
            log.info("로그아웃 처리: id={}, type={}",
                loginMember.getId(), loginMember.getMemberType());
        }

        sessionService.logout(session);
        redirectAttributes.addFlashAttribute("success", "로그아웃 되었습니다.");

        return "redirect:/";
    }

    /**
     * 회원가입 완료 페이지
     */
    @GetMapping("/signup-complete")
    public String signupComplete() {
        log.info("회원가입 완료 페이지 호출");
        return "common/signup_complete";
    }

    /**
     * 탈퇴 완료 페이지
     */
    @GetMapping("/withdraw-complete")
    public String withdrawComplete() {
        log.info("탈퇴 완료 페이지 호출");
        return "common/withdraw_complete";
    }

    /**
     * 주소 검색 팝업 페이지
     */
    @GetMapping({"/juso-popup", "/common/juso-popup"})
    public String jusoPopup() {
        log.info("주소 검색 팝업 페이지 호출");
        return "common/juso_popup";
    }

    /**
     * 에러 알림 페이지
     */
    @GetMapping("/error/alert")
    public String errorAlert() {
        log.info("에러 알림 페이지 호출");
        return "error/alert";
    }
}