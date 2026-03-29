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