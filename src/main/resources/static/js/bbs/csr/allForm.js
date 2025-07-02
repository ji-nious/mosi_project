import { ajax, PaginationUI } from '/js/common.js';

let currentPage = 1; // 현재 페이지
const recordsPerPage = 10;        // 페이지당 레코드수
const pagesPerPage = 10;          // 한페이지당 페이지수

// --- ① 버튼 영역 생성 & 추가 ---
const $controls = document.createElement('div');
$controls.setAttribute('id', 'controls');
$controls.style.margin = '20px';
document.body.appendChild($controls);

// 버튼 생성
const $createBtn = document.createElement('button');
$createBtn.textContent = '게시글 등록';
$createBtn.style.padding = '10px 20px';
$createBtn.style.fontSize = '1rem';
$createBtn.addEventListener('click', () => {
  // 등록 페이지로 이동
  location.href = '/csr/bbs/add';
});
$controls.appendChild($createBtn);

// --- ② 리스트와 페이지네이션 컨테이너 ---
const $list = document.createElement('div');
$list.setAttribute('id', 'list');
document.body.appendChild($list);

const $paginationContainer = document.createElement('div');
$paginationContainer.setAttribute('id', 'reply_pagenation');
$paginationContainer.style.margin = '20px';
document.body.appendChild($paginationContainer);

// 게시글 목록 조회
const getBbs = async (reqPage, reqRec) => {
  try {
    const url = `/api/bbs/paging?pageNo=${reqPage}&numOfRows=${reqRec}`;
    const result = await ajax.get(url);
    if (result.header.rtcd === 'S00') {
      currentPage = reqPage;
      displayBbsList(result.body);
    } else {
      alert(result.header.rtmsg);
    }
  } catch (err) {
    console.error(err);
  }
};

// 목록 그리기
function displayBbsList(bbs) {
  const makeTr = bbs => bbs
    .map(b =>
      `<tr data-pid="${b.bbsId}">
        <td>${b.bbsId}</td>
        <td>${b.title}</td>
        <td>${b.memberId}</td>
        <td>${b.createDate}</td>
        <td>${b.updateDate}</td>
      </tr>`
    )
    .join('');

  $list.innerHTML = `
    <table>
      <caption>게시글 목록</caption>
      <thead>
        <tr>
          <th>번호</th><th>제목</th><th>작성자</th><th>작성일</th><th>수정일</th>
        </tr>
      </thead>
      <tbody>
        ${makeTr(bbs)}
      </tbody>
    </table>`;

  $list.querySelectorAll('tbody tr').forEach($tr =>
    $tr.addEventListener('click', e => {
      const pid = e.currentTarget.dataset.pid;
      location.href = `/csr/bbs/${pid}`;
    })
  );
}

// 페이지네이션 설정
async function configPagination() {
  try {
    const result = await ajax.get('/api/bbs/totCnt');
    const totalRecords = result.body;

    const handlePageChange = page => getBbs(page, recordsPerPage);
    const pagination = new PaginationUI('reply_pagenation', handlePageChange);

    pagination.setTotalRecords(totalRecords);
    pagination.setRecordsPerPage(recordsPerPage);
    pagination.setPagesPerPage(pagesPerPage);

    // 첫 페이지 로드
    pagination.handleFirstClick();
  } catch (err) {
    console.error(err);
  }
}

configPagination();
