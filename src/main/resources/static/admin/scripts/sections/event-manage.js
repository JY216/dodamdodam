window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    if (!document.querySelector('.js-status-btn[data-status]')) return;

    document.querySelectorAll('.js-status-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            const title = btn.dataset.title;
            const status = btn.dataset.status;
            const page = btn.dataset.page;
            const isOpen = status === 'OPEN';
            const next = isOpen ? 'CLOSED' : 'OPEN';
            const action = isOpen ? '마감' : '재개';

            dialog.confirm(`"${title}" 행사를 ${action}하시겠어요?`, () => {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = `/admin/events/${id}/status`;
                form.innerHTML = `
                    <input type="hidden" name="status" value="${next}">
                    <input type="hidden" name="page" value="${page}">
                `;
                document.body.appendChild(form);
                form.submit();
            });
        });
    });

    document.querySelectorAll('.js-delete-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.id;
            const title = btn.dataset.title;
            const page = btn.dataset.page;

            dialog.confirm(`"${title}" 행사를 삭제하시겠어요? 이 작업은 되돌릴 수 없어요.`, () => {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = `/admin/events/${id}/delete`;
                form.innerHTML = `<input type="hidden" name="page" value="${page}">`;
                document.body.appendChild(form);
                form.submit();
            });
        });
    });
});