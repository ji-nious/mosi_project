// 🚪 로그아웃 처리 (작업 저장 확인 포함)
function logout() {
  // Step 1: 작업 저장 확인 (설계서 요구사항)
  const hasUnsavedWork = checkUnsavedWork();
  
  if (hasUnsavedWork) {
    const confirmed = confirm('작성 중인 내용이 있습니다. 정말로 로그아웃하시겠습니까?');
    if (!confirmed) {
      return;
    }
  }

  // Step 2: 서버에 로그아웃 요청
  fetch("/logout", {
    method: "POST",
    headers: {
      "X-Requested-With": "XMLHttpRequest"
    }
  })
    .then(res => {
      if (res.redirected) {
        window.location.href = res.url;
      } else {
        if (window.showMessage) {
          showMessage("로그아웃 되었습니다.", "success");
        } else {
          alert("로그아웃 되었습니다.");
        }
        setTimeout(() => {
          window.location.href = "/";
        }, 1000);
      }
    })
    .catch(err => {
      if (window.showMessage) {
        showMessage("로그아웃 실패: " + err.message, "error");
      } else {
        alert("로그아웃 실패: " + err.message);
      }
    });
}

// 저장되지 않은 작업 확인
function checkUnsavedWork() {
  const forms = document.querySelectorAll('form');
  
  for (let form of forms) {
    const inputs = form.querySelectorAll('input:not([type="password"]), textarea, select');
    for (let input of inputs) {
      if (input.value && input.value.trim() !== '' && input.value !== input.defaultValue) {
        return true;
      }
    }
  }
  
  return false;
}
