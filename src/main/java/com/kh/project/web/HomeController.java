package com.kh.project.web;

import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.entity.MemberType;
import com.kh.project.domain.SessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    private final SessionService sessionService;

    // Enum 기반 상수 사용 - MemberType 일관성 수정
    private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
    private static final String MEMBER_TYPE_SELLER = MemberType.SELLER.getCode();

    /**
     * 홈페이지 - 루트 경로 (메인 화면)
     */
    @GetMapping("/")
    public String home(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 이미 로그인된 사용자 체크
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);

        // 성공 메시지가 있는 경우 (로그인 완료 등) 메시지를 보여준 후 리다이렉트
        Object successMessage = model.asMap().get("success");
        Object showLoginModal = model.asMap().get("showLoginModal");
        Object showWithdrawModal = model.asMap().get("showWithdrawModal");

        // 탈퇴 완료 모달 표시 (로그인 상태와 무관)
        if (showWithdrawModal != null && (Boolean) showWithdrawModal) {
            model.addAttribute("showWithdrawModal", true);
        }

        if (successMessage != null) {
            if (showLoginModal != null && (Boolean) showLoginModal && loginMember != null) {
                // 로그인 완료 시 모달 표시 (success 메시지는 설정하지 않음)
                model.addAttribute("showLoginModal", true);
                model.addAttribute("loginMessage", successMessage);
                log.info("로그인 완료 모달 표시: {}", successMessage);
            } else {
                // 기타 성공 메시지 표시 (로그인 모달이 없을 때만)
                model.addAttribute("success", successMessage);
                log.info("성공 메시지 표시: {}", successMessage);
            }
        }

        // 로그인 상태에 따른 메시지 설정
        if (loginMember != null) {
            if (MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
                model.addAttribute("welcomeMessage", "안녕하세요, 구매자님!");
                model.addAttribute("dashboardUrl", "/buyer/info");
            } else if (MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
                model.addAttribute("welcomeMessage", "안녕하세요, 판매자님!");
                model.addAttribute("dashboardUrl", "/seller/dashboard");
            }
        }

        return "home";
    }

    /**
     * 로그인 선택 페이지
     */
    @GetMapping("/login")
    public String selectLogin() {
        log.info("로그인 선택 페이지 호출");
        return "common/select_login";
    }

    /**
     * 로그인 선택 페이지 (추가 경로)
     */
    @GetMapping("/common/select-login")
    public String selectLoginCommon() {
        log.info("로그인 선택 페이지 호출 (/common/select-login)");
        return "common/select_login";
    }

    /**
     * 로그인 선택 페이지 (언더스코어 경로)
     */
    @GetMapping("/common/select_login")
    public String selectLoginCommonUnderscore() {
        log.info("로그인 선택 페이지 호출 (/common/select_login)");
        return "common/select_login";
    }

    /**
     * 회원가입 선택 페이지
     */
    @GetMapping("/signup")
    public String selectSignup() {
        log.info("회원가입 선택 페이지 호출");
        return "common/select_signup";
    }

    /**
     * 회원가입 선택 페이지
     */
    @GetMapping("/common/select-signup")
    public String selectSignupCommon() {
        log.info("회원가입 선택 페이지 호출 (/common/select-signup)");
        return "common/select_signup";
    }

    /**
     * 회원가입 선택 페이지 (언더스코어 경로)
     */
    @GetMapping("/common/select_signup")
    public String selectSignupCommonUnderscore() {
        log.info("회원가입 선택 페이지 호출 (/common/select_signup)");
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
            log.info("로그아웃 처리: id={}, type={}", loginMember.getId(), loginMember.getMemberType());
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
        log.info("common/signup_complete 템플릿 반환");
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
     * 에러 알림 페이지
     */
    @GetMapping("/error/alert")
    public String errorAlert() {
        log.info("에러 알림 페이지 호출");
        return "error/alert";
    }

    /**
     * 주소 검색 팝업 페이지
     */
    @GetMapping("/juso-popup")
    public String jusoPopup() {
        log.info("주소 검색 팝업 페이지 호출");
        return "common/juso_popup";
    }

    /**
     * 주소 검색 팝업 페이지 (common 경로)
     */
    @GetMapping("/common/juso-popup")
    public String jusoPopupCommon() {
        log.info("주소 검색 팝업 페이지 호출 (/common/juso-popup)");
        return "common/juso_popup";
    }

    // Spring Boot 기본 에러 처리를 사용하도록 함
    /*
    @GetMapping("/error")
    public String error() {
        log.info("에러 페이지 호출");
        return "error/error";
    }
    */
}