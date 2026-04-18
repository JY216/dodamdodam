window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.js-delete-user').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const name = btn.dataset.name;
            const href = btn.getAttribute('href');

            dialog.confirm(`${name} 회원을 삭제하시겠어요?`, () => {
                location.href = href;
            });
        });
    });
});