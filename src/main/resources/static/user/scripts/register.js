/** @type {HTMLFormElement} */
const $registerForm = document.forms['registerForm'];

/** @type {HTMLElement[]} */
const $steps = Array.from($registerForm.querySelectorAll(':scope > .step > .item'));

/** @type {HTMLElement[]} */
const $contents = Array.from($registerForm.querySelectorAll(':scope > .content'));

// 유틸
/**
 * @param {HTMLElement} $el
 * @param {boolean} visible
 */
const setVisible = ($el, visible) => {
    if (visible) {
        $el.setAttribute('data-visible', '');
    } else {
        $el.removeAttribute('data-visible');
    }
};

// 버튼 단계별 표시 여부
const buttonVisibilityMap = {
    cancel:   [true,  true,  true,  false],
    previous: [false, true,  true,  false],
    next:     [true,  true,  true,  false],
    complete: [false, false, false, true],
};

/** @param {number} step 1-based */
const setStep = (step) => {
    // 스텝 active 클래스 교체
    $steps.forEach(($step) => $step.classList.remove('active'));
    $steps.at(step - 1)?.classList.add('active');

    // 콘텐츠 visible 교체
    $contents.forEach(($content) => setVisible($content, false));
    setVisible($contents.at(step - 1), true);

    // 버튼 visible 교체
    Object.keys(buttonVisibilityMap).forEach((buttonName) => {
        const $button = $registerForm[buttonName];
        if (!$button) return;
        const visibilityArray = buttonVisibilityMap[buttonName];
        setVisible($button, visibilityArray.at(step - 1));
    });
};

// 단계별 유효성 검사
const handleTerm = () => {
    const $termPolicyCheck  = $registerForm['termPolicyCheck'];
    const $termPrivacyCheck = $registerForm['termPrivacyCheck'];

    if (!$termPolicyCheck.checked) {
        alert('서비스 이용약관에 동의하지 않을 경우 회원가입을 계속할 수 없습니다.');
        $registerForm['termPolicy'].focus();
        return;
    }
    if (!$termPrivacyCheck.checked) {
        alert('개인정보 처리방침에 동의하지 않을 경우 회원가입을 계속할 수 없습니다.');
        $registerForm['termPrivacy'].focus();
        return;
    }
    setStep(++currentStep);
};

// 이메일 인증 단계
const handleVerification = () => {
    if (!isEmailVerified) {
        alert('이메일 인증을 완료해 주세요.');
        return;
    }
    setStep(++currentStep);
};

