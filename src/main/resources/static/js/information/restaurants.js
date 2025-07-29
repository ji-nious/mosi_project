// =============================================================================
// 백엔드 API 연동을 위한 JavaScript 수정
// =============================================================================

// 전역 변수
let map;
let districtChart;
let categoryChart;
let currentPage = 1;
const itemsPerPage = 12;
let currentFilters = {
    district: '',
    category: ''
};

// =============================================================================
// API 통신 함수들
// =============================================================================

// 대시보드 데이터 조회
async function fetchDashboardData() {
    try {
        const response = await fetch('/information/api/restaurant-info/dashboard');
        if (!response.ok) throw new Error('대시보드 데이터 조회 실패');
        return await response.json();
    } catch (error) {
        console.error('대시보드 데이터 조회 중 오류:', error);
        return null;
    }
}

// 맛집 목록 조회 (페이징)
async function fetchRestaurantsData(page = 1, size = 12, district = '', category = '') {
    try {
        const params = new URLSearchParams({
            page: page,
            size: size
        });

        if (district) params.append('district', district);
        if (category) params.append('category', category);

        const response = await fetch(`/information/api/restaurants?${params}`);
        if (!response.ok) throw new Error('맛집 데이터 조회 실패');
        return await response.json();
    } catch (error) {
        console.error('맛집 데이터 조회 중 오류:', error);
        return null;
    }
}

// 지도용 맛집 데이터 조회
async function fetchMapData(district = '', category = '') {
    try {
        const params = new URLSearchParams();
        if (district) params.append('district', district);
        if (category) params.append('category', category);

        const response = await fetch(`/information/api/restaurants/map?${params}`);
        if (!response.ok) throw new Error('지도 데이터 조회 실패');
        return await response.json();
    } catch (error) {
        console.error('지도 데이터 조회 중 오류:', error);
        return [];
    }
}

// =============================================================================
// 지도 초기화 및 관리
// =============================================================================
async function initMap() {
    // 부산 중심 좌표로 지도 초기화
    map = L.map('map').setView([35.1796, 129.0756], 11);

    // OpenStreetMap 타일 레이어 추가
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // 실제 맛집 데이터로 마커 생성
    await updateMapMarkers();
}

async function updateMapMarkers(district = '', category = '') {
    if (!map) return;

    try {
        // 기존 마커 제거
        map.eachLayer(layer => {
            if (layer instanceof L.Marker) {
                map.removeLayer(layer);
            }
        });

        // 새 마커 데이터 가져오기
        const restaurants = await fetchMapData(district, category);

        // 구군별로 그룹핑
        const districtGroups = {};
        restaurants.forEach(restaurant => {
            const districtName = restaurant.district || '기타';
            if (!districtGroups[districtName]) {
                districtGroups[districtName] = [];
            }
            districtGroups[districtName].push(restaurant);
        });

        // 각 구군별로 마커 생성
        Object.entries(districtGroups).forEach(([districtName, restaurantList]) => {
            if (restaurantList.length === 0) return;

            // 구군의 중심 좌표 계산
            const avgLat = restaurantList.reduce((sum, r) => sum + r.lat, 0) / restaurantList.length;
            const avgLng = restaurantList.reduce((sum, r) => sum + r.lng, 0) / restaurantList.length;

            const markerIcon = L.divIcon({
                className: 'custom-marker',
                html: `<div style="
                    background: #667eea;
                    color: white;
                    border-radius: 50%;
                    width: 35px;
                    height: 35px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-weight: bold;
                    font-size: 12px;
                    border: 3px solid white;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.3);
                ">${restaurantList.length}</div>`,
                iconSize: [35, 35],
                iconAnchor: [17, 17]
            });

            const marker = L.marker([avgLat, avgLng], { icon: markerIcon }).addTo(map);

            // 팝업 내용 생성
            const popupContent = `
                <div style="text-align: center; padding: 10px; min-width: 200px;">
                    <h4 style="margin: 0 0 10px 0; color: #2c3e50;">${districtName}</h4>
                    <p style="margin: 0 0 10px 0; color: #7f8c8d;">총 ${restaurantList.length}개 맛집</p>
                    <div style="max-height: 150px; overflow-y: auto;">
                        ${restaurantList.slice(0, 5).map(r => `
                            <div style="margin: 5px 0; padding: 5px; background: #f8f9fa; border-radius: 3px; cursor: pointer;"
                                 onclick="showRestaurantDetail(${r.id})">
                                <strong>${r.name}</strong><br>
                                <small>${r.category} • ${r.address}</small>
                            </div>
                        `).join('')}
                        ${restaurantList.length > 5 ? `<div style="margin-top: 10px;"><small>외 ${restaurantList.length - 5}개</small></div>` : ''}
                    </div>
                </div>
            `;

            marker.bindPopup(popupContent);
        });

    } catch (error) {
        console.error('지도 마커 업데이트 중 오류:', error);
    }
}

