const handleResponse = async (response) => {
  if (response.status === 401) {
    window.location.href = '/login'
    return null
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}))
    // ApiResponse 구조에서 메시지 추출
    const message = errorData.header?.rtmsg || errorData.message || `HTTP ${response.status}: ${response.statusText}`
    throw new Error(message)
  }

  try {
    const apiResponse = await response.json()
    // ApiResponse 구조에서 실제 데이터 추출
    if (apiResponse.header && apiResponse.header.rtcd !== 'S00') {
      throw new Error(apiResponse.header.rtmsg || '요청 처리 중 오류가 발생했습니다')
    }

    return apiResponse.body || apiResponse
  } catch (error) {
    if (error.message !== '서버 응답을 처리할 수 없습니다') {
      throw error
    }
    throw new Error('서버 응답을 처리할 수 없습니다')
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