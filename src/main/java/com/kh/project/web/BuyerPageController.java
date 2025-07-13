package com.kh.project.web;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.Seller;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.dto.BuyerSignupForm;
import com.kh.project.web.common.dto.BuyerEditForm;
import com.kh.project.web.common.dto.LoginForm;
import com.kh.project.web.exception.BusinessException;
import com.kh.project.web.exception.MemberException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.Validator;

/**
 * 구매자 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerPageController {
  
  private final BuyerSVC buyerSVC;
  private final SellerSVC sellerSVC;
  private final Validator validator;

  @GetMapping("/login")
  public String buyerLogin(Model model) {
    log.info("구매자 로그인 페이지 요청");
    model.addAttribute("loginForm", new LoginForm());
    return "buyer/buyer_login";
  }
  
  @PostMapping("/login")
  public String buyerLogin(@Valid @ModelAttribute LoginForm loginForm,
                          BindingResult bindingResult,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
    log.info("구매자 로그인 처리: email={}", loginForm.getEmail());
    
    // 1. 유효성 검사 오류
    if (bindingResult.hasErrors()) {
      return "buyer/buyer_login";
    }
    
    try {
      // 2. 로그인 처리
      Buyer buyer = buyerSVC.login(loginForm.getEmail(), loginForm.getPassword());
      
      // 3. 세션 생성
                  LoginMember loginMember = LoginMember.buyer(buyer.getBuyerId(), buyer.getEmail());
      session.setAttribute("loginMember", loginMember);
      session.setMaxInactiveInterval(1800);
      
      // 4. 성공 메시지와 리디렉션
      redirectAttributes.addFlashAttribute("message", "로그인에 성공했습니다.");
      log.info("구매자 로그인 성공: buyerId={}", buyer.getBuyerId());
      return "redirect:/";
      
    } catch (MemberException.AlreadyWithdrawnException e) {
      log.warn("탈퇴한 회원의 로그인 시도: email={}", loginForm.getEmail());
      redirectAttributes.addFlashAttribute("error", "탈퇴한 회원입니다. 재가입을 원하시면 동일한 정보로 회원가입을 진행해주세요.");
      return "redirect:/buyer/login";
    } catch (Exception e) {
      // 5. 로그인 실패
      log.error("구매자 로그인 실패: email={}, error={}", loginForm.getEmail(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
      return "redirect:/buyer/login";
    }
  }
  
  @GetMapping({"/signup", "/join"})
  public String buyerSignup(HttpSession session, Model model) {
    log.info("구매자 회원가입 페이지 요청");
    
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
    
    model.addAttribute("buyerSignupForm", new BuyerSignupForm());
    return "buyer/buyer_signup";
  }
  
  @PostMapping({"/signup", "/join"})
  public String buyerSignup(@ModelAttribute BuyerSignupForm signupForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           RedirectAttributes redirectAttributes,
                           Model model) {
    log.info("구매자 회원가입 요청: email={}", signupForm.getEmail());

    // 시나리오 분기: 재활성화 vs 신규가입
    Optional<Buyer> existingBuyer = buyerSVC.findByEmail(signupForm.getEmail());

    if (existingBuyer.isPresent() && existingBuyer.get().isWithdrawn()) {
      // 1. 재활성화 시나리오: 이메일과 비밀번호만 확인
      if (signupForm.getPassword() == null || signupForm.getPassword().isBlank()) {
        bindingResult.rejectValue("password", "error.password", "비밀번호를 입력해주세요.");
        return "buyer/buyer_signup";
      }
    } else {
      // 2. 신규가입 시나리오: 전체 폼 유효성 검사
      validator.validate(signupForm, bindingResult);
      if (bindingResult.hasErrors()) {
        return "buyer/buyer_signup";
      }
      if (!signupForm.isValidBirth()) {
        bindingResult.rejectValue("birth", "error.birth", "생년월일은 오늘 이전 날짜여야 합니다.");
        return "buyer/buyer_signup";
      }
    }
    
    try {
      // 서비스 계층에 회원가입(재활성화 포함) 위임
      Buyer buyer = new Buyer();
      buyer.setEmail(signupForm.getEmail());
      buyer.setPassword(signupForm.getPassword());
      buyer.setName(signupForm.getName());
      buyer.setNickname(signupForm.getNickname());
      buyer.setTel(signupForm.getTel());
      buyer.setGender(signupForm.getGender());
      if (signupForm.getBirth() != null) {
        buyer.setBirth(java.sql.Date.valueOf(signupForm.getBirth()));
      }
      buyer.setAddress(signupForm.getFullAddress());
      
      Buyer processedBuyer = buyerSVC.join(buyer);
      
      // 3. 성공 처리 (자동 로그인 및 리디렉션)
      LoginMember loginMember = LoginMember.buyer(processedBuyer.getBuyerId(), processedBuyer.getEmail());
      session.setAttribute("loginMember", loginMember);
      session.setMaxInactiveInterval(1800);
      
      redirectAttributes.addFlashAttribute("message", "회원가입(또는 계정 활성화)이 완료되었습니다. 환영합니다!");
      log.info("구매자 회원가입/재활성화 성공: buyerId={}", processedBuyer.getBuyerId());
      return "redirect:/";
      
    } catch (BusinessException e) {
      log.error("구매자 회원가입 실패: email={}, error={}", signupForm.getEmail(), e.getMessage());
      
      // 4. 실패 처리 (오류 메시지 표시)
      if (e.getMessage().contains("이메일")) {
        bindingResult.rejectValue("email", "error.email", e.getMessage());
      } else if (e.getMessage().contains("닉네임")) {
        bindingResult.rejectValue("nickname", "error.nickname", e.getMessage());
      } else {
        bindingResult.reject("signup.fail", e.getMessage());
      }
      
      return "buyer/buyer_signup";
    }
  }

  @GetMapping("/edit")
  public String buyerEdit(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("구매자 정보 수정 페이지 요청");
    
    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }
    
    // 기존 정보로 폼 초기화
    BuyerEditForm editForm = new BuyerEditForm();
    editForm.setName(buyer.getName());
    editForm.setNickname(buyer.getNickname());
    editForm.setTel(buyer.getTel());
    editForm.setGender(buyer.getGender());
    
    if (buyer.getBirth() != null) {
      // java.util.Date를 LocalDate로 변환하여 폼에 표시
      java.time.LocalDate birthDate = buyer.getBirth().toInstant()
          .atZone(java.time.ZoneId.systemDefault())
          .toLocalDate();
      editForm.setBirth(birthDate);
    }
    
    // 기존 주소 파싱 및 설정
    if (buyer.getAddress() != null && !buyer.getAddress().trim().isEmpty()) {
      String fullAddress = buyer.getAddress();
      
      // (12345) 도로명주소 상세주소 형태로 저장된 주소 파싱
      if (fullAddress.startsWith("(") && fullAddress.contains(")")) {
        int endIdx = fullAddress.indexOf(")");
        String postcode = fullAddress.substring(1, endIdx).trim();
        String remainingAddress = fullAddress.substring(endIdx + 1).trim();
        
        editForm.setPostcode(postcode);
        
        // 남은 주소를 기본 주소로 설정
        editForm.setAddress(remainingAddress);
      } else {
        // 단순 주소인 경우 그대로 설정
        editForm.setAddress(fullAddress);
      }
    }
    
    model.addAttribute("buyerEditForm", editForm);
    model.addAttribute("buyerEmail", buyer.getEmail());
    model.addAttribute("buyerName", buyer.getName());
    model.addAttribute("buyer", buyer); // 간단한 buyer 정보만 추가
    return "buyer/buyer_edit";
  }
  
  @PostMapping("/edit")
  public String buyerEdit(@Valid @ModelAttribute BuyerEditForm editForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         RedirectAttributes redirectAttributes,
                         Model model) {
    log.info("구매자 정보 수정 처리");
    
    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }
    
    // 1. 기본 유효성 검사
    if (bindingResult.hasErrors()) {
      model.addAttribute("buyerEmail", buyer.getEmail());
      model.addAttribute("buyerName", buyer.getName());
      model.addAttribute("buyerEditForm", editForm);
      model.addAttribute("buyer", buyer); // 간단한 buyer 정보만 추가
      return "buyer/buyer_edit";
    }
    
    // 2. 비밀번호 확인 검증
    if (!editForm.isPasswordMatching()) {
      bindingResult.rejectValue("passwordConfirm", "error.passwordConfirm", "비밀번호가 일치하지 않습니다.");
      model.addAttribute("buyerEmail", buyer.getEmail());
      model.addAttribute("buyerName", buyer.getName());
      model.addAttribute("buyerEditForm", editForm);
      model.addAttribute("buyer", buyer); // 간단한 buyer 정보만 추가
      return "buyer/buyer_edit";
    }
    
    // 3. 생년월일 검증
    if (!editForm.isValidBirth()) {
      bindingResult.rejectValue("birth", "error.birth", "생년월일은 오늘 이전 날짜여야 합니다.");
      model.addAttribute("buyerEmail", buyer.getEmail());
      model.addAttribute("buyerName", buyer.getName());
      model.addAttribute("buyerEditForm", editForm);
      model.addAttribute("buyer", buyer); // 간단한 buyer 정보만 추가
      return "buyer/buyer_edit";
    }

    
    try {
      // 4. 정보 업데이트
      buyer.setPassword(editForm.getPassword()); // 비밀번호는 필수 입력이므로 항상 업데이트
      buyer.setName(editForm.getName());
      buyer.setNickname(editForm.getNickname());
      buyer.setTel(editForm.getTel()); // 전화번호 업데이트 추가
      buyer.setGender(editForm.getGender());
      if (editForm.getBirth() != null) {
        buyer.setBirth(java.util.Date.from(editForm.getBirth()
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant()));
      }
      if (editForm.getFullAddress() != null) {
        buyer.setAddress(editForm.getFullAddress());
      }
      
      buyerSVC.update(buyer.getBuyerId(), buyer);
      
      redirectAttributes.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
      return "redirect:/buyer/info";
      
    } catch (Exception e) {
      log.error("구매자 정보 수정 실패: buyerId={}, error={}", buyer.getBuyerId(), e.getMessage());
      bindingResult.reject("error.edit", "정보 수정이 반영되지 않았습니다. 다시 시도해주세요.");
      model.addAttribute("buyerEmail", buyer.getEmail());
      model.addAttribute("buyerName", buyer.getName());
      model.addAttribute("buyerEditForm", editForm);
      model.addAttribute("buyer", buyer); // 간단한 buyer 정보만 추가
      return "buyer/buyer_edit";
    }
  }
  
  @GetMapping("/withdraw")
  public String buyerWithdraw(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("구매자 탈퇴 페이지 요청");
    
    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }
    
    model.addAttribute("buyerId", buyer.getBuyerId());
    model.addAttribute("email", buyer.getEmail());
    model.addAttribute("name", buyer.getName());
    
    return "buyer/buyer_withdraw";
  }
  
  @PostMapping("/withdraw")
  public String buyerWithdraw(HttpSession session,
                             RedirectAttributes redirectAttributes,
                             @RequestParam("password") String password,
                             @RequestParam("reason") String reason) {
    log.info("구매자 탈퇴 처리");

    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }

    try {
      // 비밀번호 확인
      if (!buyerSVC.checkPassword(buyer.getBuyerId(), password)) {
        redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
        return "redirect:/buyer/withdraw";
      }
      
      // 탈퇴 처리
      buyerSVC.withdraw(buyer.getBuyerId(), reason);
      
      // 세션 무효화
      session.invalidate();
      
      redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
      return "redirect:/";

    } catch (BusinessException e) {
      log.warn("구매자 탈퇴 비즈니스 로직 실패: buyerId={}, error={}", buyer.getBuyerId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/buyer/withdraw";
    } catch (Exception e) {
      log.error("구매자 탈퇴 실패: buyerId={}, error={}", buyer.getBuyerId(), e.getMessage());
      redirectAttributes.addFlashAttribute("error", "탈퇴 처리 중 오류가 발생했습니다.");
      return "redirect:/buyer/withdraw";
    }
  }
  
  @GetMapping("/info")
  public String buyerInfo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("구매자 정보 조회 페이지 요청");
    
    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/common/select_login";
    }
    
    addBuyerInfoToModel(model, buyer);
    return "buyer/buyer_info";
  }
  
  @PostMapping("/verify-password")
  @ResponseBody
  public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request, HttpSession session) {
    Map<String, Object> response = new HashMap<>();
    
    try {
      Buyer buyer = getAuthenticatedBuyer(session);
      if (buyer == null) {
        response.put("success", false);
        response.put("message", "로그인이 필요합니다.");
        return response;
      }
      
      String inputPassword = request.get("password");
      if (inputPassword == null || inputPassword.trim().isEmpty()) {
        response.put("success", false);
        response.put("message", "비밀번호를 입력해주세요.");
        return response;
      }
      
      boolean isValid = buyerSVC.checkPassword(buyer.getBuyerId(), inputPassword);
      response.put("success", isValid);
      
      if (!isValid) {
        response.put("message", "비밀번호가 틀렸습니다.");
      }
      
      return response;
      
    } catch (Exception e) {
      log.error("비밀번호 확인 중 오류 발생", e);
      response.put("success", false);
      response.put("message", "서버 오류가 발생했습니다.");
      return response;
    }
  }
  
  // ============ 호환성 유지 메서드들 ============
  
  @GetMapping("/{id}")
  public String detail(@PathVariable("id") Long id, Model model, HttpSession session, 
                      RedirectAttributes redirectAttributes) {
    log.info("구매자 상세 정보 페이지 요청: ID={}", id);
    
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
        return "redirect:/buyer/login";
      }
      
      if (!loginMember.getId().equals(id)) {
        redirectAttributes.addFlashAttribute("message", "본인의 정보만 접근할 수 있습니다.");
        return "redirect:/buyer/info";
      }
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(id);
      if (buyerOpt.isPresent()) {
        addBuyerInfoToModel(model, buyerOpt.get());
        return "buyer/detailForm";
      } else {
        redirectAttributes.addFlashAttribute("message", "존재하지 않는 회원입니다.");
        return "redirect:/buyer/login";
      }
      
    } catch (Exception e) {
      log.error("구매자 정보 조회 중 오류 발생: ID={}", id, e);
      redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
      return "redirect:/buyer/info";
    }
  }
  
  @GetMapping("/{id}/edit")
  public String editForm(@PathVariable("id") Long id, Model model, HttpSession session,
                        RedirectAttributes redirectAttributes) {
    log.info("구매자 수정 폼 페이지 요청: ID={}", id);
    
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
        return "redirect:/buyer/login";
      }
      
      if (!loginMember.getId().equals(id)) {
        redirectAttributes.addFlashAttribute("message", "본인의 정보만 접근할 수 있습니다.");
        return "redirect:/buyer/info";
      }
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(id);
      if (buyerOpt.isPresent()) {
        addBuyerInfoToModel(model, buyerOpt.get());
        return "buyer/updateForm";
      } else {
        redirectAttributes.addFlashAttribute("message", "존재하지 않는 회원입니다.");
        return "redirect:/buyer/login";
      }
      
    } catch (Exception e) {
      log.error("구매자 수정 폼 조회 중 오류 발생: ID={}", id, e);
      redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
      return "redirect:/buyer/info";
    }
  }

  // ============ 유틸리티 메서드 ============



  private void addBuyerInfoToModel(Model model, Buyer buyer) {
    // 기본 구매자 정보
    model.addAttribute("buyer", buyer);
    model.addAttribute("buyerId", buyer.getBuyerId());
    
    // 사용자 닉네임 (환영 메시지용)
    model.addAttribute("userNickname", buyer.getNickname());
    model.addAttribute("userName", buyer.getName());

    // 생년월일 포맷팅
    if (buyer.getBirth() != null) {
      java.time.LocalDate birthDate = buyer.getBirth().toInstant()
          .atZone(java.time.ZoneId.systemDefault())
          .toLocalDate();
      model.addAttribute("birthFormatted", birthDate.toString());
    } else {
      model.addAttribute("birthFormatted", null);
    }

    // 등급 정보 (코드 + 한글명)
    CodeNameInfo gubunInfo = buyerSVC.getGubunInfo(buyer);
    model.addAttribute("gubun", gubunInfo);

    // 상태 정보 (코드 + 한글명)
    CodeNameInfo statusInfo = buyerSVC.getStatusInfo(buyer);
    model.addAttribute("status", statusInfo);

    // 부가 정보
    model.addAttribute("canLogin", buyerSVC.canLogin(buyer));
    model.addAttribute("isWithdrawn", buyerSVC.isWithdrawn(buyer));
  }

  private Buyer getAuthenticatedBuyer(HttpSession session) {
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        log.warn("세션에 로그인 정보가 없습니다.");
        return null;
      }
      
      if (!"BUYER".equals(loginMember.getMemberType())) {
        log.warn("구매자 세션이 아닙니다: {}", loginMember.getMemberType());
        return null;
      }
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
      if (buyerOpt.isPresent()) {
        Buyer buyer = buyerOpt.get();
        
        if (!buyerSVC.canLogin(buyer)) {
          log.warn("로그인할 수 없는 상태의 회원: ID={}", loginMember.getId());
          return null;
        }
        
        return buyer;
      } else {
        log.warn("DB에서 회원 정보를 찾을 수 없습니다. ID: {}", loginMember.getId());
        return null;
      }
      
    } catch (Exception e) {
      log.error("사용자 인증 중 오류 발생", e);
      return null;
    }
  }
}
