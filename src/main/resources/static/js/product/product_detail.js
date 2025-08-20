document.addEventListener("DOMContentLoaded", function() {
  // ========================================
  // 기존 상품 상세 페이지 기능들
  // ========================================
  
  const slides = document.querySelectorAll(".slide");     // 모든 슬라이드 선택
  const prevBtn = document.getElementById("prevBtn");     // 이전 버튼
  const nextBtn = document.getElementById("nextBtn");     // 다음 버튼
  let currentIndex = 0;                                    // 현재 보여지는 인덱스

  // 슬라이드 보이고 숨기는 함수
  function showSlide(index) {
    slides.forEach((slide, i) => {
      slide.classList.toggle("active", i === index);
    });
  }

  // 이전 버튼 클릭
  if (prevBtn) {
  prevBtn.addEventListener("click", () => {
    currentIndex = (currentIndex === 0) ? slides.length - 1 : currentIndex - 1;  // 처음이면 마지막으로
    showSlide(currentIndex);
  });
  }

  // 다음 버튼 클릭
  if (nextBtn) {
  nextBtn.addEventListener("click", () => {
    currentIndex = (currentIndex === slides.length - 1) ? 0 : currentIndex + 1;  // 마지막이면 처음으로
    showSlide(currentIndex);
  });
  }

  // 처음 슬라이드 표시
  if (slides.length > 0) {
  showSlide(currentIndex);
  }

  // 숫자 포매팅
  const priceElems = document.querySelectorAll(".allPrice");
  priceElems.forEach(priceElem => {
    const originalText = priceElem.textContent;
    const onlyNumber = originalText.match(/[\d.-]+/g);  // 숫자 및 마이너스, 점만 추출
    if (onlyNumber) {
      const numberValue = Number(onlyNumber.join(""));
      if (!isNaN(numberValue)) {
        const formattedNumber = numberValue.toLocaleString();
        const newText = originalText.replace(/[\d.,-]+/, formattedNumber);
        priceElem.textContent = newText;
      }
    }
  });

  // ---------------------------------
  // 탭 메뉴 active 클래스 처리 추가
  const tabLinks = document.querySelectorAll(".tab-menu a");
  tabLinks.forEach(link => {
    link.addEventListener("click", function(event) {
      event.preventDefault();  // 기본 앵커 이동 막기 (필요 시 생략 가능)
      // 모든 탭에서 active 클래스 제거
      tabLinks.forEach(item => item.classList.remove("active"));
      // 클릭한 탭에 active 클래스 추가
      this.classList.add("active");

      // 선택한 탭에 해당하는 섹션으로 부드럽게 스크롤 이동 (필요하면)
      const targetId = this.getAttribute("href").substring(1);
      const targetElement = document.getElementById(targetId);
      if(targetElement) {
        targetElement.scrollIntoView({ behavior: "smooth" });
      }
    });
  });

// expand_button 프래그먼트가 포함된 요소 클래스명 확인 필요
  const expandButtons = document.querySelectorAll('.ex_btn');

  expandButtons.forEach(button => {
    button.style.cursor = 'pointer'; // 버튼임을 명시적 표시

    button.addEventListener('click', () => {
      const policyMenu = button.closest('.policy-menu');
      if (!policyMenu) return;

      const content = policyMenu.nextElementSibling;
      if (!content || !content.classList.contains('policy-content')) return;

      content.classList.toggle('active'); // 내용 표시 토글
      button.classList.toggle('active');  // 버튼 상태 토글 (화살표 회전 등 CSS 연동)
    });
  });

  // 장바구니, 구매 버튼 이벤트 바인딩
  const cartButtons = document.querySelectorAll('.btn-cart');
  cartButtons.forEach(button => {
    button.addEventListener('click', handleAddToCart);
  });

  const buyButtons = document.querySelectorAll('.btn-buy');
  buyButtons.forEach(button => {
    button.addEventListener('click', handleBuyNow);
  });
});

