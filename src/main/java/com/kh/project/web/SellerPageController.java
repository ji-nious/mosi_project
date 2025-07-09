package com.kh.project.web;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.web.common.CodeNameInfo;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.dto.LoginForm;
import com.kh.project.web.common.dto.SellerSignupForm;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
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

  @GetMapping("/login")
  public String sellerLogin(Model model) {
    log.info("판매자 로그인 페이지 요청");
    model.addAttribute("loginForm", new LoginForm());
    return "seller/seller_login";
  }
  
  @GetMapping({"/signup", "/join"})
  public String sellerSignup(Model model) {
    log.info("판매자 회원가입 페이지 요청");
    model.addAttribute("sellerSignupForm", new SellerSignupForm());
    return "seller/seller_signup";
  }

  @GetMapping("/mypage")
  public String sellerMypage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 마이페이지 요청");

    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }

    addSellerInfoToModel(model, seller);
    return "seller/seller_mypage";
  }
  
  @GetMapping("/update")
  public String sellerUpdate(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 정보 수정 페이지 요청");
    
    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }
    
    addSellerInfoToModel(model, seller);
    return "seller/seller_update";
  }
  
  @GetMapping("/withdraw")
  public String sellerWithdraw(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
    log.info("판매자 탈퇴 페이지 요청");
    
    Seller seller = getAuthenticatedSeller(session);
    if (seller == null) {
      redirectAttributes.addFlashAttribute("message", "로그인이 필요합니다.");
      return "redirect:/seller/login";
    }
    
    model.addAttribute("sellerId", seller.getSellerId());
    model.addAttribute("email", seller.getEmail());
    model.addAttribute("name", seller.getName());
    
    return "seller/seller_withdraw";
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
  
  // ============ 호환성 유지 메서드들 ============
  
  @GetMapping("/{id}")
  public String detail(@PathVariable Long id, Model model, HttpSession session, 
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
  public String editForm(@PathVariable Long id, Model model, HttpSession session,
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

    // 등급 정보 (코드 + 한글명)
    CodeNameInfo gubunInfo = sellerSVC.getGubunInfo(seller);
    model.addAttribute("gubun", gubunInfo);

    // 상태 정보 (코드 + 한글명)
    CodeNameInfo statusInfo = sellerSVC.getStatusInfo(seller);
    model.addAttribute("status", statusInfo);

    // 부가 정보
    model.addAttribute("canLogin", sellerSVC.canLogin(seller));
    model.addAttribute("isWithdrawn", sellerSVC.isWithdrawn(seller));

    log.debug("모델에 판매자 정보 추가 완료: ID={}, 등급={}, 상태={}", 
             seller.getSellerId(), gubunInfo.getName(), statusInfo.getName());
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
