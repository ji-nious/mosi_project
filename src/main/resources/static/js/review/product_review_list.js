import { ajax } from '/js/community/common.js';

// ====== 설정값 ======
const LIST_URL     = '/api/review/product';             // ← 베이스 URL
const TOTAL_URL    = '/api/review/product/reviewCnt';   // ← 슬래시 추가 + 이름 통일
const PROFILE_URL  = '/api/review/product/profile-images';
const PAGE_SIZE    = 5;
let   currentPage  = 1;
let   totalCount   = 0;

// ====== 유틸 ======
const reviewBox     = document.getElementById('review');

const reviewTotalEl = reviewBox.querySelector('#review-total');
const reviewSection = reviewBox.querySelector('.review_list');


const section = document.querySelector('.product-body-right');

const pid     = section.dataset.productId;  // ← dataset 사용 권장

// ====== 총개수 ======
async function fetchTotalCount() {
  if (!pid) { console.warn('productId 없음'); return; }
  try {
    const url = `${TOTAL_URL}/${pid}`;       // ← 슬래시 추가 반영
    const res = await ajax.get(url);
    if (res?.header?.rtcd === 'S00') {
      totalCount = Number(res.body ?? 0) || 0;
    } else {
      totalCount = 0;
    }
  } catch (e) {
    console.error('총개수 조회 실패', e);
    totalCount = 0;
  }
  if (reviewTotalEl) reviewTotalEl.textContent = `등록 리뷰 수 ${totalCount}건`;
}

// ====== 목록 조회 ======
const getReview = async (reqPage, reqRec) => {
  try {
    const url = `${LIST_URL}/${pid}?pageNo=${reqPage}&numOfRows=${reqRec}`;
    const result = await ajax.get(url);

    if (result?.header?.rtcd === 'S00') {
      currentPage = reqPage;
      const rows = result.body ?? [];

      renderList(rows);
    } else {
      alert(result?.header?.rtmsg ?? '목록 조회 실패');
    }
  } catch (err) {
    console.error(err);
    renderEmpty('목록을 불러오지 못했습니다.');
  }
};

// ====== 개별 아이템 템플릿 ======
function renderOneItem(item = {}) {
  const imgUrl = item.hasPic == 1
    ? `${PROFILE_URL}/${item.buyerId}`
    : '/img/bbs/bbs_detail/profile-pic.png';

  const html = `
    <div class="review" data-review-id="${item.reviewId ?? ''}">
      <div class="review_item">
        <div class="review_profile">
          <img src="${imgUrl}" alt="작성자 프로필" loading="lazy" decoding="async">
        </div>
        <div class="review_meta">
          <div class="review_rating" data-score="${Number(item.score ?? 0)}">
            <div class="stars_bg">★★★★★</div>
            <div class="stars_fill">★★★★★</div>
            <span class="rating_score">${Number(item.score ?? 0).toFixed(1)}</span>
            <span class="review_date">${item.rcreate ?? ''}</span>
          </div>
          <div class="profile_name">${item.nickname ?? ''}</div>
        </div>
      </div>
      <div class="review_tag">${item.tagLabels ?? ''}</div>   <!-- 서버에서 tagLabels로 내려온다고 가정 -->
      <div class="review_content">${item.content ?? ''}</div>
      <div class="review_option">${item.optionType ?? ''}</div>
    </div>
  `;
  return html;
}

// ====== 리스트 렌더 ======
function renderList(items = []) {
  if (!reviewSection) return;
  if (!items.length) {
    renderEmpty('등록된 리뷰가 없습니다.');
    return;
  }
  const html = items.map(renderOneItem).join('');
  reviewSection.innerHTML = html; // 누적 추가하려면 insertAdjacentHTML 사용
}

function renderEmpty(msg = '데이터가 없습니다.') {
  if (!reviewSection) return;
  reviewSection.innerHTML = `<div class="empty">${msg}</div>`;
}

// ====== 초기 구동 ======
document.addEventListener('DOMContentLoaded', async () => {
  await fetchTotalCount();
  await getReview(1, PAGE_SIZE);
});
