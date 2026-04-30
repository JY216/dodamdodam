document.addEventListener('DOMContentLoaded', () => {

    // 상태
    let journals    = [];
    let goalCount   = 12;
    let readCount   = 0;
    let currentStar = 0;
    let activeId    = null;
    let sidebarOpen = true;

    // 요소 참조
    const sidebar        = document.getElementById('sidebar');
    const sidebarToggle  = document.getElementById('sidebarToggle');
    const mainContent    = document.getElementById('mainContent');
    const journalList    = document.getElementById('journalList');
    const journalCount   = document.getElementById('journalCount');
    const searchInput    = document.getElementById('searchInput');
    const goalEditBtn    = document.getElementById('goalEditBtn');
    const goalEditArea   = document.getElementById('goalEditArea');
    const goalInput      = document.getElementById('goalInput');
    const goalSaveBtn    = document.querySelector('.goal-save-btn');
    const starGroup      = document.getElementById('starGroup');
    const pageSlider     = document.getElementById('pageSlider');
    const sliderVal      = document.getElementById('sliderVal');
    const btnCancel      = document.querySelector('.btn-cancel');
    const btnSubmit      = document.querySelector('.btn-submit');
    const detailNewBtn   = document.querySelector('.detail-new-btn');
    const detailWriteBtn = document.querySelector('.detail-btn:not(.del)');
    const detailDelBtn   = document.querySelector('.detail-btn.del');
    const btnHome = document.querySelector('.btn-home');

    // 초기 데이터 로드
    setTodayDate();
    loadGoal();
    loadJournals();

    // URL 파라미터로 책 정보 자동 입력 (대출 현황에서 넘어온 경우)
    const params    = new URLSearchParams(window.location.search);
    const preTitle  = params.get('title');
    const preAuthor = params.get('author');
    if (preTitle || preAuthor) {
        if (preTitle)  document.getElementById('bookTitle').value  = preTitle;
        if (preAuthor) document.getElementById('bookAuthor').value = preAuthor;
        document.getElementById('writeCard').scrollIntoView({ behavior: 'smooth' });
    }

    // ── API 함수 ──────────────────────────────────────────────────

    function loadJournals() {
        fetch('/journal/api/list')
            .then(function (res) {
                if (res.status === 401) { location.href = '/login'; return null; }
                return res.json();
            })
            .then(function (data) {
                if (!data) return;
                journals = data;
                renderSidebar();
            })
            .catch(function () { window.dialog.alert('일지를 불러오지 못했어요.'); });
    }

    function loadGoal() {
        fetch('/journal/api/goal')
            .then(function (res) {
                if (res.status === 401) { location.href = '/login'; return null; }
                return res.json();
            })
            .then(function (data) {
                if (!data) return;
                goalCount = data.goalCount;
                readCount = data.readCount;
                renderGoal();
            })
            .catch(function () {});
    }

    function saveJournalToServer(dto) {
        return fetch('/journal/api/save', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        }).then(function (res) {
            if (res.status === 401) { location.href = '/login'; return null; }
            return res.json();
        });
    }

    function deleteJournalFromServer(id) {
        return fetch('/journal/api/' + id, { method: 'DELETE' })
            .then(function (res) {
                if (res.status === 401) { location.href = '/login'; }
            });
    }

    function saveGoalToServer(count) {
        fetch('/journal/api/goal', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ goalCount: count })
        }).catch(function () {});
    }

    // ── 이벤트 리스너 ─────────────────────────────────────────────

    sidebarToggle.addEventListener('click', function () {
        sidebarOpen = !sidebarOpen;
        sidebar.classList.toggle('collapsed', !sidebarOpen);
        sidebarToggle.classList.toggle('collapsed', !sidebarOpen);
        mainContent.classList.toggle('expanded', !sidebarOpen);
    });

    searchInput.addEventListener('input', function () {
        renderSidebar(searchInput.value.trim());
    });

    journalList.addEventListener('click', function (e) {
        const delBtn = e.target.closest('.journal-item-del');
        if (delBtn) {
            e.stopPropagation();
            deleteJournalById(parseInt(delBtn.dataset.id));
            return;
        }
        const item = e.target.closest('.journal-item');
        if (item) selectJournal(parseInt(item.dataset.id));
    });

    goalEditBtn.addEventListener('click', function () {
        const show = !goalEditArea.classList.contains('show');
        goalEditArea.classList.toggle('show', show);
        goalEditBtn.textContent = show ? '취소' : '목표 수정';
        if (show) goalInput.value = goalCount;
    });

    goalSaveBtn.addEventListener('click', function () {
        const val = parseInt(goalInput.value);
        if (!val || val < 1) return;
        goalCount = val;
        saveGoalToServer(goalCount);
        renderGoal();
        goalEditArea.classList.remove('show');
        goalEditBtn.textContent = '목표 수정';
    });

    starGroup.addEventListener('click', function (e) {
        const btn = e.target.closest('.star-btn');
        if (!btn) return;
        currentStar = parseInt(btn.dataset.val);
        document.querySelectorAll('.star-btn').forEach(function (b) {
            b.classList.toggle('active', parseInt(b.dataset.val) <= currentStar);
        });
    });

    pageSlider.addEventListener('input', function () {
        sliderVal.textContent = pageSlider.value + ' p';
    });

    btnCancel.addEventListener('click', function() {
        window.dialog.confirm('작성 중인 내용을 초기화하시겠습니까?', function() {
            resetForm();
        });
    });
    btnSubmit.addEventListener('click', submitJournal);
    detailNewBtn.addEventListener('click', showWriteForm);
    detailWriteBtn.addEventListener('click', showWriteForm);
    detailDelBtn.addEventListener('click', function () { deleteJournal(); });

    btnHome.addEventListener('click', function() {
        window.dialog.confirm('메인페이지로 돌아갈까요? 작성 중인 내용은 저장되지 않습니다.', function() {
            location.href = '/';
        });
    });

    // ── 함수 정의 ─────────────────────────────────────────────────

    function renderSidebar(query) {
        query = query || '';
        journalCount.textContent = journals.length;

        const filtered = query
            ? journals.filter(function (j) {
                return j.title.includes(query) ||
                    j.bookTitle.includes(query) ||
                    j.content.includes(query);
            })
            : journals;

        if (filtered.length === 0) {
            journalList.innerHTML = '<div class="sidebar-empty">' + (
                query ? '검색 결과가 없어요.'
                    : '아직 작성된 일지가 없어요.<br>첫 번째 독서 일지를 남겨보세요!'
            ) + '</div>';
            return;
        }

        journalList.innerHTML = filtered
            .slice()
            .sort(function (a, b) { return new Date(b.date) - new Date(a.date); })
            .map(function (j) {
                return '<div class="journal-item ' + (j.id === activeId ? 'active' : '') + '" data-id="' + j.id + '">'
                    + '<div class="journal-item-title">' + j.title + '</div>'
                    + '<div class="journal-item-book">📖 ' + j.bookTitle + '</div>'
                    + '<div class="journal-item-date">' + formatDate(j.date) + ' · ' + '★'.repeat(j.star) + '☆'.repeat(5 - j.star) + '</div>'
                    + '<button class="journal-item-del" data-id="' + j.id + '" type="button">✕</button>'
                    + '</div>';
            }).join('');
    }

    function selectJournal(id) {
        activeId = id;
        const j = journals.find(function (x) { return x.id === id; });
        if (!j) return;

        const statusMap = { reading: '📖 읽는 중', done: '✅ 완독', pause: '⏸ 잠시 멈춤' };

        document.getElementById('detailTitle').textContent  = j.title;
        document.getElementById('detailDate').textContent   = formatDate(j.date);
        document.getElementById('detailStatus').textContent = statusMap[j.status] || '';
        document.getElementById('detailBook').innerHTML =
            '<svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">'
            + '<path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>'
            + '<path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>'
            + '</svg> ' + j.bookTitle + (j.author ? ' · ' + j.author : '');
        document.getElementById('detailStars').textContent =
            '★'.repeat(j.star) + '☆'.repeat(5 - j.star) + (j.star ? ' (' + j.star + '점)' : '');
        document.getElementById('detailProgress').textContent =
            j.pages > 0 ? '읽은 페이지: ' + j.pages + 'p' : '';
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

        if (!title)   { window.dialog.alert('일지 제목을 입력해주세요.');  return; }
        if (!date)    { window.dialog.alert('독서 날짜를 선택해주세요.');  return; }
        if (!book)    { window.dialog.alert('책 제목을 입력해주세요.');    return; }
        if (!content) { window.dialog.alert('독서 감상을 입력해주세요.');  return; }

        const dto = { title, date, bookTitle: book, author, star: currentStar, status, pages, content };

        saveJournalToServer(dto)
            .then(function (saved) {
                if (!saved) return;
                journals.unshift(saved);
                if (status === 'done') {
                    readCount++;
                    renderGoal();
                }
                renderSidebar();
                selectJournal(saved.id);
                window.dialog.alert('저장이 완료되었습니다.');
            })
            .catch(function () { window.dialog.alert('저장에 실패했어요. 다시 시도해주세요.'); });
    }

    function deleteJournal() {
        if (!activeId) return;
        deleteJournalById(activeId);
    }

    function deleteJournalById(id) {
        window.dialog.confirm('이 일지를 삭제할까요?', function() {
            const j = journals.find(function (x) { return x.id === id; });

            deleteJournalFromServer(id)  // ← 들여쓰기 맞춰서 콜백 안으로
                .then(function () {
                    journals = journals.filter(function (x) { return x.id !== id; });
                    if (j && j.status === 'done' && readCount > 0) {
                        readCount--;
                        renderGoal();
                    }
                    if (id === activeId) showWriteForm();
                    renderSidebar(searchInput.value.trim());
                })
                .catch(function () { window.dialog.alert('삭제에 실패했어요.'); });
        });
    }

    function resetForm() {
        document.getElementById('journalTitle').value   = '';
        document.getElementById('bookTitle').value      = '';
        document.getElementById('bookAuthor').value     = '';
        document.getElementById('journalContent').value = '';
        document.getElementById('readStatus').value     = 'done';
        pageSlider.value      = 0;
        sliderVal.textContent = '0 p';
        currentStar = 0;
        document.querySelectorAll('.star-btn').forEach(function (b) { b.classList.remove('active'); });
        setTodayDate();
    }

    function renderGoal() {
        const pct = Math.min(100, Math.round((readCount / goalCount) * 100));
        document.getElementById('goalCurrent').textContent = readCount;
        document.getElementById('goalTotal').textContent   = goalCount;
        document.getElementById('goalBar').style.width     = pct + '%';
        document.getElementById('goalPercent').textContent = pct + '% 달성';
        document.getElementById('goalRemain').textContent  =
            readCount >= goalCount ? '목표 달성! 🎉' : (goalCount - readCount) + '권 남음';
    }

    function setTodayDate() {
        document.getElementById('journalDate').value =
            new Date().toISOString().split('T')[0];
    }

    function formatDate(str) {
        if (!str) return '';
        const d = new Date(str);
        return d.getFullYear() + '. ' + (d.getMonth() + 1) + '. ' + d.getDate() + '.';
    }

    function showToast(msg) {
        alert(msg);
    }
});