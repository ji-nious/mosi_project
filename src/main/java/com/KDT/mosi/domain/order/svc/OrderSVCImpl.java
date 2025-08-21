package com.KDT.mosi.domain.order.svc;

import com.KDT.mosi.domain.cart.repository.CartItemRepository;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.entity.order.Order;
import com.KDT.mosi.domain.entity.order.OrderItem;
import com.KDT.mosi.domain.order.repository.OrderRepository;
import com.KDT.mosi.domain.order.repository.OrderItemRepository;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSVCImpl implements OrderSVC {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductSVC productSVC;

    @Override
    @Transactional
    public Map<String, Object> createOrder(Long buyerId, List<Long> cartItemIds, String specialRequest) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 장바구니 아이템 조회 및 검증
            List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
            if (cartItems.isEmpty()) {
                result.put("isSuccess", false);
                result.put("message", "선택된 상품이 없습니다");
                return result;
            }

            // 상품 상태 및 가격 검증
            List<OrderItem> orderItems = new ArrayList<>();
            Long totalAmount = 0L;
            
            for (CartItem cartItem : cartItems) {
                Optional<Product> productOpt = productSVC.getProduct(cartItem.getProductId());
                if (productOpt.isEmpty()) {
                    result.put("isSuccess", false);
                    result.put("errorCode", "PRODUCT_NOT_FOUND");
                    result.put("message", "상품을 찾을 수 없습니다");
                    return result;
                }
                
                Product product = productOpt.get();

                // 판매중단 검증
                if (!"판매중".equals(product.getStatus())) {
                    result.put("isSuccess", false);
                    result.put("errorCode", "PRODUCT_UNAVAILABLE");
                    result.put("message", "판매가 중단된 상품이 포함되어 있습니다");
                    result.put("productName", product.getTitle());
                    return result;
                }

                // 가격 변동 검증
                if (!product.getPrice().equals(cartItem.getPrice())) {
                    result.put("isSuccess", false);
                    result.put("errorCode", "PRICE_CHANGED");
                    result.put("message", "상품 가격이 변경되었습니다");
                    result.put("productName", product.getTitle());
                    result.put("oldPrice", cartItem.getPrice());
                    result.put("newPrice", product.getPrice());
                    return result;
                }

                // 주문 아이템 생성
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setSellerId(product.getSellerId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setOriginalPrice(product.getOriginalPrice());
                orderItem.setSalePrice(product.getPrice());
                orderItem.setOptionType(cartItem.getOptionType());
                orderItem.setReviewed("N");
                
                orderItems.add(orderItem);
                totalAmount += product.getPrice() * cartItem.getQuantity();
            }

            // 주문 생성
            Order order = new Order();
            order.setOrderCode(generateOrderCode());
            order.setBuyerId(buyerId);
            order.setTotalPrice(totalAmount);
            order.setSpecialRequest(specialRequest);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");

            // 주문 저장
            Order savedOrder = orderRepository.save(order);
            Long orderId = savedOrder.getOrderId();

            // 주문 아이템 저장
            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderId(orderId);
                orderItemRepository.save(orderItem);
            }

            // 장바구니에서 주문한 상품 제거
            cartItemRepository.deleteAllById(cartItemIds);

            result.put("isSuccess", true);
            result.put("orderId", orderId);
            result.put("orderCode", order.getOrderCode());
            result.put("totalAmount", totalAmount);
            
            log.info("주문 생성 성공: orderId={}, buyerId={}, amount={}", orderId, buyerId, totalAmount);
            
        } catch (Exception e) {
            log.error("주문 생성 실패: buyerId={}", buyerId, e);
            result.put("isSuccess", false);
            result.put("message", "주문 생성 중 오류가 발생했습니다");
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> processPayment(Long orderId, String paymentMethod, Long amount, Long buyerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty() || !orderOpt.get().getBuyerId().equals(buyerId)) {
                result.put("isSuccess", false);
                result.put("message", "주문을 찾을 수 없습니다");
                return result;
            }
            
            Order order = orderOpt.get();

            // 금액 검증
            if (!order.getTotalPrice().equals(amount)) {
                result.put("isSuccess", false);
                result.put("message", "결제 금액이 일치하지 않습니다");
                return result;
            }

            // 임시 결제 처리 (실제로는 PG사 연동)
            boolean paymentSuccess = processExternalPayment(orderId, paymentMethod, amount);
            
            if (paymentSuccess) {
                // 주문 상태 업데이트
                order.setStatus("PAID");
                orderRepository.save(order);
                
                result.put("isSuccess", true);
                result.put("orderId", orderId);
                result.put("orderCode", order.getOrderCode());
                result.put("message", "결제가 완료되었습니다");
                
                log.info("결제 처리 성공: orderId={}, amount={}", orderId, amount);
            } else {
                result.put("isSuccess", false);
                result.put("message", "결제 처리에 실패했습니다");
            }
            
        } catch (Exception e) {
            log.error("결제 처리 실패: orderId={}", orderId, e);
            result.put("isSuccess", false);
            result.put("message", "결제 처리 중 오류가 발생했습니다");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getBuyerOrders(Long buyerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Order> orders = orderRepository.findByBuyerId(buyerId);
            result.put("isSuccess", true);
            result.put("orders", orders);
            
        } catch (Exception e) {
            log.error("주문 목록 조회 실패: buyerId={}", buyerId, e);
            result.put("isSuccess", false);
            result.put("message", "주문 목록을 불러올 수 없습니다");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getOrderDetail(Long orderId, Long buyerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty() || !orderOpt.get().getBuyerId().equals(buyerId)) {
                result.put("isSuccess", false);
                result.put("message", "주문을 찾을 수 없습니다");
                return result;
            }
            
            Order order = orderOpt.get();
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            
            Map<String, Object> orderDetail = new HashMap<>();
            orderDetail.put("order", order);
            orderDetail.put("orderItems", orderItems);
            
            result.put("isSuccess", true);
            result.put("data", orderDetail);
            
        } catch (Exception e) {
            log.error("주문 상세 조회 실패: orderId={}", orderId, e);
            result.put("isSuccess", false);
            result.put("message", "주문 정보를 불러올 수 없습니다");
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> cancelOrder(Long orderId, Long buyerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty() || !orderOpt.get().getBuyerId().equals(buyerId)) {
                result.put("isSuccess", false);
                result.put("message", "주문을 찾을 수 없습니다");
                return result;
            }
            
            Order order = orderOpt.get();

            if ("CANCELLED".equals(order.getStatus())) {
                result.put("isSuccess", false);
                result.put("message", "이미 취소된 주문입니다");
                return result;
            }

            // 주문 상태 업데이트
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            
            result.put("isSuccess", true);
            result.put("message", "주문이 취소되었습니다");
            
            log.info("주문 취소 성공: orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("주문 취소 실패: orderId={}", orderId, e);
            result.put("isSuccess", false);
            result.put("message", "주문 취소 중 오류가 발생했습니다");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getOrderByCode(String orderCode, Long buyerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Order> orderOpt = orderRepository.findByOrderCodeAndBuyerId(orderCode, buyerId);
            if (orderOpt.isEmpty()) {
                result.put("isSuccess", false);
                result.put("message", "주문을 찾을 수 없습니다");
                return result;
            }
            
            Order order = orderOpt.get();

            result.put("isSuccess", true);
            result.put("order", order);
            
        } catch (Exception e) {
            log.error("주문 코드 조회 실패: orderCode={}", orderCode, e);
            result.put("isSuccess", false);
            result.put("message", "주문 정보를 불러올 수 없습니다");
        }
        
        return result;
    }

    @Override
    public int getOrderCount(Long buyerId) {
        try {
            return orderRepository.countByBuyerId(buyerId);
        } catch (Exception e) {
            log.error("주문 개수 조회 실패: buyerId={}", buyerId, e);
            return 0;
        }
    }

    /**
     * 주문 코드 생성 (MOSI-YYYYMMDD-XXX 형식)
     */
    private String generateOrderCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%03d", System.currentTimeMillis() % 1000);
        return "MOSI-" + dateStr + "-" + sequence;
    }

    /**
     * 외부 결제 처리 (임시 구현)
     */
    private boolean processExternalPayment(Long orderId, String paymentMethod, Long amount) {
        // 실제로는 PG사 API 호출
        log.info("임시 결제 처리: orderId={}, method={}, amount={}", orderId, paymentMethod, amount);
        
        // 임시로 항상 성공으로 처리
        return true;
    }
}
