package com.KDT.mosi.domain.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {

  // 회원 정보 (MEMBER 테이블)
  private String buyerName;
  private String buyerPhone;
  private String buyerEmail;

  private Long orderId;
  private String orderCode;
  private String orderDate;
  private String orderStatus;
  private List<OrderItemResponse> orderItems;
  private Long totalPrice;
  private int totalItemCount;

  /**
   * 주문서 성공 응답
   */
  public static OrderResponse createOrderFormSuccess(String buyerName, String buyerPhone, String buyerEmail,
                                                     List<OrderItemResponse> orderItems,
                                                     Long totalPrice, int totalItemCount) {
    OrderResponse response = new OrderResponse();
    response.setBuyerName(buyerName);
    response.setBuyerPhone(buyerPhone);
    response.setBuyerEmail(buyerEmail);
    response.setOrderItems(orderItems);
    response.setTotalPrice(totalPrice);
    response.setTotalItemCount(totalItemCount);
    return response;
  }

  /**
   * 주문 완료 응답
   */
  public static OrderResponse createOrderCompleteSuccess(String orderCode, Long orderId,
                                                         Long totalPrice, String orderDate) {
    OrderResponse response = new OrderResponse();
    response.setOrderId(orderId);
    response.setOrderCode(orderCode);
    response.setOrderDate(orderDate);
    response.setOrderStatus("결제완료");
    response.setTotalPrice(totalPrice);
    response.setOrderItems(List.of());
    return response;
  }

  /**
   * 주문 상세조회 성공 응답
   */
  public static OrderResponse createOrderDetailSuccess(String buyerName, String buyerPhone, String buyerEmail,
                                                       Long orderId, String orderCode, String orderDate,
                                                       String orderStatus, List<OrderItemResponse> orderItems,
                                                       Long totalPrice, int totalItemCount) {
    OrderResponse response = new OrderResponse();
    response.setBuyerName(buyerName);
    response.setBuyerPhone(buyerPhone);
    response.setBuyerEmail(buyerEmail);
    response.setOrderId(orderId);
    response.setOrderCode(orderCode);
    response.setOrderDate(orderDate);
    response.setOrderStatus(orderStatus);
    response.setOrderItems(orderItems);
    response.setTotalPrice(totalPrice);
    response.setTotalItemCount(totalItemCount);
    return response;
  }
}
