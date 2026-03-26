document.querySelectorAll('.btn-like').forEach(btn => {
    btn.addEventListener('click', async () => {
        const bookId = btn.dataset.bookId;
        try {
            const res = await fetch(`/books/like?bookId=${bookId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            if (res.status === 401) {
                alert('로그인이 필요해요.');
                return;
            }
            const data = await res.json();
            if (data.liked) {
                btn.classList.add('btn-like--active');
            } else {
                btn.classList.remove('btn-like--active');
            }
        } catch (e) {
            console.error(e);
        }
    });
});