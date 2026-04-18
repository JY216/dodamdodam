window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.btn-approve').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const statusInput = btn.closest('form').querySelector('input[name="status"]');
            const message = statusInput.value === 'PURCHASED' ? '구매 완료 처리하시겠어요?' : '승인하시겠어요?';
            dialog.confirm(message, () => {
                btn.closest('form').submit();
            });
        });
    });

    document.querySelectorAll('.btn-return').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('반려하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });
});