const handleInformation = () => {
    const $nameInput          = $registerForm['name'];
    const $birthInput         = $registerForm['birth'];
    const $genderSelect       = $registerForm['gender'];
    const $userIdInput        = $registerForm['userId'];
    const $passwordInput      = $registerForm['password'];
    const $passwordCheckInput = $registerForm['passwordCheck'];
    const $mobileSecond       = $registerForm['mobileSecond'];
    const $mobileThird        = $registerForm['mobileThird'];
    const $addressPrimary     = $registerForm['addressPrimary'];

    // 성명
    if ($nameInput.value.trim() === '') {
        alert('성명을 입력해 주세요.');
        $nameInput.focus();
        return;
    }
    if (!/^[가-힣]{2,5}$/.test($nameInput.value.trim())) {
        alert('올바른 성명을 입력해 주세요. (한글 2~5자)');
        $nameInput.focus();
        $nameInput.select();
        return;
    }

    // 생년월일
    if ($birthInput.value === '') {
        alert('생년월일을 선택해 주세요.');
        $birthInput.focus();
        return;
    }

    // 성별
    if ($genderSelect.value === '-1') {
        alert('성별을 선택해 주세요.');
        $genderSelect.focus();
        return;
    }

    // 아이디
    if ($userIdInput.value.trim() === '') {
        alert('아이디를 입력해 주세요.');
        $userIdInput.focus();
        return;
    }
    if (!/^[\da-zA-Z]{4,20}$/.test($userIdInput.value.trim())) {
        alert('올바른 아이디를 입력해 주세요. (영문, 숫자 4~20자)');
        $userIdInput.focus();
        $userIdInput.select();
        return;
    }

    // 비밀번호
    if ($passwordInput.value === '') {
        alert('비밀번호를 입력해 주세요.');
        $passwordInput.focus();
        return;
    }
    if (!/^[\da-zA-Z`~!@#$%^&*()\-_=+\[{\]}\\|;:'",<.>\/?]{6,50}$/.test($passwordInput.value)) {
        alert('올바른 비밀번호를 입력해 주세요. (6~50자)');
        $passwordInput.focus();
        $passwordInput.select();
        return;
    }

    // 비밀번호 확인
    if ($passwordCheckInput.value === '') {
        alert('비밀번호를 한 번 더 입력해 주세요.');
        $passwordCheckInput.focus();
        return;
    }
    if ($passwordCheckInput.value !== $passwordInput.value) {
        alert('입력한 비밀번호가 서로 일치하지 않습니다.');
        $passwordCheckInput.focus();
        $passwordCheckInput.select();
        return;
    }

    // 휴대번호
    if ($mobileSecond.value === '' || $mobileThird.value === '') {
        alert('휴대번호를 입력해 주세요.');
        $mobileSecond.focus();
        return;
    }
    if (!/^\d{3,4}$/.test($mobileSecond.value) || !/^\d{4}$/.test($mobileThird.value)) {
        alert('올바른 휴대번호를 입력해 주세요.');
        $mobileSecond.focus();
        return;
    }

    // 주소
    if ($addressPrimary.value === '') {
        alert('주소 찾기 버튼을 클릭하여 주소를 입력해 주세요.');
        return;
    }

    // // 서버 전송
    const formData = new FormData();
    formData.append('name',            $nameInput.value.trim());
    formData.append('birth',           $birthInput.value);
    formData.append('gender',          $genderSelect.value);
    formData.append('userId',          $userIdInput.value.trim());
    formData.append('password',        $passwordInput.value);
    formData.append('mobileFirst',     $registerForm['mobileFirst'].value);
    formData.append('mobileSecond',    $mobileSecond.value);
    formData.append('mobileThird',     $mobileThird.value);
    formData.append('addressPrimary',  $addressPrimary.value);
    formData.append('addressSecondary', $registerForm['addressSecondary'].value);
    formData.append('termMarketingAgreed', $registerForm['termMarketingCheck'].checked);
    formData.append('email',     $registerForm['email'].value.trim());
    formData.append('code', $registerForm['emailCode'].value.trim());
    formData.append('salt', emailSalt);

    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) return;
        if (xhr.status < 200 || xhr.status >= 400) {
            alert(`요청을 전송하는 도중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요. (${xhr.status})`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        switch (response.result) {
            case 'SUCCESS':
                setStep(++currentStep);
                break;
            case 'FAILURE_ID_DUPLICATE':
                alert(`입력하신 아이디(${$userIdInput.value})는 이미 사용 중입니다. 다른 아이디를 사용해 주세요.`);
                $userIdInput.focus();
                $userIdInput.select();
                break;
            case 'FAILURE':
            default:
                alert('알 수 없는 이유로 회원가입에 실패하였습니다. 잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('POST', '/register');
    xhr.send(formData);
};

const handleComplete = () => {
    location.href = '/login';
};

// 아이디 중복 확인
$registerForm['userIdCheckButton'].addEventListener('click', () => {
    const $userIdInput = $registerForm['userId'];
    if ($userIdInput.value.trim() === '') {
        alert('아이디를 입력해 주세요.');
        $userIdInput.focus();
        return;
    }
    if (!/^[\da-zA-Z]{4,20}$/.test($userIdInput.value.trim())) {
        alert('올바른 아이디를 입력해 주세요. (영문, 숫자 4~20자)');
        $userIdInput.focus();
        $userIdInput.select();
        return;
    }
    // 일단 alert으로만 처리 - 서버 연동 시 xhr 추가
    alert(`입력하신 아이디(${$userIdInput.value})는 사용 가능합니다.`);
});

// 이메일 인증번호 전송
$registerForm['emailCodeSendButton'].addEventListener('click', () => {
    const $emailInput = $registerForm['email'];
    if ($emailInput.value.trim() === '') {
        alert('이메일을 입력해 주세요.');
        $emailInput.focus();
        return;
    }

    const formData = new FormData();
    formData.append('email', $emailInput.value.trim());

    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) return;
        if (xhr.status < 200 || xhr.status >= 400) {
            alert(`오류가 발생했습니다. 잠시 후 다시 시도해 주세요. (${xhr.status})`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        if (response.result === 'SUCCESS') {
            emailSalt = response.salt;
            $registerForm['emailCode'].disabled = false;
            $registerForm['emailCodeVerifyButton'].disabled = false;
            alert('인증번호가 전송되었습니다. 이메일을 확인해 주세요.');
        } else {
            alert('인증번호 전송에 실패했습니다. 잠시 후 다시 시도해 주세요.');
        }
    };
    xhr.open('POST', '/register/email');
    xhr.send(formData);
});

// 이메일 인증번호 확인
$registerForm['emailCodeVerifyButton'].addEventListener('click', () => {
    const $emailInput = $registerForm['email'];
    const $emailCode  = $registerForm['emailCode'];

    if ($emailCode.value.trim() === '') {
        alert('인증번호를 입력해 주세요.');
        $emailCode.focus();
        return;
    }
    if (emailSalt === '') {
        alert('인증번호 전송 버튼을 클릭해 주세요.');
        return;
    }

    const formData = new FormData();
    formData.append('email', $emailInput.value.trim());
    formData.append('code',  $emailCode.value.trim());
    formData.append('salt',  emailSalt);

    const xhr = new XMLHttpRequest();
    xhr.onreadystatechange = () => {
        if (xhr.readyState !== XMLHttpRequest.DONE) return;
        if (xhr.status < 200 || xhr.status >= 400) {
            alert(`오류가 발생했습니다. 잠시 후 다시 시도해 주세요. (${xhr.status})`);
            return;
        }
        const response = JSON.parse(xhr.responseText);
        if (response.result === 'SUCCESS') {
            isEmailVerified = true;
            $registerForm['emailSalt'].value = emailSalt;
            alert('이메일 인증이 완료되었습니다.');
        } else {
            alert('인증번호가 올바르지 않거나 만료되었습니다. 다시 시도해 주세요.');
        }
    };
    xhr.open('PATCH', '/register/email');
    xhr.send(formData);
});

// 주소 찾기 (다음 우편번호 API)
$registerForm['addressFindButton'].addEventListener('click', () => {
    new daum.Postcode({
        oncomplete: (data) => {
            $registerForm['addressPrimary'].value = data['roadAddress'];
            $registerForm['addressSecondary'].focus();
        }
    }).open();
});

// 폼 submit (다음 버튼)
let currentStep = 1;
let emailSalt = '';
let isEmailVerified = false;


$registerForm.addEventListener('submit', (e) => {
    e.preventDefault();
    ({
        1: handleTerm,
        2: handleVerification,
        3: handleInformation,
        4: handleComplete,
    }[currentStep] ?? null)?.();
});

// 초기화
setStep(1);
