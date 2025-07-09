package com.kh.project.web;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.dto.BuyerSignupForm;
import com.kh.project.web.common.dto.LoginForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * 구매자 페이지 컨트롤러
 */
@Slf4j
@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerPageController {
  
  private final BuyerSVC buyerSVC;

  @GetMapping("/login")
  public String buyerLogin(Model model) {
    log.info("구매자 로그인 페이지 요청");
    model.addAttribute("loginForm", new LoginForm());
    return "buyer/buyer_login";
  }
  
  @GetMapping({"/signup", "/join"})
  public String buyerSignup(Model model) {
    log.info("구매자 회원가입 페이지 요청");
    model.addAttribute("buyerSignupForm", new BuyerSignupForm());
    return "buyer/buyer_signup";
  }

  @GetMapping("/mypage")
  public String buyerMypage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("구매자 마이페이지 요청");

    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }

    addBuyerInfoToModel(model, buyer);
    return "buyer/buyer_mypage";
  }
  
  @GetMapping("/update")
  public String buyerUpdate(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("구매자 정보 수정 페이지 요청");
    
    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }
    
    addBuyerInfoToModel(model, buyer);
    return "buyer/buyer_update";
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
  
  @GetMapping("/info")
  public String buyerInfo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("구매자 정보 조회 페이지 요청");
    
    Buyer buyer = getAuthenticatedBuyer(session);
    if (buyer == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/buyer/login";
    }
    
    addBuyerInfoToModel(model, buyer);
    return "buyer/buyer_info";
  }
  
  // ============ 호환성 유지 메서드들 ============
  
  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model, HttpSession session, 
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
  public String editForm(@PathVariable Long id, Model model, HttpSession session,
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

    // 등급 정보 (코드 + 한글명)
    CodeNameInfo gubunInfo = buyerSVC.getGubunInfo(buyer);
    model.addAttribute("gubun", gubunInfo);

    // 상태 정보 (코드 + 한글명)
    CodeNameInfo statusInfo = buyerSVC.getStatusInfo(buyer);
    model.addAttribute("status", statusInfo);

    // 부가 정보
    model.addAttribute("canLogin", buyerSVC.canLogin(buyer));
    model.addAttribute("isWithdrawn", buyerSVC.isWithdrawn(buyer));

    log.debug("모델에 구매자 정보 추가 완료: ID={}, 등급={}, 상태={}", 
             buyer.getBuyerId(), gubunInfo.getName(), statusInfo.getName());
  }

  private Buyer getAuthenticatedBuyer(HttpSession session) {
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null) {
        log.warn("세션에 로그인 정보가 없습니다.");
        return null;
      }
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
      if (buyerOpt.isPresent()) {
        Buyer buyer = buyerOpt.get();
        
        if (!buyerSVC.canLogin(buyer)) {
          log.warn("로그인할 수 없는 상태의 회원: ID={}", loginMember.getId());
          return null;
        }
        
        log.debug("인증된 사용자 정보 조회 성공: ID={}, 이메일={}", 
                 buyer.getBuyerId(), buyer.getEmail());
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
