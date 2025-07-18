package com.kh.project.web.api.member;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.buyer.svc.BuyerSVC;
import com.kh.project.domain.buyer.svc.BuyerSVCImpl;
import com.kh.project.domain.entity.*;
import com.kh.project.web.common.response.ApiResponse;
import com.kh.project.web.exception.UserException;
import com.kh.project.web.form.login.LoginForm;
import com.kh.project.web.form.member.BuyerSignupForm;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * 구매자 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
@Validated
public class BuyerApiController {

  private final BuyerSVC buyerSVC;
  private final SessionService sessionService;
  private final PasswordEncoder passwordEncoder;

  // Enum 기반 상수 사용
  private static final String MEMBER_TYPE_BUYER = MemberType.BUYER.getCode();
  private static final String STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();

  private BuyerSVCImpl getBuyerService() {
    return (BuyerSVCImpl) buyerSVC;
  }

  /**
   * 구매자 회원가입
   */
  @PostMapping
  public ResponseEntity<ApiResponse<Object>> signup(
      @Valid @RequestBody BuyerSignupForm signupForm,
      HttpSession session) {

    log.info("구매자 회원가입 요청: {}", signupForm.getEmail());

    // 중복 검증
    if (buyerSVC.existsByEmail(signupForm.getEmail())) {
      throw new UserException.EmailAlreadyExists("이미 가입된 이메일입니다.");
    }

    if (buyerSVC.existsByNickname(signupForm.getNickname())) {
      throw new UserException.NicknameAlreadyExists("이미 사용중인 닉네임입니다.");
    }

    // 회원 생성
    Buyer buyer = convertToEntity(signupForm);
    buyer.setPassword(passwordEncoder.encode(signupForm.getPassword()));

    Buyer savedBuyer = buyerSVC.join(buyer);

    // 세션에 로그인 정보 저장
    sessionService.setLoginSession(session, savedBuyer.getBuyerId(),
        savedBuyer.getEmail(), MEMBER_TYPE_BUYER, savedBuyer.getNickname());

    Map<String, Object> responseData = Map.of(
        "buyerId", savedBuyer.getBuyerId(),
        "email", savedBuyer.getEmail(),
        "name", savedBuyer.getName()
    );

    log.info("구매자 회원가입 성공: buyerId={}", savedBuyer.getBuyerId());
    return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", responseData));
  }

  /**
   * 구매자 로그인
   */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<Object>> login(
      @Valid @RequestBody LoginForm form,
      HttpSession session) {

    log.info("구매자 로그인 요청: {}", form.getEmail());

    // Service 비즈니스 로직 사용
    BuyerSVCImpl buyerService = getBuyerService();

    // 1. 이메일로 구매자 조회
    Optional<Buyer> buyerOpt = buyerSVC.findByEmail(form.getEmail());
    if (buyerOpt.isEmpty()) {
      throw new UserException.LoginFailed("입력하신 이메일은 가입되지 않은 계정입니다. 회원가입 후 이용해주세요.");
    }

    Buyer buyer = buyerOpt.get();

    // 2. 로그인 가능 여부 확인 (활성 상태, 탈퇴 여부 등)
    if (!buyerService.canLogin(buyer)) {
      if (buyerService.isWithdrawn(buyer)) {
        throw new UserException.LoginFailed("탈퇴한 회원입니다.");
      } else {
        throw new UserException.LoginFailed("로그인할 수 없는 계정입니다.");
      }
    }

    // 3. 비밀번호 검증
    if (!passwordEncoder.matches(form.getPassword(), buyer.getPassword())) {
      throw new UserException.LoginFailed("비밀번호가 올바르지 않습니다.");
    }

    // 4. 세션에 로그인 정보 저장
    sessionService.setLoginSession(session, buyer.getBuyerId(), buyer.getEmail(),
        MEMBER_TYPE_BUYER, buyer.getNickname());

    Map<String, Object> responseData = Map.of(
        "buyerId", buyer.getBuyerId(),
        "email", buyer.getEmail(),
        "name", buyer.getName(),
        "nickname", buyer.getNickname()
    );

    log.info("구매자 로그인 성공: buyerId={}", buyer.getBuyerId());
    return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", responseData));
  }

  /**
   * 구매자 로그아웃
   */
  @DeleteMapping("/auth")
  public ResponseEntity<ApiResponse<Object>> logout(HttpSession session) {
    sessionService.logout(session);
    return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다.", null));
  }

