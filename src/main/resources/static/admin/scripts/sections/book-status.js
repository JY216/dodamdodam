window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    if (!document.querySelector('.btn-add-copy')) return;

    document.querySelectorAll('.btn-add-copy').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('수량을 추가하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });
});