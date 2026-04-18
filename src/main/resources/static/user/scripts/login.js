// 로그인 실패 알림
document.addEventListener('DOMContentLoaded', () => {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error')) {
        dialog.alert('아이디 또는 비밀번호가 올바르지 않습니다.', () => {
            history.replaceState(null, '', '/login');
        });
    }
});