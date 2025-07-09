package com.kh.project.web.api;

import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.svc.SellerSVC;
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
 * ?�매??API 컨트롤러 (?��??�된 ?�답)
 */
@Slf4j
@RestController
@RequestMapping("/api/sellers")
@RequiredArgsConstructor
@Validated
public class SellerApiController {

  private final SellerSVC sellerSVC;

  /**
   * ?�매???�원가??   */
  @PostMapping("/join")
  public ResponseEntity<Map<String, Object>> join(@Valid @RequestBody Seller seller) {
    try {
      log.info("?�매???�원가???�청: email={}", seller.getEmail());
      
      Seller savedSeller = sellerSVC.join(seller);
      String gubunName = MemberGubun.getDescriptionByCode(savedSeller.getGubun());
      
      Map<String, Object> response = ApiResponse.joinSuccess(savedSeller, gubunName);
      
      log.info("?�매???�원가???�공: sellerId={}", savedSeller.getSellerId());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("?�매???�원가???�패: email={}, error={}", seller.getEmail(), e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�매??로그??   */
  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
    try {
      String email = loginRequest.get("email");
      String password = loginRequest.get("password");
      
      log.info("?�매??로그???�청: email={}", email);

      Seller seller = sellerSVC.login(email, password);

      // ?�션 보안 강화
      LoginMember loginMember = LoginMember.seller(seller.getSellerId(), seller.getEmail());
      session.setAttribute("loginMember", loginMember);
      
      // ?�션 보안 ?�정
      session.setMaxInactiveInterval(1800); // 30�??�?�아??      
      String gubunName = MemberGubun.getDescriptionByCode(seller.getGubun());
      boolean canLogin = sellerSVC.canLogin(seller);
      
      Map<String, Object> response = ApiResponse.loginSuccess(seller, gubunName, canLogin);
      
      log.info("?�매??로그???�공: sellerId={}", seller.getSellerId());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("?�매??로그???�패: email={}, error={}", loginRequest.get("email"), e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("?�메???�는 비�?번호가 ?�바르�? ?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * 로그?�웃
   */
  @PostMapping("/logout")
  public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
    try {
      if (session != null) {
        session.invalidate();
      }
      
      Map<String, Object> response = ApiResponse.success("로그?�웃?�었?�니??");
      log.info("?�매??로그?�웃 ?�공");
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("로그?�웃 처리 �??�류 발생", e);
      
      Map<String, Object> response = ApiResponse.error("로그?�웃 처리???�패?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�매???�보 조회 (?�증 ?�요)
   */
  @GetMapping("/info")
  public ResponseEntity<Map<String, Object>> getSellerInfo(HttpSession session) {
    try {
      log.info("?�매???�보 조회 ?�청");
      
      // ?�션 검�?강화
      LoginMember loginMember = (LoginMember) session.getAttribute("loginMember");
      if (loginMember == null || !"SELLER".equals(loginMember.getMemberType())) {
        Map<String, Object> response = ApiResponse.error("로그?�이 ?�요?�니??");
        return ResponseEntity.status(401).body(response);
      }

      Optional<Seller> sellerOpt = sellerSVC.findById(loginMember.getId());
      
      if (sellerOpt.isEmpty()) {
        Map<String, Object> response = ApiResponse.error("존재?��? ?�는 ?�매?�입?�다.");
        return ResponseEntity.notFound().build();
      }

      Seller seller = sellerOpt.get();
      
      // 추�? ?�보 ?�성
      Map<String, Object> additionalData = Map.of(
          "gubunName", MemberGubun.getDescriptionByCode(seller.getGubun()),
          "canLogin", sellerSVC.canLogin(seller),
          "isWithdrawn", sellerSVC.isWithdrawn(seller)
      );
      
      Map<String, Object> response = ApiResponse.entitySuccess("?�매???�보 조회 ?�공", seller, additionalData);
      
      log.info("?�매???�보 조회 ?�공: sellerId={}", seller.getSellerId());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("?�매???�보 조회 ?�패: error={}", e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("?�매???�보 조회???�패?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ID�??�매??조회
   */
  @GetMapping("/{sellerId}")
  public ResponseEntity<Map<String, Object>> getSeller(@PathVariable Long sellerId) {
    try {
      Optional<Seller> sellerOpt = sellerSVC.findById(sellerId);
      
      if (sellerOpt.isEmpty()) {
        Map<String, Object> response = ApiResponse.error("존재?��? ?�는 ?�매?�입?�다.");
        return ResponseEntity.notFound().build();
      }
      
      Seller seller = sellerOpt.get();
      
      Map<String, Object> additionalData = Map.of(
          "gubunName", MemberGubun.getDescriptionByCode(seller.getGubun()),
          "canLogin", sellerSVC.canLogin(seller),
          "isWithdrawn", sellerSVC.isWithdrawn(seller)
      );
      
      Map<String, Object> response = ApiResponse.entitySuccess("?�매??조회 ?�공", seller, additionalData);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("?�매??조회 ?�패: sellerId={}, error={}", sellerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("?�매???�보 조회???�패?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�보 ?�정
   */
  @PutMapping("/{sellerId}")
  public ResponseEntity<Map<String, Object>> update(
      @PathVariable Long sellerId,
      @Valid @RequestBody Seller seller) {
    
    try {
      int updatedRows = sellerSVC.update(sellerId, seller);
      
      if (updatedRows > 0) {
        Map<String, Object> response = ApiResponse.success("?�보가 ?�정?�었?�니??");
        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> response = ApiResponse.error("?�보 ?�정???�패?�습?�다.");
        return ResponseEntity.badRequest().body(response);
      }
      
    } catch (Exception e) {
      log.error("?�보 ?�정 ?�패: sellerId={}, error={}", sellerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�퇴 처리
   */
  @PostMapping("/{sellerId}/withdraw")
  public ResponseEntity<Map<String, Object>> withdraw(
      @PathVariable Long sellerId,
      @RequestBody Map<String, String> request) {
    
    try {
      String reason = request.get("reason");
      int withdrawnRows = sellerSVC.withdraw(sellerId, reason);
      
      if (withdrawnRows > 0) {
        Map<String, Object> response = ApiResponse.success("?�퇴 처리가 ?�료?�었?�니??");
        return ResponseEntity.ok(response);
      } else {
        Map<String, Object> response = ApiResponse.error("?�퇴 처리???�패?�습?�다.");
        return ResponseEntity.badRequest().body(response);
      }
      
    } catch (Exception e) {
      log.error("?�퇴 ?�패: sellerId={}, error={}", sellerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�급 ?�급
   */
  @PostMapping("/{sellerId}/upgrade")
  public ResponseEntity<Map<String, Object>> upgradeGrade(
      @PathVariable Long sellerId,
      @RequestBody Map<String, String> request) {
    
    try {
      String newGrade = request.get("gubun");
      sellerSVC.upgradeGubun(sellerId, newGrade);
      
      Map<String, Object> responseData = Map.of(
          "gubun", newGrade,
          "gubunName", MemberGubun.getDescriptionByCode(newGrade)
      );
      
      Map<String, Object> response = ApiResponse.success("?�급???�급?�었?�니??", responseData);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("?�급 ?�급 ?�패: sellerId={}, error={}", sellerId, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error(e.getMessage());
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�메??중복 체크
   */
  @GetMapping("/check-email")
  public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
    try {
      boolean exists = sellerSVC.existsByEmail(email);
      
      Map<String, Object> data = Map.of(
          "exists", exists,
          "available", !exists,
          "email", email
      );
      
      String message = exists ? "?��? ?�용중인 ?�메?�입?�다." : "?�용 가?�한 ?�메?�입?�다.";
      Map<String, Object> response = ApiResponse.success(message, data);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("?�메??중복 체크 ?�패: email={}, error={}", email, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("?�메??중복 체크???�패?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�업?�등록번??중복 체크
   */
  @GetMapping("/check-bizregno")
  public ResponseEntity<Map<String, Object>> checkBizRegNo(@RequestParam String bizRegNo) {
    try {
      boolean exists = sellerSVC.existsByBizRegNo(bizRegNo);
      
      Map<String, Object> data = Map.of(
          "exists", exists,
          "available", !exists,
          "bizRegNo", bizRegNo
      );
      
      String message = exists ? "?��? ?�용중인 ?�업?�등록번?�입?�다." : "?�용 가?�한 ?�업?�등록번?�입?�다.";
      Map<String, Object> response = ApiResponse.success(message, data);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("?�업?�등록번??중복 체크 ?�패: bizRegNo={}, error={}", bizRegNo, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("?�업?�등록번??중복 체크???�패?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }

  /**
   * ?�호�?중복 체크
   */
  @GetMapping("/check-shopname")
  public ResponseEntity<Map<String, Object>> checkShopName(@RequestParam String shopName) {
    try {
      boolean exists = sellerSVC.existsByShopName(shopName);
      
      Map<String, Object> data = Map.of(
          "exists", exists,
          "available", !exists,
          "shopName", shopName
      );
      
      String message = exists ? "?��? ?�용중인 ?�호명입?�다." : "?�용 가?�한 ?�호명입?�다.";
      Map<String, Object> response = ApiResponse.success(message, data);
      
      return ResponseEntity.ok(response);
      
    } catch (Exception e) {
      log.error("?�호�?중복 체크 ?�패: shopName={}, error={}", shopName, e.getMessage());
      
      Map<String, Object> response = ApiResponse.error("?�호�?중복 체크???�패?�습?�다.");
      return ResponseEntity.badRequest().body(response);
    }
  }
}
