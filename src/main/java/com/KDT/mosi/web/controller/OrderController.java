package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.order.dto.OrderRequest;
import com.KDT.mosi.domain.order.dto.PaymentRequest;
import com.KDT.mosi.domain.order.svc.OrderSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderSVC orderSVC;

    /**
     * 주문서 작성 페이지
     */
    @GetMapping
    public String orderPage(Model model, HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/members/login";
        }
        
        model.addAttribute("member", member);
        return "order/order-react";
    }

    /**
     * 주문 생성 API
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOrder(
            @Valid @RequestBody OrderRequest request, 
            HttpSession session) {
        
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null));
        }

        try {
            Map<String, Object> result = orderSVC.createOrder(
                member.getMemberId(),
                request.getCartItemIds(),
                request.getSpecialRequest()
            );
            
            boolean isSuccess = (boolean) result.get("isSuccess");
            if (isSuccess) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, result));
            } else {
                String errorCode = (String) result.get("errorCode");
                String message = (String) result.get("message");
                
                if ("PRICE_CHANGED".equals(errorCode)) {
                    return ResponseEntity.status(409)
                        .body(ApiResponse.of(ApiResponseCode.PRICE_CHANGED, result));
                } else if ("PRODUCT_UNAVAILABLE".equals(errorCode)) {
                    return ResponseEntity.status(409)
                        .body(ApiResponse.of(ApiResponseCode.PRODUCT_UNAVAILABLE, result));
                } else {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.of(ApiResponseCode.BAD_REQUEST, result));
                }
            }
        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.of(ApiResponseCode.SERVER_ERROR, null));
        }
    }

    /**
     * 결제 처리 API
     */
    @PostMapping("/payment")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> processPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpSession session) {
        
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null));
        }

        try {
            Map<String, Object> result = orderSVC.processPayment(
                request.getOrderId(),
                request.getPaymentMethod(),
                request.getAmount(),
                member.getMemberId()
            );
            
            boolean isSuccess = (boolean) result.get("isSuccess");
            if (isSuccess) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, result));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.PAYMENT_FAILED, result));
            }
        } catch (Exception e) {
            log.error("결제 처리 실패", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.of(ApiResponseCode.SERVER_ERROR, null));
        }
    }

    /**
     * 주문 완료 페이지
     */
    @GetMapping("/complete/{orderId}")
    public String orderComplete(@PathVariable Long orderId, 
                               Model model, 
                               HttpSession session) {
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/members/login";
        }

        Map<String, Object> orderDetail = orderSVC.getOrderDetail(orderId, member.getMemberId());
        if (orderDetail == null || !(boolean) orderDetail.get("isSuccess")) {
            return "redirect:/cart";
        }

        model.addAttribute("order", orderDetail.get("data"));
        model.addAttribute("member", member);
        return "order/order-complete";
    }

    /**
     * 주문 취소 API
     */
    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelOrder(
            @PathVariable Long orderId,
            HttpSession session) {
        
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return ResponseEntity.status(401)
                .body(ApiResponse.of(ApiResponseCode.LOGIN_REQUIRED, null));
        }

        try {
            Map<String, Object> result = orderSVC.cancelOrder(orderId, member.getMemberId());
            
            boolean isSuccess = (boolean) result.get("isSuccess");
            if (isSuccess) {
                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, result));
            } else {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.of(ApiResponseCode.BAD_REQUEST, result));
            }
        } catch (Exception e) {
            log.error("주문 취소 실패", e);
            return ResponseEntity.status(500)
                .body(ApiResponse.of(ApiResponseCode.SERVER_ERROR, null));
        }
    }
}
