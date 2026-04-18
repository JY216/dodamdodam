document.addEventListener('DOMContentLoaded', () => {
    // 햄버거 메뉴
    const $hamburger = document.getElementById('navHamburger');
    const $mobileMenu = document.getElementById('navMobileMenu');

    if ($hamburger && $mobileMenu) {
        $hamburger.addEventListener('click', () => {
            $hamburger.classList.toggle('active');
            $mobileMenu.classList.toggle('active');
        });
    }

    // 공통 다이얼로그
    const $overlay = document.getElementById('dialogOverlay');
    const $message = document.getElementById('dialogMessage');
    const $actions = document.getElementById('dialogActions');

    window.dialog = {
        confirm: (message, onConfirm, onCancel) => {
            $message.textContent = message;
            $actions.innerHTML = `
                <button class="dialog-btn dialog-btn--cancel" id="dialogCancel">취소</button>
                <button class="dialog-btn dialog-btn--confirm" id="dialogConfirm">확인</button>
            `;
            $overlay.classList.add('active');

            document.getElementById('dialogConfirm').addEventListener('click', () => {
                $overlay.classList.remove('active');
                if (onConfirm) onConfirm();
            });
            document.getElementById('dialogCancel').addEventListener('click', () => {
                $overlay.classList.remove('active');
                if (onCancel) onCancel();
            });
        },
        alert: (message, onClose) => {
            $message.textContent = message;
            $actions.innerHTML = `
                <button class="dialog-btn dialog-btn--alert" id="dialogClose">확인</button>
            `;
            $overlay.classList.add('active');

            document.getElementById('dialogClose').addEventListener('click', () => {
                $overlay.classList.remove('active');
                if (onClose) onClose();
            });
        }
    };
});