const handleResponse = async (response) => {
  if (response.status === 401) {
    window.location.href = '/login'
    return null
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}))
    const message = errorData.header?.rtmsg || errorData.message || `HTTP ${response.status}: ${response.statusText}`
    throw new Error(message)
  }

  const apiResponse = await response.json()
  console.log('API Response:', apiResponse)

  // 비즈니스 로직 오류 체크
  if (apiResponse.header && apiResponse.header.rtcd !== 'S00') {
    return {
      success: false,
      message: apiResponse.header.rtmsg || '요청 처리 중 오류가 발생했습니다'
    }
  }

  // 성공 응답 - success 속성 추가
  return {
    success: true,
    message: apiResponse.header?.rtmsg || '성공',
    ...apiResponse.body
  }
}

export const cartService = {
  // 장바구니 조회
  async getCart() {
    try {
      const response = await fetch('/cart', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include'
      })

      return await handleResponse(response)
    } catch (error) {
      console.error('장바구니 조회 실패:', error)
      throw error
    }
  },

  // 장바구니 상품 수량 변경
  async updateQuantity(productId, optionType, quantity) {
    try {
      const response = await fetch('/cart/quantity', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          productId,
          optionType,
          quantity
        })
      })

      return await handleResponse(response)
    } catch (error) {
      console.error('수량 변경 실패:', error)
      throw error
    }
  },

  // 장바구니에서 상품 삭제
  async removeFromCart(productId, optionType) {
    try {
      const response = await fetch('/cart/remove', {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        credentials: 'include',
        body: JSON.stringify({
          productId,
          optionType
        })
      })

      return await handleResponse(response)
    } catch (error) {
      console.error('상품 삭제 실패:', error)
      throw error
    }
  }
}