  /**
   * 현재 로그인한 구매자 정보 조회
   */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<Object>> getCurrentBuyerInfo(HttpSession session) {
    log.info("현재 구매자 정보 조회 요청");

    LoginMember loginMember = sessionService.validateBuyerSession(session);
    Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());

    if (buyerOpt.isEmpty()) {
      throw new UserException.UserNotFound("회원 정보를 찾을 수 없습니다.");
    }

    Buyer buyer = buyerOpt.get();
    BuyerSVCImpl buyerService = getBuyerService();

    Map<String, Object> responseData = Map.of(
        "buyer", buyer,
        "additionalData", Map.of(
            "canLogin", buyerService.canLogin(buyer),
            "isWithdrawn", buyerService.isWithdrawn(buyer)
        )
    );

    log.info("구매자 정보 조회 성공: buyerId={}", buyer.getBuyerId());
    return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공", responseData));
  }

  /**
   * 특정 구매자 정보 조회
   */
  @GetMapping("/{buyerId}")
  public ResponseEntity<ApiResponse<Object>> getBuyer(
      @PathVariable("buyerId") Long buyerId,
      HttpSession session) {

    log.info("구매자 정보 조회 요청: buyerId={}", buyerId);

    sessionService.validateBuyerAccess(session, buyerId);

    Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
    if (buyerOpt.isEmpty()) {
      throw new UserException.UserNotFound("회원 정보를 찾을 수 없습니다.");
    }

    Buyer buyer = buyerOpt.get();
    BuyerSVCImpl buyerService = getBuyerService();

    Map<String, Object> responseData = Map.of(
        "buyer", buyer,
        "additionalData", Map.of(
            "canLogin", buyerService.canLogin(buyer),
            "isWithdrawn", buyerService.isWithdrawn(buyer)
        )
    );

    return ResponseEntity.ok(ApiResponse.success("회원 조회 성공", responseData));
  }

  /**
   * 구매자 정보 수정
   */
  @PutMapping("/{buyerId}")
  public ResponseEntity<ApiResponse<Object>> updateBuyer(
      @PathVariable("buyerId") Long buyerId,
      @RequestBody Map<String, Object> request,
      HttpSession session) {

    log.info("구매자 정보 수정 요청: buyerId={}", buyerId);

    sessionService.validateBuyerAccess(session, buyerId);
    validateCurrentPassword(buyerId, request);

    Buyer updateBuyer = createUpdateBuyerFromRequest(request);

    int updatedRows = buyerSVC.update(buyerId, updateBuyer);
    if (updatedRows == 0) {
      throw new UserException.UpdateFailed();
    }

    log.info("구매자 정보 수정 성공: buyerId={}", buyerId);
    return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다.", null));
  }

  /**
   * 구매자 탈퇴
   */
  @DeleteMapping("/{buyerId}")
  public ResponseEntity<ApiResponse<Object>> withdrawBuyer(
      @PathVariable("buyerId") Long buyerId,
      @RequestBody Map<String, Object> request,
      HttpSession session) {

    log.info("구매자 탈퇴 요청: buyerId={}", buyerId);

    sessionService.validateBuyerAccess(session, buyerId);

    String password = (String) request.get("password");
    String reason = (String) request.get("reason");

    validateWithdrawRequest(password, reason);

    Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
    if (buyerOpt.isEmpty()) {
      throw new UserException.UserNotFound("구매자 정보를 찾을 수 없습니다.");
    }

    Buyer buyer = buyerOpt.get();
    if (!passwordEncoder.matches(password, buyer.getPassword())) {
      throw new UserException.PasswordMismatched();
    }

    int withdrawnRows = buyerSVC.withdrawWithReason(buyerId, reason);
    if (withdrawnRows == 0) {
      throw new UserException.WithdrawFailed();
    }

    sessionService.logout(session);

    log.info("구매자 탈퇴 성공: buyerId={}", buyerId);
    return ResponseEntity.ok(ApiResponse.success("탈퇴 처리되었습니다.", null));
  }

  /**
   * 구매자 서비스 이용현황 조회
   */
  @GetMapping("/{buyerId}/usage")
  public ResponseEntity<ApiResponse<Object>> getBuyerUsage(
      @PathVariable("buyerId") Long buyerId,
      HttpSession session) {

    log.info("구매자 서비스 이용현황 조회: buyerId={}", buyerId);

    sessionService.validateBuyerAccess(session, buyerId);

    // 1차 프로젝트: 간단한 더미 데이터
    Map<String, Object> usage = Map.of(
        "orderCount", 0,        // 주문 건수
        "totalAmount", 0,       // 총 구매 금액
        "points", 0,           // 적립금
        "couponCount", 0,      // 쿠폰 수
        "canWithdraw", true    // 탈퇴 가능 여부
    );

    return ResponseEntity.ok(ApiResponse.success("이용현황 조회 성공", usage));
  }

  /**
   * 구매자 비밀번호 확인
   */
  @PostMapping("/password/verification")
  public ResponseEntity<ApiResponse<Object>> verifyPassword(
      @RequestBody Map<String, Object> request,
      HttpSession session) {

    LoginMember loginMember = sessionService.validateBuyerSession(session);

    String password = (String) request.get("password");
    if (password == null || password.isBlank()) {
      throw new UserException.ValidationError("비밀번호는 필수입니다.");
    }

    Optional<Buyer> buyerOpt = buyerSVC.findById(loginMember.getId());
    if (buyerOpt.isEmpty()) {
      throw new UserException.UserNotFound("구매자 정보를 찾을 수 없습니다.");
    }

    Buyer buyer = buyerOpt.get();
    if (!passwordEncoder.matches(password, buyer.getPassword())) {
      throw new UserException.PasswordMismatched("비밀번호가 올바르지 않습니다.");
    }

    log.info("구매자 비밀번호 확인 성공: buyerId={}", loginMember.getId());
    return ResponseEntity.ok(ApiResponse.success("비밀번호 확인이 완료되었습니다.", null));
  }

  /**
   * 이메일 중복 확인
   */
  @GetMapping("/emails/{email}/availability")
  public ResponseEntity<ApiResponse<Object>> checkEmailAvailability(
      @PathVariable("email") String email) {

    log.info("이메일 중복 확인: email={}", email);

    boolean exists = buyerSVC.existsByEmail(email);

    String message = exists ? "이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다." : "사용 가능한 이메일입니다.";
    Map<String, Object> data = Map.of(
        "email", email,
        "available", !exists
    );

    return ResponseEntity.ok(ApiResponse.success(message, data));
  }

  /**
   * 닉네임 중복 확인
   */
  @GetMapping("/nicknames/{nickname}/availability")
  public ResponseEntity<ApiResponse<Object>> checkNicknameAvailability(
      @PathVariable("nickname") String nickname) {

    log.info("닉네임 중복 확인: nickname={}", nickname);

    boolean exists = buyerSVC.existsByNickname(nickname);

    String message = exists ? "이미 사용중인 닉네임입니다." : "사용 가능한 닉네임입니다.";
    Map<String, Object> data = Map.of(
        "nickname", nickname,
        "available", !exists
    );

    return ResponseEntity.ok(ApiResponse.success(message, data));
  }

  // ===== Private Helper Methods =====

  private Buyer convertToEntity(BuyerSignupForm form) {
    Buyer buyer = new Buyer();
    buyer.setEmail(form.getEmail());
    buyer.setName(form.getName());
    buyer.setNickname(form.getNickname());
    buyer.setTel(form.getTel());
    buyer.setGender(form.getGender());
    buyer.setBirth(form.getBirth());
    buyer.setPostNumber(form.getPostcode());
    buyer.setAddress(form.getFullAddress());
    buyer.setMemberGubun(MemberGubun.NEW);
    buyer.setStatus(STATUS_ACTIVE);
    return buyer;
  }

  private void validateCurrentPassword(Long buyerId, Map<String, Object> request) {
    String currentPassword = (String) request.get("currentPassword");
    if (currentPassword == null || currentPassword.isBlank()) {
      throw new UserException.ValidationError("현재 비밀번호는 필수입니다.");
    }

    Optional<Buyer> buyerOpt = buyerSVC.findById(buyerId);
    if (buyerOpt.isEmpty()) {
      throw new UserException.UserNotFound("구매자 정보를 찾을 수 없습니다.");
    }

    Buyer buyer = buyerOpt.get();
    if (!passwordEncoder.matches(currentPassword, buyer.getPassword())) {
      throw new UserException.PasswordMismatched("현재 비밀번호가 일치하지 않습니다.");
    }
  }

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
      updateBuyer.setPassword(passwordEncoder.encode((String) request.get("newPassword")));
    }

    return updateBuyer;
  }

  private void validateWithdrawRequest(String password, String reason) {
    if (password == null || password.isBlank()) {
      throw new UserException.ValidationError("비밀번호는 필수입니다.");
    }
    if (reason == null || reason.isBlank()) {
      throw new UserException.ValidationError("탈퇴 사유는 필수입니다.");
    }
  }
}