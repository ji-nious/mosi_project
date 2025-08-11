document.addEventListener('DOMContentLoaded', function() {
  // 1. 상품 설명 글자 100자 제한 후 '...' 표시
  const descriptionElements = document.querySelectorAll('.text-trim');
  descriptionElements.forEach(el => {
    if (el.textContent.length > 100) {
      el.textContent = el.textContent.substring(0, 100) + '...';
    }
  });

  // 2. CSRF 토큰 가져오기
  function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  }

  function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
  }

  const csrfToken = getCsrfToken();
  const csrfHeader = getCsrfHeader();

  // 3. 판매 상태 변경
  const statusSelects = document.querySelectorAll('.status-select');
  statusSelects.forEach(select => {
    select.addEventListener('change', e => {
      const productId = e.target.dataset.productId;
      const status = e.target.value;

      const headers = { 'Content-Type': 'application/json' };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      fetch(`/product/status/${productId}`, {
        method: 'PATCH',
        headers: headers,
        body: JSON.stringify({ status: status })
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('상태 업데이트 실패');
        }
      })
      .catch(error => {
        console.error('상태 업데이트 중 오류:', error);
        alert('상태 업데이트 실패: ' + error.message);
      });
    });
  });

  // 4. 상태 필터 변경 시 1페이지로 이동
  const statusForm = document.getElementById('statusForm');
  const statusSelect = statusForm?.querySelector('select[name="status"]');
  const pageInput = statusForm?.querySelector('input[name="page"]');

  if (statusForm && statusSelect && pageInput) {
    statusSelect.addEventListener('change', function() {
      pageInput.value = 1;
      statusForm.submit();
    });
  }

  // 5. 선택 삭제 기능 (전체선택 제거)
  const deleteBtn = document.getElementById('deleteSelected');
  const itemChecks = document.querySelectorAll('.item-check');

  if (deleteBtn && itemChecks.length) {
    // 개별 체크박스 변경 시 삭제 버튼 상태 업데이트
    itemChecks.forEach(cb => {
      cb.addEventListener('change', updateDeleteBtn);
    });

    function updateDeleteBtn() {
      const checked = document.querySelectorAll('.item-check:checked');
      deleteBtn.disabled = checked.length === 0;
    }

    // 삭제 버튼 클릭
    deleteBtn.addEventListener('click', function() {
      const selected = Array.from(document.querySelectorAll('.item-check:checked'));

      if (selected.length === 0) return;

      if (confirm(`${selected.length}개 상품을 삭제하시겠습니까?`)) {
        // 첫 번째 상품으로 이동 (기존 GET 방식 사용)
        window.location.href = `/product/delete/${selected[0].value}`;
      }
    });

    updateDeleteBtn();
  }
});