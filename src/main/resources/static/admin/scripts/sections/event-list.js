document.addEventListener('DOMContentLoaded', function () {

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
                            + '<th>이름</th><th>연락처</th><th>신청 상태</th><th>신청일</th><th>처리</th>'
                            + '</tr></thead><tbody>';
                        applicants.forEach(function (a) {
                            const isPending = a.status === 'PENDING';
                            const badge = isPending
                                ? '<span class="app-badge app-pending">대기 중</span>'
                                : a.status === 'CONFIRMED'
                                    ? '<span class="app-badge app-confirmed">확정</span>'
                                    : '<span class="app-badge app-rejected">거절</span>';
                            const actionBtns = isPending ? `
                                <button type="button" class="app-btn app-btn-confirm"
                                    data-event-id="${eventId}"
                                    data-app-id="${a.applicationId}">수락</button>
                                <button type="button" class="app-btn app-btn-reject"
                                    data-event-id="${eventId}"
                                    data-app-id="${a.applicationId}">거절</button>
                            ` : '-';
                            html += '<tr>'
                                + '<td>' + a.name + '</td>'
                                + '<td>' + a.mobileFirst + '-' + a.mobileSecond + '-' + a.mobileThird + '</td>'
                                + '<td>' + badge + '</td>'
                                + '<td>' + a.createdAt + '</td>'
                                + '<td>' + actionBtns + '</td>'
                                + '</tr>';
                        });
                        html += '</tbody></table>';
                        panel.innerHTML = html;

                        // 수락/거절 다이얼로그
                        panel.querySelectorAll('.app-btn-confirm').forEach(btn => {
                            btn.addEventListener('click', () => {
                                const eventId = btn.dataset.eventId;
                                const appId = btn.dataset.appId;
                                dialog.confirm('수락하시겠어요?', () => {
                                    submitStatus(eventId, appId, 'CONFIRMED');
                                });
                            });
                        });

                        panel.querySelectorAll('.app-btn-reject').forEach(btn => {
                            btn.addEventListener('click', () => {
                                const eventId = btn.dataset.eventId;
                                const appId = btn.dataset.appId;
                                dialog.confirm('거절하시겠어요?', () => {
                                    submitStatus(eventId, appId, 'REJECTED');
                                });
                            });
                        });
                    }
                    panel.style.maxHeight = panel.scrollHeight + 'px';
                })
                .catch(function () {
                    panel.innerHTML = '<div class="no-applicants">불러오기 실패. 다시 시도해주세요.</div>';
                    panel.style.maxHeight = panel.scrollHeight + 'px';
                });
        }
    }

    function submitStatus(eventId, appId, status) {
        const form = document.createElement('form');
        form.method = 'post';
        form.action = `/admin/event-list/${eventId}/applicants/${appId}/status`;
        form.innerHTML = `<input type="hidden" name="status" value="${status}">`;
        document.body.appendChild(form);
        form.submit();
    }
});