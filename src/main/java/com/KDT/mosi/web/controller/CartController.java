package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.cart.request.CartFormRequest;
import com.KDT.mosi.domain.cart.dto.CartResponse;
import com.KDT.mosi.domain.cart.svc.CartSVC;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;


import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartSVC cartSVC;

  /**
   * 장바구니 HTML 페이지 반환 (브라우저 직접 접근)
   * GET /cart
   */
  @GetMapping(produces = "text/html")
  public String cartPageHtml(HttpSession session, Model model) {
    Member loginMember = (Member) session.getAttribute("loginMember");

    if (loginMember == null) {
      return "redirect:/login";
    }

    model.addAttribute("member", loginMember);
    log.info("장바구니 HTML 페이지 접근: memberId={}, nickname={}",
        loginMember.getMemberId(), loginMember.getNickname());

    return "cart/cart";
  }

  /**
   * 장바구니 JSON 데이터 반환 (React AJAX 호출)
   * GET /cart
   */
  @GetMapping(produces = "application/json")
  @ResponseBody
  public ResponseEntity<ApiResponse<CartResponse>> getCartJson(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      CartResponse cartResponse = cartSVC.getCart(
          loginMember.getMemberId(),
          loginMember.getNickname()
      );

      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, cartResponse)
      );

    } catch (Exception e) {
      log.error("장바구니 조회 오류: memberId={}", loginMember.getMemberId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 상품 추가
   * POST cart/add
   */
  @PostMapping("/add")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> addToCart(
      @Valid @RequestBody CartFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      ApiResponse<Void> result = cartSVC.addToCart(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType(),
          request.getQuantity()
      );

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("장바구니 추가 오류: memberId={}, productId={}",
          loginMember.getMemberId(), request.getProductId(), e);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 수량 변경
   * PUT cart/quantity
   */
  @PutMapping("/quantity")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> updateQuantity(
      @Valid @RequestBody CartFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      ApiResponse<Void> result = cartSVC.updateQuantity(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType(),
          request.getQuantity()
      );

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("수량 변경 오류: memberId={}, productId={}",
          loginMember.getMemberId(), request.getProductId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 상품 삭제
   * DELETE /cart/remove
   */
  @DeleteMapping("/remove")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> removeFromCart(
      @Valid @RequestBody CartFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      ApiResponse<Void> result = cartSVC.removeFromCart(
          loginMember.getMemberId(),
          request.getProductId(),
          request.getOptionType()
      );

      return ResponseEntity.ok(result);

    } catch (Exception e) {
      log.error("상품 삭제 오류: memberId={}, productId={}",
          loginMember.getMemberId(), request.getProductId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 상품 개수 조회
   * GET /cart/count
   */
  @GetMapping("/count")
  @ResponseBody
  public ResponseEntity<ApiResponse<Integer>> getCartItemCount(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, 0)
      );
    }

    try {
      int count = cartSVC.getCartItemCount(loginMember.getMemberId());
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, count)
      );
    } catch (Exception e) {
      log.error("장바구니 개수 조회 오류: memberId={}", loginMember.getMemberId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, 0)
      );
    }
  }

  /**
   * 장바구니 아이템 ID 목록으로 조회
   * GET /cart/items?ids=1,2,3
   */
  @GetMapping("/items")
  @ResponseBody
  public ResponseEntity<ApiResponse<Object>> getCartItemsByIds(
      @RequestParam("ids") String ids,
      HttpSession session) {
    
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      // 전체 장바구니 조회 후 클라이언트에서 필터링하도록 반환
      // (실제 구현에서는 서비스 레이어에서 ID별 필터링 구현 권장)
      CartResponse cartResponse = cartSVC.getCart(
          loginMember.getMemberId(),
          loginMember.getNickname()
      );

      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, cartResponse.getCartItems())
      );

    } catch (Exception e) {
      log.error("장바구니 아이템 조회 오류: memberId={}, ids={}", 
          loginMember.getMemberId(), ids, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }

  /**
   * 장바구니 전체 비우기
   * DELETE /cart/clear
   */
  @DeleteMapping("/clear")
  @ResponseBody
  public ResponseEntity<ApiResponse<Void>> clearCart(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      cartSVC.clearCart(loginMember.getMemberId());
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, null)
      );

    } catch (Exception e) {
      log.error("장바구니 비우기 오류: memberId={}", loginMember.getMemberId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }


}