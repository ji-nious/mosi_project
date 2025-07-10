// ✅ DOM 로딩 시 각 입력 필드에 이벤트 바인딩
document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("email").addEventListener("input", validateEmail);
  document.getElementById("password").addEventListener("input", validatePassword);
  document.getElementById("password2").addEventListener("input", validatePasswordMatch);
  document.getElementById("bizreg").addEventListener("input", function () {
    this.value = formatBizNo(this.value);
    validateBizReg();
  });
  document.getElementById("shopname").addEventListener("input", validateShopName);
  document.getElementById("name").addEventListener("input", validateName);
  document.getElementById("shopaddress").addEventListener("input", validateShopAddress);
  document.getElementById("tel").addEventListener("input", function () {
    this.value = formatPhoneNumber(this.value);
    validateTel();
  });
  document.getElementById("birth").addEventListener("change", validateBirth);
});


// -------------------------------------------
// 🔢 자동 포맷 함수들
// -------------------------------------------

// ✅ 사업자등록번호 하이픈 자동 삽입 (000-00-00000)
function formatBizNo(value) {
  const numbers = value.replace(/\D/g, '').slice(0, 10);
  if (numbers.length < 4) return numbers;
  if (numbers.length < 6) return numbers.slice(0, 3) + '-' + numbers.slice(3);
  return numbers.slice(0, 3) + '-' + numbers.slice(3, 5) + '-' + numbers.slice(5);
}

// ✅ 전화번호 하이픈 자동 삽입 (010-1234-5678)
function formatPhoneNumber(value) {
  const numbers = value.replace(/\D/g, '').slice(0, 11);
  if (numbers.startsWith("02")) {
    if (numbers.length < 3) return numbers;
    if (numbers.length < 6) return numbers.slice(0, 2) + '-' + numbers.slice(2);
    return numbers.slice(0, 2) + '-' + numbers.slice(2, 6) + '-' + numbers.slice(6);
  } else {
    if (numbers.length < 4) return numbers;
    if (numbers.length < 8) return numbers.slice(0, 3) + '-' + numbers.slice(3);
    return numbers.slice(0, 3) + '-' + numbers.slice(3, 7) + '-' + numbers.slice(7);
  }
}


// -------------------------------------------
// ✅ 유효성 검사 함수들
// -------------------------------------------
//이메일
function validateEmail() {
  const val = document.getElementById("email").value.trim();
  const error = document.getElementById("error-email");
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  error.textContent = (!regex.test(val) || val.length > 50)
    ? "올바른 이메일 형식이여야 합니다." : "";
}
// 비밀번호
function validatePassword() {
  const val = document.getElementById("password").value;
  const error = document.getElementById("error-password");
  const regex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,15}$/;
  error.textContent = !regex.test(val)
    ? "8~15자, 영문+숫자+특수문자를 포함해야 합니다." : "";
  validatePasswordMatch();
}
// 비밀번호 확인
function validatePasswordMatch() {
  const pw = document.getElementById("password").value;
  const pw2 = document.getElementById("password2").value;
  const error = document.getElementById("error-password2");
  error.textContent = (pw !== pw2) ? "비밀번호가 일치하지 않습니다." : "";
}
// 사업자등록번호
function validateBizReg() {
  const val = document.getElementById("bizreg").value.trim();
  const error = document.getElementById("error-bizreg");
  const regex = /^\d{3}-\d{2}-\d{5}$/;
  error.textContent = !regex.test(val)
    ? "사업자등록번호를를 입력해주세요." : "";
}
// 상호명
function validateShopName() {
  const val = document.getElementById("shopname").value.trim();
  const error = document.getElementById("error-shopname");
  error.textContent = (val.length < 2 || val.length > 24)
    ? "상호명을 입력해주세요." : "";
}
// 이름
function validateName() {
  const val = document.getElementById("name").value.trim();
  const error = document.getElementById("error-name");
  const isKor = /^[가-힣]{2,8}$/.test(val);
  const isEng = /^[a-zA-Z\s]{2,45}$/.test(val);
  error.textContent = (!isKor && !isEng)
    ? "이름은 한글 2~8자 입력해주세요." : "";
}
// 가게 주소
function validateShopAddress() {
  const val = document.getElementById("shopaddress").value.trim();
  const error = document.getElementById("error-shopaddress");
  error.textContent = (val.length < 5 || val.length > 200)
    ? "가게 주소를를 입력해주세요." : "";
}
// 전화번호
function validateTel() {
  const val = document.getElementById("tel").value.trim();
  const error = document.getElementById("error-tel");
  const regex = /^(01[016789]|02|0[3-9][0-9])-\d{3,4}-\d{4}$/;
  error.textContent = !regex.test(val)
    ? "전화번호를 입력해주세요." : "";
}
// 생년월일
function validateBirth() {
  const val = document.getElementById("birth").value;
  const error = document.getElementById("error-birth");
  error.textContent = !val ? "생년월일을 입력해주세요." : "";
}


// -------------------------------------------
// ✅ 폼 제출 이벤트 처리
// -------------------------------------------

document.getElementById("seller-signup-form").addEventListener("submit", function (e) {
  e.preventDefault();

  // 모든 유효성 검사 실행
  validateEmail();
  validatePassword();
  validatePasswordMatch();
  validateBizReg();
  validateShopName();
  validateName();
  validateShopAddress();
  validateTel();
  validateBirth();

  const hasError = Array.from(document.querySelectorAll(".error-msg"))
    .some(div => div.textContent !== "");
  if (hasError) return;

  // 서버에 전송할 데이터 준비
  const formData = {
    email: this.email.value.trim(),
    password: this.password.value,
    bizreg: this.bizreg.value.trim(),
    shopname: this.shopname.value.trim(),
    name: this.name.value.trim(),
    shopaddress: this.shopaddress.value.trim(),
    tel: this.tel.value.trim(),
    birth: this.birth.value
  };

  // Spring Boot 서버로 POST 요청
  fetch("/api/sellers/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(formData)
  })
    .then(res => res.json()) // 1. 응답을 JSON으로 파싱
    .then(data => {
      // 2. 응답 본문(data)을 바탕으로 처리
      if (data.header.code === "0" && data.data.redirectUrl) {
        alert("회원가입이 완료되었습니다. 메인 페이지로 이동합니다.");
        window.location.href = data.data.redirectUrl; // data 객체 안의 redirectUrl 사용
      } else {
        // 회원가입 실패 시 서버가 보낸 메시지 표시
        throw new Error(data.header.message || "알 수 없는 오류가 발생했습니다.");
      }
    })
    .catch(err => {
      alert("회원가입 실패: " + err.message);
    });
});

// -------------------------------------------
// ✅ 주소 검색 팝업 관련 함수
// -------------------------------------------

// 팝업 열기
function openAddressPopup() {
    const url = "/common/juso-popup";
    const name = "jusoPopup";
    const options = "width=570,height=420,scrollbars=yes,resizable=yes";
    window.open(url, name, options);
}

// 팝업으로부터 주소 데이터 수신 및 처리
window.addEventListener("message", (event) => {
    // juso_popup.html에서 보낸 데이터인지 확인 (보안 강화)
    if (event.source.name === "jusoPopup") {
        const data = event.data;
        if (data.zipNo && data.roadAddr) {
            document.getElementById("postcode").value = data.zipNo;
            document.getElementById("address").value = data.roadAddr;
            document.getElementById("detailAddress").focus(); // 상세주소로 포커스 이동
        }
    }
}, false);
