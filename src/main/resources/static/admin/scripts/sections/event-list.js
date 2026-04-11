document.addEventListener('DOMContentLoaded', function () {

    // ── 아코디언 토글 ──────────────────────────────────────────────
    document.querySelectorAll('.js-event-row').forEach(function (rowEl) {
        rowEl.addEventListener('click', function () {
            toggleAccordion(rowEl);
        });
    });

    function toggleAccordion(rowEl) {
        const item   = rowEl.closest('.event-item');
        const panel  = item.querySelector('.accordion-panel');
        const arrow  = rowEl.querySelector('.accordion-arrow');
        const isOpen = item.classList.contains('open');

        document.querySelectorAll('.event-item.open').forEach(function (el) {
            el.classList.remove('open');
            el.querySelector('.accordion-panel').style.maxHeight = null;
            el.querySelector('.accordion-arrow').textContent = '▾';
        });

        if (!isOpen) {
            item.classList.add('open');
            arrow.textContent = '▴';

            if (item.dataset.loaded) {
                panel.style.maxHeight = panel.scrollHeight + 'px';
                return;
            }

            const eventId = item.dataset.id;
            fetch('/admin/event-list/' + eventId + '/applicants')
                .then(function (res) { return res.json(); })
                .then(function (applicants) {
                    item.dataset.loaded = 'true';

                    if (applicants.length === 0) {
                        panel.innerHTML = '<div class="no-applicants">신청 회원이 없습니다.</div>';
                    } else {
                        let html = '<table class="applicant-table"><thead><tr>'
                            + '<th>이름</th><th>연락처</th><th>신청 상태</th><th>신청일</th>'
                            + '</tr></thead><tbody>';
                        applicants.forEach(function (a) {
                            const badge = a.status === 'PENDING'
                                ? '<span class="app-badge app-pending">대기 중</span>'
                                : '<span class="app-badge app-confirmed">확정</span>';
                            html += '<tr>'
                                + '<td>' + a.name + '</td>'
                                + '<td>' + a.mobileFirst + '-' + a.mobileSecond + '-' + a.mobileThird + '</td>'
                                + '<td>' + badge + '</td>'
                                + '<td>' + a.createdAt + '</td>'
                                + '</tr>';
                        });
                        html += '</tbody></table>';
                        panel.innerHTML = html;
                    }
                    panel.style.maxHeight = panel.scrollHeight + 'px';
                })
                .catch(function () {
                    panel.innerHTML = '<div class="no-applicants">불러오기 실패. 다시 시도해주세요.</div>';
                    panel.style.maxHeight = panel.scrollHeight + 'px';
                });
        }
    }
});