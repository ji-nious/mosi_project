package com.kh.project.web;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.web.common.form.BuyerEditForm;
import com.kh.project.web.common.form.BuyerSignupForm;
import com.kh.project.web.common.form.LoginForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberType;
import com.kh.project.domain.entity.MemberStatus;

/**
 * 구매자 페이지 컨트롤러
 * 구매자 관련 페이지 요청 처리
 */
@Slf4j
@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerPageController {

    private final BuyerSVC buyerSVC;
    private final SessionService sessionService;
    
    // 상수 정의
    private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
    private static final int STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(Model model) {
        log.info("구매자 로그인 페이지 호출");
        model.addAttribute("loginForm", new LoginForm());
        return "buyer/buyer_login";
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
            Optional<Buyer> buyerOpt = buyerSVC.findByEmail(loginForm.getEmail());
            if (buyerOpt.isEmpty()) {
                model.addAttribute("errorMessage", "로그인에 실패했습니다.");
                return "buyer/buyer_login";
            }
            
            Buyer buyer = buyerOpt.get();
            if (!buyer.canLogin() || !buyer.getPassword().equals(loginForm.getPassword())) {
                model.addAttribute("errorMessage", "로그인에 실패했습니다.");
                return "buyer/buyer_login";
            }
            
            // 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, buyer.getBuyerId(), buyer.getEmail(), MEMBER_TYPE_BUYER);
            
            log.info("구매자 로그인 성공: buyerId={}, email={}", buyer.getBuyerId(), buyer.getEmail());
            redirectAttributes.addFlashAttribute("success", "로그인이 완료되었습니다.");
            return "redirect:/";
            
        } catch (Exception e) {
            log.error("구매자 로그인 실패: email={}, error={}", loginForm.getEmail(), e.getMessage());
            model.addAttribute("errorMessage", "로그인에 실패했습니다.");
            return "buyer/buyer_login";
        }
    }

    // 회원가입 페이지
    @GetMapping("/signup")
    public String signupPage(Model model) {
        log.info("구매자 회원가입 페이지 호출");
        model.addAttribute("buyerSignupForm", new BuyerSignupForm());
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

        if (bindingResult.hasErrors()) {
            log.warn("회원가입 폼 검증 실패: {}", bindingResult.getAllErrors());
            return "buyer/buyer_signup";
        }

        // 이메일 중복 체크 (탈퇴하지 않은 회원만)
        if (buyerSVC.existsByEmail(signupForm.getEmail())) {
            log.warn("이메일 중복: email={}", signupForm.getEmail());
            model.addAttribute("errorMessage", "이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
            return "buyer/buyer_signup";
        }

        // 닉네임 중복 체크 (탈퇴하지 않은 회원만)
        if (buyerSVC.existsByNickname(signupForm.getNickname())) {
            log.warn("닉네임 중복: nickname={}", signupForm.getNickname());
            model.addAttribute("errorMessage", "이미 사용중인 닉네임입니다.");
            return "buyer/buyer_signup";
        }

        try {
            Buyer buyer = new Buyer();
            buyer.setEmail(signupForm.getEmail());
            buyer.setPassword(signupForm.getPassword());
            buyer.setName(signupForm.getName());
            buyer.setNickname(signupForm.getNickname());
            buyer.setTel(signupForm.getTel());
            buyer.setGender(signupForm.getGender());
            buyer.setBirth(signupForm.getBirth());
            buyer.setAddress(signupForm.getFullAddress());
            buyer.setMemberGubun(MemberGubun.NEW);
            buyer.setStatus(STATUS_ACTIVE);

            Buyer savedBuyer = buyerSVC.join(buyer);
            
            // 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, savedBuyer.getBuyerId(), savedBuyer.getEmail(), MEMBER_TYPE_BUYER);
            
            log.info("구매자 회원가입 성공: buyerId={}, email={}", savedBuyer.getBuyerId(), savedBuyer.getEmail());
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다.");
            redirectAttributes.addFlashAttribute("showSignupModal", true);
            redirectAttributes.addFlashAttribute("memberType", MEMBER_TYPE_BUYER);
            
            return "redirect:/";
            
        } catch (Exception e) {
            log.error("구매자 회원가입 실패: email={}, error={}", signupForm.getEmail(), e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "buyer/buyer_signup";
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
                
                // 생일 포맷 처리
                if (buyer.getBirth() != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    String birthFormatted = sdf.format(buyer.getBirth());
                    model.addAttribute("birthFormatted", birthFormatted);
                } else {
                    model.addAttribute("birthFormatted", "정보 없음");
                }
                
                // 회원 등급 정보 추가
                String gubunInfo = buyerSVC.getGubunInfo(buyer);
                model.addAttribute("gubunInfo", gubunInfo);
                
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

    // 구매자 정보 수정 페이지
    @GetMapping("/edit")
    public String buyerEditPage(HttpSession session, Model model) {
        log.info("구매자 정보 수정 페이지 호출");
        
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 구매자 정보 수정 페이지 접근");
            return "redirect:/buyer/login";
        }

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isPresent()) {
                Buyer buyer = buyerOpt.get();
                
                // 기존 주소 데이터를 분리 처리
                String postcode = null;
                String address = null;
                String detailAddress = null;
                
                // DB에서 가져온 통합 주소를 분리 처리
                String fullAddress = buyer.getAddress();
                if (fullAddress != null && fullAddress.startsWith("(") && fullAddress.contains(") ")) {
                    try {
                        // "(41095) 대구광역시 동구 금강로 225 (금강동) 2동" 형태에서 분리
                        int postcodeEnd = fullAddress.indexOf(") ");
                        
                        if (postcodeEnd > 0) {
                            // 우편번호 추출 - "(41095)" → "41095"
                            postcode = fullAddress.substring(1, postcodeEnd).trim();
                            
                            // 나머지 부분 추출 - "대구광역시 동구 금강로 225 (금강동) 2동"
                            String remaining = fullAddress.substring(postcodeEnd + 2).trim();
                            
                            // 도로명주소와 상세주소 분리
                            int lastParenEnd = remaining.lastIndexOf(")");
                            if (lastParenEnd > 0 && lastParenEnd < remaining.length() - 1) {
                                // "(금강동)" 이후에 내용이 있으면
                                String afterParen = remaining.substring(lastParenEnd + 1).trim();
                                if (!afterParen.isEmpty()) {
                                    address = remaining.substring(0, lastParenEnd + 1).trim();
                                    detailAddress = afterParen;
                                } else {
                                    address = remaining;
                                }
                            } else {
                                // 괄호가 없거나 마지막에 있으면 전체를 주소로
                                address = remaining;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("주소 분리 실패, 전체 주소 사용: {}", fullAddress);
                        address = fullAddress;
                    }
                } else {
                    // 통합 형태가 아닌 경우 그대로 사용
                    address = fullAddress;
                }

                BuyerEditForm editForm = BuyerEditForm.builder()
                        .email(buyer.getEmail())
                        .name(buyer.getName())
                        .nickname(buyer.getNickname())
                        .tel(buyer.getTel())
                        .gender(buyer.getGender())
                        .birth(buyer.getBirth())
                        .postcode(postcode)
                        .address(address)
                        .detailAddress(detailAddress)
                        .build();
                
                model.addAttribute("buyerEditForm", editForm);
                log.info("구매자 정보 수정 페이지 로드 성공: buyerId={}", buyer.getBuyerId());
                return "buyer/buyer_edit";
            } else {
                log.warn("구매자 정보를 찾을 수 없습니다: buyerId={}", loginMember.getId());
                return "redirect:/buyer/login";
            }
        } catch (Exception e) {
            log.error("구매자 정보 수정 페이지 로드 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            return "redirect:/buyer/info";
        }
    }

    // 구매자 정보 수정 처리
    @PostMapping("/edit")
    public String buyerEdit(@Valid @ModelAttribute BuyerEditForm editForm,
                          BindingResult bindingResult,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        log.info("구매자 정보 수정 처리 시작");
        
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 구매자 정보 수정 시도");
            return "redirect:/buyer/login";
        }

        if (bindingResult.hasErrors()) {
            log.warn("구매자 정보 수정 폼 검증 실패: {}", bindingResult.getAllErrors());
            
            // 오류 발생 시 현재 구매자 정보 다시 로드
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isPresent()) {
                model.addAttribute("buyer", buyerOpt.get());
            }
            
            return "buyer/buyer_edit";
        }

        try {
            Buyer buyer = new Buyer();
            buyer.setBuyerId(loginMember.getId());
            buyer.setPassword(editForm.getPassword());
            buyer.setName(editForm.getName());
            buyer.setNickname(editForm.getNickname());
            buyer.setTel(editForm.getTel());
            buyer.setGender(editForm.getGender());
            buyer.setBirth(editForm.getBirth());
            buyer.setAddress(editForm.getFullAddress());

            int updatedRows = buyerSVC.update(loginMember.getId(), buyer);
            
            if (updatedRows > 0) {
                log.info("구매자 정보 수정 성공: buyerId={}", loginMember.getId());
                redirectAttributes.addFlashAttribute("success", "정보가 성공적으로 수정되었습니다.");
                return "redirect:/buyer/info";
            } else {
                log.warn("구매자 정보 수정 실패: buyerId={}", loginMember.getId());
                model.addAttribute("errorMessage", "정보 수정에 실패했습니다.");
                return "buyer/buyer_edit";
            }
            
        } catch (Exception e) {
            log.error("구매자 정보 수정 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
            return "buyer/buyer_edit";
        }
    }

    // 구매자 탈퇴 페이지
    @GetMapping("/withdraw")
    public String buyerWithdrawPage(HttpSession session, Model model) {
        log.info("구매자 탈퇴 페이지 호출");
        
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 탈퇴 페이지 접근");
            return "redirect:/buyer/login";
        }

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isPresent()) {
                Buyer buyer = buyerOpt.get();
                
                // 탈퇴 가능 여부 확인 - 기본 정보만 표시
                model.addAttribute("buyer", buyer);
                model.addAttribute("canWithdraw", true);
                
                log.info("구매자 탈퇴 페이지 로드 성공: buyerId={}", buyer.getBuyerId());
                return "buyer/buyer_withdraw";
            } else {
                log.warn("구매자 정보를 찾을 수 없음: buyerId={}", loginMember.getId());
                return "redirect:/buyer/login";
            }
        } catch (Exception e) {
            log.error("구매자 탈퇴 페이지 로드 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            return "redirect:/buyer/login";
        }
    }

    // 구매자 탈퇴 처리
    @PostMapping("/withdraw")
    public String buyerWithdraw(@RequestParam("reason") String reason,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        log.info("구매자 탈퇴 처리 시작");
        
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 탈퇴 시도");
            return "redirect:/buyer/login";
        }

        try {
            int result = buyerSVC.withdrawWithReason(loginMember.getId(), reason);
            
            if (result > 0) {
                log.info("구매자 탈퇴 성공: buyerId={}", loginMember.getId());
                
                // 세션 무효화
                sessionService.logout(session);
                
                redirectAttributes.addFlashAttribute("success", "PLANT 웹사이트의 회원탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.");
                redirectAttributes.addFlashAttribute("showWithdrawModal", true);
                return "redirect:/";
            } else {
                log.warn("구매자 탈퇴 실패: buyerId={}", loginMember.getId());
                redirectAttributes.addFlashAttribute("error", "탈퇴 처리에 실패했습니다.");
                return "redirect:/buyer/withdraw";
            }
            
        } catch (Exception e) {
            log.error("구매자 탈퇴 처리 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/buyer/withdraw";
        }
    }

    // 구매자 탈퇴 1단계 → 2단계: 서비스 이용현황 조회
    @PostMapping("/withdraw-status")
    public String buyerWithdrawStatus(@RequestParam("password") String password,
                                    @RequestParam("reason") String reason,
                                    @RequestParam(value = "feedback", required = false) String feedback,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        log.info("구매자 탈퇴 1단계 → 2단계 이동");
        
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 탈퇴 시도");
            return "redirect:/buyer/login";
        }

        try {
            // 비밀번호 확인
            Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
            if (buyerOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
                return "redirect:/buyer/withdraw";
            }
            
            Buyer buyer = buyerOpt.get();
            if (!buyer.getPassword().equals(password)) {
                redirectAttributes.addFlashAttribute("error", "비밀번호가 일치하지 않습니다.");
                return "redirect:/buyer/withdraw";
            }
            
            // 서비스 이용현황 정보 생성 (기본값으로 설정)
            com.kh.project.web.common.form.MemberStatusInfo memberStatus = 
                com.kh.project.web.common.form.MemberStatusInfo.forBuyer(0, 0);
            
            // 2단계 페이지에 전달할 데이터
            model.addAttribute("memberStatus", memberStatus);
            model.addAttribute("reason", reason);
            model.addAttribute("feedback", feedback);
            
            log.info("구매자 탈퇴 2단계 페이지 로드: buyerId={}", buyer.getBuyerId());
            return "buyer/buyer_withdraw_status";
            
        } catch (Exception e) {
            log.error("구매자 탈퇴 1단계 처리 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", "처리 중 오류가 발생했습니다.");
            return "redirect:/buyer/withdraw";
        }
    }

    // 구매자 탈퇴 최종 처리
    @PostMapping("/withdraw-final")
    public String buyerWithdrawFinal(@RequestParam("reason") String reason,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        log.info("구매자 최종 탈퇴 처리 시작");
        
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
        if (loginMember == null || !MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            log.warn("구매자가 아닌 사용자가 탈퇴 시도");
            return "redirect:/buyer/login";
        }

        try {
            int result = buyerSVC.withdrawWithReason(loginMember.getId(), reason);
            
            if (result > 0) {
                log.info("구매자 탈퇴 성공: buyerId={}", loginMember.getId());
                
                // 세션 무효화
                sessionService.logout(session);
                
                // 탈퇴 완료 후 바로 메인화면으로 이동 (모달은 buyer_withdraw_status.html에서 처리)
                return "redirect:/";
            } else {
                log.warn("구매자 탈퇴 실패: buyerId={}", loginMember.getId());
                redirectAttributes.addFlashAttribute("error", "탈퇴 처리에 실패했습니다.");
                return "redirect:/buyer/withdraw";
            }
            
        } catch (Exception e) {
            log.error("구매자 탈퇴 처리 실패: buyerId={}, error={}", loginMember.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/buyer/withdraw";
        }
    }

    // 탈퇴 상태 페이지 (GET - 직접 접근 차단)
    @GetMapping("/withdraw-status")
    public String buyerWithdrawStatusGet(RedirectAttributes redirectAttributes) {
        log.warn("구매자 탈퇴 상태 페이지 직접 접근 시도");
        redirectAttributes.addFlashAttribute("error", "잘못된 접근입니다.");
        return "redirect:/buyer/withdraw";
    }

    // 비밀번호 확인 API
    @PostMapping("/verify-password")
    @ResponseBody
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request, HttpSession session) {
        log.info("구매자 비밀번호 확인 요청");
        
        LoginMember loginMember = sessionService.getCurrentUserInfo(session);
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
                boolean isValid = buyer.getPassword().equals(password);
            
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
}
