const $loginForm = document.forms['loginForm'];

$loginForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const $userIdInput  = $loginForm['userId'];
    const $passwordInput = $loginForm['password'];

    if ($userIdInput.value.trim() === '') {
        alert('아이디를 입력해 주세요.');
        $userIdInput.focus();
        return;
    }
    if ($passwordInput.value === '') {
        alert('비밀번호를 입력해 주세요.');
        $passwordInput.focus();
        return;
    }

    const formData = new FormData();
    formData.append('userId',   $userIdInput.value.trim());
    formData.append('password', $passwordInput.value);

    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) return;
        if (xhr.status < 200 || xhr.status >= 400) {
            alert(`오류가 발생했습니다. 잠시 후 다시 시도해 주세요. (${xhr.status})`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        if (response.result === 'SUCCESS') {
            location.href = '/';
        } else {
            alert('아이디 또는 비밀번호가 올바르지 않습니다.');
            $passwordInput.value = '';
            $passwordInput.focus();
        }
    };
    xhr.open('POST', '/login');
    xhr.send(formData);
});