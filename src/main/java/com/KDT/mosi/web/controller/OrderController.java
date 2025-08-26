package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.*;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.order.dto.OrderResponse;
import com.KDT.mosi.domain.order.request.OrderFormRequest;
import com.KDT.mosi.domain.order.svc.OrderSVC;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderSVC orderSVC;
  private final ProductSVC productSVC;
  private final ProductImageSVC productImageSVC;
  private final SellerPageSVC sellerPageSVC;

  /**
   * 주문서 페이지 (통합)
   */
  @GetMapping
  public String orderPage(
      @RequestParam(value = "cartItemIds", required = false) List<Long> cartItemIds,
      HttpSession session, Model model) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    log.info("주문 페이지 접근: memberId={}, cartItemIds={}", 
        loginMember.getMemberId(), cartItemIds);

    // URL 파라미터가 있으면 model에 추가 (기존 방식)
    if (cartItemIds != null && !cartItemIds.isEmpty()) {
      model.addAttribute("cartItemIds", cartItemIds);
      
      // 주문 진행 상태를 세션에 저장 (페이지 이동 시 상태 유지용)
      Map<String, Object> orderState = new HashMap<>();
      orderState.put("cartItemIds", cartItemIds);
      orderState.put("timestamp", System.currentTimeMillis());
      session.setAttribute("orderInProgress", orderState);
    } else {
      // URL 파라미터가 없으면 세션에서 주문 상태 복원 시도
      Map<String, Object> orderState = (Map<String, Object>) session.getAttribute("orderInProgress");
      if (orderState != null) {
        List<Long> sessionCartItemIds = (List<Long>) orderState.get("cartItemIds");
        if (sessionCartItemIds != null && !sessionCartItemIds.isEmpty()) {
          model.addAttribute("cartItemIds", sessionCartItemIds);
        }
      }
    }

    return "order/order";
  }

  /**
   * 주문서 데이터 조회 API
   */
  @GetMapping(value = "/form", produces = "application/json")
  @ResponseBody
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderForm(
      @RequestParam("cartItemIds") List<Long> cartItemIds,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    OrderResponse response = orderSVC.getOrderForm(loginMember.getMemberId(), cartItemIds);
    return ResponseEntity.ok(
        ApiResponse.of(ApiResponseCode.SUCCESS, response)
    );
  }



  /**
   * 주문 생성 API
   */
  @PostMapping(value = "/create", produces = "application/json")
  @ResponseBody
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @Valid @RequestBody OrderFormRequest request,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    OrderResponse response = orderSVC.createOrder(loginMember.getMemberId(), request);
    
    // 주문 생성 성공 시 주문 상태를 세션에 저장
    if (response != null) {
      Map<String, Object> orderState = new HashMap<>();
      orderState.put("orderCode", response.getOrderCode());
      orderState.put("orderItems", response.getOrderItems());
      orderState.put("totalAmount", response.getTotalAmount());
      orderState.put("timestamp", System.currentTimeMillis());
      session.setAttribute("orderInProgress", orderState);
    }
    
    return ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResponse.of(ApiResponseCode.SUCCESS, response)
    );
  }

  /**
   * 주문 완료 페이지
   */
  @GetMapping("/complete")
  public String orderCompletePage(
      @RequestParam("orderCode") String orderCode,
      HttpSession session, Model model) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return "redirect:/login";
    }

    // 주문 완료 시 주문 진행 상태 세션에서 제거
    session.removeAttribute("orderInProgress");

    model.addAttribute("orderCode", orderCode);
    model.addAttribute("member", loginMember);
    return "order/order-complete";
  }

  /**
   * 주문 완료 데이터 조회 API
   */
  @GetMapping(value = "/complete/data", produces = "application/json")
  @ResponseBody
  public ResponseEntity<ApiResponse<OrderResponse>> getOrderCompleteData(
      @RequestParam("orderCode") String orderCode,
      HttpSession session) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    // 주문번호로 주문 상세 정보 조회
    try {
      OrderResponse response = orderSVC.getOrderDetailByCode(orderCode, loginMember.getMemberId());
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, response)
      );
    } catch (Exception e) {
      log.error("주문 완료 데이터 조회 오류: orderCode={}, memberId={}", 
          orderCode, loginMember.getMemberId(), e);
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null)
        );
    }
  }

  /**
   * 주문내역 확인 이동
   */
  @GetMapping("/complete/history")
  public String redirectToOrderHistory(Model model, HttpSession session,
                                       HttpServletRequest request,
                                       @RequestParam(name = "page", defaultValue = "1") int page,
                                       @RequestParam(name = "size", defaultValue = "5") int size ) {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      throw new IllegalStateException("로그인한 회원이 아닙니다.");
    }
    Long memberId = loginMember.getMemberId();

    // ⭐⭐ 수정된 부분: page와 size를 서비스 메소드로 전달 ⭐⭐
    ApiResponse<List<OrderResponse>> orderHistoryResponse = orderSVC.getOrderHistory(memberId, page, size);

    List<OrderResponse> orderResponses = null;
    if (orderHistoryResponse.getHeader().getRtcd().equals(ApiResponseCode.SUCCESS.getRtcd())) {
      orderResponses = orderHistoryResponse.getBody();

      // ⭐⭐ 수정된 부분: 페이징 정보를 모델에 추가 ⭐⭐
      model.addAttribute("paging", orderHistoryResponse.getPaging());
    } else {
      orderResponses = Collections.emptyList();
      log.error("주문 목록을 가져오는 데 실패했습니다. 오류 코드: {}", orderHistoryResponse.getHeader().getRtcd());
    }

    model.addAttribute("loginMember",loginMember);
    model.addAttribute("activePath", request.getRequestURI());
    model.addAttribute("orderHistory", orderResponses);


    return "order/review_list_buyer";
  }

  /**
   * 쇼핑 계속하기 이동
   */
  @GetMapping("/complete/shopping")
  public String continueShopping() {
    return "redirect:/product/list";
  }

  /**
   * 주문용 회원 정보 조회 API (책임 분리)
   */
  @GetMapping("/member-info")
  @ResponseBody
  public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderMemberInfo(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");

    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    Map<String, Object> memberInfo = new HashMap<>();
    memberInfo.put("name", loginMember.getName());
    memberInfo.put("tel", loginMember.getTel());
    memberInfo.put("email", loginMember.getEmail());

    return ResponseEntity.ok(
        ApiResponse.of(ApiResponseCode.SUCCESS, memberInfo)
    );
  }

  /**
   * 주문 상태 복원 API
   */
  @GetMapping("/session-state")
  @ResponseBody
  public ResponseEntity<ApiResponse<Map<String, Object>>> getOrderSessionState(HttpSession session) {
    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    Map<String, Object> orderState = (Map<String, Object>) session.getAttribute("orderInProgress");
    if (orderState == null) {
      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, new HashMap<>())
      );
    }

    return ResponseEntity.ok(
        ApiResponse.of(ApiResponseCode.SUCCESS, orderState)
    );
  }

  /**
   * 결제 처리 API (임시 시뮬레이션)
   */
  @PostMapping("/payment")
  @ResponseBody
  public ResponseEntity<ApiResponse<OrderResponse>> processPayment(
      @RequestBody Map<String, Object> request,
      HttpSession session) throws InterruptedException {

    Member loginMember = (Member) session.getAttribute("loginMember");
    if (loginMember == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
          ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null)
      );
    }

    try {
      // 임시 결제 시뮬레이션 (2초 지연)
      Thread.sleep(2000);

      // 주문 코드 추출
      String orderCode = (String) request.get("orderCode");
      if (orderCode == null || orderCode.isEmpty()) {
        return ResponseEntity.badRequest().body(
            ApiResponse.of(ApiResponseCode.INVALID_PARAMETER, null)
        );
      }

      // 결제 완료 처리 (상태를 결제대기 → 결제완료로 변경)
      OrderResponse response = orderSVC.completePayment(orderCode, loginMember.getMemberId());

      return ResponseEntity.ok(
          ApiResponse.of(ApiResponseCode.SUCCESS, response)
      );

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(
          ApiResponse.of(ApiResponseCode.INVALID_PARAMETER, null)
      );
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
          ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null)
      );
    }
  }


}