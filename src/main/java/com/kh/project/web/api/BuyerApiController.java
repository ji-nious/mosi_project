package com.kh.project.web.api;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.web.common.ApiResponse;
import com.kh.project.web.common.AuthUtils;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.MemberGubunUtils;
import com.kh.project.web.common.dto.BuyerSignupForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * 구매자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
@Validated
public class BuyerApiController {

  private final BuyerSVC buyerSVC;

  /**
   * 구매자 회원가입
   * 
   * @param signupForm 회원가입 정보
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 가입 결과 및 회원 정보
   */
  @PostMapping("/signup")
  public ResponseEntity<Map<String, Object>> signup(
          @Valid @RequestBody BuyerSignupForm signupForm,
          HttpSession session) {
    log.info("구매자 회원가입 요청: {}", signupForm.getEmail());

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

    // 자동 로그인
                LoginMember loginMember = LoginMember.buyer(savedBuyer.getBuyerId(), savedBuyer.getEmail());
    session.setAttribute("loginMember", loginMember);
    session.setMaxInactiveInterval(1800);

    Map<String, Object> response = ApiResponse.joinSuccess(savedBuyer, 
        MemberGubunUtils.getDescriptionByCode(savedBuyer.getMemberGubun()));
    
    return ResponseEntity.ok(response);
  }

