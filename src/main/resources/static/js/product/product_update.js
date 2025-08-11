document.addEventListener('DOMContentLoaded', function () {
  // 기본 설정
  const uploadBox = document.querySelector('.upload-box');
  const input = document.querySelector('#productImage');
  const preview = uploadBox.querySelector('.image-preview');
  const icon = uploadBox.querySelector('i');
  const text = uploadBox.querySelector('p');
  const nameList = document.getElementById('imageNameList');
  const maxCount = 10;
  let filesArr = [];

  initializeExistingImages();

  // 업로드 박스 클릭 이벤트
  uploadBox.addEventListener('click', function (e) {
    if(e.target === input) return;
    input.click();
  });

  // 파일 선택/드롭 공통 처리 함수
  function handleFiles(newFiles) {
    // 파일 검증
    for (let file of newFiles) {
      if (!file.type.startsWith('image/')) {
        alert(file.name + '은(는) 이미지 파일이 아닙니다.');
        return false;
      }
      if (file.size > 5 * 1024 * 1024) {
        alert(file.name + '은(는) 5MB를 초과합니다.');
        return false;
      }
    }

    // 중복 제거 후 추가
    filesArr = filesArr.concat(newFiles.filter(newFile =>
      !filesArr.some(f => f.name === newFile.name && f.size === newFile.size)
    ));

    // 최대 개수 체크
    if (filesArr.length > maxCount) {
      alert(`이미지는 최대 ${maxCount}개까지 업로드할 수 있습니다.`);
      filesArr = filesArr.slice(0, maxCount);
    }

    updateInputFiles();
    updatePreview();
    return true;
  }

  // 미리보기 업데이트
  function updatePreview() {
    if (filesArr.length > 0) {
      showPreview(filesArr[0]);
      renderNameList(filesArr[0]);
    } else {
      hidePreview();
      renderNameList(null);
    }
  }

  // 파일 선택 처리
  input.addEventListener('change', function (e) {
    const files = Array.from(input.files);
    if (!handleFiles(files)) {
      this.value = '';
    }
  });

  // 드래그 효과 (간단하게)
  uploadBox.addEventListener('dragover', function (e) {
    e.preventDefault();
  });

  // 파일 드롭 처리
  uploadBox.addEventListener('drop', function (e) {
    e.preventDefault();
    if (e.dataTransfer.files.length > 0) {
      handleFiles(Array.from(e.dataTransfer.files));
    }
  });

  // 새 파일 목록 렌더링
  function renderNameList(activeFile) {
    const newFileItems = nameList.querySelectorAll('.new-file-item');
    newFileItems.forEach(item => item.remove());

    filesArr.forEach((file, idx) => {
      const item = document.createElement('div');
      item.className = 'image-name-item new-file-item';

      const fileNameSpan = document.createElement('span');
      fileNameSpan.textContent = file.name;
      item.appendChild(fileNameSpan);

      // 현재 미리보기 파일이면 active 추가
      if (activeFile && activeFile.name === file.name && activeFile.size === file.size) {
        item.classList.add('active');
      }

      // 파일 클릭 시 미리보기 변경
      item.addEventListener('click', (e) => {
        e.stopPropagation();
        showPreview(file);
        renderNameList(file);
      });

      // 파일 삭제 버튼
      const removeBtn = document.createElement('button');
      removeBtn.type = 'button';
      removeBtn.className = 'remove-image';
      removeBtn.innerText = 'x';

      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        filesArr.splice(idx, 1);
        updateInputFiles();

        // 간단한 삭제 후 처리
        if (filesArr.length > 0) {
          const wasActive = activeFile && activeFile.name === file.name;
          showPreview(wasActive ? filesArr[0] : activeFile || filesArr[0]);
          renderNameList(wasActive ? filesArr[0] : activeFile || filesArr[0]);
        } else {
          hidePreview();
          renderNameList(null);
        }
      });

      item.appendChild(removeBtn);
      nameList.appendChild(item);
    });
  }

  // 이미지 미리보기 표시
  function showPreview(source) {
    if (source instanceof File) {
      const reader = new FileReader();
      reader.onload = (e) => {
        preview.src = e.target.result;
        preview.style.display = 'block';
        icon.style.display = 'none';
        text.style.display = 'none';
      };
      reader.readAsDataURL(source);
    } else if (typeof source === 'string') {
      preview.src = source;
      preview.style.display = 'block';
      icon.style.display = 'none';
      text.style.display = 'none';
    }
  }

  // 미리보기 숨기기
  function hidePreview() {
    preview.style.display = 'none';
    preview.src = '';
    icon.style.display = 'block';
    text.style.display = 'block';
  }

  // input 파일 속성 업데이트
  function updateInputFiles() {
    const dt = new DataTransfer();
    filesArr.forEach(f => dt.items.add(f));
    input.files = dt.files;
  }

  // 기존 이미지 초기화
  function initializeExistingImages() {
    const existingImages = document.querySelectorAll('.existing-image');

    if (existingImages.length > 0) {
      const firstImage = existingImages[0];
      const firstImageId = firstImage.getAttribute('data-image-id');
      showPreview(`/product-images/${firstImageId}/data`);
      firstImage.classList.add('active');
    }

    existingImages.forEach((imageItem) => {
      const imageId = imageItem.getAttribute('data-image-id');

      // 이미지 클릭 이벤트
      imageItem.addEventListener('click', function(e) {
        if (e.target.tagName === 'BUTTON') return;
        e.stopPropagation();

        showPreview(`/product-images/${imageId}/data`);
        document.querySelectorAll('.image-name-item.active').forEach(el => el.classList.remove('active'));
        this.classList.add('active');
      });

      // 이미지 삭제 이벤트
      const removeBtn = imageItem.querySelector('.remove-image');
      if (removeBtn) {
        removeBtn.addEventListener('click', function(e) {
          e.stopPropagation();
          imageItem.remove();
        });
      }
    });
  }

  // 문서 파일 업로드
  const documentFileInput = document.querySelector('input[name="documentFile"]');
  let currentBlobUrl = null;

  if (documentFileInput) {
    documentFileInput.addEventListener('change', function(e) {
      const currentFileInfo = document.querySelector('.current-file-info');
      if (e.target.files.length > 0 && currentFileInfo) {
        const file = e.target.files[0];

        if (currentBlobUrl) {
          URL.revokeObjectURL(currentBlobUrl);
        }

        currentBlobUrl = URL.createObjectURL(file);
        const fileLink = currentFileInfo.querySelector('.current-file-link');
        const fileSizeSpan = currentFileInfo.querySelector('.file-size');

        if (fileLink) {
          fileLink.textContent = file.name;
          fileLink.href = currentBlobUrl;
          fileLink.download = file.name;
        }
        if (fileSizeSpan) {
          fileSizeSpan.innerHTML = `(${Math.round(file.size / 1024)} KB)`;
        }
      }
    });
  }
});