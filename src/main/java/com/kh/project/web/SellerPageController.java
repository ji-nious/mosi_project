package com.kh.project.web;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.entity.MemberType;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.util.SellerStatusHelper;
import com.kh.project.web.common.form.LoginForm;
import com.kh.project.web.common.form.SellerEditForm;
import com.kh.project.web.common.form.SellerSignupForm;
import com.kh.project.web.exception.BusinessValidationException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

/**
 * 판매자 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerPageController {

  private final SellerSVC sellerSVC;
  private final BuyerSVC buyerSVC;
  private final SessionService sessionService;

  private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
  private static final String MEMBER_TYPE_SELLER = MemberType.SELLER.getCode();
  private static final Integer STATUS_ACTIVE = 1;
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
      Optional<Seller> sellerOpt = sellerSVC.findByEmail(form.getEmail());
      if (sellerOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return REDIRECT_SELLER_LOGIN;
      }
      
      Seller seller = sellerOpt.get();
      if (!seller.canLogin() || !seller.getPassword().equals(form.getPassword())) {
        redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        return REDIRECT_SELLER_LOGIN;
      }

      sessionService.setLoginSession(session, seller.getSellerId(), seller.getEmail(), MEMBER_TYPE_SELLER);
      
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
    model.addAttribute("sellerSignupForm", new SellerSignupForm());
    return "seller/seller_signup";
  }
  
  @PostMapping("/signup")
  public String sellerSignup(@Validated @ModelAttribute SellerSignupForm signupForm,
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
    
    // 중복 체크 - 에러 시 폼 데이터 유지를 위해 바로 뷰 반환
    if (sellerSVC.existsByEmail(signupForm.getEmail())) {
      log.warn("이메일 중복: email={}", signupForm.getEmail());
      model.addAttribute("error", "이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }

    if (sellerSVC.existsByBizRegNo(signupForm.getBusinessNumber())) {
      log.warn("사업자등록번호 중복 (재가입 불가): bizRegNo={}", signupForm.getBusinessNumber());
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

    if (sellerSVC.existsByName(signupForm.getName())) {
      log.warn("대표자명 중복: name={}", signupForm.getName());
      model.addAttribute("error", "이미 등록된 대표자명입니다.");
      addUserInfoToModel(session, model);
      return "seller/seller_signup";
    }
    
    try {
      // 폼 데이터를 엔티티로 변환
      Seller seller = new Seller();
      seller.setEmail(signupForm.getEmail());
      seller.setPassword(signupForm.getPassword());
      seller.setName(signupForm.getName());
      seller.setBizRegNo(signupForm.getBusinessNumber());
      seller.setShopName(signupForm.getStoreName());
      seller.setShopAddress(signupForm.getFullAddress());
      seller.setTel(signupForm.getTel());
      seller.setStatus(STATUS_ACTIVE);
      
      // 회원가입 처리
      Seller savedSeller = sellerSVC.join(seller);
      
      // 자동 로그인 처리
      sessionService.setLoginSession(session, savedSeller.getSellerId(), savedSeller.getEmail(), MEMBER_TYPE_SELLER);
      
      redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다. 환영합니다!");
      redirectAttributes.addFlashAttribute("memberType", MEMBER_TYPE_SELLER);
      redirectAttributes.addFlashAttribute("showSignupModal", true);
      log.info("판매자 회원가입 성공: email={}, sellerId={}", savedSeller.getEmail(), savedSeller.getSellerId());
      return "redirect:/";
      
    } catch (BusinessValidationException e) {
      log.error("판매자 회원가입 실패: email={}, error={}", signupForm.getEmail(), e.getMessage());
      
      if (e.getMessage().contains("이메일") || e.getMessage().contains("email")) {
        bindingResult.rejectValue("email", "error.email", e.getMessage());
      } else if (e.getMessage().contains("사업자등록번호")) {
        bindingResult.rejectValue("businessNumber", "error.businessNumber", e.getMessage());
      } else if (e.getMessage().contains("상호명")) {
        bindingResult.rejectValue("storeName", "error.storeName", e.getMessage());
      } else if (e.getMessage().contains("대표자명")) {
        bindingResult.rejectValue("name", "error.name", e.getMessage());
      } else if (e.getMessage().contains("사업장 주소")) {
        bindingResult.rejectValue("address", "error.address", e.getMessage());
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
    
    // 기존 주소 데이터를 분리 처리
    String postcode = null;
    String address = null;
    String detailAddress = null;
    
    String fullAddress = seller.getShopAddress();
    if (fullAddress != null) {
      String[] addressParts = fullAddress.split("\\|");
      if (addressParts.length >= 2) {
        postcode = addressParts[0].trim();
        String[] mainDetailParts = addressParts[1].split(" ", 2);
        if (mainDetailParts.length >= 2) {
          address = mainDetailParts[0].trim();
          detailAddress = mainDetailParts[1].trim();
        } else {
          address = addressParts[1].trim();
        }
      } else {
        address = fullAddress;
      }
    }
    
    SellerEditForm editForm = new SellerEditForm();
    editForm.setPassword(seller.getPassword());
    editForm.setTel(seller.getTel());
    editForm.setPostcode(postcode);
    editForm.setAddress(address);
    editForm.setDetailAddress(detailAddress);
    
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
      seller.setPassword(editForm.getPassword());
      seller.setTel(editForm.getTel());
      if (editForm.getFullAddress() != null) {
        seller.setShopAddress(editForm.getFullAddress());
      }
      
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
      
    } catch (BusinessValidationException e) {
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
      log.info("비밀번호 검증 시작: sellerId={}, 입력비밀번호길이={}", 
               loginMember.getId(), password != null ? password.length() : 0);
      
      // 회원 정보 조회
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
        return REDIRECT_SELLER_LOGIN;
      }
      
      Seller seller = sellerOpt.get();
      
      // 비밀번호 검증
      boolean passwordValid = seller.getPassword().equals(password);
      log.info("비밀번호 검증 결과: {}", passwordValid);
      
      if (!passwordValid) {
        log.warn("비밀번호 검증 실패: sellerId={}", loginMember.getId());
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
        return "redirect:/seller/withdraw";
      }
      
      // 탈퇴 가능 여부 확인
      boolean canWithdraw = checkSellerCanWithdraw(seller);
      Map<String, Object> sellerStatus = getSellerServiceStatus(seller);
      
      // 모델에 데이터 설정
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
      boolean canWithdraw = checkSellerCanWithdraw(seller);

      if (canWithdraw) {
        int result = sellerSVC.withdrawWithReason(loginMember.getId(), reason);
        
        if (result > 0) {
          sessionService.logout(session);
          log.info("판매자 탈퇴 성공: sellerId={}", loginMember.getId());
          // 탈퇴 완료 후 바로 메인화면으로 이동 (모달은 seller_withdraw_status.html에서 처리)
          return "redirect:/";
        } else {
          redirectAttributes.addFlashAttribute("error", "탈퇴 처리에 실패했습니다.");
          return "redirect:/seller/withdraw";
        }
      } else {
        Map<String, Object> sellerStatus = getSellerServiceStatus(seller);
        redirectAttributes.addFlashAttribute("error", "현재 탈퇴할 수 없는 상태입니다: " + sellerStatus.get("blockReasons"));
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
      boolean isValid = sellerOpt.isPresent() && sellerOpt.get().getPassword().equals(password);
      
      if (isValid) {
        log.info("판매자 비밀번호 확인 성공: sellerId={}", loginMember.getId());
        return Map.of("success", true);
      } else {
        log.warn("판매자 비밀번호 확인 실패: sellerId={}", loginMember.getId());
        return Map.of("success", false, "message", "비밀번호가 틀렸습니다.");
      }
      
    } catch (Exception e) {
      log.error("판매자 비밀번호 확인 오류: sellerId={}, error={}", loginMember.getId(), e.getMessage());
      return Map.of("success", false, "message", "서버 오류가 발생했습니다.");
    }
  }

  // 유틸리티 메서드들

  private void addSellerInfoToModel(Model model, Seller seller) {
    model.addAttribute("seller", seller);
    model.addAttribute("sellerId", seller.getSellerId());
    model.addAttribute("userNickname", seller.getShopName());
    model.addAttribute("userName", seller.getName());

    Map<String, String> gubunInfo = Map.of("code", MEMBER_TYPE_SELLER, "name", MemberType.SELLER.getDescription());
    model.addAttribute("gubunInfo", gubunInfo);

    Map<String, Object> statusInfo = Map.of(
        "code", seller.getStatus(), 
        "name", seller.getStatusDisplay()
    );
    model.addAttribute("statusInfo", statusInfo);

    model.addAttribute("canLogin", seller.canLogin());
    model.addAttribute("isWithdrawn", seller.getWithdrawnAt() != null);

    log.debug("모델에 판매자 정보 추가 완료: ID={}, 등급={}, 상태={}", 
             seller.getSellerId(), gubunInfo.get("name"), statusInfo.get("name"));
  }
  
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

  private Seller getAuthenticatedSeller(HttpSession session) {
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null || !MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
        return null;
      }
      
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isPresent()) {
        Seller seller = sellerOpt.get();
        
        if (!seller.canLogin()) {
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
  
  private boolean isSellerLoggedIn(HttpSession session) {
    LoginMember loginMember = sessionService.getCurrentUserInfo(session);
    return loginMember != null && MEMBER_TYPE_SELLER.equals(loginMember.getMemberType());
  }

  /**
   * 판매자 탈퇴 가능 여부 검증
   */
  private boolean checkSellerCanWithdraw(Seller seller) {
    return SellerStatusHelper.canWithdraw(seller);
  }
  
  /**
   * 판매자 서비스 이용 현황 조회
   */
  private Map<String, Object> getSellerServiceStatus(Seller seller) {
    // 실제 서비스 이용현황 조회 (현재는 더미 데이터, 향후 실제 DB 조회로 대체)
    int totalProducts = 0;    // 등록된 상품 수
    int activeProducts = 0;   // 활성 상품 수
    int activeOrders = 0;     // 활성 주문 수
    int shippingOrders = 0;   // 배송 중인 주문 수
    int pendingAmount = 0;    // 정산 대기 금액
    
    // 서비스 이용 중인 항목이 있는지 체크
    boolean hasActiveService = (totalProducts > 0 || activeProducts > 0 || 
                               activeOrders > 0 || shippingOrders > 0 || pendingAmount > 0);
    
    // 기본 상태 체크 + 서비스 이용현황 체크
    boolean basicCanWithdraw = SellerStatusHelper.canWithdraw(seller);
    boolean canWithdraw = basicCanWithdraw && !hasActiveService;
    
    String blockReasons;
    if (!basicCanWithdraw) {
      blockReasons = "계정 상태가 탈퇴 불가 상태입니다.";
    } else if (hasActiveService) {
      blockReasons = "진행 중인 상품/주문/정산이 있어 탈퇴할 수 없습니다.";
    } else {
      blockReasons = "없음";
    }
    
    return Map.of(
        "serviceUsage", hasActiveService ? 1 : 0,
        "canWithdraw", canWithdraw,
        "blockReasons", blockReasons,
        "totalProducts", totalProducts,
        "activeProducts", activeProducts,
        "activeOrders", activeOrders,
        "shippingOrders", shippingOrders,
        "pendingAmount", pendingAmount
    );
  }
}
