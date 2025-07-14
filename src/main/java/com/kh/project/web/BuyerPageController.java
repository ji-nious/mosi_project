package com.kh.project.web;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.util.CommonConstants;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.SessionService;
import com.kh.project.web.common.form.BuyerEditForm;
import com.kh.project.web.common.form.BuyerSignupForm;
import com.kh.project.web.common.form.LoginForm;
import com.kh.project.web.common.form.MemberStatusInfo;
import com.kh.project.web.exception.BusinessException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * 구매자 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/buyer")
public class BuyerPageController {

  private final BuyerSVC buyerSVC;
  private final SessionService sessionService;

  /**
   * 로그인 페이지
   */
  @GetMapping("/login")
  public String loginPage(Model model) {
    log.info("구매자 로그인 페이지 호출");
    model.addAttribute("loginForm", new LoginForm());
    return "buyer/buyer_login";
  }

  /**
   * 로그인 처리
   */
  @PostMapping("/login")
  public String login(@Valid @ModelAttribute LoginForm loginForm, 
                      BindingResult bindingResult, 
                      HttpSession session, 
                      RedirectAttributes redirectAttributes) {
    log.info("구매자 로그인 처리: {}", loginForm.getEmail());
    
    if (bindingResult.hasErrors()) {
      log.warn("로그인 폼 검증 실패: {}", bindingResult.getAllErrors());
      return "buyer/buyer_login";
    }
    
    try {
      Buyer buyer = buyerSVC.login(loginForm.getEmail(), loginForm.getPassword());
      
      LoginMember loginMember = LoginMember.buyer(buyer.getBuyerId(), buyer.getEmail());
      session.setAttribute(CommonConstants.LOGIN_MEMBER_KEY, loginMember);
      session.setMaxInactiveInterval(CommonConstants.SESSION_TIMEOUT);
      
      log.info("구매자 로그인 성공: buyerId={}", buyer.getBuyerId());
      return "redirect:/buyer/info";
    } catch (BusinessException e) {
      log.warn("구매자 로그인 실패: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/buyer/login";
    }
  }

  /**
   * 회원가입 페이지
   */
  @GetMapping("/signup")
  public String signupPage(Model model) {
    log.info("구매자 회원가입 페이지 호출");
    model.addAttribute("buyerSignupForm", new BuyerSignupForm());
    return "buyer/buyer_signup";
  }

  // 회원가입 처리
  @PostMapping("/signup")
  public String signup(@Valid @ModelAttribute BuyerSignupForm signupForm, 
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
    log.info("구매자 회원가입 처리: {}", signupForm.getEmail());
    
    if (bindingResult.hasErrors()) {
      log.warn("회원가입 폼 검증 실패: {}", bindingResult.getAllErrors());
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
      if (signupForm.getBirth() != null) {
        buyer.setBirth(java.sql.Date.valueOf(signupForm.getBirth()));
      }
      buyer.setAddress(signupForm.getAddress());
      
      Buyer savedBuyer = buyerSVC.join(buyer);
      log.info("구매자 회원가입 성공: buyerId={}", savedBuyer.getBuyerId());
      
      return "redirect:/common/signup-complete";
    } catch (BusinessException e) {
      log.warn("구매자 회원가입 실패: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/buyer/signup";
    }
  }

  // 내 정보 조회 페이지
  @GetMapping("/info")
  public String infoPage(HttpSession session, Model model) {
    log.info("구매자 정보 조회 페이지 호출");
    
    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null || !CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
      log.warn("로그인 정보가 없거나 구매자가 아님");
      return "redirect:/buyer/login";
    }
    
    Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
    if (buyerOpt.isEmpty()) {
      log.warn("구매자 정보를 찾을 수 없음: buyerId={}", loginMember.getId());
      return "redirect:/buyer/login";
    }
    
    Buyer buyer = buyerOpt.get();
    model.addAttribute("buyer", buyer);
    
    if (buyer.getBirth() != null) {
      String birthFormatted = LocalDate.ofInstant(buyer.getBirth().toInstant(), 
                                                 java.time.ZoneId.systemDefault())
                                       .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      model.addAttribute("birthFormatted", birthFormatted);
    }
    
    log.info("구매자 정보 조회 완료: buyerId={}", buyer.getBuyerId());
    return "buyer/buyer_info";
  }

  // 정보 수정 페이지
  @GetMapping("/edit")
  public String editPage(HttpSession session, Model model) {
    log.info("구매자 정보 수정 페이지 호출");
    
    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null || !CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
      log.warn("로그인 정보가 없거나 구매자가 아님");
      return "redirect:/buyer/login";
    }
    
    Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
    if (buyerOpt.isEmpty()) {
      log.warn("구매자 정보를 찾을 수 없음: buyerId={}", loginMember.getId());
      return "redirect:/buyer/login";
    }
    
    Buyer buyer = buyerOpt.get();
    BuyerEditForm editForm = new BuyerEditForm();
    editForm.setName(buyer.getName());
    editForm.setNickname(buyer.getNickname());
    editForm.setTel(buyer.getTel());
    editForm.setGender(buyer.getGender());
    editForm.setAddress(buyer.getAddress());
    
    if (buyer.getBirth() != null) {
      editForm.setBirth(LocalDate.ofInstant(buyer.getBirth().toInstant(), 
                                           java.time.ZoneId.systemDefault()));
    }
    
    model.addAttribute("buyerEditForm", editForm);
    model.addAttribute("buyerEmail", buyer.getEmail());
    log.info("구매자 정보 수정 페이지 로드 완료: buyerId={}", buyer.getBuyerId());
    return "buyer/buyer_edit";
  }

