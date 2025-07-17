package com.kh.project.web;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.buyer.svc.BuyerSVCImpl;
import com.kh.project.domain.entity.*;
import com.kh.project.web.common.form.BuyerEditForm;
import com.kh.project.web.form.login.LoginForm;
import com.kh.project.web.form.member.BuyerSignupForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
@Slf4j
public class BuyerPageController {

    private final BuyerSVC buyerSVC;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    // 상수 정의
    private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
    private static final String STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();

    private BuyerSVCImpl getBuyerService() {
        return (BuyerSVCImpl) buyerSVC;
    }

    /**
     * 주소 파싱
     */
    private String[] parseAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        
        // "(우편번호) 주소" 형태에서 우편번호 부분만 제거
        String address = fullAddress.replaceFirst("^\\(\\d+\\)\\s*", "").trim();
        
        return new String[]{address, ""};
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String buyerLoginForm(Model model) {
        log.info("구매자 로그인 페이지 호출");
        model.addAttribute("loginForm", new LoginForm());
        return "buyer/buyer_login";
    }

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage(Model model, HttpSession session) {
        log.info("구매자 회원가입 페이지 호출");
        model.addAttribute("buyerSignupForm", new BuyerSignupForm());

        // 로그인된 사용자 닉네임 추가 (없으면 null)
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember != null) {
            model.addAttribute("userNickname", loginMember.getNickname());
        }

        return "buyer/buyer_signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String buyerSignup(@Valid @ModelAttribute BuyerSignupForm signupForm,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        log.info("구매자 회원가입 처리 시작: email={}", signupForm.getEmail());

        // 1. 기본 Bean Validation 검증
        if (bindingResult.hasErrors()) {
            log.warn("회원가입 폼 기본 검증 실패: {}", bindingResult.getAllErrors());
            model.addAttribute("errorMessage", "입력한 정보를 다시 확인해주세요.");
            return "buyer/buyer_signup";
        }

