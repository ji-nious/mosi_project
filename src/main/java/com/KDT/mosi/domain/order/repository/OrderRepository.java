package com.KDT.mosi.domain.order.repository;

import com.KDT.mosi.domain.entity.order.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /**
   * 주문 목록 조회
   */
  List<Order> findByBuyerId(Long buyerId, Sort sort);

  /**
   * 구매자별 주문 개수
   */
  int countByBuyerId(Long buyerId);

  /**
   * 주문 코드로 주문 조회
   */
  Order findByOrderCode(String orderCode);

  /**
   * 주문번호 생성용
   */
  int countByOrderCodeStartingWith(String prefix);
}