  // 정보 수정 처리
  @PostMapping("/edit")
  public String edit(@Valid @ModelAttribute BuyerEditForm editForm, 
                     BindingResult bindingResult, 
                     HttpSession session,
                     RedirectAttributes redirectAttributes) {
    log.info("구매자 정보 수정 처리");
    
    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null || !CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
      log.warn("로그인 정보가 없거나 구매자가 아님");
      return "redirect:/buyer/login";
    }
    
    if (bindingResult.hasErrors()) {
      log.warn("수정 폼 검증 실패: {}", bindingResult.getAllErrors());
      return "buyer/buyer_edit";
    }
    
    try {
      Buyer updateBuyer = new Buyer();
      updateBuyer.setName(editForm.getName());
      updateBuyer.setNickname(editForm.getNickname());
      updateBuyer.setTel(editForm.getTel());
      updateBuyer.setGender(editForm.getGender());
      updateBuyer.setAddress(editForm.getAddress());
      
      if (editForm.getBirth() != null) {
        updateBuyer.setBirth(java.sql.Date.valueOf(editForm.getBirth()));
      }
      
      // 새 비밀번호는 필수이므로 항상 설정
      updateBuyer.setPassword(editForm.getPassword());
      
      int updatedRows = buyerSVC.update(loginMember.getId(), updateBuyer);
      
      if (updatedRows > 0) {
        log.info("구매자 정보 수정 성공: buyerId={}", loginMember.getId());
        redirectAttributes.addFlashAttribute("message", "정보가 성공적으로 수정되었습니다.");
        return "redirect:/buyer/info";
      } else {
        log.warn("구매자 정보 수정 실패: buyerId={}", loginMember.getId());
        redirectAttributes.addFlashAttribute("error", "정보 수정에 실패했습니다.");
        return "redirect:/buyer/edit";
      }
    } catch (BusinessException e) {
      log.warn("구매자 정보 수정 실패: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/buyer/edit";
    }
  }

  // 탈퇴 안내 페이지
  @GetMapping("/withdraw")
  public String withdrawPage() {
    log.info("구매자 탈퇴 1단계 페이지 호출");
    return "buyer/buyer_withdraw";
  }

  // 탈퇴 현황 조회 페이지
  @PostMapping("/withdraw-status")
  public String withdrawStatusPage(@RequestParam("reason") String reason, 
                                   HttpSession session, 
                                   Model model) {
    log.info("구매자 탈퇴 2단계 페이지 호출: reason={}", reason);
    
    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null || !CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
      log.warn("로그인 정보가 없거나 구매자가 아님");
      return "redirect:/buyer/login";
    }
    
    try {
      MemberStatusInfo statusInfo = buyerSVC.getServiceUsage(loginMember.getId());
      if (statusInfo == null) {
        log.warn("구매자 서비스 이용현황 조회 실패: buyerId={}", loginMember.getId());
        statusInfo = MemberStatusInfo.forBuyer(0, 0); // 기본값으로 생성
      }
      
      model.addAttribute("reason", reason);
      model.addAttribute("memberStatus", statusInfo);
      
      log.info("구매자 탈퇴 2단계 페이지 로드 완료: buyerId={}", loginMember.getId());
      return "buyer/buyer_withdraw_status";
    } catch (Exception e) {
      log.error("구매자 탈퇴 2단계 페이지 로드 실패: {}", e.getMessage());
      model.addAttribute("reason", reason);
      model.addAttribute("memberStatus", MemberStatusInfo.forBuyer(0, 0)); // 기본값으로 생성
      return "buyer/buyer_withdraw_status";
    }
  }

  // 탈퇴 처리
  @PostMapping("/withdraw-final")
  public String withdrawFinal(@RequestParam("reason") String reason, 
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
    log.info("구매자 탈퇴 처리: reason={}", reason);
    
    LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
    if (loginMember == null || !CommonConstants.MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
      log.warn("로그인 정보가 없거나 구매자가 아님");
      return "redirect:/buyer/login";
    }
    
    try {
      if (!buyerSVC.canWithdraw(loginMember.getId())) {
        log.warn("구매자 탈퇴 불가능: buyerId={}", loginMember.getId());
        redirectAttributes.addFlashAttribute("error", "현재 탈퇴할 수 없습니다. 진행 중인 주문이 있습니다.");
        return "redirect:/buyer/withdraw";
      }
      
      int withdrawnRows = buyerSVC.withdraw(loginMember.getId(), reason);
      
      if (withdrawnRows > 0) {
        log.info("구매자 탈퇴 성공: buyerId={}", loginMember.getId());
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "회원 탈퇴가 완료되었습니다.");
        return "redirect:/";
      } else {
        log.warn("구매자 탈퇴 실패: buyerId={}", loginMember.getId());
        redirectAttributes.addFlashAttribute("error", "탈퇴 처리에 실패했습니다.");
        return "redirect:/buyer/withdraw";
      }
    } catch (BusinessException e) {
      log.warn("구매자 탈퇴 실패: {}", e.getMessage());
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/buyer/withdraw";
    }
  }

  @PostMapping("/verify-password")
  @ResponseBody
  public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request, HttpSession session) {
    String password = request.get("password");
    return sessionService.verifyPassword(session, password);
  }
}
