package com.KDT.mosi.domain.order.svc;

import com.KDT.mosi.domain.cart.repository.CartItemRepository;
import com.KDT.mosi.domain.entity.Member;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.entity.order.Order;
import com.KDT.mosi.domain.entity.order.OrderItem;
import com.KDT.mosi.domain.member.dao.MemberDAO;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.order.dto.OrderItemResponse;
import com.KDT.mosi.domain.order.dto.OrderResponse;
import com.KDT.mosi.domain.order.repository.OrderItemRepository;
import com.KDT.mosi.domain.order.repository.OrderRepository;
import com.KDT.mosi.domain.order.request.OrderFormRequest;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.domain.product.svc.ProductImageSVC;
import com.KDT.mosi.domain.entity.ProductImage;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSVCImpl implements OrderSVC {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberDAO memberDAO;
    private final ProductSVC productSVC;
    private final ProductImageSVC productImageSVC;
    private final SellerPageSVC sellerPageSVC;

    /**
     * 주문번호 생성 (MOSI-YYYYMMDD-XXX 형식)
     */
    private String generateOrderCode() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "MOSI-" + dateStr + "-";

        int todayCount = orderRepository.countByOrderCodeStartingWith(prefix);
        return String.format("MOSI-%s-%03d", dateStr, todayCount + 1);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderForm(Long buyerId, List<Long> cartItemIds) {
        try {
            // 1. 회원 정보 조회 (실시간)
            Member member = memberDAO.findById(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다"));

            // 2. 선택된 장바구니 상품들 조회
            List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
            if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("선택된 상품이 없습니다");
            }

            // 3. CartItem → OrderItemResponse 변환
            List<OrderItemResponse> orderItems = convertToOrderItems(cartItems);

            // 4. 결제 금액 계산 (할인/배송비 없음)
            Long totalPrice = 0L;
            int totalItemCount = 0;

            for (OrderItemResponse item : orderItems) {
                if (item.isAvailable()) {
                    totalPrice += item.getCurrentPrice() * item.getQuantity();
                    totalItemCount += item.getQuantity().intValue();
                }
            }

            return OrderResponse.createOrderFormSuccess(
                member.getName(),
                member.getTel(),
                member.getEmail(),
                orderItems,
                totalPrice,
                totalItemCount
            );

        } catch (IllegalArgumentException e) {
            log.error("주문서 조회 실패 - 잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문서 조회 실패: buyerId={}", buyerId, e);
            throw new RuntimeException("주문서 조회 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long buyerId, OrderFormRequest request) {
        try {
            // 1. 서버에서 실제 금액 재계산 (보안)
            Long serverCalculatedAmount = calculateTotalAmount(request.getCartItemIds());

            // 2. 클라이언트 금액과 서버 금액 비교
            if (!serverCalculatedAmount.equals(request.getTotalAmount())) {
                throw new IllegalArgumentException("결제 금액이 일치하지 않습니다. 새로고침 후 다시 시도해주세요.");
            }

            // 3. 장바구니 상품 재검증 (결제 직전 상태 확인)
            List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());
            validateCartItems(cartItems);

            // 5. 주문 생성
            String orderCode = generateOrderCode();

            Order order = new Order();
            order.setOrderCode(orderCode);
            order.setBuyerId(buyerId);
            order.setTotalPrice(serverCalculatedAmount);
            order.setSpecialRequest(request.getRequirements());
            order.setStatus("결제대기"); // 업계 표준: 결제 완료 전까지 대기 상태 유지
            order.setOrderDate(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            // 5. 주문 상품 생성
            createOrderItems(savedOrder.getOrderId(), cartItems);

            // 6. 장바구니에서 주문한 상품들 제거 (주문 확정 시)
            cartItemRepository.deleteAllById(request.getCartItemIds());

            return OrderResponse.createOrderCompleteSuccess(
                orderCode,
                savedOrder.getOrderId(),
                serverCalculatedAmount,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );

        } catch (IllegalArgumentException e) {
            log.error("주문 생성 실패 - 잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 생성 및 결제 실패: buyerId={}", buyerId, e);
            throw new RuntimeException("주문 처리 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(Long orderId, Long buyerId) {
        try {
            // 주문과 회원 정보를 함께 조회
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

            if (!order.getBuyerId().equals(buyerId)) {
                throw new IllegalArgumentException("접근 권한이 없습니다");
            }

            // 회원 정보 조회 (실시간)
            Member member = memberDAO.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다"));

            // 주문 상품 목록 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
            List<OrderItemResponse> orderItemResponses = convertOrderItemsToResponse(orderItems);

            return OrderResponse.createOrderDetailSuccess(
                member.getName(),
                member.getTel(),
                member.getEmail(),
                order.getOrderId(),
                order.getOrderCode(),
                order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                order.getStatus(),
                order.getSpecialRequest(),
                orderItemResponses,
                order.getTotalPrice(),
                orderItems.size()
            );

        } catch (IllegalArgumentException e) {
            log.error("주문 상세 조회 실패 - 잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 상세 조회 실패: orderId={}", orderId, e);
            throw new RuntimeException("주문 정보를 불러올 수 없습니다");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetailByCode(String orderCode, Long buyerId) {
        try {
            // 주문번호로 주문 조회
            Order order = orderRepository.findByOrderCode(orderCode);
            if (order == null) {
                throw new IllegalArgumentException("주문을 찾을 수 없습니다.");
            }

            // 구매자 확인
            if (!order.getBuyerId().equals(buyerId)) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }

            // 주문 상품 목록 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
            
            // OrderResponse 생성 (정가/할인가 정보 포함)
            return createOrderCompleteResponse(order, orderItems);

        } catch (IllegalArgumentException e) {
            log.error("주문 조회 실패 - 잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 조회 실패: orderCode={}, buyerId={}", orderCode, buyerId, e);
            throw new RuntimeException("주문 조회 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long buyerId) {
        try {
            // 주문 조회 및 권한 확인
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

            if (!order.getBuyerId().equals(buyerId)) {
                throw new IllegalArgumentException("접근 권한이 없습니다");
            }

            // 취소 가능 상태 확인
            if (!"결제완료".equals(order.getStatus()) && !"결제대기".equals(order.getStatus())) {
                throw new IllegalArgumentException("취소할 수 없는 주문 상태입니다");
            }

            // 주문 상태 변경
            order.setStatus("취소완료");
            orderRepository.save(order);

            return OrderResponse.createOrderCompleteSuccess(
                order.getOrderCode(),
                order.getOrderId(),
                order.getTotalPrice(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );

        } catch (IllegalArgumentException e) {
            log.error("주문 취소 실패 - 잘못된 요청: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("주문 취소 실패: orderId={}", orderId, e);
            throw new RuntimeException("주문 취소 중 오류가 발생했습니다");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrderResponse>> getOrderHistory(Long buyerId) {
        try {
            // Sort 파라미터로 최신순 정렬
            Sort sort = Sort.by("orderDate").descending();
            List<Order> orders = orderRepository.findByBuyerId(buyerId, sort);

            List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());
                    List<OrderItemResponse> orderItemResponses = convertOrderItemsToResponse(orderItems);

                    OrderResponse response = new OrderResponse();
                    response.setOrderId(order.getOrderId());
                    response.setOrderCode(order.getOrderCode());
                    response.setOrderDate(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    response.setOrderStatus(order.getStatus());
                    response.setOrderItems(orderItemResponses);
                    response.setTotalPrice(order.getTotalPrice());
                    response.setTotalItemCount(orderItems.size());

                    return response;
                })
                .collect(Collectors.toList());

            return ApiResponse.of(ApiResponseCode.SUCCESS, orderResponses);

        } catch (Exception e) {
            log.error("주문 목록 조회 실패: buyerId={}", buyerId, e);
            return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int getOrderCount(Long buyerId) {
        try {
            return orderRepository.countByBuyerId(buyerId);
        } catch (Exception e) {
            log.error("주문 개수 조회 실패: buyerId={}", buyerId, e);
            return 0;
        }
    }

    /**
     * CartItem → OrderItemResponse 변환
     */
    private List<OrderItemResponse> convertToOrderItems(List<CartItem> cartItems) {
        List<OrderItemResponse> result = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Optional<Product> productOpt = productSVC.getProduct(cartItem.getProductId());

            if (productOpt.isPresent()) {
                Product product = productOpt.get();

                // 모든 정보를 실시간으로 조회
                String sellerNickname = getSellerNickname(cartItem.getSellerId());
                String imageData = getProductImage(product);

                // 현재 상품 정보 (PRODUCT 테이블에서 실시간)
                String currentStatus = product.getStatus();
                Long currentPrice = getCurrentPrice(product, cartItem.getOptionType());
                Long currentOriginalPrice = getCurrentOriginalPrice(product, cartItem.getOptionType());

                // 장바구니 당시 정보 (CART_ITEMS 테이블)
                Long cartPrice = cartItem.getSalePrice();
                Long cartOriginalPrice = cartItem.getOriginalPrice();

                // 비즈니스 로직: 주문 가능 여부 판단 (Service Layer에서 처리)
                boolean isAvailable = "판매중".equals(currentStatus);
                boolean isPriceChanged = !currentPrice.equals(cartPrice);

                // 비즈니스 로직: 상태 메시지 결정 (Service Layer에서 처리)
                String statusMessage = null;
                if (!isAvailable) {
                    statusMessage = getStatusMessage(currentStatus);
                } else if (isPriceChanged) {
                    statusMessage = "가격변경";
                }

                if (isAvailable && !isPriceChanged) {
                    result.add(OrderItemResponse.createAvailable(
                        cartItem.getProductId(),
                        product.getTitle(),
                        product.getDescription(),
                        currentPrice,
                        currentOriginalPrice,
                        cartPrice,
                        cartOriginalPrice,
                        cartItem.getQuantity(),
                        cartItem.getOptionType(),
                        imageData,
                        sellerNickname
                    ));
                } else {
                    result.add(OrderItemResponse.createUnavailable(
                        cartItem.getProductId(),
                        product.getTitle(),
                        product.getDescription(),
                        currentPrice,
                        currentOriginalPrice,
                        cartPrice,
                        cartOriginalPrice,
                        cartItem.getQuantity(),
                        cartItem.getOptionType(),
                        imageData,
                        sellerNickname,
                        currentStatus,
                        statusMessage
                    ));
                }
            }
        }

        return result;
    }

    /**
     * OrderItem → OrderItemResponse 변환
     */
    private List<OrderItemResponse> convertOrderItemsToResponse(List<OrderItem> orderItems) {
        List<OrderItemResponse> result = new ArrayList<>();

        for (OrderItem orderItem : orderItems) {
            Optional<Product> productOpt = productSVC.getProduct(orderItem.getProductId());

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                String sellerNickname = getSellerNickname(orderItem.getSellerId());
                String imageData = getProductImage(product);

                result.add(OrderItemResponse.createAvailable(
                    orderItem.getProductId(),
                    product.getTitle(),
                    product.getDescription(),
                    orderItem.getSalePrice(),
                    orderItem.getOriginalPrice(),
                    orderItem.getSalePrice(),
                    orderItem.getOriginalPrice(),
                    orderItem.getQuantity(),
                    orderItem.getOptionType(),
                    imageData,
                    sellerNickname
                ));
            }
        }

        return result;
    }

    /**
     * 총 금액 계산
     */
    private Long calculateTotalAmount(List<Long> cartItemIds) {
        List<CartItem> cartItems = cartItemRepository.findAllById(cartItemIds);
        
        return cartItems.stream()
            .mapToLong(cartItem -> {
                // 업계 표준: 장바구니에 저장된 가격 사용 (UI 일관성)
                // 단, 상품 상태는 실시간 검증
                Optional<Product> productOpt = productSVC.getProduct(cartItem.getProductId());
                if (productOpt.isPresent() && "판매중".equals(productOpt.get().getStatus())) {
                    // CartItem의 salePrice 사용 (할인가)
                    return cartItem.getSalePrice() * cartItem.getQuantity();
                } else {
                    return 0L;
                }
            })
            .sum();
    }

    /**
     * 장바구니 상품 검증
     */
    private void validateCartItems(List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            Optional<Product> productOpt = productSVC.getProduct(cartItem.getProductId());

            if (productOpt.isEmpty()) {
                throw new IllegalArgumentException("상품을 찾을 수 없습니다");
            }

            Product product = productOpt.get();

            // 실시간 판매 상태 체크
            if (!"판매중".equals(product.getStatus())) {
                String message = String.format("%s 상품이 %s 상태입니다",
                    product.getTitle(), getStatusMessage(product.getStatus()));
                throw new IllegalArgumentException(message);
            }

            // 가격 변동 체크
            Long currentPrice = getCurrentPrice(product, cartItem.getOptionType());
            if (!currentPrice.equals(cartItem.getSalePrice())) {
                throw new IllegalArgumentException(String.format("%s 상품의 가격이 변경되었습니다", product.getTitle()));
            }
        }
    }

    /**
     * 주문 상품 생성
     */
    private void createOrderItems(Long orderId, List<CartItem> cartItems) {
        List<OrderItem> orderItems = cartItems.stream()
            .map(cartItem -> {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderId(orderId);
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setSellerId(cartItem.getSellerId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setOriginalPrice(cartItem.getOriginalPrice());
                orderItem.setSalePrice(cartItem.getSalePrice());
                orderItem.setOptionType(cartItem.getOptionType());
                orderItem.setReviewed("N");
                return orderItem;
            })
            .collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
    }

    /**
     * 주문 완료 응답 생성 (정가/할인가 정보 포함)
     */
    private OrderResponse createOrderCompleteResponse(Order order, List<OrderItem> orderItems) {
        // 주문자 정보 조회
        Member buyer = memberDAO.findById(order.getBuyerId())
            .orElse(null);
        List<OrderItemResponse> orderItemResponses = orderItems.stream()
            .map(orderItem -> {
                // 상품 정보 조회
                Optional<Product> productOpt = productSVC.getProduct(orderItem.getProductId());
                if (productOpt.isEmpty()) {
                    throw new IllegalArgumentException("상품을 찾을 수 없습니다");
                }
                Product product = productOpt.get();

                // 판매자 정보 조회
                String sellerNickname = "판매자";
                try {
                    Optional<SellerPage> sellerPageOpt = sellerPageSVC.findByMemberId(orderItem.getSellerId());
                    if (sellerPageOpt.isPresent()) {
                        sellerNickname = sellerPageOpt.get().getNickname();
                    }
                } catch (Exception e) {
                    log.warn("판매자 정보 조회 실패: sellerId={}", orderItem.getSellerId());
                }

                // 현재 상품의 정가/할인가 조회
                Long currentOriginalPrice = getCurrentOriginalPrice(product, orderItem.getOptionType());
                Long currentSalePrice = getCurrentPrice(product, orderItem.getOptionType());
                
                // 상품 이미지 조회
                String imageData = getProductImage(product);

                return OrderItemResponse.createAvailable(
                    orderItem.getProductId(),
                    product.getTitle(),
                    product.getDescription(),
                    currentSalePrice,
                    currentOriginalPrice,
                    orderItem.getSalePrice(),
                    orderItem.getOriginalPrice(),
                    orderItem.getQuantity().longValue(),
                    orderItem.getOptionType(),
                    imageData,
                    sellerNickname
                );
            })
            .collect(Collectors.toList());

        return OrderResponse.createOrderDetailSuccess(
            buyer != null ? buyer.getName() : null,
            buyer != null ? buyer.getTel() : null,
            buyer != null ? buyer.getEmail() : null,
            order.getOrderId(),
            order.getOrderCode(),
            order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            order.getStatus(),
            order.getSpecialRequest(),
            orderItemResponses,
            order.getTotalPrice(),
            orderItems.size()
        );
    }

    /**
     * 옵션에 따른 현재 정가 조회
     */
    private Long getCurrentOriginalPrice(Product product, String optionType) {
        if ("가이드포함".equals(optionType)) {
            return product.getGuidePrice() != null ?
                product.getGuidePrice().longValue() : 0L;
        } else {
            return product.getNormalPrice() != null ?
                product.getNormalPrice().longValue() : 0L;
        }
    }

    /**
     * 옵션에 따른 현재 판매가 조회
     */
    private Long getCurrentPrice(Product product, String optionType) {
        if ("가이드포함".equals(optionType)) {
            return product.getSalesGuidePrice() != null ?
                product.getSalesGuidePrice().longValue() : 0L;
        } else {
            return product.getSalesPrice() != null ?
                product.getSalesPrice().longValue() : 0L;
        }
    }

    /**
     * 상품 상태에 따른 메시지 반환
     */
    private String getStatusMessage(String productStatus) {
        switch (productStatus) {
            case "판매대기":
                return "판매중단";
            default:
                return productStatus;
        }
    }

    /**
     * 판매자 닉네임 조회
     */
    private String getSellerNickname(Long sellerId) {
        return sellerPageSVC.findByMemberId(sellerId)
            .map(SellerPage::getNickname)
            .orElse("판매자");
    }

    /**
     * 상품 이미지 조회
     * ProductImageSVC를 사용하여 상품 기본 정보에서 이미지를 가져옴
     */
    private String getProductImage(Product product) {
        List<ProductImage> images = productImageSVC.findByProductId(product.getProductId());
        log.info("🖼️ 주문 상품 이미지 조회: productId={}, 이미지 개수={}", product.getProductId(), images != null ? images.size() : 0);
        if (images != null && !images.isEmpty()) {
            String imageData = images.get(0).getBase64ImageData();
            log.info("🎯 주문 이미지 데이터 설정 완료: {}", imageData != null ? "성공" : "실패");
            return imageData;
        }
        return null;
    }


}
