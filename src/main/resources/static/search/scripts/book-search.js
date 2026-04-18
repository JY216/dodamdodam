// 하트 버튼 로직
document.querySelectorAll('.btn-like:not(.btn-like--api)').forEach(btn => {
    btn.addEventListener('click', async () => {
        const bookId = btn.dataset.bookId;
        try {
            const res = await fetch(`/books/like?bookId=${bookId}`, {
                method: 'POST'
            });
            if (res.status === 401) { alert('로그인이 필요해요.'); return; }
            const data = await res.json();
            if (data.liked) btn.classList.add('btn-like--active');
            else btn.classList.remove('btn-like--active');
        } catch (e) { console.error(e); }
    });
});

// API 책 자동 저장 함수
async function ensureBook(btn) {
    const formData = new FormData();
    formData.append('isbn', btn.dataset.isbn);
    formData.append('title', btn.dataset.title);
    formData.append('author', btn.dataset.author);
    formData.append('publisher', btn.dataset.publisher);
    formData.append('coverImage', btn.dataset.cover || '');

    const res = await fetch('/books/ensure', {
        method: 'POST',
        body: formData
    });
    const data = await res.json();
    return data.bookId;
}

// API 책 예약 버튼
document.querySelectorAll('.btn-reserve--api').forEach(btn => {
    btn.addEventListener('click', async () => {
        try {
            const bookId = await ensureBook(btn);
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/reserve';
            form.innerHTML = `
                <input type="hidden" name="bookId" value="${bookId}">
                <input type="hidden" name="redirect" value="/search">
            `;
            document.body.appendChild(form);
            form.submit();
        } catch (e) { alert('오류가 발생했습니다.'); }
    });
});

// API 책 찜 버튼
document.querySelectorAll('.btn-like--api').forEach(btn => {
    btn.addEventListener('click', async () => {
        try {
            const bookId = await ensureBook(btn);
            const res = await fetch(`/books/like?bookId=${bookId}`, {
                method: 'POST'
            });
            if (res.status === 401) { alert('로그인이 필요해요.'); return; }
            const data = await res.json();
            if (data.liked) btn.classList.add('btn-like--active');
            else btn.classList.remove('btn-like--active');
        } catch (e) { alert('오류가 발생했습니다.'); }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const flash = document.getElementById('flashData');
    if (flash) {
        const success = flash.dataset.success;
        const error = flash.dataset.error;
        if (success) dialog.alert(success);
        else if (error) dialog.alert(error);
    }
});

// 책 목록 클릭 시 상세 페이지 이동
document.querySelectorAll('.book-item').forEach(function (item) {
    item.addEventListener('click', function (e) {
        // 버튼 클릭 시엔 이동 안 함
        if (e.target.closest('button') || e.target.closest('a') || e.target.closest('form')) return;
        const url = item.dataset.url;
        if (url) location.href = url;
    });
});

document.querySelectorAll('.book-item').forEach(function (item) {
    item.addEventListener('click', function (e) {
        if (e.target.closest('button') || e.target.closest('a') || e.target.closest('form')) return;

        const url = item.dataset.url;
        if (url) {
            // DB에 있는 책
            location.href = url;
        } else {
            // API에만 있는 책 → ensureBook 후 이동
            const reserveBtn = item.querySelector('.btn-reserve--api');
            if (!reserveBtn) return;
            ensureBook(reserveBtn).then(function (bookId) {
                location.href = '/books/' + bookId;
            }).catch(function () {});
        }
    });
});

// 하트 이미지 처리
document.querySelectorAll('.btn-like').forEach(btn => {
    const img = btn.querySelector('.like-img');

    btn.addEventListener('mouseenter', () => {
        img.src = '/user/images/mypage/likes-hover.png';
    });

    btn.addEventListener('mouseleave', () => {
        if (btn.classList.contains('btn-like--active')) {
            img.src = '/user/images/mypage/likes-active.png';
        } else {
            img.src = '/user/images/mypage/likes.png';
        }
    });
});