  /**
   * 구매자 로그인
   * 
   * @param loginRequest 로그인 요청 정보 (email, password)
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 로그인 결과 및 구매자 정보
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(
          @RequestBody Map<String, String> loginRequest, 
          HttpSession session) {
    try {
      String email = loginRequest.get("email");
      String password = loginRequest.get("password");
      log.info("구매자 로그인 요청: email={}", email);
      
      Buyer buyer = buyerSVC.login(email, password);
                  LoginMember loginMember = LoginMember.buyer(buyer.getBuyerId(), buyer.getEmail());
      session.setAttribute("loginMember", loginMember);
      session.setMaxInactiveInterval(1800);
      
      String gubunName = MemberGubunUtils.getDescriptionByCode(buyer.getMemberGubun());
      boolean canLogin = buyerSVC.canLogin(buyer);
      Map<String, Object> response = ApiResponse.loginSuccess(buyer, gubunName, canLogin);
      
      log.info("구매자 로그인 성공: buyerId={}", buyer.getBuyerId());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("구매자 로그인 실패: email={}, error={}", loginRequest.get("email"), e.getMessage());
      Map<String, Object> response = ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 현재 로그인한 구매자 정보 조회
   * 
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 구매자 정보 및 추가 데이터
   */
  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> getBuyerInfo(HttpSession session) {
    try {
      log.info("구매자 정보 조회 요청");
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null || !"BUYER".equals(loginMember.getMemberType())) {
        return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
      }
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
      if (buyerOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      
      Buyer buyer = buyerOpt.get();
      Map<String, Object> additionalData = Map.of(
              "gubunName", MemberGubunUtils.getDescriptionByCode(buyer.getMemberGubun()),
              "canLogin", buyerSVC.canLogin(buyer),
              "isWithdrawn", buyerSVC.isWithdrawn(buyer)
      );
      
      Map<String, Object> response = ApiResponse.entitySuccess("회원 정보 조회 성공", buyer, additionalData);
      log.info("구매자 정보 조회 성공: buyerId={}", buyer.getBuyerId());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("구매자 정보 조회 실패: error={}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error("회원 정보 조회에 실패했습니다."));
    }
  }

  /**
   * 특정 구매자 정보 조회 (본인만 가능)
   * 
   * @param buyerId 구매자 ID
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 구매자 정보
   */
  @GetMapping("/{buyerId}")
  public ResponseEntity<Map<String, Object>> getBuyer(
          @PathVariable("buyerId") Long buyerId, 
          HttpSession session) {
    try {
      AuthUtils.validateBuyerAccess(session, buyerId);
      Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
      if (buyerOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      
      Buyer buyer = buyerOpt.get();
      Map<String, Object> additionalData = Map.of(
          "gubunName", MemberGubunUtils.getDescriptionByCode(buyer.getMemberGubun()),
          "canLogin", buyerSVC.canLogin(buyer),
          "isWithdrawn", buyerSVC.isWithdrawn(buyer)
      );
      
      return ResponseEntity.ok(ApiResponse.entitySuccess("회원 조회 성공", buyer, additionalData));
    } catch (SecurityException e) {
      log.warn("권한 없는 회원 조회 시도: buyerId={}", buyerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("회원 조회 실패: buyerId={}", buyerId);
      return ResponseEntity.badRequest().body(ApiResponse.error("회원 정보 조회에 실패했습니다."));
    }
  }

  /**
   * 구매자 정보 수정
   * 
   * @param buyerId 구매자 ID
   * @param request 수정할 정보 (currentPassword 필수)
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 수정 결과
   */
  @PutMapping("/{buyerId}")
  public ResponseEntity<Map<String, Object>> update(
          @PathVariable("buyerId") Long buyerId, 
          @RequestBody Map<String, Object> request, 
          HttpSession session) {
    try {
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      String currentPassword = (String) request.get("currentPassword");
      if (currentPassword == null || currentPassword.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("현재 비밀번호는 필수입니다."));
      }
      
      if (!buyerSVC.checkPassword(buyerId, currentPassword)) {
        return ResponseEntity.badRequest().body(ApiResponse.error("현재 비밀번호가 일치하지 않습니다."));
      }
      
      Buyer updateBuyer = new Buyer();
      if (request.get("name") != null) updateBuyer.setName((String) request.get("name"));
      if (request.get("nickname") != null) updateBuyer.setNickname((String) request.get("nickname"));
      if (request.get("tel") != null) updateBuyer.setTel((String) request.get("tel"));
      if (request.get("gender") != null) updateBuyer.setGender((String) request.get("gender"));
      if (request.get("address") != null) updateBuyer.setAddress((String) request.get("address"));
      if (request.get("newPassword") != null && !((String) request.get("newPassword")).isBlank()) {
        updateBuyer.setPassword((String) request.get("newPassword"));
      }
      
      int updatedRows = buyerSVC.update(buyerId, updateBuyer);
      if (updatedRows > 0) {
        log.info("구매자 정보 수정 성공: buyerId={}", buyerId);
        return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다."));
      } else {
        return ResponseEntity.badRequest().body(ApiResponse.error("정보 수정에 실패했습니다."));
      }
    } catch (SecurityException e) {
      log.warn("권한 없는 정보 수정 시도: buyerId={}", buyerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("정보 수정 실패: buyerId={}, error={}", buyerId, e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * 구매자 탈퇴
   * 
   * @param buyerId 구매자 ID
   * @param request 탈퇴 정보 (password, reason 필수)
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 탈퇴 결과
   */
  @PostMapping("/{buyerId}/withdraw")
  public ResponseEntity<Map<String, Object>> withdraw(
          @PathVariable("buyerId") Long buyerId, 
          @RequestBody Map<String, Object> request, 
          HttpSession session) {
    try {
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      String password = (String) request.get("password");
      String reason = (String) request.get("reason");
      if (password == null || password.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호는 필수입니다."));
      }
      if (reason == null || reason.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("탈퇴 사유는 필수입니다."));
      }
      
      if (!buyerSVC.checkPassword(buyerId, password)) {
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 일치하지 않습니다."));
      }
      
      int withdrawnRows = buyerSVC.withdraw(buyerId, reason);
      if (withdrawnRows > 0) {
        session.invalidate();
        log.info("구매자 탈퇴 성공: buyerId={}", buyerId);
        return ResponseEntity.ok(ApiResponse.success("탈퇴 처리되었습니다."));
      } else {
        return ResponseEntity.badRequest().body(ApiResponse.error("탈퇴 처리에 실패했습니다."));
      }
    } catch (SecurityException e) {
      log.warn("권한 없는 탈퇴 시도: buyerId={}", buyerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("탈퇴 처리 실패: buyerId={}, error={}", buyerId, e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  /**
   * 구매자 서비스 이용현황 조회
   * 
   * @param buyerId 구매자 ID
   * @param session HTTP 세션
   * @return ResponseEntity<Map<String, Object>> 서비스 이용현황 정보
   */
  @GetMapping("/{buyerId}/usage")
  public ResponseEntity<Map<String, Object>> getUsage(
          @PathVariable("buyerId") Long buyerId, 
          HttpSession session) {
    try {
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      Map<String, Object> usage = Map.of(
          "orderCount", 42,
          "totalAmount", 125000,
          "points", 2500,
          "couponCount", 3
      );
      
      return ResponseEntity.ok(ApiResponse.success("이용현황 조회 성공", usage));
    } catch (SecurityException e) {
      log.warn("권한 없는 이용현황 조회 시도: buyerId={}", buyerId);
      return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
    } catch (Exception e) {
      log.error("이용현황 조회 실패: buyerId={}", buyerId);
      return ResponseEntity.badRequest().body(ApiResponse.error("이용현황 조회에 실패했습니다."));
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
      if (loginMember == null || !"BUYER".equals(loginMember.getMemberType())) {
        return ResponseEntity.status(401).body(ApiResponse.error("로그인이 필요합니다."));
      }
      
      String password = (String) request.get("password");
      if (password == null || password.isBlank()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호는 필수입니다."));
      }
      
      boolean isValid = buyerSVC.checkPassword(loginMember.getId(), password);
      if (isValid) {
        log.info("구매자 비밀번호 확인 성공: buyerId={}", loginMember.getId());
        return ResponseEntity.ok(ApiResponse.success("비밀번호 확인 성공"));
      } else {
        log.warn("구매자 비밀번호 확인 실패: buyerId={}", loginMember.getId());
        return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 올바르지 않습니다."));
      }
    } catch (Exception e) {
      log.error("비밀번호 확인 중 오류 발생: error={}", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호 확인에 실패했습니다."));
    }
  }

  /**
   * 이메일 중복 확인
   * 
   * @param email 확인할 이메일
   * @return ResponseEntity<Map<String, Object>> 중복 확인 결과
   */
  @GetMapping("/check-email")
  public ResponseEntity<Map<String, Object>> checkEmailDuplication(@RequestParam String email) {
    try {
      boolean exists = buyerSVC.existsByEmail(email);
      
      if (exists) {
        return ResponseEntity.ok(ApiResponse.error("이미 사용중인 이메일입니다."));
      } else {
        return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다."));
      }
    } catch (Exception e) {
      log.error("이메일 중복 확인 실패: email={}, error={}", email, e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error("이메일 중복 확인에 실패했습니다."));
    }
  }

  /**
   * 닉네임 중복 확인
   * 
   * @param nickname 확인할 닉네임
   * @return ResponseEntity<Map<String, Object>> 중복 확인 결과
   */
  @GetMapping("/check-nickname")
  public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
    try {
      boolean exists = buyerSVC.existsByNickname(nickname);
      Map<String, Object> data = Map.of(
          "exists", exists, "available", !exists, "nickname", nickname);
      String message = exists ? "이미 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
      return ResponseEntity.ok(ApiResponse.success(message, data));
    } catch (Exception e) {
      log.error("닉네임 중복 체크 실패: nickname={}, error={}", nickname, e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error("닉네임 중복 체크에 실패했습니다."));
    }
  }
}