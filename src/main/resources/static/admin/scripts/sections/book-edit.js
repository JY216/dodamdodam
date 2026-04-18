window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form[action="/admin/books/update"]');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            dialog.confirm('도서 정보를 수정하시겠어요?', () => {
                form.submit();
            });
        });
    }
});