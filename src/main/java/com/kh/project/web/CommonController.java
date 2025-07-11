package com.kh.project.web;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.Seller;
import com.kh.project.web.common.LoginMember;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * 공통 컨트롤러 (로그인/로그아웃/홈)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CommonController {
  
  private final BuyerSVC buyerSVC;
  private final SellerSVC sellerSVC;

  /**
   * 메인 페이지
   */
  @GetMapping("/")
  public String home(HttpSession session, Model model) {
    log.info("메인 페이지 요청");
    
    // 로그인 사용자 정보 조회
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
    
    return "home";
  }

  /**
   * 공통 로그인 선택 페이지 (구매자/판매자 선택)
   */
  @GetMapping({"/login", "/common/select_login", "/common/select-login"})
  public String loginSelect() {
    log.info("로그인 유형 선택 페이지 요청");
    return "common/select_login";
  }

  /**
   * 회원가입 유형 선택 페이지 (구매자/판매자 선택)
   */
  @GetMapping({"/signup", "/common/select_signup"})
  public String signupSelect() {
    log.info("회원가입 유형 선택 페이지 요청");
    return "common/select_signup";
  }

  /**
   * 로그아웃 처리 (GET/POST 모두 지원)
   */
  @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
  public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
    try {
      if (session != null) {
        Object loginMember = session.getAttribute("loginMember");
        session.invalidate();

        if (loginMember != null) {
          log.info("로그아웃 처리 완료 - 세션 무효화");
          redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");
        } else {
          log.info("이미 로그아웃된 상태");
          redirectAttributes.addFlashAttribute("message", "이미 로그아웃된 상태입니다.");
        }
      }

      return "redirect:/";

    } catch (Exception e) {
      log.error("로그아웃 처리 중 오류 발생", e);
      redirectAttributes.addFlashAttribute("message", "서버 오류가 발생했습니다.");
      return "redirect:/";
    }
  }

  @GetMapping("/common/juso-popup")
  public String jusoPopup() {
    log.info("주소 검색 팝업 요청");
    return "common/juso_popup";
  }
}