window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form[action="/admin/notices/update"]');
    if (!form) return;

    form.addEventListener('submit', (e) => {
        e.preventDefault();

        const title = form.querySelector('input[name="title"]').value.trim();
        const content = form.querySelector('textarea[name="content"]').value.trim();

        if (!title) { dialog.alert('공지 제목을 입력해주세요.'); return; }
        if (!content) { dialog.alert('공지 내용을 입력해주세요.'); return; }

        dialog.confirm('공지를 수정하시겠어요?', () => {
            form.submit();
        });
    });
});