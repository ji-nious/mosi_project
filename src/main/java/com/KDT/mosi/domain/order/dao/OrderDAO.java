package com.KDT.mosi.domain.order.dao;

import com.KDT.mosi.domain.entity.order.Order;

import java.util.List;

public interface OrderDAO {
    
    // 주문 저장
    Long save(Order order);
    
    // 주문 조회
    Order findById(Long orderId);
    
    // 주문 코드로 조회
    Order findByOrderCode(String orderCode);
    
    // 구매자별 주문 목록
    List<Order> findByBuyerId(Long buyerId);
    
    // 주문 개수 조회
    int countByBuyerId(Long buyerId);
    
    // 주문 업데이트
    void update(Order order);
    
    // 주문 삭제
    void deleteById(Long orderId);
}
