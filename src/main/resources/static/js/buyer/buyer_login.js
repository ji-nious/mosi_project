document.querySelector(".buyer-form").addEventListener("submit", function (e) {
  e.preventDefault(); // 폼 기본 동작 방지

  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  // 1. 유효성 검사
  if (!email || !password) {
    alert("이메일과 비밀번호를 모두 입력하세요.");
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    alert("올바른 이메일 형식이 아닙니다.");
    return;
  }

  if (password.length < 4) {
    alert("비밀번호는 최소 4자 이상이어야 합니다.");
    return;
  }

  // 2. 서버에 로그인 요청
  fetch("/buyer/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ email, password })
  })
    .then(res => {
      if (res.redirected) {
        window.location.href = res.url;
      } else if (res.ok) {
        alert("구매자 로그인 성공! 메인페이지로 이동합니다.");
        window.location.href = "/home";
      } else {
        return res.text().then(msg => { throw new Error(msg); });
      }
    })
    .catch(err => {
      alert("로그인 실패: " + err.message);
    });
});
