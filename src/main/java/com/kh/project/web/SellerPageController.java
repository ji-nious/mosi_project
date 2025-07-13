package com.kh.project.web;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.util.AuthUtils;
import com.kh.project.util.CommonConstants;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.SessionService;
import com.kh.project.web.common.form.LoginForm;
import com.kh.project.web.common.form.SellerSignupForm;
import com.kh.project.web.common.form.SellerEditForm;
import com.kh.project.web.common.form.MemberStatusInfo;
import com.kh.project.web.exception.BusinessException;
import com.kh.project.web.exception.MemberException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestParam;

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

  @GetMapping("/login")
  public String sellerLogin(HttpSession session, Model model) {
    log.info("판매자 로그인 페이지 요청");
    
    // 로그인 사용자 정보 조회 (홈페이지와 동일한 로직)
    LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
    if (loginMember != null) {
      try {
        if ("BUYER".equals(loginMember.getMemberType())) {
          Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
          if (buyerOpt.isPresent()) {
            Buyer buyer = buyerOpt.get();
            model.addAttribute("userNickname", buyer.getNickname());
            model.addAttribute("userName", buyer.getName());
          }
        } else if ("SELLER".equals(loginMember.getMemberType())) {
          Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
          if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            model.addAttribute("userNickname", seller.getShopName()); // 판매자는 상호명을 닉네임으로 사용
            model.addAttribute("userName", seller.getName());
          }
        }
      } catch (Exception e) {
        log.error("사용자 정보 조회 실패: {}", e.getMessage());
      }
    }
    
    model.addAttribute("loginForm", new LoginForm());
    return "seller/seller_login";
  }
  
  @PostMapping("/login")
  public String sellerLogin(@Valid @ModelAttribute LoginForm loginForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
    log.info("판매자 로그인 처리: email={}", loginForm.getEmail());
    
    // 1. 유효성 검사 오류
    if (bindingResult.hasErrors()) {
      return "seller/seller_login";
    }
    
    try {
      // 2. 로그인 처리
      Seller seller = sellerSVC.login(loginForm.getEmail(), loginForm.getPassword());
      
      // 3. 세션 생성
      LoginMember loginMember = LoginMember.seller(seller.getSellerId(), seller.getEmail());
      session.setAttribute("loginMember", loginMember);
      session.setMaxInactiveInterval(1800);
      
      // 4. 성공 메시지와 리디렉션 (판매자는 대시보드로)
      redirectAttributes.addFlashAttribute("message", "로그인에 성공했습니다.");
      log.info("판매자 로그인 성공: sellerId={}", seller.getSellerId());
      return "redirect:/seller/dashboard";
      
    } catch (MemberException.AlreadyWithdrawnException e) {
      log.warn("탈퇴한 판매자의 로그인 시도: email={}", loginForm.getEmail());
      redirectAttributes.addFlashAttribute("error", "탈퇴한 회원입니다. 재가입을 원하시면 동일한 정보로 회원가입을 진행해주세요.");
      return "redirect:/seller/login";
    } catch (Exception e) {
      // 5. 로그인 실패
      log.error("판매자 로그인 실패: email={}, error={}", loginForm.getEmail(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
      return "redirect:/seller/login";
    }
  }
  
  @GetMapping({"/signup", "/join"})
  public String sellerSignup(HttpSession session, Model model) {
    log.info("판매자 회원가입 페이지 요청");
    
    // 로그인 사용자 정보 조회 (홈페이지와 동일한 로직)
    LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
    if (loginMember != null) {
      try {
        if ("BUYER".equals(loginMember.getMemberType())) {
          Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
          if (buyerOpt.isPresent()) {
            Buyer buyer = buyerOpt.get();
            model.addAttribute("userNickname", buyer.getNickname());
            model.addAttribute("userName", buyer.getName());
          }
        } else if ("SELLER".equals(loginMember.getMemberType())) {
          Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
          if (sellerOpt.isPresent()) {
            Seller seller = sellerOpt.get();
            model.addAttribute("userNickname", seller.getShopName()); // 판매자는 상호명을 닉네임으로 사용
            model.addAttribute("userName", seller.getName());
          }
        }
      } catch (Exception e) {
        log.error("사용자 정보 조회 실패: {}", e.getMessage());
      }
    }
    
    model.addAttribute("sellerSignupForm", new SellerSignupForm());
    return "seller/seller_signup";
  }
  
  @PostMapping({"/signup", "/join"})
  public String sellerSignup(@Valid @ModelAttribute SellerSignupForm signupForm,
                            BindingResult bindingResult,
                            HttpSession session,
                            RedirectAttributes redirectAttributes,
                            Model model) {
    log.info("판매자 회원가입 처리: email={}", signupForm.getEmail());
    
    // 1. 기본 유효성 검사 오류
    if (bindingResult.hasErrors()) {
      log.warn("판매자 회원가입 유효성 검사 실패: {}", bindingResult.getAllErrors());
      return "seller/seller_signup";
    }
    
    // 2. 비밀번호 확인 검증
    if (!signupForm.isPasswordMatching()) {
      bindingResult.rejectValue("passwordConfirm", "error.passwordConfirm", "비밀번호가 일치하지 않습니다.");
      return "seller/seller_signup";
    }
    
    try {
      // 3. 회원가입 처리
      Seller seller = new Seller();
      seller.setEmail(signupForm.getEmail());
      seller.setPassword(signupForm.getPassword());
      seller.setBizRegNo(signupForm.getBusinessNumber());
      seller.setShopName(signupForm.getStoreName());
      seller.setName(signupForm.getName());
      seller.setShopAddress(signupForm.getFullAddress());
      seller.setTel(signupForm.getTel());
      // Seller 엔티티에는 birth 필드가 없으므로 제거
      // if (signupForm.getBirth() != null) {
      //   seller.setBirth(signupForm.getBirth());
      // }
      
      Seller savedSeller = sellerSVC.join(seller);
      
      // 4. 성공 처리 (모달 페이지로 리다이렉트)
      log.info("판매자 회원가입 성공: sellerId={}", savedSeller.getSellerId());
      return "redirect:/common/signup-complete";
      
    } catch (BusinessException e) {
      // 6. 회원가입 실패
      log.error("판매자 회원가입 실패: email={}, error={}", signupForm.getEmail(), e.getMessage());
      
      // 이메일 중복 등의 경우 필드 에러로 처리
      if (e.getMessage().contains("이메일") || e.getMessage().contains("email")) {
        bindingResult.rejectValue("email", "error.email", e.getMessage());
      } else if (e.getMessage().contains("사업자등록번호")) {
        bindingResult.rejectValue("businessNumber", "error.businessNumber", e.getMessage());
      } else if (e.getMessage().contains("상점명") || e.getMessage().contains("상호명")) {
        bindingResult.rejectValue("storeName", "error.storeName", e.getMessage());
      } else if (e.getMessage().contains("대표자명")) {
        bindingResult.rejectValue("name", "error.name", e.getMessage());
      } else if (e.getMessage().contains("사업장 주소")) {
        bindingResult.rejectValue("address", "error.address", e.getMessage());
      } else {
        bindingResult.reject("error.signup", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
      }
      
      return "seller/seller_signup";
    }
  }

  @GetMapping("/dashboard")
  public String sellerDashboard(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 대시보드 페이지 요청");

    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }

    addSellerInfoToModel(model, seller);
    return "seller/seller_dashboard";
  }
  
  @GetMapping("/edit")
  public String sellerEdit(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 정보 수정 페이지 요청");
    
    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }
    
    // 기존 정보로 폼 초기화
    SellerEditForm editForm = new SellerEditForm();
    editForm.setTel(seller.getTel());
    
    // 기존 가게 주소 파싱 및 설정
    if (seller.getShopAddress() != null && !seller.getShopAddress().trim().isEmpty()) {
      String fullAddress = seller.getShopAddress();
      // (12345) 도로명주소 상세주소 형태로 저장된 주소 파싱
      if (fullAddress.startsWith("(") && fullAddress.contains(")")) {
        int endIdx = fullAddress.indexOf(")");
        String postcode = fullAddress.substring(1, endIdx).trim();
        String remainingAddress = fullAddress.substring(endIdx + 1).trim();
        
        editForm.setPostcode(postcode);
        
        // 더 정확한 주소 분리: 마지막 닫는 괄호를 찾아서 도로명주소와 상세주소 분리
        int lastParenIdx = remainingAddress.lastIndexOf(")");
        if (lastParenIdx > 0) {
          // 괄호가 있는 경우: 괄호까지가 도로명주소, 그 이후가 상세주소
          String roadAddress = remainingAddress.substring(0, lastParenIdx + 1).trim();
          String detailAddress = remainingAddress.substring(lastParenIdx + 1).trim();
          
          editForm.setAddress(roadAddress);
          if (!detailAddress.isEmpty()) {
            editForm.setDetailAddress(detailAddress);
          }
        } else {
          // 괄호가 없는 경우: 전체를 도로명주소로 설정
          editForm.setAddress(remainingAddress);
        }
      } else {
        // 단순 주소인 경우 그대로 설정
        editForm.setAddress(fullAddress);
      }
    }
    
    model.addAttribute("sellerEditForm", editForm);
    addSellerInfoToModel(model, seller);
    return "seller/seller_edit";
  }
  
  @PostMapping("/edit")
  public String sellerEdit(@Valid @ModelAttribute SellerEditForm editForm,
                          BindingResult bindingResult,
                          HttpSession session,
                          RedirectAttributes redirectAttributes,
                          Model model) {
    log.info("판매자 정보 수정 처리");
    
    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }
    
    // 1. 기본 유효성 검사
    if (bindingResult.hasErrors()) {
      addSellerInfoToModel(model, seller);
      return "seller/seller_edit";
    }
    
    // 2. 비밀번호 확인 검증
    if (!editForm.isPasswordMatching()) {
      bindingResult.rejectValue("passwordConfirm", "error.passwordConfirm", "비밀번호가 일치하지 않습니다.");
      addSellerInfoToModel(model, seller);
      return "seller/seller_edit";
    }
    
    try {
      // 3. 정보 업데이트 (읽기전용 필드 제외)
      seller.setPassword(editForm.getPassword()); // 비밀번호는 필수 입력이므로 항상 업데이트
      seller.setTel(editForm.getTel());
      if (editForm.getFullAddress() != null) {
        seller.setShopAddress(editForm.getFullAddress());
      }
      
      sellerSVC.update(seller.getSellerId(), seller);
      
      redirectAttributes.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
      return "redirect:/seller/info";
      
    } catch (Exception e) {
      log.error("판매자 정보 수정 실패: sellerId={}, error={}", seller.getSellerId(), e.getMessage());
      bindingResult.reject("error.edit", "정보 수정 중 오류가 발생했습니다: " + e.getMessage());
      addSellerInfoToModel(model, seller);
      return "seller/seller_edit";
    }
  }
  
  @GetMapping("/withdraw")
  public String sellerWithdraw(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 탈퇴 페이지 요청");

    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null) {
      redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }
    
    // 이 페이지는 단순히 폼을 보여주는 역할만 함
    return "seller/seller_withdraw";
  }

  // 판매자 탈퇴 1단계: 비밀번호 확인 및 탈퇴 사유 입력
  @PostMapping("/withdraw-status")
  public String sellerWithdrawStatus(HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    @RequestParam("password") String password,
                                    @RequestParam("reason") String reason,
                                    Model model) {
    log.info("판매자 탈퇴 1단계 처리 요청");

    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null) {
      redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }

    try {
      // 1. 비밀번호 확인
      if (!sellerSVC.checkPassword(loginMember.getId(), password)) {
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
        return "redirect:/seller/withdraw";
      }

      // 2. 서비스 이용 현황 조회
      MemberStatusInfo statusInfo = sellerSVC.getServiceUsage(loginMember.getId());
      
      // 3. 판매자 정보 조회
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isEmpty()) {
        redirectAttributes.addFlashAttribute("error", "회원 정보를 찾을 수 없습니다.");
        return "redirect:/seller/login";
      }
      
      Seller seller = sellerOpt.get();
      
      // 4. 모델에 데이터 추가
      model.addAttribute("memberStatus", statusInfo);
      model.addAttribute("reason", reason);
      model.addAttribute("sellerId", seller.getSellerId());
      model.addAttribute("email", seller.getEmail());
      model.addAttribute("name", seller.getName());
      model.addAttribute("shopName", seller.getShopName());
      
      log.info("판매자 탈퇴 1단계 완료: sellerId={}, canWithdraw={}", loginMember.getId(), statusInfo.isCanWithdraw());
      return "seller/seller_withdraw_status";

    } catch (Exception e) {
      log.error("판매자 탈퇴 1단계 처리 중 오류 발생: sellerId={}, error={}", loginMember.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      return "redirect:/seller/withdraw";
    }
  }
  
  // 판매자 탈퇴 2단계: 최종 탈퇴 처리
  @PostMapping("/withdraw-final")
  public String sellerWithdrawFinal(HttpSession session,
                                   RedirectAttributes redirectAttributes,
                                   @RequestParam("reason") String reason) {
    log.info("판매자 탈퇴 2단계 처리 요청");

    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null) {
      redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }

    try {
      // 1. 탈퇴 가능 여부 재확인
      boolean canWithdraw = sellerSVC.canWithdraw(loginMember.getId());

      if (canWithdraw) {
        // 2. 탈퇴 처리
        sellerSVC.withdraw(loginMember.getId(), reason);
        session.invalidate(); // 세션 무효화
        redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 정상적으로 처리되었습니다.");
        log.info("판매자 탈퇴 성공: sellerId={}", loginMember.getId());
        return "redirect:/";
      } else {
        // 3. 탈퇴 불가 처리
        redirectAttributes.addFlashAttribute("error", "현재 탈퇴할 수 없는 상태입니다. 다시 확인해주세요.");
        log.warn("판매자 탈퇴 실패 (탈퇴 불가): sellerId={}", loginMember.getId());
        return "redirect:/seller/withdraw";
      }

    } catch (Exception e) {
      log.error("판매자 탈퇴 2단계 처리 중 오류 발생: sellerId={}, error={}", loginMember.getId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "탈퇴 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
      return "redirect:/seller/withdraw";
    }
  }
  
  @GetMapping("/info")
  public String sellerInfo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 정보 조회 페이지 요청");
    
    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }
    
    addSellerInfoToModel(model, seller);
    return "seller/seller_info";
  }
  
  @PostMapping("/verify-password")
  @ResponseBody
  public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request, HttpSession session) {
    String password = request.get("password");
    return sessionService.verifyPassword(session, password);
  }
  
  // ============ 호환성 유지 메서드들 ============
  
  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id, Model model, HttpSession session, 
                      RedirectAttributes redirectAttributes) {
    log.info("판매자 상세 정보 페이지 요청: ID={}", id);
    
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
        return "redirect:/seller/login";
      }
      
      if (!loginMember.getId().equals(id)) {
        redirectAttributes.addFlashAttribute("message", "본인의 정보만 접근할 수 있습니다.");
        return "redirect:/seller/info";
      }
      
      Optional<Seller> sellerOpt = sellerSVC.findById(id);
      if (sellerOpt.isPresent()) {
        addSellerInfoToModel(model, sellerOpt.get());
        return "seller/detailForm";
      } else {
        redirectAttributes.addFlashAttribute("message", "존재하지 않는 판매자입니다.");
        return "redirect:/seller/login";
      }
      
    } catch (Exception e) {
      log.error("판매자 정보 조회 중 오류 발생: ID={}", id, e);
      redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
      return "redirect:/seller/info";
    }
  }
  
  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable("id") Long id, Model model, HttpSession session,
                        RedirectAttributes redirectAttributes) {
    log.info("판매자 수정 폼 페이지 요청: ID={}", id);
    
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
        return "redirect:/seller/login";
      }
      
      if (!loginMember.getId().equals(id)) {
        redirectAttributes.addFlashAttribute("message", "본인의 정보만 접근할 수 있습니다.");
        return "redirect:/seller/info";
      }
      
      Optional<Seller> sellerOpt = sellerSVC.findById(id);
      if (sellerOpt.isPresent()) {
        addSellerInfoToModel(model, sellerOpt.get());
        return "seller/updateForm";
      } else {
        redirectAttributes.addFlashAttribute("message", "존재하지 않는 판매자입니다.");
        return "redirect:/seller/login";
      }
      
    } catch (Exception e) {
      log.error("판매자 수정 폼 조회 중 오류 발생: ID={}", id, e);
      redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
      return "redirect:/seller/info";
    }
  }

  // ============ 유틸리티 메서드 (단순화) ============

  private void addSellerInfoToModel(Model model, Seller seller) {
    // 기본 판매자 정보
    model.addAttribute("seller", seller);
    model.addAttribute("sellerId", seller.getSellerId());
    
    // 사용자 닉네임 (환영 메시지용)
    model.addAttribute("userNickname", seller.getShopName()); // 판매자는 상호명을 닉네임으로 사용
    model.addAttribute("userName", seller.getName());

    // 등급 정보 (코드 + 한글명)
    Map<String, String> gubunInfo = sellerSVC.getGubunInfo(seller);
    model.addAttribute("gubunInfo", gubunInfo);

    // 상태 정보 (코드 + 한글명)
    Map<String, String> statusInfo = sellerSVC.getStatusInfo(seller);
    model.addAttribute("statusInfo", statusInfo);

    // 부가 정보
    model.addAttribute("canLogin", sellerSVC.canLogin(seller));
    model.addAttribute("isWithdrawn", sellerSVC.isWithdrawn(seller));

    log.debug("모델에 판매자 정보 추가 완료: ID={}, 등급={}, 상태={}", 
             seller.getSellerId(), gubunInfo.get("name"), statusInfo.get("name"));
  }

  private Seller getAuthenticatedSeller(HttpSession session) {
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        log.warn("세션에 로그인 정보가 없습니다.");
        return null;
      }
      
      if (!"SELLER".equals(loginMember.getMemberType())) {
        log.warn("판매자 세션이 아닙니다: {}", loginMember.getMemberType());
        return null;
      }
      
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isPresent()) {
        Seller seller = sellerOpt.get();
        
        if (!sellerSVC.canLogin(seller)) {
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
}
