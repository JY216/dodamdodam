window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.btn-approve').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('대출을 승인하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });

    document.querySelectorAll('.btn-return').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('반납 처리하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });
});