document.addEventListener('DOMContentLoaded', () => {
    const flash = document.getElementById('flashData');
    if (flash) {
        const success = flash.dataset.success;
        const error = flash.dataset.error;
        if (success) dialog.alert(success);
        else if (error) dialog.alert(error);
    }

    document.querySelectorAll('.btn-unlike-confirm').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('찜을 취소하시겠어요?', () => {
                btn.closest('form').submit();
            });
        });
    });
});