// 장바구니 담기
async function handleAddToCart(event) {
  const button = event.target;
  const optionType = button.getAttribute('data-option');
  
  button.disabled = true;
  
  try {
    // 검증
    const validation = await validatePurchase(optionType);
    if (!validation.isValid) {
      showAlert(validation.message);
      return;
    }
    
    const productId = getProductIdFromPage();
    
    // 중복 체크
    const isDuplicate = await checkDuplicateProduct(productId, optionType);
    if (isDuplicate) {
      showAlert('이미 장바구니에 동일한 상품이 존재합니다.');
      return;
    }
    
    const result = await addToCartAPI(productId, optionType, 1);
    
    if (result.header?.rtcd === 'S00') {
      showSuccessModal('장바구니에 상품이 추가되었습니다');
      updateCartCount();
    } else {
      showAlert(result.header?.rtmsg || '장바구니 추가에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('장바구니 추가 오류:', error);
    showAlert('네트워크 오류가 발생했습니다. 다시 시도해주세요.');
  } finally {
    button.disabled = false;
  }
}

// 구매하기
async function handleBuyNow(event) {
  const button = event.target;
  const optionType = button.getAttribute('data-option');
  
  button.disabled = true;
  
  try {
    // 검증
    const validation = await validatePurchase(optionType);
    if (!validation.isValid) {
      showAlert(validation.message);
      return;
    }
    
    // 주문 페이지 이동
    const productId = getProductIdFromPage();
    const orderUrl = `/order?productId=${productId}&optionType=${encodeURIComponent(optionType)}&quantity=1`;
    window.location.href = orderUrl;
    
  } catch (error) {
    console.error('구매하기 오류:', error);
    showAlert('오류가 발생했습니다. 다시 시도해주세요.');
  } finally {
    button.disabled = false;
  }
}

// 유효성 검사
async function validatePurchase(optionType) {
  // 로그인 체크
  const isLoggedIn = await checkLoginStatus();
  if (!isLoggedIn) {
    return {
      isValid: false,
      message: '로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?'
    };
  }
  
  // 판매 상태 체크
  const productStatus = getProductStatusFromPage();
  if (productStatus === '판매대기') {
    return {
      isValid: false,
      message: '현재 판매중단된 상품입니다.'
    };
  }
  
  // 옵션 체크
  if (!optionType) {
    return {
      isValid: false,
      message: '옵션을 선택해주세요.'
    };
  }
  
  return { isValid: true };
}

// 장바구니 API
async function addToCartAPI(productId, optionType, quantity) {
  // CSRF 토큰 가져오기
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
  
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };
  
  // CSRF 헤더 추가
  if (csrfToken && csrfHeader) {
    headers[csrfHeader] = csrfToken;
  }

  const response = await fetch('/cart/add', {
    method: 'POST',
    headers,
    credentials: 'include',
    body: JSON.stringify({
      productId: parseInt(productId),
      optionType,
      quantity: parseInt(quantity)
    })
  });
  
  return await response.json();
}

// 로그인 체크
async function checkLoginStatus() {
  try {
    const response = await fetch('/cart/count', {
      method: 'GET',
      credentials: 'include'
    });
    return response.status !== 401;
  } catch (error) {
    return false;
  }
}

// 상품 ID 추출
function getProductIdFromPage() {
  return document.querySelector('[data-product-id]').getAttribute('data-product-id');
}

// 상품 상태 추출
function getProductStatusFromPage() {
  const statusElement = document.querySelector('[data-product-status]');
  return statusElement ? statusElement.getAttribute('data-product-status') : '판매중';
}

// 성공 모달
function showSuccessModal(message) {
  const modal = document.createElement('div');
  modal.style.cssText = `
    position: fixed; top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.5); display: flex; align-items: center;
    justify-content: center; z-index: 10000;
  `;
  
  modal.innerHTML = `
    <div style="
      background: white; 
      padding: 50px 40px; 
      border-radius: 12px; 
      text-align: center; 
      width: 420px;
      min-height: 200px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.15);
      display: flex;
      flex-direction: column;
      justify-content: center;
    ">
      <p style="
        margin: 0 0 40px 0; 
        font-size: 20px; 
        font-weight: 600;
        line-height: 1.5;
        color: #333;
      ">${message}</p>
      <div style="display: flex; gap: 12px; justify-content: center;">
        <button id="continue-shopping" style="
          width: 100%;
          height: 50px;
          border: 1px solid #BFBFBF;
          border-radius: 10px;
          background-color: #fff;
          font-size: 16px;
          font-weight: 500;
          cursor: pointer;
          color: #333;
          transition: all 0.2s ease;
        ">계속 쇼핑하기</button>
        <button id="go-to-cart" style="
          width: 100%;
          height: 50px;
          border: 1px solid #BFBFBF;
          border-radius: 10px;
          background-color: #80CCD6;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          color: #333;
          transition: all 0.2s ease;
        ">장바구니 확인하기</button>
      </div>
    </div>
  `;
  
  document.body.appendChild(modal);
  
  // 버튼 요소 선택
  const continueBtn = modal.querySelector('#continue-shopping');
  const cartBtn = modal.querySelector('#go-to-cart');
  
  // 호버 효과
  continueBtn.addEventListener('mouseenter', () => {
    continueBtn.style.backgroundColor = '#f8f8f8';
  });
  continueBtn.addEventListener('mouseleave', () => {
    continueBtn.style.backgroundColor = '#fff';
  });
  
  cartBtn.addEventListener('mouseenter', () => {
    cartBtn.style.backgroundColor = '#70B7C1';
  });
  cartBtn.addEventListener('mouseleave', () => {
    cartBtn.style.backgroundColor = '#80CCD6';
  });
  
  // 클릭 이벤트
  continueBtn.addEventListener('click', () => {
    document.body.removeChild(modal);
  });
  
  cartBtn.addEventListener('click', () => {
    window.location.href = '/cart';
  });
  
  // 배경 클릭시 닫기
  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      document.body.removeChild(modal);
    }
  });
}

// 알림
function showAlert(message) {
  if (message.includes('로그인이 필요합니다')) {
    if (confirm(message)) {
      window.location.href = '/login';
    }
  } else {
    alert(message);
  }
}

// 헤더 카운트 업데이트
async function updateCartCount() {
  try {
    const response = await fetch('/cart/count', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (response.ok) {
      const data = await response.json();
      const badge = document.getElementById('cart-count');
      
      if (badge) {
        if (data.count > 0) {
          badge.textContent = data.count > 99 ? '99+' : data.count;
          badge.style.display = 'inline';
        } else {
          badge.style.display = 'none';
        }
      }
    }
  } catch (error) {

  }
}

// 중복 상품 체크
async function checkDuplicateProduct(productId, optionType) {
  try {
    const response = await fetch('/cart', {
      method: 'GET',
      credentials: 'include'
    });
    
    if (response.ok) {
      const data = await response.json();
      // 동일한 상품ID와 옵션이 이미 장바구니에 있는지 확인
      // 판매 상품은 최대 1개만 구매 가능 (중복 불가)
      const existingItem = data.cartItems?.find(item => 
        item.productId == productId && item.optionType === optionType
      );
      
      return !!existingItem;
    }
    
    return false;
  } catch (error) {
    return false;
  }
}