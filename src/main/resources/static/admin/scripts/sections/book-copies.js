window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    if (!document.querySelector('.status-select')) return;

    document.querySelectorAll('.btn-edit').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('상태를 변경하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });
});