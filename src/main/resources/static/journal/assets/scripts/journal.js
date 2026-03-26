document.addEventListener('DOMContentLoaded', () => {

    // 상태
    let journals    = JSON.parse(localStorage.getItem('journals') || '[]');
    let goalCount   = parseInt(localStorage.getItem('goalCount') || '12');
    let readCount   = parseInt(localStorage.getItem('readCount') || '3');
    let currentStar = 0;
    let activeId    = null;
    let sidebarOpen = true;

    // 요소 참조
    const sidebar       = document.getElementById('sidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const mainContent   = document.getElementById('mainContent');
    const journalList   = document.getElementById('journalList');
    const journalCount  = document.getElementById('journalCount');
    const searchInput   = document.getElementById('searchInput');

    const goalEditBtn   = document.getElementById('goalEditBtn');
    const goalEditArea  = document.getElementById('goalEditArea');
    const goalInput     = document.getElementById('goalInput');
    const goalSaveBtn   = document.querySelector('.goal-save-btn');

    const starGroup     = document.getElementById('starGroup');
    const pageSlider    = document.getElementById('pageSlider');
    const sliderVal     = document.getElementById('sliderVal');

    const btnCancel     = document.querySelector('.btn-cancel');
    const btnSubmit     = document.querySelector('.btn-submit');

    const detailNewBtn  = document.querySelector('.detail-new-btn');
    const detailWriteBtn = document.querySelector('.detail-btn:not(.del)');
    const detailDelBtn  = document.querySelector('.detail-btn.del');

    // 초기화
    setTodayDate();
    renderGoal();
    renderSidebar();

    // 날짜 기본값
    function setTodayDate() {
        document.getElementById('journalDate').value =
            new Date().toISOString().split('T')[0];
    }

    // 사이드바 토글
    sidebarToggle.addEventListener('click', () => {
        sidebarOpen = !sidebarOpen;
        sidebar.classList.toggle('collapsed', !sidebarOpen);
        sidebarToggle.classList.toggle('collapsed', !sidebarOpen);
        mainContent.classList.toggle('expanded', !sidebarOpen);
    });

    // 사이드바 검색
    searchInput.addEventListener('input', () => {
        renderSidebar(searchInput.value.trim());
    });

    // 사이드바 목록 클릭 (이벤트 위임)
    journalList.addEventListener('click', (e) => {
        // 삭제 버튼
        const delBtn = e.target.closest('.journal-item-del');
        if (delBtn) {
            e.stopPropagation();
            deleteJournalById(delBtn.dataset.id);
            return;
        }
        // 일지 항목 선택
        const item = e.target.closest('.journal-item');
        if (item) selectJournal(item.dataset.id);
    });

    // 목표 수정 토글
    goalEditBtn.addEventListener('click', () => {
        const show = !goalEditArea.classList.contains('show');
        goalEditArea.classList.toggle('show', show);
        goalEditBtn.textContent = show ? '취소' : '목표 수정';
        if (show) goalInput.value = goalCount;
    });

    // 목표 저장
    goalSaveBtn.addEventListener('click', () => {
        const val = parseInt(goalInput.value);
        if (!val || val < 1) return;
        goalCount = val;
        localStorage.setItem('goalCount', goalCount);
        renderGoal();
        // 편집 영역 닫기
        goalEditArea.classList.remove('show');
        goalEditBtn.textContent = '목표 수정';
    });

    // 별점 (이벤트 위임)
    starGroup.addEventListener('click', (e) => {
        const btn = e.target.closest('.star-btn');
        if (!btn) return;
        currentStar = parseInt(btn.dataset.val);
        document.querySelectorAll('.star-btn').forEach(b => {
            b.classList.toggle('active', parseInt(b.dataset.val) <= currentStar);
        });
    });

    // 페이지 슬라이더
    pageSlider.addEventListener('input', () => {
        sliderVal.textContent = pageSlider.value + ' p';
    });

    // 폼 초기화
    btnCancel.addEventListener('click', resetForm);

    // 일지 저장
    btnSubmit.addEventListener('click', submitJournal);

    // 상세 뷰 버튼들
    detailNewBtn.addEventListener('click', showWriteForm);
    detailWriteBtn.addEventListener('click', showWriteForm);
    detailDelBtn.addEventListener('click', () => deleteJournal());

    // 함수 정의
    function renderSidebar(query = '') {
        journalCount.textContent = journals.length;

        const filtered = query
            ? journals.filter(j =>
                j.title.includes(query) ||
                j.bookTitle.includes(query) ||
                j.content.includes(query))
            : journals;

        if (filtered.length === 0) {
            journalList.innerHTML = `<div class="sidebar-empty">${
                query
                    ? '검색 결과가 없어요.'
                    : '아직 작성된 일지가 없어요.<br>첫 번째 독서 일지를 남겨보세요!'
            }</div>`;
            return;
        }

        journalList.innerHTML = filtered
            .slice()
            .sort((a, b) => new Date(b.date) - new Date(a.date))
            .map(j => `
        <div class="journal-item ${j.id === activeId ? 'active' : ''}" data-id="${j.id}">
          <div class="journal-item-title">${j.title}</div>
          <div class="journal-item-book">📖 ${j.bookTitle}</div>
          <div class="journal-item-date">
            ${formatDate(j.date)} · ${'★'.repeat(j.star)}${'☆'.repeat(5 - j.star)}
          </div>
          <button class="journal-item-del" data-id="${j.id}" type="button">✕</button>
        </div>
      `).join('');
    }

    function selectJournal(id) {
        activeId = id;
        const j = journals.find(x => x.id === id);
        if (!j) return;

        const statusMap = { reading: '📖 읽는 중', done: '✅ 완독', pause: '⏸ 잠시 멈춤' };

        document.getElementById('detailTitle').textContent  = j.title;
        document.getElementById('detailDate').textContent   = formatDate(j.date);
        document.getElementById('detailStatus').textContent = statusMap[j.status] || '';
        document.getElementById('detailBook').innerHTML =
            `<svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
        <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
        <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
       </svg>
       ${j.bookTitle}${j.author ? ' · ' + j.author : ''}`;
        document.getElementById('detailStars').textContent =
            '★'.repeat(j.star) + '☆'.repeat(5 - j.star) + (j.star ? ` (${j.star}점)` : '');
        document.getElementById('detailProgress').textContent =
            j.pages > 0 ? `읽은 페이지: ${j.pages}p` : '';
        document.getElementById('detailContent').textContent = j.content;

        document.getElementById('writeCard').style.display = 'none';
        document.getElementById('detailView').classList.add('show');
        renderSidebar(searchInput.value.trim());
    }

    function showWriteForm() {
        activeId = null;
        document.getElementById('detailView').classList.remove('show');
        document.getElementById('writeCard').style.display = 'block';
        renderSidebar(searchInput.value.trim());
        resetForm();
    }

    function submitJournal() {
        const title   = document.getElementById('journalTitle').value.trim();
        const date    = document.getElementById('journalDate').value;
        const book    = document.getElementById('bookTitle').value.trim();
        const author  = document.getElementById('bookAuthor').value.trim();
        const content = document.getElementById('journalContent').value.trim();
        const status  = document.getElementById('readStatus').value;
        const pages   = parseInt(pageSlider.value) || 0;

        if (!title)   { alert('일지 제목을 입력해주세요.');  return; }
        if (!date)    { alert('독서 날짜를 선택해주세요.');  return; }
        if (!book)    { alert('책 제목을 입력해주세요.');    return; }
        if (!content) { alert('독서 감상을 입력해주세요.');  return; }

        const newJournal = {
            id: 'j_' + Date.now(),
            title, date, bookTitle: book, author,
            star: currentStar, status, pages, content,
            createdAt: new Date().toISOString(),
        };

        journals.unshift(newJournal);
        localStorage.setItem('journals', JSON.stringify(journals));

        if (status === 'done') {
            readCount++;
            localStorage.setItem('readCount', readCount);
            renderGoal();
        }

        renderSidebar();
        selectJournal(newJournal.id);
    }

    function deleteJournal() {
        if (!activeId) return;
        deleteJournalById(activeId);
        showWriteForm();
    }

    function deleteJournalById(id) {
        if (!confirm('이 일지를 삭제할까요?')) return;
        const j = journals.find(x => x.id === id);
        journals = journals.filter(x => x.id !== id);
        localStorage.setItem('journals', JSON.stringify(journals));

        if (j?.status === 'done' && readCount > 0) {
            readCount--;
            localStorage.setItem('readCount', readCount);
            renderGoal();
        }
        if (id === activeId) showWriteForm();
        renderSidebar(searchInput.value.trim());
    }

    function resetForm() {
        document.getElementById('journalTitle').value   = '';
        document.getElementById('bookTitle').value      = '';
        document.getElementById('bookAuthor').value     = '';
        document.getElementById('journalContent').value = '';
        document.getElementById('readStatus').value     = 'done';
        pageSlider.value     = 0;
        sliderVal.textContent = '0 p';
        currentStar = 0;
        document.querySelectorAll('.star-btn').forEach(b => b.classList.remove('active'));
        setTodayDate();
    }

    function renderGoal() {
        const pct = Math.min(100, Math.round((readCount / goalCount) * 100));
        document.getElementById('goalCurrent').textContent = readCount;
        document.getElementById('goalTotal').textContent   = goalCount;
        document.getElementById('goalBar').style.width     = pct + '%';
        document.getElementById('goalPercent').textContent = pct + '% 달성';
        document.getElementById('goalRemain').textContent  =
            readCount >= goalCount ? '목표 달성! 🎉' : `${goalCount - readCount}권 남음`;
    }

    function formatDate(str) {
        if (!str) return '';
        const d = new Date(str);
        return `${d.getFullYear()}. ${d.getMonth() + 1}. ${d.getDate()}.`;
    }
});