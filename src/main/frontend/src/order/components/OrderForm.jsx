// 주문서 작성 폼 컴포넌트
import React, { useState } from 'react'

// 입력 필드 컴포넌트
function Input({
  label,
  placeholder,
  value,
  onChange,
  type = "text",
  required = false,
  disabled = false
}) {
  return (
    <div className="form-group">
      <label className="form-label">
        {label}
      </label>
      <input
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        disabled={disabled}
        className="form-input"
      />
    </div>
  )
}

// 라디오 버튼 컴포넌트
function RadioButton({ name, value, checked, onChange, children }) {
  return (
    <div
      className={`payment-option ${checked ? 'selected' : ''}`}
      onClick={() => onChange(value)}
    >
      <input
        type="radio"
        name={name}
        value={value}
        checked={checked}
        onChange={() => onChange(value)}
        className="payment-radio"
      />
      <div className="payment-content">
        <div className="payment-label">{children}</div>
      </div>
    </div>
  )
}

/**
 * 주문 상품 아이템 컴포넌트 - 장바구니와 동일한 형태
 */
function OrderItem({ productName, price, quantity, optionType, sellerNickname, productImage, originalPrice }) {
  console.log('🖼️ OrderItem 이미지 데이터:', productName, '→', productImage ? 'Base64 있음' : 'null/undefined')

  return (
    <div className="order-item">
      <div className="order-item-image">
        {productImage ? (
          <img
            src={productImage}
            alt={productName}
            loading="lazy"
            onError={(e) => {
              console.log('❌ 이미지 로드 실패:', productName, '→', productImage)
              e.target.style.display = 'none'
              e.target.nextSibling.style.display = 'block'
            }}
          />
        ) : (
          <div className="no-image">이미지 없음</div>
        )}
        <div className="no-image" style={{display: 'none'}}>이미지 없음</div>
      </div>
      <div className="order-item-info">
        <div className="order-item-title">{productName}</div>
        <div className="order-item-option">옵션: {optionType}</div>
        <div className="order-item-seller">판매자: {sellerNickname || '판매자'}</div>
      </div>
      <div className="order-item-price">
        {originalPrice && originalPrice !== price && (
          <span className="original-price">{(originalPrice * quantity)?.toLocaleString()}원</span>
        )}
        <span className="sale-price">{(price * quantity)?.toLocaleString()}원</span>
      </div>
    </div>
  )
}

/**
 * 주문서 작성 폼 컴포넌트
 * Image 2와 완전 동일하게 구현
 */
export default function OrderForm({
  orderItems = [],
  memberInfo = {},
  onSubmit,
  paymentMethod,
  onPaymentMethodChange,
  onRequirementsChange
}) {
  // 폼 데이터 상태
  const [formData, setFormData] = useState({
    ordererName: memberInfo.name || '',
    phone: memberInfo.phone || '',
    email: memberInfo.email || '',
    requirements: ''
  })

  // 폼 데이터 변경 처리
  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }))

    // 요청사항 변경 시 부모에게 알림
    if (field === 'requirements' && onRequirementsChange) {
      onRequirementsChange(value)
    }
  }

  // 폼 제출 처리
  const handleSubmit = (e) => {
    e.preventDefault()

    if (!formData.ordererName || !formData.phone || !formData.email) {
      alert('필수 정보를 모두 입력해주세요.')
      return
    }

    if (!paymentMethod) {
      alert('결제수단을 선택해주세요.')
      return
    }

    onSubmit({
      ...formData,
      items: orderItems
    })
  }

  return (
    <form onSubmit={handleSubmit} className="order-form">
      {/* 주문자 정보 섹션 - Image 2와 동일 */}
      <div className="form-section">
        <div className="section-title">
          <i className="fas fa-user section-icon"></i>
          주문자 정보
        </div>



        <div className="order-info-item">
          <div className="order-info-content">
            <div className="order-info-label">주문자명</div>
            <div className="order-info-value">{formData.ordererName || '정보 없음'}</div>
          </div>
        </div>

        <div className="order-info-item">
          <div className="order-info-content">
            <div className="order-info-label">연락처</div>
            <div className="order-info-value">{formData.phone || '정보 없음'}</div>
          </div>
        </div>

        <div className="order-info-item">
          <div className="order-info-content">
            <div className="order-info-label">이메일</div>
            <div className="order-info-value">{formData.email || '정보 없음'}</div>
          </div>
        </div>

        <div className="form-group requirements-group">
          <label className="form-label">요청사항(50자 이내)</label>
          <textarea
            placeholder="요구사항을 입력해주세요"
            value={formData.requirements}
            onChange={(e) => handleInputChange('requirements', e.target.value)}
            className="form-textarea"
            rows="2"
            maxLength={50}
          />
          <div className="character-counter">
            <span className={formData.requirements.length > 50 ? 'error' : ''}>
              {formData.requirements.length}/50자
            </span>
            {formData.requirements.length > 50 && (
              <span className="error-message">50자를 초과했습니다.</span>
            )}
          </div>
        </div>
      </div>

      {/* 결제수단 섹션 - Image 2와 동일 */}
      <div className="form-section">
        <div className="section-title">
          <i className="fas fa-credit-card section-icon"></i>
          결제수단
        </div>

        <div className="payment-options">
          <RadioButton
            name="payment"
            value="card"
            checked={paymentMethod === 'card'}
            onChange={(value) => {
              onPaymentMethodChange(value)
              // 라디오 버튼 선택 시 즉시 결제 진행
              if (window.processPaymentImmediately) {
                window.processPaymentImmediately(value)
              }
            }}
          >
            신용카드 / 체크카드
          </RadioButton>

          <RadioButton
            name="payment"
            value="bank"
            checked={paymentMethod === 'bank'}
            onChange={(value) => {
              onPaymentMethodChange(value)
              // 라디오 버튼 선택 시 즉시 결제 진행
              if (window.processPaymentImmediately) {
                window.processPaymentImmediately(value)
              }
            }}
          >
            무통장입금
          </RadioButton>

          <RadioButton
            name="payment"
            value="simple"
            checked={paymentMethod === 'simple'}
            onChange={(value) => {
              onPaymentMethodChange(value)
              // 라디오 버튼 선택 시 즉시 결제 진행
              if (window.processPaymentImmediately) {
                window.processPaymentImmediately(value)
              }
            }}
          >
            간편결제
          </RadioButton>
        </div>
      </div>

      {/* 주문상품 섹션 - Image 2와 동일 */}
      <div className="form-section">
        <div className="section-title">
          <i className="fas fa-shopping-bag section-icon"></i>
          주문상품({orderItems.length}개)
        </div>

        {orderItems.length > 0 ? (
          orderItems.map((item, index) => (
            <OrderItem
              key={index}
              productName={item.productName}
              price={item.price}
              quantity={item.quantity}
              optionType={item.optionType}
              sellerNickname={item.sellerNickname}
              productImage={item.productImage}
              originalPrice={item.originalPrice}
            />
          ))
        ) : (
          <div className="no-items">
            <p>주문할 상품이 없습니다.</p>
            <a href="/cart" className="go-cart-btn">장바구니로 이동</a>
          </div>
        )}
      </div>


    </form>
  )
}