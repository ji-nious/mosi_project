/**
 * 간단한 메시지 처리 (alert 대체)
 */

// 메시지 표시
function showMessage(message, type = 'info') {
  // 기존 메시지 컨테이너 찾기 또는 생성
  let container = document.getElementById('message-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'message-container';
    container.style.cssText = `
      position: fixed; top: 20px; left: 50%; transform: translateX(-50%);
      padding: 12px 20px; border-radius: 8px; font-weight: 500;
      z-index: 9999; max-width: 500px; text-align: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    `;
    document.body.appendChild(container);
  }
  
  // 타입별 스타일
  const styles = {
    success: 'background: #d4edda; color: #155724; border: 1px solid #c3e6cb;',
    error: 'background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb;',
    info: 'background: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb;'
  };
  
  container.style.cssText += styles[type] || styles.info;
  container.textContent = message;
  container.style.display = 'block';
  
  // 3초 후 자동 숨김
  setTimeout(() => {
    if (container) {
      container.style.display = 'none';
    }
  }, 3000);
}

// 전역 함수
window.showMessage = showMessage; 