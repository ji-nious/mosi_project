package com.kh.project.web.api;

import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.SessionService;
import com.kh.project.util.AuthUtils;
import com.kh.project.web.common.form.LoginForm;
import com.kh.project.web.common.form.BuyerEditForm;
import com.kh.project.web.common.response.ApiResponse;
import com.kh.project.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.kh.project.domain.entity.MemberType;
import com.kh.project.web.common.form.BuyerSignupForm;
import com.kh.project.web.common.form.MemberStatusInfo;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;

/**
 * 구매자 REST API 컨트롤러
 * 구매자 관련 RESTful API 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
@Validated
public class BuyerApiController {

    private final BuyerSVC buyerSVC;
    private final SessionService sessionService;
    
    // 상수 정의
    private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
    private static final int STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();

    /**
     * 구매자 회원가입
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> signup(
            @Valid @RequestBody BuyerSignupForm signupForm,
            HttpSession session) {
        
        log.info("구매자 회원가입 요청: {}", signupForm.getEmail());

        try {
            Buyer buyer = new Buyer();
            buyer.setEmail(signupForm.getEmail());
            buyer.setPassword(signupForm.getPassword());
            buyer.setName(signupForm.getName());
            buyer.setNickname(signupForm.getNickname());
            buyer.setTel(signupForm.getTel());
            buyer.setGender(signupForm.getGender());
            buyer.setBirth(signupForm.getBirth());
            buyer.setAddress(signupForm.getFullAddress());
            buyer.setMemberGubun(MemberGubun.NEW);
            buyer.setStatus(STATUS_ACTIVE);

            Buyer savedBuyer = buyerSVC.join(buyer);
            
            // 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, savedBuyer.getBuyerId(), savedBuyer.getEmail(), MEMBER_TYPE_BUYER);
            
            log.info("구매자 회원가입 성공: buyerId={}", savedBuyer.getBuyerId());
            
            return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", Map.of(
                    "buyerId", savedBuyer.getBuyerId(),
                    "email", savedBuyer.getEmail(),
                    "name", savedBuyer.getName()
            )));
            
        } catch (Exception e) {
            log.error("구매자 회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 구매자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginForm form,
            HttpSession session) {
        
        log.info("구매자 로그인 요청: {}", form.getEmail());

        try {
            Optional<Buyer> buyerOpt = buyerSVC.findByEmail(form.getEmail());
            if (buyerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("로그인에 실패했습니다."));
            }
            
            Buyer buyer = buyerOpt.get();
            if (!buyer.canLogin() || !buyer.getPassword().equals(form.getPassword())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("로그인에 실패했습니다."));
            }
            
            // 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, buyer.getBuyerId(), buyer.getEmail(), MEMBER_TYPE_BUYER);
            
            log.info("구매자 로그인 성공: buyerId={}", buyer.getBuyerId());
            
            return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", Map.of(
                    "buyerId", buyer.getBuyerId(),
                    "email", buyer.getEmail(),
                    "name", buyer.getName(),
                    "nickname", buyer.getNickname()
            )));
            
        } catch (Exception e) {
            log.error("구매자 로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("로그인에 실패했습니다."));
        }
    }

    /**
     * 구매자 로그아웃
     */
    @DeleteMapping("/auth")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        
          sessionService.logout(session);
        
        Map<String, Object> response = ApiResponse.success("로그아웃되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인한 구매자 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentBuyerInfo(HttpSession session) {
        
        log.info("현재 구매자 정보 조회 요청");
        
        LoginMember loginMember = getCurrentLoginMember(session);
      Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
        
      if (buyerOpt.isEmpty()) {
            throw new BusinessValidationException("회원 정보를 찾을 수 없습니다.");
      }
      
      Buyer buyer = buyerOpt.get();
        Map<String, Object> additionalData = createBuyerAdditionalData(buyer);
        
        Map<String, Object> response = ApiResponse.entitySuccess(
            "회원 정보 조회 성공", buyer, additionalData);
        
      log.info("구매자 정보 조회 성공: buyerId={}", buyer.getBuyerId());
      return ResponseEntity.ok(response);
    }

    /**
     * 특정 구매자 정보 조회
     */
  @GetMapping("/{buyerId}")
  public ResponseEntity<Map<String, Object>> getBuyer(
          @PathVariable("buyerId") Long buyerId, 
          HttpSession session) {
        
        log.info("구매자 정보 조회 요청: buyerId={}", buyerId);
        
      AuthUtils.validateBuyerAccess(session, buyerId);
        
      Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
      if (buyerOpt.isEmpty()) {
            throw new BusinessValidationException("회원 정보를 찾을 수 없습니다.");
      }
      
      Buyer buyer = buyerOpt.get();
        Map<String, Object> additionalData = createBuyerAdditionalData(buyer);
        
        Map<String, Object> response = ApiResponse.entitySuccess(
            "회원 조회 성공", buyer, additionalData);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 구매자 정보 수정
     */
  @PutMapping("/{buyerId}")
    public ResponseEntity<Map<String, Object>> updateBuyer(
          @PathVariable("buyerId") Long buyerId, 
          @RequestBody Map<String, Object> request, 
          HttpSession session) {
        
        log.info("구매자 정보 수정 요청: buyerId={}", buyerId);
        
      AuthUtils.validateBuyerAccess(session, buyerId);
      
        validateCurrentPassword(buyerId, request);
        
        Buyer updateBuyer = createUpdateBuyerFromRequest(request);
        
        int updatedRows = buyerSVC.update(buyerId, updateBuyer);
        if (updatedRows == 0) {
            throw new BusinessValidationException("정보 수정에 실패했습니다.");
        }
        
        log.info("구매자 정보 수정 성공: buyerId={}", buyerId);
        Map<String, Object> response = ApiResponse.success("정보가 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 구매자 탈퇴
     */
    @DeleteMapping("/{buyerId}")
    public ResponseEntity<Map<String, Object>> withdrawBuyer(
          @PathVariable("buyerId") Long buyerId, 
          @RequestBody Map<String, Object> request, 
          HttpSession session) {
        
        log.info("구매자 탈퇴 요청: buyerId={}", buyerId);
        
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      String password = (String) request.get("password");
      String reason = (String) request.get("reason");
        
        validateWithdrawRequest(password, reason);
      
      Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
        if (buyerOpt.isEmpty()) {
            throw new BusinessValidationException("구매자 정보를 찾을 수 없습니다.");
        }
        
        Buyer buyer = buyerOpt.get();
        if (!buyer.getPassword().equals(password)) {
            throw new BusinessValidationException("비밀번호가 일치하지 않습니다.");
        }
        
        int withdrawnRows = buyerSVC.withdrawWithReason(buyerId, reason);
        if (withdrawnRows == 0) {
            throw new BusinessValidationException("탈퇴 처리에 실패했습니다.");
        }
        
                    sessionService.logout(session);
        
        log.info("구매자 탈퇴 성공: buyerId={}", buyerId);
        Map<String, Object> response = ApiResponse.success("탈퇴 처리되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 구매자 서비스 이용현황 조회
     */
  @GetMapping("/{buyerId}/usage")
    public ResponseEntity<Map<String, Object>> getBuyerUsage(
          @PathVariable("buyerId") Long buyerId, 
          HttpSession session) {
        
        log.info("구매자 서비스 이용현황 조회: buyerId={}", buyerId);
        
      AuthUtils.validateBuyerAccess(session, buyerId);
      
      Map<String, Object> usage = Map.of(
          "orderCount", 42,
          "totalAmount", 125000,
          "points", 2500,
          "couponCount", 3
      );
      
        Map<String, Object> response = ApiResponse.success("이용현황 조회 성공", usage);
        return ResponseEntity.ok(response);
    }

    /**
     * 구매자 비밀번호 확인
     */
    @PostMapping("/password/verification")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        LoginMember loginMember = getCurrentLoginMember(session);
        
        String password = (String) request.get("password");
        if (password == null || password.isBlank()) {
            throw new BusinessValidationException("비밀번호는 필수입니다.");
        }
        
        Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
        if (buyerOpt.isEmpty()) {
            throw new BusinessValidationException("구매자 정보를 찾을 수 없습니다.");
        }
        
        Buyer buyer = buyerOpt.get();
        if (!buyer.getPassword().equals(password)) {
            throw new BusinessValidationException("비밀번호가 올바르지 않습니다.");
        }
        
        log.info("구매자 비밀번호 확인 성공: buyerId={}", loginMember.getId());
        Map<String, Object> response = ApiResponse.success("비밀번호 확인이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/emails/{email}/availability")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(
            @PathVariable("email") String email) {
        
        log.info("이메일 중복 확인: email={}", email);
        
        boolean exists = buyerSVC.existsByEmail(email);
        
        String message = exists ? "이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다." : "사용 가능한 이메일입니다.";
        Map<String, Object> data = Map.of(
            "email", email,
            "available", !exists
        );
        
        Map<String, Object> response = ApiResponse.success(message, data);
        return ResponseEntity.ok(response);
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/nicknames/{nickname}/availability")
    public ResponseEntity<Map<String, Object>> checkNicknameAvailability(
            @PathVariable("nickname") String nickname) {
        
        log.info("닉네임 중복 확인: nickname={}", nickname);
        
        boolean exists = buyerSVC.existsByNickname(nickname);
        
        String message = exists ? "이미 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
        Map<String, Object> data = Map.of(
            "nickname", nickname,
            "available", !exists
        );
        
        Map<String, Object> response = ApiResponse.success(message, data);
        return ResponseEntity.ok(response);
    }

    // ===== Private Helper Methods =====

    /**
     * 구매자 추가 정보 생성
     */
    private Map<String, Object> createBuyerAdditionalData(Buyer buyer) {
        return Map.of(
            "canLogin", buyer.canLogin(),
            "isWithdrawn", buyer.isWithdrawn()
        );
    }

    /**
     * 회원가입 폼을 엔티티로 변환
     */
    private Buyer convertToEntity(BuyerSignupForm form) {
        Buyer buyer = new Buyer();
        buyer.setEmail(form.getEmail());
        buyer.setPassword(form.getPassword());
        buyer.setName(form.getName());
        buyer.setNickname(form.getNickname());
        buyer.setTel(form.getTel());
        buyer.setGender(form.getGender());
        
        if (form.getBirth() != null) {
            buyer.setBirth(form.getBirth());
        }
        
        buyer.setAddress(form.getFullAddress());
        return buyer;
    }

    /**
     * 현재 로그인한 구매자 정보 조회
     */
    private LoginMember getCurrentLoginMember(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            throw new BusinessValidationException("로그인이 필요합니다.");
        }
        
        if (!MEMBER_TYPE_BUYER.equals(loginMember.getMemberType())) {
            throw new SecurityException("구매자만 접근 가능합니다.");
        }
        
        return loginMember;
    }

    /**
     * 현재 비밀번호 검증
     */
    private void validateCurrentPassword(Long buyerId, Map<String, Object> request) {
        String currentPassword = (String) request.get("currentPassword");
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BusinessValidationException("현재 비밀번호는 필수입니다.");
        }
        
        Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
        if (buyerOpt.isEmpty()) {
            throw new BusinessValidationException("구매자 정보를 찾을 수 없습니다.");
        }
        
        Buyer buyer = buyerOpt.get();
        if (!buyer.getPassword().equals(currentPassword)) {
            throw new BusinessValidationException("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 요청에서 수정할 구매자 정보 생성
     */
    private Buyer createUpdateBuyerFromRequest(Map<String, Object> request) {
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
        if (request.get("newPassword") != null && !((String) request.get("newPassword")).isBlank()) {
            updateBuyer.setPassword((String) request.get("newPassword"));
        }
        
        return updateBuyer;
    }

    /**
     * 탈퇴 요청 검증
     */
    private void validateWithdrawRequest(String password, String reason) {
        if (password == null || password.isBlank()) {
            throw new BusinessValidationException("비밀번호는 필수입니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new BusinessValidationException("탈퇴 사유는 필수입니다.");
        }
    }

    /**
     * 탈퇴 가능 여부 검증 (항상 가능으로 처리)
     */
    private void validateWithdrawEligibility(Long buyerId) {
        // 기본적으로 탈퇴 가능으로 처리
    }
}