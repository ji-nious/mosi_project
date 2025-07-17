package com.kh.project.web;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.*;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.domain.seller.svc.SellerSVCImpl;
import com.kh.project.web.exception.BusinessException;
import com.kh.project.web.form.login.LoginForm;
import com.kh.project.web.common.form.SellerEditForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;


@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerPageController {

  private final SellerSVC sellerSVC;
  private final BuyerSVC buyerSVC;
  private final SessionService sessionService;
  private final PasswordEncoder passwordEncoder;

  // 상수 사용
  private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
  private static final String MEMBER_TYPE_SELLER = MemberType.SELLER.getCode();
  private static final String STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();
  private static final String REDIRECT_SELLER_LOGIN = "redirect:/seller/login";

  @GetMapping("/login")
  public String sellerLogin(HttpSession session, Model model) {
    log.info("판매자 로그인 페이지 요청");

    addUserInfoToModel(session, model);
    model.addAttribute("loginForm", new LoginForm());
    return "seller/seller_login";
  }

  @PostMapping("/login")
  public String login(@Validated @ModelAttribute LoginForm form,
                      BindingResult bindingResult,
                      HttpSession session,
                      RedirectAttributes redirectAttributes) {

    log.info("판매자 로그인 요청: {}", form.getEmail());

    if (bindingResult.hasErrors()) {
      return "seller/seller_login";
    }

    try {
      SellerSVCImpl sellerService = getSellerService();

      // 1. 이메일로 판매자 조회
      Optional<Seller> sellerOpt = sellerSVC.findByEmail(form.getEmail());
      if (sellerOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "이메일이 올바르지 않습니다.");
        return REDIRECT_SELLER_LOGIN;
      }

      Seller seller = sellerOpt.get();

      // 2. 로그인 가능 여부 확인 (활성 상태, 탈퇴 여부 등)
      if (!sellerService.canLogin(seller)) {
        if (sellerService.isWithdrawn(seller)) {
          redirectAttributes.addFlashAttribute("error", "탈퇴한 회원입니다.");
        } else {
          redirectAttributes.addFlashAttribute("error", "로그인할 수 없는 계정입니다.");
        }
        return REDIRECT_SELLER_LOGIN;
      }

      // 3. 비밀번호 검증
      if (!passwordEncoder.matches(form.getPassword(), seller.getPassword())) {
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
        return REDIRECT_SELLER_LOGIN;
      }

      // 4. 세션 설정
      sessionService.setLoginSession(session, seller.getSellerId(), seller.getEmail(),
          MEMBER_TYPE_SELLER, seller.getShopName());

      redirectAttributes.addFlashAttribute("success", "로그인이 완료되었습니다. 환영합니다!");
      log.info("판매자 로그인 성공: {}", seller.getEmail());
      return "redirect:/seller/dashboard";

    } catch (Exception e) {
      log.error("판매자 로그인 실패: email={}, error={}", form.getEmail(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
      return REDIRECT_SELLER_LOGIN;
    }
  }

  @GetMapping("/signup")
  public String sellerSignup(HttpSession session, Model model) {
    log.info("판매자 회원가입 페이지 요청");

    addUserInfoToModel(session, model);
    model.addAttribute("sellerSignupForm", new com.kh.project.web.common.form.SellerSignupForm());
    return "seller/seller_signup";
  }

  @PostMapping("/signup")
  public String sellerSignup(@Validated @ModelAttribute com.kh.project.web.common.form.SellerSignupForm signupForm,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {

    log.info("판매자 회원가입 요청: {}", signupForm.getEmail());

    if (bindingResult.hasErrors()) {
      log.warn("판매자 회원가입 폼 검증 실패: {}", bindingResult.getAllErrors());
      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }

    // 중복 체크
    if (sellerSVC.existsByEmail(signupForm.getEmail())) {
      log.warn("이메일 중복: email={}", signupForm.getEmail());
      model.addAttribute("error", "이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }

    if (sellerSVC.existsByBizRegNo(signupForm.getBusinessNumber())) {
      log.warn("사업자등록번호 중복: bizRegNo={}", signupForm.getBusinessNumber());
      model.addAttribute("error", "이미 등록된 사업자등록번호입니다. 탈퇴 후 재가입은 불가능합니다.");
      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }

    if (sellerSVC.existsByShopName(signupForm.getStoreName())) {
      log.warn("상호명 중복: shopName={}", signupForm.getStoreName());
      model.addAttribute("error", "이미 사용중인 상호명입니다.");
      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }

    try {
      Seller seller = new Seller();
      seller.setEmail(signupForm.getEmail());
      seller.setPassword(passwordEncoder.encode(signupForm.getPassword()));
      seller.setName(signupForm.getName());
      seller.setBizRegNo(signupForm.getBusinessNumber());
      seller.setShopName(signupForm.getStoreName());
      seller.setShopAddress(signupForm.getFullAddress());
      seller.setTel(signupForm.getTel());
      seller.setStatus(STATUS_ACTIVE);

      Seller savedSeller = sellerSVC.join(seller);

      sessionService.setLoginSession(session, savedSeller.getSellerId(), savedSeller.getEmail(),
          MEMBER_TYPE_SELLER, savedSeller.getShopName());

      log.info("판매자 회원가입 성공: email={}, sellerId={}", savedSeller.getEmail(), savedSeller.getSellerId());
      return "common/signup_complete";

    } catch (BusinessException e) {
      log.error("판매자 회원가입 실패: email={}, error={}", signupForm.getEmail(), e.getMessage());

      if (e.getMessage().contains("이메일") || e.getMessage().contains("email")) {
        bindingResult.rejectValue("email", "error.email", e.getMessage());
      } else if (e.getMessage().contains("사업자등록번호")) {
        bindingResult.rejectValue("businessNumber", "error.businessNumber", e.getMessage());
      } else if (e.getMessage().contains("상호명")) {
        bindingResult.rejectValue("storeName", "error.storeName", e.getMessage());
      } else if (e.getMessage().contains("대표자명")) {
        bindingResult.rejectValue("name", "error.name", e.getMessage());
      } else {
        bindingResult.reject("error.signup", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
      }

      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }
  }

  @GetMapping("/dashboard")
  public String sellerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 대시보드 페이지 요청");

    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return REDIRECT_SELLER_LOGIN;
    }

    addSellerInfoToModel(model, seller);

    SellerSVCImpl sellerService = getSellerService();
    Map<String, Object> dashboardInfo = sellerService.createDashboardInfo(seller);
    model.addAttribute("dashboardInfo", dashboardInfo);

    return "seller/seller_dashboard";
  }

  @GetMapping("/edit")
  public String editForm(HttpSession session, Model model) {
    if (!isSellerLoggedIn(session)) {
      return REDIRECT_SELLER_LOGIN;
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
    if (sellerOpt.isEmpty()) {
      return REDIRECT_SELLER_LOGIN;
    }

    Seller seller = sellerOpt.get();
    addSellerInfoToModel(model, seller);

    // Service 비즈니스 로직으로 수정 폼 생성
    SellerSVCImpl sellerService = getSellerService();
    SellerEditForm editForm = sellerService.createEditForm(seller);

    model.addAttribute("sellerEditForm", editForm);
    model.addAttribute("seller", seller);

    return "seller/seller_edit";
  }

  @PostMapping("/edit")
  public String editSeller(@Validated @ModelAttribute SellerEditForm editForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
    log.info("판매자 정보 수정 처리");

    if (!isSellerLoggedIn(session)) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return REDIRECT_SELLER_LOGIN;
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
    if (sellerOpt.isEmpty()) {
      redirectAttributes.addFlashAttribute("message", "회원 정보를 찾을 수 없습니다.");
      return REDIRECT_SELLER_LOGIN;
    }

    Seller seller = sellerOpt.get();

    if (bindingResult.hasErrors()) {
      log.warn("수정 폼 검증 실패: {}", bindingResult.getAllErrors());
      addSellerInfoToModel(model, seller);
      model.addAttribute("seller", seller);
      model.addAttribute("sellerEditForm", editForm);
      return "seller/seller_edit";
    }

    if (!editForm.isPasswordMatching()) {
      bindingResult.rejectValue("passwordConfirm", "error.passwordConfirm", "비밀번호가 일치하지 않습니다.");
      addSellerInfoToModel(model, seller);
      model.addAttribute("seller", seller);
      model.addAttribute("sellerEditForm", editForm);
      return "seller/seller_edit";
    }

    try {
      SellerSVCImpl sellerService = getSellerService();

      // 비밀번호 암호화
      if (editForm.getPassword() != null && !editForm.getPassword().trim().isEmpty()) {
        editForm.setPassword(passwordEncoder.encode(editForm.getPassword()));
      }

      sellerService.updateSellerFromForm(seller, editForm);

      int updatedRows = sellerSVC.update(seller.getSellerId(), seller);

      if (updatedRows > 0) {
        log.info("판매자 정보 수정 성공: sellerId={}", seller.getSellerId());
        redirectAttributes.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
        return "redirect:/seller/info";
      } else {
        log.warn("판매자 정보 수정 실패: sellerId={}", seller.getSellerId());
        redirectAttributes.addFlashAttribute("error", "정보 수정에 실패했습니다.");
        return "redirect:/seller/edit";
      }

    } catch (BusinessException e) {
      log.warn("판매자 정보 수정 실패: {}", e.getMessage());

      if (e.getMessage().contains("상호명")) {
        bindingResult.rejectValue("shopName", "error.shopName", e.getMessage());
      } else {
        model.addAttribute("error", e.getMessage());
      }

      addSellerInfoToModel(model, seller);
      model.addAttribute("seller", seller);
      model.addAttribute("sellerEditForm", editForm);
      return "seller/seller_edit";
    } catch (Exception e) {
      log.error("판매자 정보 수정 실패: sellerId={}, error={}", seller.getSellerId(), e.getMessage());
      model.addAttribute("error", "정보 수정 중 오류가 발생했습니다: " + e.getMessage());
      addSellerInfoToModel(model, seller);
      model.addAttribute("seller", seller);
      model.addAttribute("sellerEditForm", editForm);
      return "seller/seller_edit";
    }
  }

  @GetMapping("/withdraw")
  public String withdrawForm(HttpSession session, Model model) {
    if (!isSellerLoggedIn(session)) {
      return REDIRECT_SELLER_LOGIN;
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
    if (sellerOpt.isPresent()) {
      Seller seller = sellerOpt.get();
      model.addAttribute("seller", seller);

      addUserInfoToModel(session, model);
      return "seller/seller_withdraw";
    }

    return REDIRECT_SELLER_LOGIN;
  }

  @PostMapping("/withdraw-status")
  public String sellerWithdrawStatus(@RequestParam("reason") String reason,
                                     @RequestParam("password") String password,
                                     @RequestParam(value = "feedback", required = false) String feedback,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
    log.info("판매자 탈퇴 1단계 처리 요청: reason={}", reason);

    if (!isSellerLoggedIn(session)) {
      redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
      return REDIRECT_SELLER_LOGIN;
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);

    try {
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
        return REDIRECT_SELLER_LOGIN;
      }

      Seller seller = sellerOpt.get();

      // 비밀번호 검증
      if (!passwordEncoder.matches(password, seller.getPassword())) {
        log.warn("비밀번호 검증 실패: sellerId={}", loginMember.getId());
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
        return "redirect:/seller/withdraw";
      }

      SellerSVCImpl sellerService = getSellerService();
      boolean canWithdraw = sellerService.canWithdraw(seller);
      Map<String, Object> sellerStatus = getSellerServiceStatus(seller);

      model.addAttribute("canWithdraw", canWithdraw);
      model.addAttribute("sellerStatus", sellerStatus);
      model.addAttribute("reason", reason);
      model.addAttribute("feedback", feedback);
      model.addAttribute("sellerId", seller.getSellerId());
      model.addAttribute("email", seller.getEmail());
      model.addAttribute("name", seller.getName());
      model.addAttribute("shopName", seller.getShopName());

      if (!canWithdraw) {
        model.addAttribute("withdrawBlockReasons", sellerStatus.get("blockReasons"));
      }

      log.info("판매자 탈퇴 1단계 완료: sellerId={}, canWithdraw={}", loginMember.getId(), canWithdraw);
      return "seller/seller_withdraw_status";

    } catch (Exception e) {
      log.error("판매자 탈퇴 1단계 처리 중 오류 발생: sellerId={}, error={}", loginMember.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      return "redirect:/seller/withdraw";
    }
  }

  @PostMapping("/withdraw-final")
  public String sellerWithdrawFinal(HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    @RequestParam("reason") String reason) {
    log.info("판매자 탈퇴 2단계 처리 요청");

    if (!isSellerLoggedIn(session)) {
      redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
      return REDIRECT_SELLER_LOGIN;
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);

    try {
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
        return REDIRECT_SELLER_LOGIN;
      }

      Seller seller = sellerOpt.get();

      SellerSVCImpl sellerService = getSellerService();
      boolean canWithdraw = sellerService.canWithdraw(seller);

      if (canWithdraw) {
        int result = sellerSVC.withdrawWithReason(loginMember.getId(), reason);

        if (result > 0) {
          sessionService.logout(session);
          log.info("판매자 탈퇴 성공: sellerId={}", loginMember.getId());
          return "redirect:/";
        } else {
          redirectAttributes.addFlashAttribute("error", "탈퇴 처리에 실패했습니다.");
          return "redirect:/seller/withdraw";
        }
      } else {
        String blockReason = sellerService.getWithdrawBlockReason(seller);
        redirectAttributes.addFlashAttribute("error", "현재 탈퇴할 수 없는 상태입니다: " + blockReason);
        log.warn("판매자 탈퇴 실패 (탈퇴 불가): sellerId={}", loginMember.getId());
        return "redirect:/seller/withdraw";
      }

    } catch (Exception e) {
      log.error("판매자 탈퇴 2단계 처리 중 오류 발생: sellerId={}, error={}", loginMember.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      return "redirect:/seller/withdraw";
    }
  }

  @GetMapping("/withdraw-status")
  public String withdrawStatus() {
    log.info("판매자 탈퇴 상태 페이지 호출");
    return "seller/seller_withdraw_status";
  }

  @GetMapping("/info")
  public String info(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 정보 조회 페이지 요청");

    if (!isSellerLoggedIn(session)) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return REDIRECT_SELLER_LOGIN;
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
    if (sellerOpt.isEmpty()) {
      return REDIRECT_SELLER_LOGIN;
    }

    Seller seller = sellerOpt.get();
    model.addAttribute("seller", seller);

    addUserInfoToModel(session, model);
    return "seller/seller_info";
  }

  @PostMapping("/verify-password")
  @ResponseBody
  public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request, HttpSession session) {
    log.info("판매자 비밀번호 확인 요청");

    if (!isSellerLoggedIn(session)) {
      log.warn("판매자가 아닌 사용자가 비밀번호 확인 시도");
      return Map.of("success", false, "message", "로그인이 필요합니다.");
    }

    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    String password = request.get("password");
    if (password == null || password.trim().isEmpty()) {
      return Map.of("success", false, "message", "비밀번호를 입력해주세요.");
    }

    try {
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isPresent()) {
        Seller seller = sellerOpt.get();
        boolean isValid = passwordEncoder.matches(password, seller.getPassword());

        if (isValid) {
          log.info("판매자 비밀번호 확인 성공: sellerId={}", loginMember.getId());
          return Map.of("success", true);
        } else {
          log.warn("판매자 비밀번호 확인 실패: sellerId={}", loginMember.getId());
          return Map.of("success", false, "message", "비밀번호가 틀렸습니다.");
        }
      } else {
        log.warn("판매자 정보를 찾을 수 없음: sellerId={}", loginMember.getId());
        return Map.of("success", false, "message", "사용자 정보를 찾을 수 없습니다.");
      }

    } catch (Exception e) {
      log.error("판매자 비밀번호 확인 오류: sellerId={}, error={}", loginMember.getId(), e.getMessage());
      return Map.of("success", false, "message", "서버 오류가 발생했습니다.");
    }
  }



  /**
   * SellerSVCImpl 인스턴스
   */
  private SellerSVCImpl getSellerService() {
    return (SellerSVCImpl) sellerSVC;
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

  /**
   * 판매자 정보를 모델에 추가
   */
  private void addSellerInfoToModel(Model model, Seller seller) {
    model.addAttribute("seller", seller);
    model.addAttribute("sellerId", seller.getSellerId());
    model.addAttribute("userNickname", seller.getShopName());
    model.addAttribute("userName", seller.getName());

    Map<String, String> gubunInfo = Map.of("code", MEMBER_TYPE_SELLER, "name", MemberType.SELLER.getDescription());
    model.addAttribute("gubunInfo", gubunInfo);

    SellerSVCImpl sellerService = getSellerService();

    Map<String, Object> statusInfo = Map.of(
        "code", seller.getStatus(),
        "name", sellerService.getStatusDisplay(seller)
    );
    model.addAttribute("statusInfo", statusInfo);
    model.addAttribute("canLogin", sellerService.canLogin(seller));
    model.addAttribute("isWithdrawn", sellerService.isWithdrawn(seller));

    // 주소 파싱해서 분리된 형태로 모델에 추가
    String[] addressParts = parseAddress(seller.getShopAddress());
    model.addAttribute("parsedAddress", addressParts[0]);
    model.addAttribute("parsedDetailAddress", "");

    log.debug("모델에 판매자 정보 추가 완료: ID={}, 등급={}, 상태={}",
        seller.getSellerId(), gubunInfo.get("name"), statusInfo.get("name"));
  }

  /**
   * 사용자 정보를 모델에 추가
   */
  private void addUserInfoToModel(HttpSession session, Model model) {
    LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
    if (loginMember != null) {
      try {
        if (MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
          Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
          if (buyerOpt.isPresent()) {
            Buyer buyer = buyerOpt.get();
            model.addAttribute("userNickname", buyer.getNickname());
            model.addAttribute("userName", buyer.getName());
          }
        } else if (MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
          Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
          if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            model.addAttribute("userNickname", seller.getShopName());
            model.addAttribute("userName", seller.getName());
          }
        }
      } catch (Exception e) {
        log.error("사용자 정보 조회 실패: {}", e.getMessage());
      }
    }
  }

  /**
   * 인증된 판매자 조회 (Service 비즈니스 로직 활용)
   */
  private Seller getAuthenticatedSeller(HttpSession session) {
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null || !MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
        return null;
      }

      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isPresent()) {
        Seller seller = sellerOpt.get();

        SellerSVCImpl sellerService = getSellerService();
        if (!sellerService.canLogin(seller)) {
          log.warn("로그인할 수 없는 상태의 판매자: ID={}", loginMember.getId());
          return null;
        }

        log.debug("인증된 판매자 정보 조회 성공: ID={}, 이메일={}",
            seller.getSellerId(), seller.getEmail());
        return seller;
      } else {
        log.warn("DB에서 판매자 정보를 찾을 수 없습니다. ID: {}", loginMember.getId());
        return null;
      }

    } catch (Exception e) {
      log.error("판매자 인증 중 오류 발생", e);
      return null;
    }
  }

  /**
   * 판매자 로그인 여부 확인
   */
  private boolean isSellerLoggedIn(HttpSession session) {
    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    return loginMember != null && MEMBER_TYPE_SELLER.equals(loginMember.getMemberType());
  }

  /**
   * 판매자 서비스 이용 현황 조회
   */
  private Map<String, Object> getSellerServiceStatus(Seller seller) {
    SellerSVCImpl sellerService = getSellerService();

    boolean canWithdraw = sellerService.canWithdraw(seller);
    String blockReason = sellerService.getWithdrawBlockReason(seller);

    // 1차 프로젝트에서는 모든 서비스 이용현황이 0
    return Map.of(
        "serviceUsage", 0,
        "canWithdraw", canWithdraw,
        "blockReasons", blockReason != null ? blockReason : "없음",
        "totalProducts", 0,
        "activeProducts", 0,
        "activeOrders", 0,
        "shippingOrders", 0,
        "pendingAmount", 0
    );
  }
}