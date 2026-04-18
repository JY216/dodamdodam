window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.js-delete-notice').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('공지를 삭제하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });
});

const flash = document.getElementById('flashData');
if (flash && flash.dataset.success) {
    dialog.alert(flash.dataset.success);
}