document.addEventListener('DOMContentLoaded', () => {
    const flash = document.getElementById('flashData');
    if (flash) {
        const success = flash.dataset.success;
        const error = flash.dataset.error;
        if (success) dialog.alert(success);
        else if (error) dialog.alert(error);
    }

    // 신청 취소 confirm 다이얼로그
    const cancelBtn = document.querySelector('.btn-apply--cancel');
    if (cancelBtn) {
        cancelBtn.removeAttribute('onclick');
        cancelBtn.addEventListener('click', (e) => {
            e.preventDefault();
            dialog.confirm('신청을 취소하시겠어요?', () => {
                cancelBtn.closest('form').submit();
            });
        });
    }
});