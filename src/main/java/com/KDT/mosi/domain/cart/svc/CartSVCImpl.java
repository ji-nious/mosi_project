package com.KDT.mosi.domain.cart.svc;

import com.KDT.mosi.domain.cart.dto.CartItemResponse;
import com.KDT.mosi.domain.cart.dto.CartResponse;
import com.KDT.mosi.domain.cart.repository.CartItemRepository;
import com.KDT.mosi.domain.cart.repository.CartRepository;
import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.SellerPage;
import com.KDT.mosi.domain.entity.cart.Cart;
import com.KDT.mosi.domain.entity.cart.CartItem;
import com.KDT.mosi.domain.mypage.seller.svc.SellerPageSVC;
import com.KDT.mosi.domain.product.svc.ProductSVC;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CartSVCImpl implements CartSVC {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductSVC productSVC;
  private final SellerPageSVC sellerPageSVC;


  /**
   * 장바구니 상품 추가
   */
  @Override
  public ApiResponse<Void> addToCart(Long buyerId, Long productId, String optionType, Long quantity) {
    try {
      if (quantity <= 0) {
        return ApiResponse.of(ApiResponseCode.INVALID_PARAMETER, null);
      }

      Optional<Product> productOpt = productSVC.getProduct(productId);
      if (productOpt.isEmpty() || !"판매중".equals(productOpt.get().getStatus())) {
        return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
      }

      Product product = productOpt.get();

      Optional<CartItem> existingItem = cartItemRepository
          .findByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      if (existingItem.isPresent()) {
        CartItem item = existingItem.get();
        item.setQuantity(item.getQuantity() + quantity);
        cartItemRepository.save(item);
      } else {
        Cart cart = getOrCreateCart(buyerId);

        CartItem newItem = new CartItem();
        newItem.setCartId(cart.getCartId());
        newItem.setBuyerId(buyerId);
        newItem.setSellerId(product.getMember().getMemberId());
        newItem.setProductId(productId);
        newItem.setOptionType(optionType);
        newItem.setQuantity(quantity);

        setPrice(newItem, product, optionType);
        cartItemRepository.save(newItem);
      }

      updateCartTotal(buyerId);
      return ApiResponse.of(ApiResponseCode.SUCCESS, null);

    } catch (Exception e) {
      log.error("장바구니 추가 중 오류 발생: buyerId={}, productId={}", buyerId, productId, e);
      return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
    }
  }

  /**
   * 장바구니 조회
   */
  @Override
  @Transactional(readOnly = true)
  public CartResponse getCart(Long buyerId, String memberNickname) {
    try {
      List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);

      if (items.isEmpty()) {
        return CartResponse.createEmptyCart(memberNickname, buyerId);
      }

      // Entity → DTO 변환
      List<CartItemResponse> cartItems = convertToCartItemResponses(items);

      long totalPrice = 0;
      int totalQuantity = 0;

      // 서버에서 미리 계산
      for (CartItemResponse dto : cartItems) {
        if (dto.isAvailable()) {
          totalPrice += dto.getPrice() * dto.getQuantity();
          totalQuantity += dto.getQuantity().intValue();
        }
      }

      return CartResponse.createSuccess(
          memberNickname,
          buyerId,
          cartItems,
          (long) cartItems.size(),
          (long) totalQuantity,
          totalPrice
      );

    } catch (Exception e) {
      log.error("장바구니 조회 중 오류 발생: buyerId={}", buyerId, e);
      return CartResponse.createError(memberNickname, buyerId, "장바구니 조회 중 오류가 발생했습니다");
    }
  }

  /**
   * 수량 변경
   */
  @Override
  public ApiResponse<Void> updateQuantity(Long buyerId, Long productId, String optionType, Long quantity) {
    try {
      if (quantity <= 0) {
        cartItemRepository.deleteByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);
        updateCartTotal(buyerId);
        return ApiResponse.of(ApiResponseCode.SUCCESS, null);
      }

      Optional<CartItem> itemOpt = cartItemRepository
          .findByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);

      if (itemOpt.isEmpty()) {
        return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
      }

      CartItem item = itemOpt.get();
      item.setQuantity(quantity);
      cartItemRepository.save(item);

      updateCartTotal(buyerId);
      return ApiResponse.of(ApiResponseCode.SUCCESS, null);

    } catch (Exception e) {
      log.error("수량 변경 중 오류 발생: buyerId={}, productId={}", buyerId, productId, e);
      return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
    }
  }

  /**
   * 상품 삭제
   */
  @Override
  public ApiResponse<Void> removeFromCart(Long buyerId, Long productId, String optionType) {
    try {
      cartItemRepository.deleteByBuyerIdAndProductIdAndOptionType(buyerId, productId, optionType);
      updateCartTotal(buyerId);
      return ApiResponse.of(ApiResponseCode.SUCCESS, null);

    } catch (Exception e) {
      log.error("상품 삭제 중 오류 발생: buyerId={}, productId={}", buyerId, productId, e);
      return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, null);
    }
  }

  /**
   * 장바구니 비우기
   */
  @Override
  public void clearCart(Long buyerId) {
    try {
      cartItemRepository.deleteByBuyerId(buyerId);
      updateCartTotal(buyerId);
      log.info("장바구니 전체 비우기 완료: buyerId={}", buyerId);
    } catch (Exception e) {
      log.error("장바구니 비우기 중 오류 발생: buyerId={}", buyerId, e);
    }
  }

  /**
   * 상품 개수 조회
   */
  @Override
  @Transactional(readOnly = true)
  public int getCartItemCount(Long buyerId) {
    try {
      return cartItemRepository.countByBuyerId(buyerId);
    } catch (Exception e) {
      log.error("장바구니 개수 조회 중 오류 발생: buyerId={}", buyerId, e);
      return 0;
    }
  }

  /**
   * CartItem → CartItemResponse 변환
   * React+Vite와 호환되는 DTO 변환
   */
  private List<CartItemResponse> convertToCartItemResponses(List<CartItem> items) {
    List<CartItemResponse> result = new ArrayList<>();

    for (CartItem item : items) {
      Optional<Product> productOpt = productSVC.getProduct(item.getProductId());

      if (productOpt.isPresent()) {
        Product product = productOpt.get();
        String sellerNickname = getSellerNickname(item.getSellerId());
        boolean isAvailable = "판매중".equals(product.getStatus());
        
        // 첫 번째 상품 이미지 (간단하게)
        String imageData = null;
        if (product.getProductImages() != null && !product.getProductImages().isEmpty()) {
          imageData = product.getProductImages().get(0).getBase64ImageData();
        }
        
        CartItemResponse dto = isAvailable ?
            CartItemResponse.createAvailable(
                item.getProductId(),
                product.getTitle(),
                product.getDescription(),
                item.getSalePrice(),
                item.getOriginalPrice(),
                item.getQuantity(),
                item.getOptionType(),
                imageData,
                sellerNickname
            ) :
            CartItemResponse.createUnavailable(
                item.getProductId(),
                product.getTitle(),
                product.getDescription(),
                item.getSalePrice(),
                item.getOriginalPrice(),
                item.getQuantity(),
                item.getOptionType(),
                imageData,
                sellerNickname
            );

        result.add(dto);
      }
    }

    return result;
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
   * 장바구니 가져오기
   */
  private Cart getOrCreateCart(Long buyerId) {
    return cartRepository.findByBuyerId(buyerId)
        .orElseGet(() -> {
          Cart newCart = new Cart();
          newCart.setBuyerId(buyerId);
          newCart.setTotalPrice(0L);
          return cartRepository.save(newCart);
        });
  }

  /**
   * 상품 가격 설정
   */
  private void setPrice(CartItem newItem, Product product, String optionType) {
    if ("가이드포함".equals(optionType)) {
      newItem.setOriginalPrice(product.getGuidePrice() != null ?
          product.getGuidePrice().longValue() : 0L);
      newItem.setSalePrice(product.getSalesGuidePrice() != null ?
          product.getSalesGuidePrice().longValue() : 0L);
    } else {
      newItem.setOriginalPrice(product.getNormalPrice() != null ?
          product.getNormalPrice().longValue() : 0L);
      newItem.setSalePrice(product.getSalesPrice() != null ?
          product.getSalesPrice().longValue() : 0L);
    }
  }

  /**
   * 장바구니 총액 업데이트
   */
  private void updateCartTotal(Long buyerId) {
    List<CartItem> items = cartItemRepository.findByBuyerId(buyerId);

    long totalPrice = items.stream()
        .filter(item -> {
          Optional<Product> prod = productSVC.getProduct(item.getProductId());
          return prod.isPresent() && "판매중".equals(prod.get().getStatus());
        })
        .mapToLong(item -> item.getSalePrice() * item.getQuantity())
        .sum();

    cartRepository.findByBuyerId(buyerId).ifPresent(cart -> {
      cart.setTotalPrice(totalPrice);
      cartRepository.save(cart);
    });
  }
}