// =============================================================================
// 차트 초기화 및 관리
// =============================================================================
async function initCharts() {
    const dashboardData = await fetchDashboardData();
    if (!dashboardData) {
        console.error('대시보드 데이터를 불러올 수 없습니다.');
        return;
    }

    initDistrictChart(dashboardData.districts);
    initCategoryChart(dashboardData.categories);

    // 총 맛집 수 업데이트
    updateStatistics(dashboardData.totalRestaurants);
}

function initDistrictChart(districtData) {
    const ctx = document.getElementById('districtChart').getContext('2d');

    districtChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(districtData),
            datasets: [{
                data: Object.values(districtData),
                backgroundColor: [
                    '#FF6B6B', '#4ECDC4', '#45B7D1',
                    '#96CEB4', '#FFEAA7', '#DDA0DD',
                    '#74b9ff', '#fd79a8', '#fdcb6e'
                ],
                borderWidth: 0,
                cutout: '60%'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return `${label}: ${value}개 (${percentage}%)`;
                        }
                    }
                }
            },
            animation: {
                animateRotate: true,
                duration: 1000
            }
        }
    });
}

function initCategoryChart(categoryData) {
    const ctx = document.getElementById('categoryChart').getContext('2d');

    categoryChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: Object.keys(categoryData),
            datasets: [{
                data: Object.values(categoryData),
                backgroundColor: '#667eea',
                borderRadius: 5,
                borderSkipped: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `${context.label}: ${context.parsed.y}개`;
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            size: 11
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            size: 11
                        },
                        maxRotation: 45
                    }
                }
            },
            animation: {
                duration: 1000,
                easing: 'easeOutQuart'
            }
        }
    });
}

