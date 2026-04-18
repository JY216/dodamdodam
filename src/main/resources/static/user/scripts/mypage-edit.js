document.addEventListener('DOMContentLoaded', function () {

    // 프로필 저장
    document.getElementById('btnSaveProfile').addEventListener('click', function () {
        const name          = document.getElementById('name').value.trim();
        const mobileFirst   = document.getElementById('mobileFirst').value.trim();
        const mobileSecond  = document.getElementById('mobileSecond').value.trim();
        const mobileThird   = document.getElementById('mobileThird').value.trim();
        const addressPrimary   = document.getElementById('addressPrimary').value.trim();
        const addressSecondary = document.getElementById('addressSecondary').value.trim();
        const msg = document.getElementById('profileMsg');

        if (!name) {
            showMsg(msg, '이름을 입력해주세요.', false);
            return;
        }
        if (!mobileFirst || !mobileSecond || !mobileThird) {
            showMsg(msg, '휴대폰 번호를 모두 입력해주세요.', false);
            return;
        }

        const params = new URLSearchParams({
            name, mobileFirst, mobileSecond, mobileThird,
            addressPrimary, addressSecondary
        });

        fetch('/mypage/edit/profile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.result === 'SUCCESS') {
                    showMsg(msg, '정보가 성공적으로 수정되었어요.', true);
                } else {
                    showMsg(msg, '수정에 실패했어요. 다시 시도해주세요.', false);
                }
            })
            .catch(function () {
                showMsg(msg, '오류가 발생했어요. 다시 시도해주세요.', false);
            });
    });

    // 비밀번호 변경
    document.getElementById('btnSavePassword').addEventListener('click', function () {
        const currentPassword = document.getElementById('currentPassword').value;
        const newPassword     = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const msg = document.getElementById('passwordMsg');

        if (!currentPassword) {
            showMsg(msg, '현재 비밀번호를 입력해주세요.', false);
            return;
        }
        if (!newPassword) {
            showMsg(msg, '새 비밀번호를 입력해주세요.', false);
            return;
        }
        if (newPassword.length < 8) {
            showMsg(msg, '비밀번호는 8자 이상이어야 해요.', false);
            return;
        }
        if (newPassword !== confirmPassword) {
            showMsg(msg, '새 비밀번호가 일치하지 않아요.', false);
            return;
        }

        const params = new URLSearchParams({ currentPassword, newPassword, confirmPassword });

        fetch('/mypage/edit/password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.result === 'SUCCESS') {
                    showMsg(msg, '비밀번호가 변경되었어요.', true);
                    document.getElementById('currentPassword').value = '';
                    document.getElementById('newPassword').value     = '';
                    document.getElementById('confirmPassword').value = '';
                } else if (data.result === 'FAILURE_WRONG_PASSWORD') {
                    showMsg(msg, '현재 비밀번호가 올바르지 않아요.', false);
                } else if (data.result === 'FAILURE_MISMATCH') {
                    showMsg(msg, '새 비밀번호가 일치하지 않아요.', false);
                } else {
                    showMsg(msg, '변경에 실패했어요. 다시 시도해주세요.', false);
                }
            })
            .catch(function () {
                showMsg(msg, '오류가 발생했어요. 다시 시도해주세요.', false);
            });
    });

    // 다음 주소 찾기
    document.getElementById('btnAddressSearch').addEventListener('click', function () {
        new daum.Postcode({
            oncomplete: function (data) {
                document.getElementById('addressPrimary').value =
                    data.roadAddress || data.jibunAddress;
                document.getElementById('addressSecondary').focus();
            }
        }).open();
    });

    ['mobileFirst', 'mobileSecond', 'mobileThird'].forEach(function (id) {
        document.getElementById(id).addEventListener('input', function () {
            this.value = this.value.replace(/[^0-9]/g, '');
        });
    });

    // 메시지 표시
    function showMsg(el, text, isSuccess) {
        el.textContent  = text;
        el.className    = 'edit-msg ' + (isSuccess ? 'success' : 'error');
        setTimeout(function () {
            el.textContent = '';
            el.className   = 'edit-msg';
        }, 3000);
    }
});