package com.kh.project.web.api;

import com.kh.project.domain.SessionService;
import com.kh.project.domain.entity.LoginMember;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.entity.MemberType;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.util.AuthUtils;
import com.kh.project.web.common.form.LoginForm;
import com.kh.project.web.common.form.MemberStatusInfo;
import com.kh.project.web.common.form.SellerSignupForm;
import com.kh.project.web.common.response.ApiResponse;
import com.kh.project.web.exception.BusinessValidationException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    
    // 상수 정의
    private static final String MEMBER_TYPE_SELLER = MemberType.SELLER.getCode();
    private static final int STATUS_ACTIVE = MemberStatus.ACTIVE.getCode();

    /**
     * 판매자 회원가입
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> signup(
            @Valid @RequestBody SellerSignupForm signupForm,
            HttpSession session) {
        
        log.info("판매자 회원가입 요청: {}", signupForm.getEmail());

        try {
            Seller seller = convertToEntity(signupForm);
            Seller savedSeller = sellerSVC.join(seller);
            
            // 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, savedSeller.getSellerId(), savedSeller.getEmail(), MEMBER_TYPE_SELLER);
            
            log.info("판매자 회원가입 성공: sellerId={}", savedSeller.getSellerId());
            
            return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다.", Map.of(
                    "sellerId", savedSeller.getSellerId(),
                    "email", savedSeller.getEmail(),
                    "name", savedSeller.getName()
            )));
            
        } catch (Exception e) {
            log.error("판매자 회원가입 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 판매자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginForm form,
            HttpSession session) {
        
        log.info("판매자 로그인 요청: {}", form.getEmail());

        try {
            Optional<Seller> sellerOpt = sellerSVC.findByEmail(form.getEmail());
            if (sellerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
            }
            
            Seller seller = sellerOpt.get();
            if (!seller.canLogin() || !seller.getPassword().equals(form.getPassword())) {
                return ResponseEntity.badRequest().body(ApiResponse.error("이메일 또는 비밀번호가 올바르지 않습니다."));
            }
            
            // 세션에 로그인 정보 저장
            sessionService.setLoginSession(session, seller.getSellerId(), seller.getEmail(), MEMBER_TYPE_SELLER);
            
            log.info("판매자 로그인 성공: sellerId={}", seller.getSellerId());
            
            return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", Map.of(
                    "sellerId", seller.getSellerId(),
                    "email", seller.getEmail(),
                    "name", seller.getName(),
                    "shopName", seller.getShopName()
            )));
            
        } catch (Exception e) {
            log.error("판매자 로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 판매자 정보 조회
     */
    @GetMapping("/{sellerId}")
    public ResponseEntity<Map<String, Object>> getSellerInfo(
            @PathVariable Long sellerId,
            HttpSession session) {
        
        log.info("판매자 정보 조회 요청: sellerId={}", sellerId);

        try {
            AuthUtils.validateSellerAccess(session, sellerId);
            
            Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
            if (sellerOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Seller seller = sellerOpt.get();
            
            // 회원 상태 정보 (기본값)
            Map<String, Object> statusInfo = getBasicSellerStatus();
            
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
            
        } catch (Exception e) {
            log.error("판매자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 판매자 정보 수정
     */
    @PutMapping("/{sellerId}")
    public ResponseEntity<Map<String, Object>> updateSellerInfo(
            @PathVariable Long sellerId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        log.info("판매자 정보 수정 요청: sellerId={}", sellerId);

        try {
            AuthUtils.validateSellerAccess(session, sellerId);
            
            Seller seller = new Seller();
            seller.setSellerId(sellerId);
            seller.setPassword((String) request.get("password"));
            seller.setName((String) request.get("name"));
            seller.setBizRegNo((String) request.get("bizRegNo"));
            seller.setShopName((String) request.get("shopName"));
            seller.setShopAddress((String) request.get("shopAddress"));
            seller.setTel((String) request.get("tel"));
            
            int result = sellerSVC.update(sellerId, seller);
            
            if (result > 0) {
                log.info("판매자 정보 수정 성공: sellerId={}", sellerId);
                return ResponseEntity.ok(ApiResponse.success("정보가 수정되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("정보 수정에 실패했습니다."));
            }
            
        } catch (Exception e) {
            log.error("판매자 정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 판매자 비밀번호 확인
     */
    @PostMapping("/{sellerId}/verify-password")
    public ResponseEntity<Map<String, Object>> verifyPassword(
            @PathVariable Long sellerId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        log.info("판매자 비밀번호 확인 요청: sellerId={}", sellerId);

        try {
            AuthUtils.validateSellerAccess(session, sellerId);
            
            Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
            if (sellerOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 올바르지 않습니다."));
            }
            
            Seller seller = sellerOpt.get();
            String password = (String) request.get("password");
            boolean isValid = seller.getPassword().equals(password);
            
            if (isValid) {
                log.info("판매자 비밀번호 확인 성공: sellerId={}", sellerId);
                return ResponseEntity.ok(ApiResponse.success("비밀번호가 확인되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("비밀번호가 올바르지 않습니다."));
            }
            
        } catch (Exception e) {
            log.error("판매자 비밀번호 확인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 판매자 탈퇴
     */
    @DeleteMapping("/{sellerId}")
    public ResponseEntity<Map<String, Object>> withdrawSeller(
            @PathVariable Long sellerId,
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        
        log.info("판매자 탈퇴 요청: sellerId={}", sellerId);

        try {
            AuthUtils.validateSellerAccess(session, sellerId);
            
            String reason = (String) request.get("reason");
            int result = sellerSVC.withdrawWithReason(sellerId, reason);
            
            if (result > 0) {
                // 세션 무효화
                sessionService.logout(session);
                
                log.info("판매자 탈퇴 성공: sellerId={}", sellerId);
                return ResponseEntity.ok(ApiResponse.success("PLANT 웹사이트의 회원탈퇴가 완료되었습니다. 그동안 이용해주셔서 감사합니다."));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("탈퇴 처리에 실패했습니다."));
            }
            
        } catch (Exception e) {
            log.error("판매자 탈퇴 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 판매자 서비스 이용 현황 조회
     */
    @GetMapping("/{sellerId}/service-usage")
    public ResponseEntity<Map<String, Object>> getServiceUsage(
            @PathVariable Long sellerId,
            HttpSession session) {
        
        log.info("판매자 서비스 이용 현황 조회 요청: sellerId={}", sellerId);

        try {
            AuthUtils.validateSellerAccess(session, sellerId);
            
            Map<String, Object> statusInfo = getBasicSellerStatus();
            
            log.info("판매자 서비스 이용 현황 조회 성공: sellerId={}", sellerId);
            return ResponseEntity.ok(ApiResponse.success("조회가 완료되었습니다.", statusInfo));
            
        } catch (Exception e) {
            log.error("판매자 서비스 이용 현황 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailDuplication(
            @RequestParam String email) {
        
        log.info("이메일 중복 확인 요청: email={}", email);

        try {
            boolean exists = sellerSVC.existsByEmail(email);
            
            if (exists) {
                return ResponseEntity.ok(ApiResponse.error("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다."));
            } else {
                return ResponseEntity.ok(ApiResponse.success("사용 가능한 이메일입니다."));
            }
            
        } catch (Exception e) {
            log.error("이메일 중복 확인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("확인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사업자등록번호 중복 확인
     */
    @GetMapping("/check-biz-reg-no")
    public ResponseEntity<Map<String, Object>> checkBizRegNoDuplication(
            @RequestParam String bizRegNo) {
        
        log.info("사업자등록번호 중복 확인 요청: bizRegNo={}", bizRegNo);

        try {
            boolean exists = sellerSVC.existsByBizRegNo(bizRegNo);
            
            if (exists) {
                return ResponseEntity.ok(ApiResponse.error("이미 등록된 사업자등록번호입니다. 탈퇴 후 재가입은 불가능합니다."));
            } else {
                return ResponseEntity.ok(ApiResponse.success("사용 가능한 사업자등록번호입니다."));
            }
            
        } catch (Exception e) {
            log.error("사업자등록번호 중복 확인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("확인 중 오류가 발생했습니다."));
        }
    }

    /**
     * 탈퇴 가능 여부 확인
     */
    @GetMapping("/{sellerId}/can-withdraw")
    public ResponseEntity<Map<String, Object>> canWithdraw(
            @PathVariable Long sellerId,
            HttpSession session) {
        
        log.info("탈퇴 가능 여부 확인 요청: sellerId={}", sellerId);

        try {
            AuthUtils.validateSellerAccess(session, sellerId);
            
            // 판매자는 복잡한 탈퇴 검증이 필요하므로 기본적으로 가능으로 처리 (1차 구현)
            boolean canWithdraw = true;
            
            if (canWithdraw) {
                return ResponseEntity.ok(ApiResponse.success("탈퇴 가능합니다."));
            } else {
                throw new BusinessValidationException("탈퇴할 수 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("탈퇴 가능 여부 확인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ============ 유틸리티 메서드 ============

    /**
     * 폼 데이터를 엔티티로 변환
     */
    private Seller convertToEntity(SellerSignupForm form) {
        Seller seller = new Seller();
        seller.setEmail(form.getEmail());
        seller.setPassword(form.getPassword());
        seller.setName(form.getName());
        seller.setBizRegNo(form.getBusinessNumber());
        seller.setShopName(form.getStoreName());
        seller.setShopAddress(form.getFullAddress());
        seller.setTel(form.getTel());
        seller.setStatus(STATUS_ACTIVE);
        
        return seller;
    }

    /**
     * 현재 로그인한 판매자 정보 조회
     */
    private LoginMember getCurrentLoginMember(HttpSession session) {
        LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
        if (loginMember == null) {
            throw new BusinessValidationException("로그인이 필요합니다.");
        }
        
        if (!MEMBER_TYPE_SELLER.equals(loginMember.getMemberType())) {
            throw new SecurityException("판매자만 접근 가능합니다.");
        }
        
        return loginMember;
    }

    /**
     * 현재 비밀번호 확인
     */
    private boolean validateCurrentPassword(HttpSession session, String password) {
        LoginMember loginMember = getCurrentLoginMember(session);
        Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
        if (sellerOpt.isPresent()) {
            return sellerOpt.get().getPassword().equals(password);
        }
        return false;
    }
    
    /**
     * 기본 판매자 상태 정보 반환
     */
    private Map<String, Object> getBasicSellerStatus() {
        return Map.of(
            "activeProducts", 0,
            "pendingOrders", 0,
            "unsettledAmount", 0,
            "canWithdraw", true
        );
    }
}
