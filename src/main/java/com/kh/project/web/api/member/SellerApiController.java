package com.kh.project.web.api.member;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.domain.entity.MemberType;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.domain.seller.svc.SellerSVCImpl;
import com.kh.project.web.common.response.ApiResponse;
import com.kh.project.web.exception.UserException;
import com.kh.project.web.form.login.LoginForm;
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
 * 판매자 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Validated
public class SellerApiController {

    private final SellerSVC sellerSVC;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    // 상수 사용
    private static final String MEMBER_TYPE_SELLER = MemberType.SELLER.getCode();
    private static final String STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();

    private SellerSVCImpl getSellerService() {
        return (SellerSVCImpl) sellerSVC;
    }

    /**
     * 판매자 회원가입
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Object>> signup(
        @Valid @RequestBody com.kh.project.web.common.form.SellerSignupForm signupForm,
        HttpSession session) {

        log.info("판매자 회원가입 요청: {}", signupForm.getEmail());

        // 중복 검증
        if (sellerSVC.existsByEmail(signupForm.getEmail())) {
            throw new UserException.EmailAlreadyExists("이미 가입된 이메일입니다.");
        }

        if (sellerSVC.existsByBizRegNo(signupForm.getBusinessNumber())) {
            throw new UserException.BizRegNoAlreadyExists("이미 등록된 사업자등록번호입니다.");
        }

        // 회원 생성
        Seller seller = convertToEntity(signupForm);
        seller.setPassword(passwordEncoder.encode(signupForm.getPassword()));

        Seller savedSeller = sellerSVC.join(seller);

        // 세션에 로그인 정보 저장
        sessionService.setLoginSession(session, savedSeller.getSellerId(), savedSeller.getEmail(), MEMBER_TYPE_SELLER, savedSeller.getShopName());

        Map<String, Object> responseData = Map.of(
            "sellerId", savedSeller.getSellerId(),
            "email", savedSeller.getEmail(),
            "name", savedSeller.getName()
        );

        log.info("판매자 회원가입 성공: sellerId={}", savedSeller.getSellerId());
        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", responseData));
    }

    /**
     * 판매자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(
        @Valid @RequestBody LoginForm form,
        HttpSession session) {

        log.info("판매자 로그인 요청: {}", form.getEmail());

        // 1. 이메일로 판매자 조회
        Optional<Seller> sellerOpt = sellerSVC.findByEmail(form.getEmail());
        if (sellerOpt.isEmpty()) {
            throw new UserException.LoginFailed("이메일이 올바르지 않습니다.");
        }

        Seller seller = sellerOpt.get();

        SellerSVCImpl sellerService = getSellerService();
        
        // 2. 로그인 가능 여부 확인 (활성 상태, 탈퇴 여부 등)
        if (!sellerService.canLogin(seller)) {
            if (sellerService.isWithdrawn(seller)) {
                throw new UserException.LoginFailed("탈퇴한 회원입니다.");
            } else {
                throw new UserException.LoginFailed("로그인할 수 없는 계정입니다.");
            }
        }

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(form.getPassword(), seller.getPassword())) {
            throw new UserException.LoginFailed("비밀번호가 올바르지 않습니다.");
        }

        // 4. 세션에 로그인 정보 저장
        sessionService.setLoginSession(session, seller.getSellerId(), seller.getEmail(), MEMBER_TYPE_SELLER, seller.getShopName());

        Map<String, Object> responseData = Map.of(
            "sellerId", seller.getSellerId(),
            "email", seller.getEmail(),
            "name", seller.getName(),
            "shopName", seller.getShopName()
        );

        log.info("판매자 로그인 성공: sellerId={}", seller.getSellerId());
        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", responseData));
    }

    /**
     * 판매자 정보 조회
     */
    @GetMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<Object>> getSellerInfo(
        @PathVariable Long sellerId,
        HttpSession session) {

        log.info("판매자 정보 조회 요청: sellerId={}", sellerId);

        sessionService.validateSellerAccess(session, sellerId);

        Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new UserException.UserNotFound("판매자 정보를 찾을 수 없습니다.");
        }

        Seller seller = sellerOpt.get();
        SellerSVCImpl sellerService = getSellerService();

