// API 호출해서 사용자 정보 가져오기
window.onload = () => {
  fetch('/api/account/info')
    .then(res => res.json())
    .then(data => {
      document.getElementById('email').textContent = data.email;
      document.getElementById('name').textContent = data.name;
      document.getElementById('phone').textContent = data.phone;
      document.getElementById('joinDate').textContent = data.joinDate;
    })
    .catch(err => {
      alert("계정 정보를 불러오는 데 실패했습니다.");
      console.error(err);
    });
}

function confirmDelete() {
  if (confirm("정말로 탈퇴하시겠습니까?")) {
    fetch('/api/account/delete', { method: 'DELETE' })
      .then(res => {
        if (res.ok) {
          alert('탈퇴가 완료되었습니다.');
          window.location.href = '/home';
        } else {
          alert('탈퇴에 실패했습니다.');
        }
      })
      .catch(err => {
        alert('오류가 발생했습니다.');
        console.error(err);
      });
  }
}