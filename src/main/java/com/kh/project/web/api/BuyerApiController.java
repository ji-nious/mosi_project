package com.kh.project.web.api;

import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.web.common.LoginMember;
import com.kh.project.web.common.ApiResponse;
import com.kh.project.web.common.AuthUtils;
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
 * 구매자 API 컨트롤러 (표준화된 응답)
 */
@Slf4j
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
@Validated
public class BuyerApiController {

  private final BuyerSVC buyerSVC;

  /**
   * 회원가입
   */
  @PostMapping("/join")
  public ResponseEntity<Map<String, Object>> join(@Valid @RequestBody Buyer buyer) {
    try {
      log.info("구매자 회원가입 요청: email={}", buyer.getEmail());
      
      Buyer savedBuyer = buyerSVC.join(buyer);
      String gubunName = MemberGubun.getDescriptionByCode(savedBuyer.getGubun());
      
      Map<String, Object> response = ApiResponse.joinSuccess(savedBuyer, gubunName);
      
      log.info("구매자 회원가입 성공: buyerId={}", savedBuyer.getBuyerId());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("구매자 회원가입 실패: email={}, error={}", buyer.getEmail(), e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 로그인
   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
    try {
      String email = loginRequest.get("email");
      String password = loginRequest.get("password");
      
      log.info("구매자 로그인 요청: email={}", email);

      Buyer buyer = buyerSVC.login(email, password);

      // 세션 보안 강화
      LoginMember loginMember = LoginMember.buyer(buyer.getBuyerId(), buyer.getEmail());
      session.setAttribute("loginMember", loginMember);
      
      // 세션 보안 설정
      session.setMaxInactiveInterval(1800); // 30분 타임아웃
      
      String gubunName = MemberGubun.getDescriptionByCode(buyer.getGubun());
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
   * 로그아웃
   */
  @PostMapping("/logout")
  public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
    try {
      if (session != null) {
        session.invalidate();
      }
      
      Map<String, Object> response = ApiResponse.success("로그아웃되었습니다.");
      log.info("구매자 로그아웃 성공");
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("로그아웃 처리 중 오류 발생", e);
      
      Map<String, Object> response = ApiResponse.error("로그아웃 처리에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 회원 정보 조회 (인증 필요)
   */
  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> getBuyerInfo(HttpSession session) {
    try {
      log.info("구매자 정보 조회 요청");
      
      // 세션 검증 강화
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null || !"BUYER".equals(loginMember.getMemberType())) {
        Map<String, Object> response = ApiResponse.error("로그인이 필요합니다.");
        return ResponseEntity.status(401).body(response);
      }

      Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
      
      if (buyerOpt.isEmpty()) {
        Map<String, Object> response = ApiResponse.error("존재하지 않는 회원입니다.");
        return ResponseEntity.notFound().build();
      }

      Buyer buyer = buyerOpt.get();
      
      // 추가 정보 생성
      Map<String, Object> additionalData = Map.of(
          "gubunName", MemberGubun.getDescriptionByCode(buyer.getGubun()),
          "canLogin", buyerSVC.canLogin(buyer),
          "isWithdrawn", buyerSVC.isWithdrawn(buyer)
      );
      
      Map<String, Object> response = ApiResponse.entitySuccess("회원 정보 조회 성공", buyer, additionalData);
      
      log.info("구매자 정보 조회 성공: buyerId={}", buyer.getBuyerId());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("구매자 정보 조회 실패: error={}", e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("회원 정보 조회에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ID로 회원 조회
   */
  @GetMapping("/{buyerId}")
  public ResponseEntity<Map<String, Object>> getBuyer(
      @PathVariable Long buyerId, 
      HttpSession session) {
    try {
      // 권한 검증: 본인만 접근 가능
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
      
      if (buyerOpt.isEmpty()) {
        Map<String, Object> response = ApiResponse.error("존재하지 않는 회원입니다.");
        return ResponseEntity.notFound().build();
      }
      
      Buyer buyer = buyerOpt.get();
      
      Map<String, Object> additionalData = Map.of(
          "gubunName", MemberGubun.getDescriptionByCode(buyer.getGubun()),
          "canLogin", buyerSVC.canLogin(buyer),
          "isWithdrawn", buyerSVC.isWithdrawn(buyer)
      );
      
      Map<String, Object> response = ApiResponse.entitySuccess("회원 조회 성공", buyer, additionalData);
      
      return ResponseEntity.ok(response);
      
    } catch (SecurityException e) {
      log.warn("권한 없는 회원 조회 시도: buyerId={}, error={}", buyerId, e.getMessage());
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.status(403).body(response);
      
    } catch (Exception e) {
      log.error("회원 조회 실패: buyerId={}, error={}", buyerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("회원 정보 조회에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 정보 수정 (프로세스 설계서: 수정 전 비밀번호 재확인 필수)
   */
  @PutMapping("/{buyerId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable Long buyerId,
      @RequestBody Map<String, Object> request,
      HttpSession session) {
    
    try {
      // 🔒 권한 검증: 본인만 접근 가능
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      // 🔒 프로세스 설계서: 필수 필드 검증
      String currentPassword = (String) request.get("currentPassword");
      if (currentPassword == null || currentPassword.isBlank()) {
        Map<String, Object> response = ApiResponse.error("현재 비밀번호는 필수입니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
      // 🔒 프로세스 설계서: 비밀번호 재확인
      if (!buyerSVC.checkPassword(buyerId, currentPassword)) {
        Map<String, Object> response = ApiResponse.error("현재 비밀번호가 일치하지 않습니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
      // 수정할 정보 구성
      Buyer updateBuyer = new Buyer();
      
      if (request.get("name") != null) {
        updateBuyer.setName((String) request.get("name"));
      }
      if (request.get("nickname") != null) {
        updateBuyer.setNickname((String) request.get("nickname"));
      }
      if (request.get("tel") != null) {
        updateBuyer.setTel((String) request.get("tel"));
      }
      if (request.get("gender") != null) {
        updateBuyer.setGender((String) request.get("gender"));
      }
      if (request.get("address") != null) {
        updateBuyer.setAddress((String) request.get("address"));
      }
      
      // 새 비밀번호가 있는 경우
      if (request.get("newPassword") != null && !((String) request.get("newPassword")).isBlank()) {
        updateBuyer.setPassword((String) request.get("newPassword"));
      }
      
      int updatedRows = buyerSVC.update(buyerId, updateBuyer);
      
      if (updatedRows > 0) {
        Map<String, Object> response = ApiResponse.success("정보가 수정되었습니다.");
        log.info("구매자 정보 수정 성공: buyerId={}", buyerId);
        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> response = ApiResponse.error("정보 수정에 실패했습니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
    } catch (SecurityException e) {
      log.warn("권한 없는 정보 수정 시도: buyerId={}, error={}", buyerId, e.getMessage());
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.status(403).body(response);
      
    } catch (Exception e) {
      log.error("정보 수정 실패: buyerId={}, error={}", buyerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 탈퇴 처리 (프로세스 설계서: 비밀번호 재확인 + 서비스 이용현황 조회)
   */
  @PostMapping("/{buyerId}/withdraw")
  public ResponseEntity<Map<String, Object>> withdraw(
      @PathVariable Long buyerId,
      @RequestBody Map<String, Object> request,
      HttpSession session) {
    
    try {
      // 🔒 권한 검증: 본인만 접근 가능
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      // 🔒 프로세스 설계서: 필수 필드 검증
      String password = (String) request.get("password");
      String reason = (String) request.get("reason");
      
      if (password == null || password.isBlank()) {
        Map<String, Object> response = ApiResponse.error("비밀번호는 필수입니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
      if (reason == null || reason.isBlank()) {
        Map<String, Object> response = ApiResponse.error("탈퇴 사유는 필수입니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
      // 🔒 프로세스 설계서: 비밀번호 재확인
      if (!buyerSVC.checkPassword(buyerId, password)) {
        Map<String, Object> response = ApiResponse.error("비밀번호가 일치하지 않습니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
      // 🔒 프로세스 설계서: 서비스 이용현황 조회 → 탈퇴가능성 판단
      Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(buyerId);
      boolean canWithdraw = (Boolean) serviceUsage.get("canWithdraw");
      
      if (!canWithdraw) {
        @SuppressWarnings("unchecked")
        java.util.List<String> blockReasons = (java.util.List<String>) serviceUsage.get("withdrawBlockReasons");
        
        String blockMessage = "현재 탈퇴할 수 없습니다:\n" + String.join("\n", blockReasons);
        
        Map<String, Object> response = Map.of(
            "success", false,
            "message", blockMessage,
            "serviceUsage", serviceUsage,
            "timestamp", java.time.LocalDateTime.now().toString()
        );
        
        log.warn("탈퇴 불가 상태: buyerId={}, blockReasons={}", buyerId, blockReasons);
        return ResponseEntity.badRequest().body(response);
      }
      
      // 탈퇴 처리 실행
      int withdrawnRows = buyerSVC.withdraw(buyerId, reason);
      
      if (withdrawnRows > 0) {
        // 세션 무효화
        if (session != null) {
          session.invalidate();
        }
        
        Map<String, Object> response = ApiResponse.success("탈퇴 처리가 완료되었습니다.");
        log.info("구매자 탈퇴 완료: buyerId={}, reason={}", buyerId, reason);
        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> response = ApiResponse.error("탈퇴 처리에 실패했습니다.");
        return ResponseEntity.badRequest().body(response);
      }
      
    } catch (SecurityException e) {
      log.warn("권한 없는 탈퇴 시도: buyerId={}, error={}", buyerId, e.getMessage());
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.status(403).body(response);
      
    } catch (Exception e) {
      log.error("탈퇴 실패: buyerId={}, error={}", buyerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 서비스 이용현황 조회 (프로세스 설계서: 탈퇴 전 이용현황 확인)
   */
  @GetMapping("/{buyerId}/service-usage")
  public ResponseEntity<Map<String, Object>> getServiceUsage(
      @PathVariable Long buyerId,
      HttpSession session) {
    
    try {
      // 🔒 권한 검증: 본인만 접근 가능
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      // 서비스 이용현황 조회
      Map<String, Object> serviceUsage = buyerSVC.getServiceUsage(buyerId);
      
      Map<String, Object> response = ApiResponse.success("서비스 이용현황 조회 성공", serviceUsage);
      
      log.info("구매자 서비스 이용현황 조회 성공: buyerId={}", buyerId);
      return ResponseEntity.ok(response);
      
    } catch (SecurityException e) {
      log.warn("권한 없는 서비스 이용현황 조회 시도: buyerId={}, error={}", buyerId, e.getMessage());
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.status(403).body(response);
      
    } catch (Exception e) {
      log.error("서비스 이용현황 조회 실패: buyerId={}, error={}", buyerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("서비스 이용현황 조회에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 등급 승급
   */
  @PostMapping("/{buyerId}/upgrade")
  public ResponseEntity<Map<String, Object>> upgradeGrade(
      @PathVariable Long buyerId,
      @RequestBody Map<String, String> request) {
    
    try {
      String newGrade = request.get("gubun");
      buyerSVC.upgradeGubun(buyerId, newGrade);
      
      Map<String, Object> responseData = Map.of(
          "gubun", newGrade,
          "gubunName", MemberGubun.getDescriptionByCode(newGrade)
      );
      
      Map<String, Object> response = ApiResponse.success("등급이 승급되었습니다.", responseData);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("등급 승급 실패: buyerId={}, error={}", buyerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 이메일 중복 체크
   */
  @GetMapping("/check-email")
  public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
    try {
      boolean exists = buyerSVC.existsByEmail(email);
      
      Map<String, Object> data = Map.of(
          "exists", exists,
          "available", !exists,
          "email", email
      );
      
      String message = exists ? "이미 사용중인 이메일입니다." : "사용 가능한 이메일입니다.";
      Map<String, Object> response = ApiResponse.success(message, data);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("이메일 중복 체크 실패: email={}, error={}", email, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("이메일 중복 체크에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 닉네임 중복 체크
   */
  @GetMapping("/check-nickname")
  public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
    try {
      boolean exists = buyerSVC.existsByNickname(nickname);
      
      Map<String, Object> data = Map.of(
          "exists", exists,
          "available", !exists,
          "nickname", nickname
      );
      
      String message = exists ? "이미 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
      Map<String, Object> response = ApiResponse.success(message, data);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("닉네임 중복 체크 실패: nickname={}, error={}", nickname, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("닉네임 중복 체크에 실패했습니다.");
      return ResponseEntity.badRequest().body(response);
    }
  }
}