// =============================================================================
// 맛집 목록 렌더링
// =============================================================================
async function renderRestaurants(page = 1) {
    showLoadingState();

    try {
        const data = await fetchRestaurantsData(
            page,
            itemsPerPage,
            currentFilters.district,
            currentFilters.category
        );

        if (!data) {
            showErrorState();
            return;
        }

        const grid = document.getElementById('restaurantGrid');

        if (data.restaurants.length === 0) {
            grid.innerHTML = `
                <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #7f8c8d;">
                    <div style="font-size: 48px; margin-bottom: 20px;">🔍</div>
                    <h3>검색 결과가 없습니다</h3>
                    <p>다른 조건으로 검색해보세요.</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = data.restaurants.map(restaurant => `
            <div class="restaurant-card" onclick="showRestaurantDetail(${restaurant.ucSeq})">
                <div class="card-image">
                    <div class="card-badge">${restaurant.gugunNm || ''}</div>
                    ${restaurant.mainImgThumb ?
                        `<img src="${restaurant.mainImgThumb}" alt="${restaurant.mainTitle}" style="width: 100%; height: 200px; object-fit: cover;">` :
                        '<div style="width: 100%; height: 200px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; font-size: 48px;">🍽️</div>'
                    }
                </div>
                <div class="card-content">
                    <div class="card-title">${restaurant.mainTitle || restaurant.title || '이름 없음'}</div>
                    <div class="card-description">
                        ${restaurant.itemCntnts || '카테고리 없음'} • ${restaurant.gugunNm || ''}<br>
                        ${restaurant.rprsnTvMenu ? `대표메뉴: ${restaurant.rprsnTvMenu}` : ''}
                    </div>
                    ${restaurant.addr1 ? `<div class="card-address">${restaurant.addr1}</div>` : ''}
                </div>
            </div>
        `).join('');

        // 페이징 업데이트
        updatePagination(data.currentPage, data.totalPages);

    } catch (error) {
        console.error('맛집 목록 렌더링 중 오류:', error);
        showErrorState();
    }
}

// =============================================================================
// 필터링 기능
// =============================================================================
async function applyFilters() {
    const district = document.getElementById('districtFilter').value;
    const category = document.getElementById('categoryFilter').value;

    currentFilters = { district, category };
    currentPage = 1;

    // 맛집 목록 업데이트
    await renderRestaurants(1);

    // 지도 업데이트
    await updateMapMarkers(district, category);

    // 필터 적용 피드백
    showFilterFeedback(district, category);
}

async function resetFilters() {
    // 필터 초기화
    document.getElementById('districtFilter').value = '';
    document.getElementById('categoryFilter').value = '';

    currentFilters = { district: '', category: '' };
    currentPage = 1;

    // 데이터 다시 로드
    await renderRestaurants(1);
    await updateMapMarkers();

    // 지도 원래 상태로 복원
    if (map) {
        map.setView([35.1796, 129.0756], 11);
    }

    console.log('필터가 초기화되었습니다.');
}

function showFilterFeedback(district, category) {
    let message = '필터가 적용되었습니다';

    if (district || category) {
        const filters = [];
        if (district) filters.push(`지역: ${district}`);
        if (category) filters.push(`업종: ${category}`);
        message += ` (${filters.join(', ')})`;
    }

    console.log(message);
}

// =============================================================================
// 페이징 기능
// =============================================================================
function changePage(direction) {
    if (typeof direction === 'number') {
        currentPage = direction;
    } else {
        switch(direction) {
            case 'first':
                currentPage = 1;
                break;
            case 'prev':
                if (currentPage > 1) currentPage--;
                break;
            case 'next':
                currentPage++;
                break;
            case 'last':
                // totalPages는 renderRestaurants에서 설정됨
                break;
        }
    }

    renderRestaurants(currentPage);
}

function updatePagination(current, total) {
    currentPage = current;
    const pagination = document.getElementById('pagination');

    if (total <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';

    let paginationHTML = `
        <button class="page-btn" onclick="changePage('first')" ${current === 1 ? 'disabled' : ''}>처음</button>
        <button class="page-btn" onclick="changePage('prev')" ${current === 1 ? 'disabled' : ''}>이전</button>
    `;

    // 페이지 번호 생성 (현재 페이지 주변 5개만 표시)
    const startPage = Math.max(1, current - 2);
    const endPage = Math.min(total, current + 2);

    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <button class="page-btn ${i === current ? 'active' : ''}" onclick="changePage(${i})">${i}</button>
        `;
    }

    paginationHTML += `
        <button class="page-btn" onclick="changePage('next')" ${current === total ? 'disabled' : ''}>다음</button>
        <button class="page-btn" onclick="changePage(${total})" ${current === total ? 'disabled' : ''}>마지막</button>
    `;

    pagination.innerHTML = paginationHTML;
}

// =============================================================================
// 필터 옵션 초기화
// =============================================================================
async function initFilterOptions() {
    try {
        // 대시보드 데이터에서 옵션 가져오기
        const dashboardData = await fetchDashboardData();
        if (!dashboardData) return;

        // 지역 옵션 설정
        const districtSelect = document.getElementById('districtFilter');
        const districts = Object.keys(dashboardData.districts);
        districtSelect.innerHTML = '<option value="">전체 지역</option>' +
            districts.map(district => `<option value="${district}">${district}</option>`).join('');

        // 카테고리 옵션 설정
        const categorySelect = document.getElementById('categoryFilter');
        const categories = Object.keys(dashboardData.categories);
        categorySelect.innerHTML = '<option value="">전체 업종</option>' +
            categories.map(category => `<option value="${category}">${category}</option>`).join('');

    } catch (error) {
        console.error('필터 옵션 초기화 중 오류:', error);
    }
}

// =============================================================================
// 유틸리티 함수
// =============================================================================
function updateStatistics(totalRestaurants) {
    const statsElement = document.querySelector('.map-stats');
    if (statsElement && totalRestaurants) {
        statsElement.textContent = `총 ${totalRestaurants}개 맛집`;
    }
}

function showLoadingState() {
    const grid = document.getElementById('restaurantGrid');
    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #7f8c8d;">
            <div style="font-size: 48px; margin-bottom: 20px;">⏳</div>
            <h3>데이터를 불러오는 중...</h3>
        </div>
    `;
}

function showErrorState() {
    const grid = document.getElementById('restaurantGrid');
    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #e74c3c;">
            <div style="font-size: 48px; margin-bottom: 20px;">❌</div>
            <h3>데이터를 불러올 수 없습니다</h3>
            <button class="btn btn-primary" onclick="location.reload()">다시 시도</button>
        </div>
    `;
}

// =============================================================================
// 이벤트 핸들러
// =============================================================================
function showRestaurantDetail(id) {
    // 상세 페이지로 이동
    window.location.href = `/information/restaurant/${id}`;
}

function showFilter(type) {
    console.log('필터 표시:', type);

    switch(type) {
        case '전체':
            resetFilters();
            break;
        case '소개':
            alert('부산 맛집 가이드에 오신 것을 환영합니다!');
            break;
        case '검색':
            document.getElementById('districtFilter').focus();
            break;
    }
}

// =============================================================================
// 초기화 및 메인 실행
// =============================================================================
document.addEventListener('DOMContentLoaded', async function() {
    console.log('부산 맛집 가이드 초기화 시작...');

    try {
        // 지도 초기화
        await initMap();
        console.log('✅ 지도 초기화 완료');

        // 차트 초기화
        await initCharts();
        console.log('✅ 차트 초기화 완료');

        // 필터 옵션 초기화
        await initFilterOptions();
        console.log('✅ 필터 옵션 초기화 완료');

        // 맛집 목록 렌더링
        await renderRestaurants(1);
        console.log('✅ 맛집 목록 렌더링 완료');

        console.log('🎉 부산 맛집 가이드 초기화 완료!');

    } catch (error) {
        console.error('❌ 초기화 중 오류 발생:', error);
        showErrorState();
    }
});

// =============================================================================
// 전역 이벤트 리스너
// =============================================================================
window.addEventListener('resize', function() {
    // 창 크기 변경시 차트 리사이즈
    if (districtChart) districtChart.resize();
    if (categoryChart) categoryChart.resize();

    // 지도 리사이즈
    if (map) {
        setTimeout(() => {
            map.invalidateSize();
        }, 100);
    }
});