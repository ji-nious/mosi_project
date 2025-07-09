// ✅ 판매자 로그인 폼 제출 처리
document.querySelector(".seller-form").addEventListener("submit", function (e) {
  e.preventDefault(); // 기본 제출 동작 차단

  // 입력값 수집
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  // ✅ 1. 기본 유효성 검사
  if (!email || !password) {
    showMessage("이메일과 비밀번호를 입력해주세요.", "error");
    return;
  }

  // ✅ 2. 이메일 형식 검사
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showMessage("올바른 이메일 형식을 입력해주세요.", "error");
    return;
  }

  // ✅ 3. 비밀번호 길이 검사
  if (password.length < 4) {
    showMessage("비밀번호는 최소 4자 이상이어야 합니다.", "error");
    return;
  }

  // ✅ 4. 서버에 로그인 요청
  fetch("/api/sellers/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  })
    .then(response => response.json())
    .then(data => {
      if (data.success === true) {
        showMessage("판매자 로그인 성공! 메인페이지로 이동합니다.", "success");
        setTimeout(() => {
          window.location.href = "/home";
        }, 1000);
      } else {
        showMessage("로그인 실패: " + (data.message || "알 수 없는 오류"), "error");
      }
    })
    .catch(err => {
      showMessage("로그인 실패: " + err.message, "error");
    });
});