        Map<String, Object> statusInfo = Map.of(
            "activeProducts", 0,     // 활성 상품 수
            "pendingOrders", 0,      // 대기 주문 수
            "unsettledAmount", 0,    // 미정산 금액
            "canWithdraw", sellerService.canWithdraw(seller)
        );

        Map<String, Object> sellerInfo = Map.of(
            "sellerId", seller.getSellerId(),
            "email", seller.getEmail(),
            "name", seller.getName(),
            "bizRegNo", seller.getBizRegNo(),
            "shopName", seller.getShopName(),
            "shopAddress", seller.getShopAddress(),
            "tel", seller.getTel(),
            "status", seller.getStatus(),
            "statusInfo", statusInfo
        );

        log.info("판매자 정보 조회 성공: sellerId={}", sellerId);
        return ResponseEntity.ok(ApiResponse.success("조회가 완료되었습니다.", sellerInfo));
    }

    /**
     * 판매자 정보 수정
     */
    @PutMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<Object>> updateSellerInfo(
        @PathVariable Long sellerId,
        @RequestBody Map<String, Object> request,
        HttpSession session) {

        log.info("판매자 정보 수정 요청: sellerId={}", sellerId);

        sessionService.validateSellerAccess(session, sellerId);
        validateCurrentPassword(sellerId, request);

        Seller updateSeller = createUpdateSellerFromRequest(request);
        updateSeller.setSellerId(sellerId);

        int result = sellerSVC.update(sellerId, updateSeller);

        if (result == 0) {
            throw new UserException.UpdateFailed();
        }

        log.info("판매자 정보 수정 성공: sellerId={}", sellerId);
        return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다.", null));
    }

    /**
     * 판매자 비밀번호 확인
     */
    @PostMapping("/{sellerId}/verify-password")
    public ResponseEntity<ApiResponse<Object>> verifyPassword(
        @PathVariable Long sellerId,
        @RequestBody Map<String, Object> request,
        HttpSession session) {

        log.info("판매자 비밀번호 확인 요청: sellerId={}", sellerId);

        sessionService.validateSellerAccess(session, sellerId);

        String password = (String) request.get("password");
        if (password == null || password.isBlank()) {
            throw new UserException.ValidationError("비밀번호는 필수입니다.");
        }

        Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new UserException.UserNotFound("판매자 정보를 찾을 수 없습니다.");
        }

        Seller seller = sellerOpt.get();
        if (!passwordEncoder.matches(password, seller.getPassword())) {
            throw new UserException.PasswordMismatched("비밀번호가 올바르지 않습니다.");
        }

        log.info("판매자 비밀번호 확인 성공: sellerId={}", sellerId);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 확인되었습니다.", null));
    }

    /**
     * 판매자 탈퇴
     */
    @DeleteMapping("/{sellerId}")
    public ResponseEntity<ApiResponse<Object>> withdrawSeller(
        @PathVariable Long sellerId,
        @RequestBody Map<String, Object> request,
        HttpSession session) {

        log.info("판매자 탈퇴 요청: sellerId={}", sellerId);

        sessionService.validateSellerAccess(session, sellerId);

        String reason = (String) request.get("reason");
        if (reason == null || reason.isBlank()) {
            throw new UserException.ValidationError("탈퇴 사유는 필수입니다.");
        }

        // Service의 withdrawWithReason이 탈퇴 가능 여부를 확인함
        int result = sellerSVC.withdrawWithReason(sellerId, reason);

        if (result == 0) {
            throw new UserException.WithdrawFailed();
        }

        // 세션 무효화
        sessionService.logout(session);

        log.info("판매자 탈퇴 성공: sellerId={}", sellerId);
        return ResponseEntity.ok(ApiResponse.success("PLANT 웹사이트의 회원탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다.", null));
    }

    /**
     * 판매자 서비스 이용 현황 조회
     */
    @GetMapping("/{sellerId}/service-usage")
    public ResponseEntity<ApiResponse<Object>> getServiceUsage(
        @PathVariable Long sellerId,
        HttpSession session) {

        log.info("판매자 서비스 이용 현황 조회 요청: sellerId={}", sellerId);

        sessionService.validateSellerAccess(session, sellerId);

        // 1차 프로젝트: 간단한 더미 데이터
        Map<String, Object> statusInfo = Map.of(
            "activeProducts", 0,     // 활성 상품 수
            "pendingOrders", 0,      // 대기 주문 수
            "unsettledAmount", 0,    // 미정산 금액
            "monthlyRevenue", 0,     // 월 매출
            "canWithdraw", true      // 탈퇴 가능 여부
        );

        log.info("판매자 서비스 이용 현황 조회 성공: sellerId={}", sellerId);
        return ResponseEntity.ok(ApiResponse.success("조회가 완료되었습니다.", statusInfo));
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Object>> checkEmailDuplication(
        @RequestParam String email) {

        log.info("이메일 중복 확인 요청: email={}", email);

        boolean exists = sellerSVC.existsByEmail(email);

        String message = exists ? "이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다." : "사용 가능한 이메일입니다.";
        Map<String, Object> data = Map.of(
            "email", email,
            "available", !exists
        );

        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    /**
     * 사업자등록번호 중복 확인
     */
    @GetMapping("/check-biz-reg-no")
    public ResponseEntity<ApiResponse<Object>> checkBizRegNoDuplication(
        @RequestParam String bizRegNo) {

        log.info("사업자등록번호 중복 확인 요청: bizRegNo={}", bizRegNo);

        boolean exists = sellerSVC.existsByBizRegNo(bizRegNo);

        String message = exists ? "이미 등록된 사업자등록번호입니다. 탈퇴 후 재가입은 불가능합니다." : "사용 가능한 사업자등록번호입니다.";
        Map<String, Object> data = Map.of(
            "bizRegNo", bizRegNo,
            "available", !exists
        );

        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    /**
     * 탈퇴 가능 여부 확인
     */
    @GetMapping("/{sellerId}/can-withdraw")
    public ResponseEntity<ApiResponse<Object>> canWithdraw(
        @PathVariable Long sellerId,
        HttpSession session) {

        log.info("탈퇴 가능 여부 확인 요청: sellerId={}", sellerId);

        sessionService.validateSellerAccess(session, sellerId);

        Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new UserException.UserNotFound("판매자 정보를 찾을 수 없습니다.");
        }

        Seller seller = sellerOpt.get();
        SellerSVCImpl sellerService = getSellerService();

        boolean canWithdrawResult = sellerService.canWithdraw(seller);

        if (!canWithdrawResult) {
            String blockReason = sellerService.getWithdrawBlockReason(seller);
            throw new UserException.WithdrawFailed(blockReason != null ? blockReason : "탈퇴할 수 없습니다.");
        }

        return ResponseEntity.ok(ApiResponse.success("탈퇴 가능합니다.", null));
    }

    // ============ Private Helper Methods ============

    private Seller convertToEntity(com.kh.project.web.common.form.SellerSignupForm form) {
        Seller seller = new Seller();
        seller.setEmail(form.getEmail());
        seller.setName(form.getName());
        seller.setBizRegNo(form.getBusinessNumber());
        seller.setShopName(form.getStoreName());
        seller.setShopAddress(form.getFullAddress());
        seller.setTel(form.getTel());
        seller.setStatus(STATUS_ACTIVE);

        return seller;
    }

    private void validateCurrentPassword(Long sellerId, Map<String, Object> request) {
        String currentPassword = (String) request.get("currentPassword");
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new UserException.ValidationError("현재 비밀번호는 필수입니다.");
        }

        Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new UserException.UserNotFound("판매자 정보를 찾을 수 없습니다.");
        }

        Seller seller = sellerOpt.get();
        if (!passwordEncoder.matches(currentPassword, seller.getPassword())) {
            throw new UserException.PasswordMismatched("현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private Seller createUpdateSellerFromRequest(Map<String, Object> request) {
        Seller updateSeller = new Seller();

        if (request.get("name") != null) {
            updateSeller.setName((String) request.get("name"));
        }
        if (request.get("bizRegNo") != null) {
            updateSeller.setBizRegNo((String) request.get("bizRegNo"));
        }
        if (request.get("shopName") != null) {
            updateSeller.setShopName((String) request.get("shopName"));
        }
        if (request.get("shopAddress") != null) {
            updateSeller.setShopAddress((String) request.get("shopAddress"));
        }
        if (request.get("tel") != null) {
            updateSeller.setTel((String) request.get("tel"));
        }
        if (request.get("newPassword") != null && !((String) request.get("newPassword")).isBlank()) {
            updateSeller.setPassword(passwordEncoder.encode((String) request.get("newPassword")));
        }

        return updateSeller;
    }
}