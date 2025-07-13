package com.kh.project.web.api;

import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.web.common.ApiResponse;
import com.kh.project.web.common.AuthUtils;
import com.kh.project.web.common.CommonConstants;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.MemberGubunUtils;
import com.kh.project.web.common.dto.SellerSignupForm;
import com.kh.project.web.exception.BusinessException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.kh.project.web.common.dto.MemberStatusInfo;

/**
 * 판매자 API 컨트롤러
 * 판매자 회원가입, 로그인, 정보조회/수정, 탈퇴, 등급관리 등 REST API 제공
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers")
@Validated
public class SellerApiController {

  private final SellerSVC sellerSVC;

  /**
   * 판매자 회원가입
   * 
   * @param signupForm 회원가입 정보
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 가입 결과 및 회원 정보
   */
  @PostMapping("/signup")
  public ResponseEntity<Map<String, Object>> signup(
          @Valid @RequestBody SellerSignupForm signupForm,
          HttpSession session) {
    log.info("판매자 회원가입 요청: {}", signupForm.getEmail());

    Seller seller = new Seller();
    seller.setEmail(signupForm.getEmail());
    seller.setPassword(signupForm.getPassword());
    seller.setBizRegNo(signupForm.getBusinessNumber());
    seller.setShopName(signupForm.getStoreName());
    seller.setName(signupForm.getName());
    seller.setShopAddress(String.format("(%s) %s %s",
            signupForm.getPostcode(), signupForm.getAddress(), signupForm.getDetailAddress()).trim());
    seller.setTel(signupForm.getTel());

    Seller savedSeller = sellerSVC.join(seller);
    log.info("판매자 회원가입 성공: sellerId={}", savedSeller.getSellerId());

    Map<String, Object> response = ApiResponse.joinSuccess(savedSeller, 
        MemberGubunUtils.getDescriptionByCode(savedSeller.getMemberGubun()));
    
    return ResponseEntity.ok(response);
  }

  /**
   * 판매자 로그인
   * 
   * @param loginRequest 로그인 요청 정보 (email, password)
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 로그인 결과 및 판매자 정보
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(
          @RequestBody Map<String, String> loginRequest, 
          HttpSession session) {
    try {
      String email = loginRequest.get("email");
      String password = loginRequest.get("password");
      log.info("판매자 로그인 요청: email={}", email);
      
      Seller seller = sellerSVC.login(email, password);
      LoginMember loginMember = LoginMember.seller(seller.getSellerId(), seller.getEmail());
      session.setAttribute(CommonConstants.LOGIN_MEMBER_KEY, loginMember);
      session.setMaxInactiveInterval(CommonConstants.SESSION_TIMEOUT);
      
      String gubunName = MemberGubunUtils.getDescriptionByCode(seller.getMemberGubun());
      boolean canLogin = sellerSVC.canLogin(seller);
      Map<String, Object> response = ApiResponse.loginSuccess(seller, gubunName, canLogin);
      
      log.info("판매자 로그인 성공: sellerId={}", seller.getSellerId());
      return ResponseEntity.ok(response);
    } catch (BusinessException e) {
      log.warn("판매자 로그인 실패 (비즈니스 로직): email={}", loginRequest.get("email"), e);
      Map<String, Object> response = ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다.");
      return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
      log.error("판매자 로그인 실패 (시스템 오류): email={}", loginRequest.get("email"), e);
      Map<String, Object> response = ApiResponse.error("로그인 처리 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 현재 로그인한 판매자 정보 조회
   * 
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 판매자 정보 및 추가 데이터
   */
  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> getSellerInfo(HttpSession session) {
    try {
      log.info("판매자 정보 조회 요청");
      LoginMember loginMember = (LoginMember) session.getAttribute(CommonConstants.LOGIN_MEMBER_KEY);
      if (loginMember == null || !CommonConstants.MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
        return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
      }
      
      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      if (sellerOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      
      Seller seller = sellerOpt.get();
      Map<String, Object> additionalData = Map.of(
              "gubunName", MemberGubunUtils.getDescriptionByCode(seller.getMemberGubun()),
              "canLogin", sellerSVC.canLogin(seller),
              "isWithdrawn", sellerSVC.isWithdrawn(seller)
      );
      
      Map<String, Object> response = ApiResponse.entitySuccess("회원 정보 조회 성공", seller, additionalData);
      log.info("판매자 정보 조회 성공: sellerId={}", seller.getSellerId());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("판매자 정보 조회 실패: error={}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error("회원 정보 조회에 실패했습니다."));
    }
  }

  /**
   * 특정 판매자 정보 조회 (본인만 가능)
   * 
   * @param sellerId 판매자 ID
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 판매자 정보
   */
  @GetMapping("/{sellerId}")
  public ResponseEntity<Map<String, Object>> getSeller(
          @PathVariable("sellerId") Long sellerId, 
          HttpSession session) {
    try {
      AuthUtils.validateSellerAccess(session, sellerId);
      Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
      if (sellerOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      
      Seller seller = sellerOpt.get();
      Map<String, Object> additionalData = Map.of(
          "gubunName", MemberGubunUtils.getDescriptionByCode(seller.getMemberGubun()),
          "canLogin", sellerSVC.canLogin(seller),
          "isWithdrawn", sellerSVC.isWithdrawn(seller)
      );
      
      return ResponseEntity.ok(ApiResponse.entitySuccess("회원 조회 성공", seller, additionalData));
    } catch (SecurityException e) {
      log.warn("권한 없는 회원 조회 시도: sellerId={}", sellerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("회원 조회 실패: sellerId={}", sellerId);
      return ResponseEntity.badRequest().body(ApiResponse.error("회원 정보 조회에 실패했습니다."));
    }
  }

  /**
   * 판매자 정보 수정
   * 
   * @param sellerId 판매자 ID
   * @param request 수정할 정보 (currentPassword 필수)
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 수정 결과
   */
  @PutMapping("/{sellerId}")
  public ResponseEntity<Map<String, Object>> update(
          @PathVariable("sellerId") Long sellerId, 
          @RequestBody Map<String, Object> request, 
          HttpSession session) {
    try {
      AuthUtils.validateSellerAccess(session, sellerId);
      
      String currentPassword = (String) request.get("currentPassword");
      if (currentPassword == null || currentPassword.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("현재 비밀번호는 필수입니다."));
      }
      
      if (!sellerSVC.checkPassword(sellerId, currentPassword)) {
        return ResponseEntity.badRequest().body(ApiResponse.error("현재 비밀번호가 일치하지 않습니다."));
      }
      
      Seller updateSeller = new Seller();
      if (request.get("name") != null) updateSeller.setName((String) request.get("name"));
      if (request.get("shopName") != null) updateSeller.setShopName((String) request.get("shopName"));
      if (request.get("tel") != null) updateSeller.setTel((String) request.get("tel"));
      if (request.get("shopAddress") != null) updateSeller.setShopAddress((String) request.get("shopAddress"));
      if (request.get("newPassword") != null && !((String) request.get("newPassword")).isBlank()) {
        updateSeller.setPassword((String) request.get("newPassword"));
      }
      
      int updatedRows = sellerSVC.update(sellerId, updateSeller);
      if (updatedRows > 0) {
        log.info("판매자 정보 수정 성공: sellerId={}", sellerId);
        return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다."));
      } else {
        return ResponseEntity.badRequest().body(ApiResponse.error("정보 수정에 실패했습니다."));
      }
    } catch (SecurityException e) {
      log.warn("권한 없는 정보 수정 시도: sellerId={}", sellerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("정보 수정 실패: sellerId={}, error={}", sellerId, e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * 판매자 탈퇴
   * 
   * @param sellerId 판매자 ID
   * @param request 탈퇴 정보 (password, reason 필수)
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 탈퇴 결과
   */
  @PostMapping("/{sellerId}/withdraw")
  public ResponseEntity<Map<String, Object>> withdraw(
          @PathVariable("sellerId") Long sellerId, 
          @RequestBody Map<String, Object> request, 
          HttpSession session) {
    try {
      AuthUtils.validateSellerAccess(session, sellerId);
      
      String password = (String) request.get("password");
      String reason = (String) request.get("reason");
      
      if (password == null || password.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호는 필수입니다."));
      }
      if (reason == null || reason.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("탈퇴 사유는 필수입니다."));
      }
      if (!sellerSVC.checkPassword(sellerId, password)) {
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 일치하지 않습니다."));
      }
      
      // 탈퇴 가능 여부 확인
      if (!sellerSVC.canWithdraw(sellerId)) {
        MemberStatusInfo statusInfo = sellerSVC.getServiceUsage(sellerId);
        Map<String, Object> usage = statusInfo.toMap();
        @SuppressWarnings("unchecked")
        List<String> blockReasons = (List<String>) usage.get("withdrawBlockReasons");
        String reasonText = String.join(", ", blockReasons);
        return ResponseEntity.badRequest().body(ApiResponse.error("탈퇴할 수 없습니다. 사유: " + reasonText));
      }
      
      int withdrawnRows = sellerSVC.withdraw(sellerId, reason);
      if (withdrawnRows > 0) {
        if (session != null) {
          session.invalidate();
        }
        log.info("판매자 탈퇴 완료: sellerId={}, reason={}", sellerId, reason);
        return ResponseEntity.ok(ApiResponse.success("탈퇴 처리가 완료되었습니다."));
      } else {
        return ResponseEntity.badRequest().body(ApiResponse.error("탈퇴 처리에 실패했습니다."));
      }
    } catch (SecurityException e) {
      log.warn("권한 없는 탈퇴 시도: sellerId={}", sellerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("탈퇴 처리 실패: sellerId={}, error={}", sellerId, e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * 비밀번호 확인 API
   * 
   * @param request 비밀번호 정보
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 확인 결과
   */
  @PostMapping("/verify-password")
  public ResponseEntity<Map<String, Object>> verifyPassword(
          @RequestBody Map<String, Object> request,
          HttpSession session) {
    try {
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null || !"SELLER".equals(loginMember.getMemberType())) {
        return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
      }
      
      String password = (String) request.get("password");
      if (password == null || password.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호는 필수입니다."));
      }
      
      boolean isValid = sellerSVC.checkPassword(loginMember.getId(), password);
      if (isValid) {
        log.info("판매자 비밀번호 확인 성공: sellerId={}", loginMember.getId());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 확인 성공"));
      } else {
        log.warn("판매자 비밀번호 확인 실패: sellerId={}", loginMember.getId());
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 올바르지 않습니다."));
      }
    } catch (Exception e) {
      log.error("비밀번호 확인 중 오류 발생: error={}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호 확인에 실패했습니다."));
    }
  }
}