        // 2. 비밀번호 확인 검증
        if (!signupForm.isPasswordMatch()) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch",
                "비밀번호가 일치하지 않습니다.");
            return "buyer/buyer_signup";
        }

        try {
            // 3. 이메일 중복 검사
            if (buyerSVC.existsByEmail(signupForm.getEmail())) {
                log.warn("이메일 중복: email={}", signupForm.getEmail());
                bindingResult.rejectValue("email", "email.duplicate",
                    "이미 등록된 이메일입니다. 다른 이메일을 사용해주세요.");
                return "buyer/buyer_signup";
            }

            // 4. 닉네임 중복 검사
            if (buyerSVC.existsByNickname(signupForm.getNickname())) {
                log.warn("닉네임 중복: nickname={}", signupForm.getNickname());
                bindingResult.rejectValue("nickname", "nickname.duplicate",
                    "이미 사용중인 닉네임입니다. 다른 닉네임을 사용해주세요.");
                return "buyer/buyer_signup";
            }

            // 5. 회원가입 처리
            Buyer buyer = new Buyer();
            buyer.setEmail(signupForm.getEmail());
            buyer.setPassword(passwordEncoder.encode(signupForm.getPassword()));
            buyer.setName(signupForm.getName());
            buyer.setNickname(signupForm.getNickname());
            buyer.setTel(signupForm.getTel());
            buyer.setGender(signupForm.getGender());
            buyer.setBirth(signupForm.getBirth());
            if (signupForm.getPostcode() != null && !signupForm.getPostcode().trim().isEmpty()) {
                try {
                    buyer.setPostNumber(Integer.parseInt(signupForm.getPostcode()));
                } catch (NumberFormatException e) {
                    log.warn("우편번호 형식 오류: {}", signupForm.getPostcode());
                }
            }
            buyer.setAddress(signupForm.getFullAddress());
            buyer.setMemberGubun(MemberGubun.NEW);
            buyer.setStatus(STATUS_ACTIVE);

            Buyer savedBuyer = buyerSVC.join(buyer);

            log.info("구매자 회원가입 성공: buyerId={}, email={}",
                savedBuyer.getBuyerId(), savedBuyer.getEmail());

            // 6. 자동 로그인 처리
            sessionService.setLoginSession(session, savedBuyer.getBuyerId(),
                savedBuyer.getEmail(), MEMBER_TYPE_BUYER, savedBuyer.getNickname());

            log.info("회원가입 후 자동 로그인 완료: buyerId={}", savedBuyer.getBuyerId());

            return "common/signup_complete";

        } catch (DataIntegrityViolationException e) {
            // 데이터베이스 제약조건 위반 (동시성 문제)
            log.error("데이터 무결성 위반: {}", e.getMessage());

            if (e.getMessage().contains("email")) {
                bindingResult.rejectValue("email", "email.duplicate",
                    "이미 등록된 이메일입니다.");
            } else if (e.getMessage().contains("nickname")) {
                bindingResult.rejectValue("nickname", "nickname.duplicate",
                    "이미 사용중인 닉네임입니다.");
            } else {
                model.addAttribute("errorMessage", "회원가입 처리 중 오류가 발생했습니다.");
            }

            return "buyer/buyer_signup";

        } catch (Exception e) {
            // 기타 예외 처리
            log.error("구매자 회원가입 실패: email={}, error={}",
                signupForm.getEmail(), e.getMessage());

            model.addAttribute("errorMessage",
                "회원가입 처리 중 예상치 못한 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "buyer/buyer_signup";
        }
    }

    // 로그인 처리
    @PostMapping("/login")
    public String buyerLogin(@Valid @ModelAttribute LoginForm loginForm,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        log.info("구매자 로그인 처리 시작: email={}", loginForm.getEmail());

        if (bindingResult.hasErrors()) {
            log.warn("로그인 폼 검증 실패: {}", bindingResult.getAllErrors());
            return "buyer/buyer_login";
        }

        try {
            BuyerSVCImpl buyerService = getBuyerService();

            // 1. 이메일로 구매자 조회
            Optional<Buyer> buyerOpt = buyerSVC.findByEmail(loginForm.getEmail());
            if (buyerOpt.isEmpty()) {
                bindingResult.rejectValue("email", "email.notfound",
                    "이메일이 올바르지 않습니다.");
                return "buyer/buyer_login";
            }

            Buyer buyer = buyerOpt.get();

            // 2. 로그인 가능 여부 확인 (활성 상태, 탈퇴 여부 등)
            if (!buyerService.canLogin(buyer)) {
                if (buyerService.isWithdrawn(buyer)) {
                    model.addAttribute("errorMessage", "탈퇴한 회원입니다.");
                } else {
                    model.addAttribute("errorMessage", "로그인할 수 없는 계정입니다.");
                }
                return "buyer/buyer_login";
            }

            // 3. 비밀번호 검증
            if (!passwordEncoder.matches(loginForm.getPassword(), buyer.getPassword())) {
                bindingResult.rejectValue("password", "password.incorrect",
                    "비밀번호가 올바르지 않습니다.");
                return "buyer/buyer_login";
            }

            // 4. 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, buyer.getBuyerId(),
                buyer.getEmail(), MEMBER_TYPE_BUYER, buyer.getNickname());

            log.info("구매자 로그인 성공: buyerId={}, email={}",
                buyer.getBuyerId(), buyer.getEmail());

            redirectAttributes.addFlashAttribute("success",
                "로그인이 완료되었습니다. 환영합니다!");
            redirectAttributes.addFlashAttribute("showLoginModal", true);

            return "redirect:/";

        } catch (Exception e) {
            log.error("구매자 로그인 실패: email={}, error={}",
                loginForm.getEmail(), e.getMessage());

            model.addAttribute("errorMessage",
                "로그인 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "buyer/buyer_login";
        }
    }

    @GetMapping("/edit")
    public String editForm(HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            return "redirect:/buyer/login";
        }

        Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
        if (buyerOpt.isEmpty()) {
            return "redirect:/buyer/login";
        }

        Buyer buyer = buyerOpt.get();
        
        // 기존 buyer 정보로 edit form 생성 
        String[] addressParts = parseAddress(buyer.getAddress());
        
        BuyerEditForm editForm = BuyerEditForm.builder()
            .email(buyer.getEmail())
            .name(buyer.getName())
            .nickname(buyer.getNickname())
            .tel(buyer.getTel())
            .gender(buyer.getGender())
            .birth(buyer.getBirth())
            .postcode(buyer.getPostNumber() != null ? buyer.getPostNumber().toString() : "")
            .address(addressParts[0])
            .detailAddress("")
            .build();

        model.addAttribute("buyerEditForm", editForm);
        model.addAttribute("buyer", buyer);

        return "buyer/buyer_edit";
    }

    @PostMapping("/edit")
    public String editBuyer(@ModelAttribute BuyerEditForm editForm,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        log.info("구매자 정보 수정 처리");

        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
            return "redirect:/buyer/login";
        }

        Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
        if (buyerOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "회원 정보를 찾을 수 없습니다.");
            return "redirect:/buyer/login";
        }

        try {
            log.info("수정 폼 데이터: {}", editForm);
            redirectAttributes.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
            return "redirect:/buyer/info";
        } catch (Exception e) {
            log.error("구매자 정보 수정 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            model.addAttribute("error", "정보 수정 중 오류가 발생했습니다: " + e.getMessage());
            return "buyer/buyer_edit";
        }
    }

    @PostMapping("/verify-password")
    @ResponseBody
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request, HttpSession session) {
        log.info("구매자 비밀번호 확인 요청");

        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 비밀번호 확인 시도");
            return Map.of("success", false, "message", "로그인이 필요합니다.");
        }

        String password = request.get("password");
        if (password == null || password.trim().isEmpty()) {
            return Map.of("success", false, "message", "비밀번호를 입력해주세요.");
        }

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isPresent()) {
                Buyer buyer = buyerOpt.get();
                boolean isValid = passwordEncoder.matches(password, buyer.getPassword());

                if (isValid) {
                    log.info("구매자 비밀번호 확인 성공: buyerId={}", loginMember.getId());
                    return Map.of("success", true);
                } else {
                    log.warn("구매자 비밀번호 확인 실패: buyerId={}", loginMember.getId());
                    return Map.of("success", false, "message", "비밀번호가 틀렸습니다.");
                }
            } else {
                log.warn("구매자 정보를 찾을 수 없음: buyerId={}", loginMember.getId());
                return Map.of("success", false, "message", "사용자 정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            log.error("구매자 비밀번호 확인 오류: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            return Map.of("success", false, "message", "서버 오류가 발생했습니다.");
        }
    }

    // 구매자 정보 조회
    @GetMapping("/info")
    public String buyerInfo(HttpSession session, Model model) {
        log.info("구매자 정보 조회 페이지 호출");

        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 구매자 정보 페이지 접근");
            return "redirect:/buyer/login";
        }

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isPresent()) {
                Buyer buyer = buyerOpt.get();
                model.addAttribute("buyer", buyer);

                // 생년월일 포맷팅
                String birthFormatted = null;
                if (buyer.getBirth() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    birthFormatted = sdf.format(buyer.getBirth());
                }
                model.addAttribute("birthFormatted", birthFormatted);

                // 주소 파싱해서 분리된 형태로 모델에 추가
                String[] addressParts = parseAddress(buyer.getAddress());
                model.addAttribute("parsedAddress", addressParts[0]);
                model.addAttribute("parsedDetailAddress", "");

                BuyerSVCImpl buyerService = getBuyerService();
                model.addAttribute("canLogin", buyerService.canLogin(buyer));
                model.addAttribute("isWithdrawn", buyerService.isWithdrawn(buyer));
                model.addAttribute("statusDisplay", buyerService.getStatusDisplay(buyer));

                log.info("구매자 정보 조회 성공: buyerId={}", buyer.getBuyerId());
                return "buyer/buyer_info";
            } else {
                log.warn("구매자 정보를 찾을 수 없음: buyerId={}", loginMember.getId());
                return "redirect:/buyer/login";
            }
        } catch (Exception e) {
            log.error("구매자 정보 조회 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            return "redirect:/buyer/login";
        }
    }

    @GetMapping("/withdraw")
    public String withdrawForm(HttpSession session, Model model) {
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            return "redirect:/buyer/login";
        }

        Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
        if (buyerOpt.isPresent()) {
            Buyer buyer = buyerOpt.get();
            model.addAttribute("buyer", buyer);
            return "buyer/buyer_withdraw";
        }

        return "redirect:/buyer/login";
    }

    @PostMapping("/withdraw-status")
    public String buyerWithdrawStatus(@RequestParam("reason") String reason,
                                     @RequestParam("password") String password,
                                     @RequestParam(value = "feedback", required = false) String feedback,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        log.info("구매자 탈퇴 1단계 처리 요청: reason={}", reason);

        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/buyer/login";
        }

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
                return "redirect:/buyer/login";
            }

            Buyer buyer = buyerOpt.get();

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, buyer.getPassword())) {
                log.warn("비밀번호 검증 실패: buyerId={}", loginMember.getId());
                redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
                return "redirect:/buyer/withdraw";
            }

            BuyerSVCImpl buyerService = getBuyerService();
            boolean canWithdraw = !buyerService.isWithdrawn(buyer); // 기본적으로 탈퇴되지 않은 회원은 탈퇴 가능

            model.addAttribute("canWithdraw", canWithdraw);
            model.addAttribute("reason", reason);
            model.addAttribute("feedback", feedback);
            model.addAttribute("buyerId", buyer.getBuyerId());
            model.addAttribute("email", buyer.getEmail());
            model.addAttribute("name", buyer.getName());
            model.addAttribute("nickname", buyer.getNickname());

            log.info("구매자 탈퇴 1단계 완료: buyerId={}, canWithdraw={}", loginMember.getId(), canWithdraw);
            return "buyer/buyer_withdraw_status";

        } catch (Exception e) {
            log.error("구매자 탈퇴 1단계 처리 중 오류 발생: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/buyer/withdraw";
        }
    }

    @PostMapping("/withdraw-final")
    public String buyerWithdrawFinal(HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    @RequestParam("reason") String reason) {
        log.info("구매자 탈퇴 2단계 처리 요청");

        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/buyer/login";
        }

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
                return "redirect:/buyer/login";
            }

            Buyer buyer = buyerOpt.get();

            BuyerSVCImpl buyerService = getBuyerService();
            boolean canWithdraw = !buyerService.isWithdrawn(buyer);

            if (canWithdraw) {
                int result = buyerSVC.withdrawWithReason(loginMember.getId(), reason);

                if (result > 0) {
                    sessionService.logout(session);
                    log.info("구매자 탈퇴 성공: buyerId={}", loginMember.getId());
                    return "redirect:/";
                } else {
                    redirectAttributes.addFlashAttribute("error", "탈퇴 처리에 실패했습니다.");
                    return "redirect:/buyer/withdraw";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "현재 탈퇴할 수 없는 상태입니다.");
                log.warn("구매자 탈퇴 실패 (탈퇴 불가): buyerId={}", loginMember.getId());
                return "redirect:/buyer/withdraw";
            }

        } catch (Exception e) {
            log.error("구매자 탈퇴 2단계 처리 중 오류 발생: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "redirect:/buyer/withdraw";
        }
    }
}