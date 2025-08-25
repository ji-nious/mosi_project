import React, { useState, useEffect, useCallback } from 'react'
import { cartService } from '../services/CartService'
import CartItem from '../components/CartItem'

function CartPage() {
  // 상태 관리
  const [cartData, setCartData] = useState(null)
  const [error, setError] = useState(null)
  const [selectedItems, setSelectedItems] = useState(new Set())
  const [updating, setUpdating] = useState(false)
  const [loading, setLoading] = useState(true)

  // 컴포넌트 마운트시 장바구니 데이터 가져오기
  useEffect(() => {
    fetchCartData()
  }, [])

  // 서버에서 장바구니 데이터 가져오기
  const fetchCartData = useCallback(async () => {
    try {
      setError(null)
      setLoading(true)

      console.log('🛒 장바구니 데이터 요청 시작...')
      const data = await cartService.getCart()
      console.log('📦 장바구니 응답 데이터:', data)

      if (data && data.success) {
        console.log('✅ 장바구니 데이터 성공:', data)
        setCartData(data)

        if (data.cartItems && data.cartItems.length > 0) {
          const allItems = data.cartItems.map(item =>
            `${item.productId}-${item.optionType}`
          )
          setSelectedItems(new Set(allItems))
        } else {
          setSelectedItems(new Set())
        }
      } else {
        console.log('❌ 장바구니 데이터 실패:', data)
        setError('장바구니 데이터를 불러올 수 없습니다')
      }
    } catch (error) {
      console.log('🚨 장바구니 네트워크 오류:', error)
      setError('네트워크 오류가 발생했습니다')
    } finally {
      setLoading(false)
    }
  }, [])

  // 상품 수량 변경
  const handleQuantityChange = useCallback(async (productId, optionType, newQuantity) => {
    try {
      setUpdating(true)
      const result = await cartService.updateQuantity(productId, optionType, newQuantity)

      if (result && result.success) {
        await fetchCartData()
        // 헤더 장바구니 개수 업데이트 (수량이 0이 되면 아이템이 삭제됨)
        if (window.updateCartCount) {
          await window.updateCartCount()
        }
      } else {
        alert(result?.message || '수량 변경에 실패했습니다')
      }
    } catch (error) {
      alert('수량 변경 중 오류가 발생했습니다')
    } finally {
      setUpdating(false)
    }
  }, [fetchCartData])

  // 상품 삭제
  const removeItem = useCallback(async (productId, optionType) => {
    try {
      setUpdating(true)
      const result = await cartService.removeFromCart(productId, optionType)

      if (result && result.success) {
        await fetchCartData()
        setSelectedItems(prev => {
          const newSet = new Set(prev)
          newSet.delete(`${productId}-${optionType}`)
          return newSet
        })
        // 헤더 장바구니 개수 업데이트
        if (window.updateCartCount) {
          await window.updateCartCount()
        }
      } else {
        alert(result?.message || '삭제에 실패했습니다')
      }
    } catch (error) {
      alert('삭제 중 오류가 발생했습니다')
    } finally {
      setUpdating(false)
    }
  }, [fetchCartData])

  // 개별 상품 선택/해제
  const handleSelectItem = useCallback((productId, optionType, selected) => {
    const itemKey = `${productId}-${optionType}`
    setSelectedItems(prev => {
      const newSet = new Set(prev)
      if (selected) {
        newSet.add(itemKey)
      } else {
        newSet.delete(itemKey)
      }
      return newSet
    })
  }, [])

  // 전체 선택/해제
  const handleSelectAll = useCallback((selected) => {
    if (selected) {
      const availableItems = cartData?.cartItems?.filter(item => item.available).map(item =>
        `${item.productId}-${item.optionType}`
      ) || []
      setSelectedItems(new Set(availableItems))
    } else {
      setSelectedItems(new Set())
    }
  }, [cartData?.cartItems])

  // 선택된 상품들 삭제
  const handleDeleteSelected = useCallback(async () => {
    if (selectedItems.size === 0) {
      alert('삭제할 상품을 선택해주세요.')
      return
    }

    if (!confirm(`${selectedItems.size}개 상품을 삭제하시겠습니까?`)) return

    try {
      setUpdating(true)

      for (const itemKey of selectedItems) {
        const [productId, optionType] = itemKey.split('-')
        await cartService.removeFromCart(parseInt(productId), optionType)
      }

      await fetchCartData()
      setSelectedItems(new Set())
      // 헤더 장바구니 개수 업데이트
      if (window.updateCartCount) {
        await window.updateCartCount()
      }
      alert(`${selectedItems.size}개 상품이 삭제되었습니다.`)
    } catch (error) {
      alert('삭제 중 오류가 발생했습니다')
    } finally {
      setUpdating(false)
    }
  }, [selectedItems, fetchCartData])

  // 주문하기
  const goToOrder = useCallback(async () => {
    const selectedCartItems = cartData?.cartItems?.filter(item =>
      selectedItems.has(`${item.productId}-${item.optionType}`)
    ) || []

    if (selectedCartItems.length === 0) {
      alert('주문할 상품을 선택해주세요')
      return
    }

    try {
      sessionStorage.setItem('selectedCartItems', JSON.stringify(selectedCartItems))
      window.location.href = '/order'
    } catch (error) {
      alert('주문 생성 중 오류가 발생했습니다')
    }
  }, [cartData?.cartItems, selectedItems])

  // 계산된 값들
  const cartItems = cartData?.cartItems || []
  const availableItems = cartItems.filter(item => item.available)
  const selectedCartItems = cartItems.filter(item =>
    selectedItems.has(`${item.productId}-${item.optionType}`)
  )
  const isAllSelected = availableItems.length > 0 && selectedItems.size === availableItems.length

  // 로딩 화면
  if (loading) {
    return (
      <div className="react-cart-content">
        <div className="loading-container">
          <div className="custom-spinner"></div>
          <div className="loading-text">장바구니를 불러오는 중...</div>
        </div>
      </div>
    )
  }

  // 에러 화면
  if (error) {
    return (
      <div className="cart-container">
        <div className="error-container">
          <div className="error-icon">
            <i className="fas fa-exclamation-triangle"></i>
          </div>
          <h2>오류가 발생했습니다</h2>
          <p>{error}</p>
          <button onClick={fetchCartData} className="order-button">다시 시도</button>
        </div>
      </div>
    )
  }

  // 빈 장바구니 화면 (Islands: 레이아웃 제거)
  if (cartData?.empty) {
    return (
      <div className="empty-cart-content">
        <div className="empty-cart-icon">
          <i className="fas fa-shopping-cart cart-icon"></i>
        </div>
        <div className="empty-cart-title">장바구니가 비어 있습니다</div>
        <a href="/product/list" className="shop-button">쇼핑하러 가기</a>
      </div>
    )
  }

  // 메인 장바구니 화면 (Islands: 레이아웃 제거, 순수 장바구니 리스트만)
  return (
    <div className="react-cart-content">
        {/* 계속 쇼핑하기 버튼 */}
        <div className="continue-shopping-section">
          <a href="/product/list" className="continue-shopping-btn">&lt; 계속 쇼핑하기</a>
        </div>

        {/* 전체 선택 및 삭제 영역 */}
        <div className={`select-all-section ${!isAllSelected ? 'unselected' : ''}`}>
          <div className="left">
            <input
              type="checkbox"
              checked={isAllSelected}
              onChange={(e) => handleSelectAll(e.target.checked)}
              className="custom-checkbox"
            />
            <span className="select-all-text">
              전체선택({selectedItems.size}/{availableItems.length})
            </span>
          </div>

          <div className="select-actions">
            <button
              onClick={handleDeleteSelected}
              disabled={selectedItems.size === 0 || updating}
              className="delete-selected-btn"
            >
              선택삭제
            </button>
          </div>
        </div>

        {/* 장바구니 상품 목록 */}
        {cartItems.map((item) => (
          <CartItem
            key={`${item.productId}-${item.optionType}`}
            item={item}
            selected={selectedItems.has(`${item.productId}-${item.optionType}`)}
            onSelect={handleSelectItem}
            onQuantityChange={handleQuantityChange}
            onRemove={removeItem}
            updating={updating}
          />
        ))}

        {/* 주문 요약 영역 */}
        <div className="order-summary">
          <div className="summary-title">
            <i className="fas fa-file-alt"></i>
            주문요약
          </div>

          <div className="summary-section">
            <div className="summary-label">
              선택된 상품
              <span className="selected-count">{selectedCartItems.length}개</span>
            </div>
          </div>

          <div className="price-list">
            {selectedCartItems.map((item) => (
              <div key={`item-${item.productId}-${item.optionType}`} className="price-item">
                <div className="price-item-info">
                  <div className="price-item-name">{item.productName}</div>
                  <div className="price-item-type">{item.optionType}</div>
                </div>
                <span className="price-item-amount">
                  {(item.price * item.quantity)?.toLocaleString()}원
                </span>
              </div>
            ))}
          </div>

          <div className="total-amount">
            <span className="total-label">총 결제 금액</span>
            <span className="total-price">
              {selectedCartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)?.toLocaleString()}원
            </span>
          </div>

          <div className="download-notice">
            ※ 파일은 결제 후 7일간 다운로드 가능합니다.
          </div>

          <button
            onClick={goToOrder}
            className="order-button"
            disabled={selectedCartItems.length === 0 || updating}
          >
            {updating ? (
              <div className="button-spinner"></div>
            ) : (
              `${selectedCartItems.length}개 상품 주문하기`
            )}
          </button>
        </div>
    </div>
  )
}

export default CartPage