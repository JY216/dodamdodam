document.addEventListener('DOMContentLoaded', function () {

    // ── 아코디언 토글 ──────────────────────────────────────────────
    document.querySelectorAll('.js-toggle-row').forEach(function (rowEl) {
        rowEl.addEventListener('click', function () {
            toggleUserAccordion(rowEl);
        });
    });

    // 상태 변경 버튼 클릭 시 이벤트 전파 중단
    document.querySelectorAll('.js-stop-propagation').forEach(function (el) {
        el.addEventListener('click', function (e) {
            e.stopPropagation();
        });
    });

    // ── 상태 변경 버튼 ─────────────────────────────────────────────
    document.querySelectorAll('.js-status-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const userId = btn.dataset.id;
            const userName = btn.dataset.name;
            const action = btn.dataset.action;
            const isSuspend = action === 'SUSPENDED';

            const message = isSuspend
                ? `${userName} 회원을 정지하시겠어요?`
                : `${userName} 회원의 정지를 해제하시겠어요?`;

            dialog.confirm(message, () => {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = `/admin/users/${userId}/status`;
                document.body.appendChild(form);
                form.submit();
            });
        });
    });

    // ── 스위치 탭 ──────────────────────────────────────────────────
    document.querySelectorAll('.switch-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const tab  = btn.dataset.tab;
            const wrap = btn.closest('.activity-wrap');
            const item = btn.closest('.user-item');

            wrap.querySelectorAll('.switch-btn').forEach(function (b) {
                b.classList.remove('active');
            });
            btn.classList.add('active');

            wrap.querySelector('.tab-loan').style.display  = tab === 'loan'  ? 'block' : 'none';
            wrap.querySelector('.tab-event').style.display = tab === 'event' ? 'block' : 'none';

            if (tab === 'event' && !item.dataset.eventLoaded) {
                loadEventTab(item);
                item.dataset.eventLoaded = 'true';
            }

            adjustPanelHeight(item);
        });
    });

    // ── 아코디언 ───────────────────────────────────────────────────
    function toggleUserAccordion(rowEl) {
        const item   = rowEl.closest('.user-item');
        const arrow  = rowEl.querySelector('.accordion-arrow');
        const isOpen = item.classList.contains('open');

        document.querySelectorAll('.user-item.open').forEach(function (el) {
            if (el !== item) {
                el.classList.remove('open');
                el.querySelector('.user-accordion-panel').style.maxHeight = '0';
                el.querySelector('.accordion-arrow').textContent = '▾';
            }
        });

        if (isOpen) {
            item.classList.remove('open');
            item.querySelector('.user-accordion-panel').style.maxHeight = '0';
            arrow.textContent = '▾';
        } else {
            item.classList.add('open');
            arrow.textContent = '▴';

            if (!item.dataset.loaded) {
                loadLoanTab(item);
                item.dataset.loaded = 'true';
            }

            setTimeout(() => {
                adjustPanelHeight(item);
            }, 50);
        }
    }

    function adjustPanelHeight(item) {
        const panel = item.querySelector('.user-accordion-panel');
        panel.style.maxHeight = (panel.scrollHeight + 300) + 'px';
    }

    // ── 대출 탭 로드 ───────────────────────────────────────────────
    function loadLoanTab(item) {
        const userId = item.dataset.userid;
        const tabEl  = item.querySelector('.tab-loan');

        fetch('/admin/users/' + userId + '/loans')
            .then(function (res) { return res.json(); })
            .then(function (data) {
                const loans   = data.loans;
                const overdue = data.overdue;

                let html = '<div class="overdue-info">';
                html += '<span class="overdue-label">누적 연체 횟수</span>';
                html += '<span class="overdue-count' + (overdue > 0 ? ' has-overdue' : '') + '">'
                    + overdue + '회</span>';
                html += '</div>';

                if (loans.length === 0) {
                    html += '<div class="no-data">대출 기록이 없습니다.</div>';
                } else {
                    html += '<table class="activity-table"><thead><tr>'
                        + '<th>책 제목</th><th>대출일</th><th>반납 기한</th><th>반납일</th><th>상태</th>'
                        + '</tr></thead><tbody>';
                    loans.forEach(function (l) {
                        const badge = l.status === 'LOANED'
                            ? '<span class="act-badge act-loaned">대출 중</span>'
                            : '<span class="act-badge act-returned">반납</span>';
                        html += '<tr>'
                            + '<td class="td-book-title">' + l.bookTitle + '</td>'
                            + '<td>' + (l.loanDate   || '-') + '</td>'
                            + '<td>' + (l.dueDate    || '-') + '</td>'
                            + '<td>' + (l.returnDate || '-') + '</td>'
                            + '<td>' + badge + '</td>'
                            + '</tr>';
                    });
                    html += '</tbody></table>';
                }

                tabEl.innerHTML = html;
                adjustPanelHeight(item);
            })
            .catch(function () {
                tabEl.innerHTML = '<div class="no-data">불러오기 실패. 다시 시도해주세요.</div>';
            });
    }

    // ── 행사 탭 로드 ───────────────────────────────────────────────
    function loadEventTab(item) {
        const userId = item.dataset.userid;
        const tabEl  = item.querySelector('.tab-event');

        fetch('/admin/users/' + userId + '/events')
            .then(function (res) { return res.json(); })
            .then(function (events) {
                if (events.length === 0) {
                    tabEl.innerHTML = '<div class="no-data">행사 참여 기록이 없습니다.</div>';
                } else {
                    let html = '<table class="activity-table"><thead><tr>'
                        + '<th>행사명</th><th>행사 기간</th><th>신청 상태</th>'
                        + '</tr></thead><tbody>';
                    events.forEach(function (e) {
                        const badge = e.status === 'PENDING'
                            ? '<span class="act-badge act-pending">신청 중</span>'
                            : '<span class="act-badge act-confirmed">수강 완료</span>';
                        html += '<tr>'
                            + '<td class="td-book-title">' + e.eventTitle + '</td>'
                            + '<td>' + e.eventStartAt + ' ~ ' + e.eventEndAt + '</td>'
                            + '<td>' + badge + '</td>'
                            + '</tr>';
                    });
                    html += '</tbody></table>';
                    tabEl.innerHTML = html;
                }
                adjustPanelHeight(item);
            })
            .catch(function () {
                tabEl.innerHTML = '<div class="no-data">불러오기 실패. 다시 시도해주세요.</div>';
            });
    }
});