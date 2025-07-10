package com.kh.project.web.api;

import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.web.common.ApiResponse;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.dto.SellerSignupForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers")
@Validated
public class SellerApiController {

  private final SellerSVC sellerSVC;

  @SuppressWarnings("unchecked")
  @PostMapping("/signup")
  public ResponseEntity<Map<String, Object>> signup(
          @Valid @RequestBody SellerSignupForm signupForm,
          HttpSession session) {
    log.info("판매자 회원가입 요청: {}", signupForm);

    Seller seller = new Seller();
    seller.setEmail(signupForm.getEmail());
    seller.setPassword(signupForm.getPassword());
    seller.setBizRegNo(signupForm.getBusinessNumber());
    seller.setShopName(signupForm.getStoreName());
    seller.setName(signupForm.getName());

    // 주소 필드 통합
    String fullAddress = String.format("(%s) %s %s",
            signupForm.getPostcode(), signupForm.getAddress(), signupForm.getDetailAddress()).trim();
    seller.setShopAddress(fullAddress);

    seller.setTel(signupForm.getTel());
    // seller.setBirth(signupForm.getBirth()); // 타입 불일치로 주석 처리, 필요 시 변환 로직 추가

    Seller savedSeller = sellerSVC.join(seller);
    log.info("판매자 회원가입 성공: {}", savedSeller);

    // 자동 로그인 (세션 사용)
    LoginMember loginMember = LoginMember.seller(savedSeller.getSellerId(), savedSeller.getEmail());
    session.setAttribute("loginMember", loginMember);
    session.setMaxInactiveInterval(1800); // 30분

    Map<String, Object> response = ApiResponse.joinSuccess(savedSeller, MemberGubun.getDescriptionByCode(savedSeller.getGubun()));
    Map<String, Object> data = (Map<String, Object>) response.get("data");
    if (data == null) {
      data = new HashMap<>();
      response.put("data", data);
    }
    data.put("redirectUrl", "/"); // 리다이렉트 URL 추가

    return ResponseEntity.ok(response);
  }
}
