
document.addEventListener('DOMContentLoaded', function() {
    // 이벤트 리스너 등록
    setupEventListeners();
});

// =============================
// 이벤트 리스너 설정
// =============================
function setupEventListeners() {
    // 필터 버튼
    const filterBtn = document.getElementById('filterBtn');
    if (filterBtn) {
        filterBtn.addEventListener('click', applyFilters);
    }

    // 필터 셀렉트박스
    const districtFilter = document.getElementById('districtFilter');
    const categoryFilter = document.getElementById('categoryFilter');

    if (districtFilter) {
        districtFilter.addEventListener('change', applyFilters);
    }

    if (categoryFilter) {
        categoryFilter.addEventListener('change', applyFilters);
    }
}

// =============================
// 필터 적용
// =============================
function applyFilters() {
    const districtFilter = document.getElementById('districtFilter');
    const categoryFilter = document.getElementById('categoryFilter');

    const district = districtFilter ? districtFilter.value : '';
    const category = categoryFilter ? categoryFilter.value : '';

    // URL 파라미터로 페이지 이동 (서버사이드 필터링)
    const params = new URLSearchParams();
    params.set('page', '1');

    if (district) params.set('district', district);
    if (category) params.set('category', category);

    window.location.href = `/information?${params.toString()}`;
}

console.log('부산 맛집 가이드 페이지가 로드되었습니다.');