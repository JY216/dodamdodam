document.addEventListener('DOMContentLoaded', () => {

    // 탭 전환
    const tabs = document.querySelectorAll('.find-tab');
    const contents = document.querySelectorAll('.find-content');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('find-tab--active'));
            tab.classList.add('find-tab--active');

            const target = tab.dataset.tab;
            contents.forEach(c => c.style.display = 'none');
            document.getElementById(target).style.display = 'block';
        });
    });

    // 아이디 찾기
    document.getElementById('findIdBtn').addEventListener('click', async () => {
        const email = document.getElementById('findIdEmail').value.trim();
        if (!email) { alert('이메일을 입력해 주세요.'); return; }

        const res = await fetch(`/find-id?email=${encodeURIComponent(email)}`, { method: 'POST' });
        const data = await res.json();

        document.getElementById('findIdResult').style.display = 'none';
        document.getElementById('findIdError').style.display = 'none';

        if (data.result === 'SUCCESS') {
            document.getElementById('findIdValue').textContent = data.userId;
            document.getElementById('findIdResult').style.display = 'flex';
        } else {
            document.getElementById('findIdError').style.display = 'block';
        }
    });

    // 인증번호 전송
    document.getElementById('sendCodeBtn').addEventListener('click', async () => {
        const email = document.getElementById('resetEmail').value.trim();
        if (!email) { alert('이메일을 입력해 주세요.'); return; }

        document.getElementById('resetEmailError').style.display = 'none';

        const formData = new FormData();
        formData.append('email', email);

        const res = await fetch('/reset-password/email', { method: 'POST', body: formData });
        const data = await res.json();

        if (data.result === 'SUCCESS') {
            document.getElementById('resetSalt').value = data.salt;
            document.getElementById('codeField').style.display = 'block';
            document.getElementById('sendCodeBtn').textContent = '재전송';
            alert('인증번호가 전송됐어요!');
        } else {
            document.getElementById('resetEmailError').style.display = 'block';
        }
    });

    // 인증번호 확인
    document.getElementById('verifyCodeBtn').addEventListener('click', async () => {
        const email = document.getElementById('resetEmail').value.trim();
        const code = document.getElementById('resetCode').value.trim();
        const salt = document.getElementById('resetSalt').value;

        if (!code) { alert('인증번호를 입력해 주세요.'); return; }

        const formData = new FormData();
        formData.append('email', email);
        formData.append('code', code);
        formData.append('salt', salt);

        const res = await fetch('/reset-password/verify', { method: 'POST', body: formData });
        const data = await res.json();

        if (data.result === 'SUCCESS') {
            document.getElementById('pw-step-1').style.display = 'none';
            document.getElementById('pw-step-2').style.display = 'block';
            document.getElementById('pw-links').style.display = 'none';
        } else {
            alert('인증번호가 올바르지 않아요. 다시 확인해 주세요.');
        }
    });

    // 비밀번호 재설정
    document.getElementById('resetPwBtn').addEventListener('click', async () => {
        const email = document.getElementById('resetEmail').value.trim();
        const code = document.getElementById('resetCode').value.trim();
        const salt = document.getElementById('resetSalt').value;
        const newPassword = document.getElementById('newPassword').value;
        const newPasswordCheck = document.getElementById('newPasswordCheck').value;

        document.getElementById('resetPwError').style.display = 'none';

        if (newPassword.length < 6) {
            document.getElementById('resetPwErrorMsg').textContent = '비밀번호는 6자 이상이어야 해요.';
            document.getElementById('resetPwError').style.display = 'block';
            return;
        }

        if (newPassword !== newPasswordCheck) {
            document.getElementById('resetPwErrorMsg').textContent = '비밀번호가 일치하지 않아요.';
            document.getElementById('resetPwError').style.display = 'block';
            return;
        }

        const formData = new FormData();
        formData.append('email', email);
        formData.append('code', code);
        formData.append('salt', salt);
        formData.append('newPassword', newPassword);

        const res = await fetch('/reset-password', { method: 'POST', body: formData });
        const data = await res.json();

        if (data.result === 'SUCCESS') {
            document.getElementById('pw-step-2').style.display = 'none';
            document.getElementById('pw-step-3').style.display = 'block';
        } else {
            document.getElementById('resetPwErrorMsg').textContent = '비밀번호 재설정에 실패했어요. 다시 시도해 주세요.';
            document.getElementById('resetPwError').style.display = 'block';
        }
    });
});