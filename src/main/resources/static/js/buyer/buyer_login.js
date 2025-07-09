document.querySelector(".buyer-form").addEventListener("submit", function (e) {
  e.preventDefault(); // 폼 기본 동작 방지

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  // 1. 유효성 검사
  if (!email || !password) {
    showMessage("이메일과 비밀번호를 모두 입력하세요.", "error");
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showMessage("올바른 이메일 형식이 아닙니다.", "error");
    return;
  }

  if (password.length < 4) {
    showMessage("비밀번호는 최소 4자 이상이어야 합니다.", "error");
    return;
  }

  // 2. 서버에 로그인 요청
  fetch("/api/buyers/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  })
    .then(response => response.json())
    .then(data => {
      if (data.success === true) {
        showMessage("구매자 로그인 성공! 메인페이지로 이동합니다.", "success");
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
