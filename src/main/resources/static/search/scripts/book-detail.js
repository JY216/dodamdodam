document.addEventListener('DOMContentLoaded', function () {

    // ── 관심 도서 버튼 ─────────────────────────────────────────────
    const likeBtn = document.querySelector('.js-like-btn');
    if (likeBtn) {
        likeBtn.addEventListener('click', function () {
            const bookId  = likeBtn.dataset.id;
            const isLiked = likeBtn.dataset.liked === 'true';

            fetch('/books/like', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'bookId=' + bookId
            })
                .then(function (res) {
                    if (res.status === 401) { location.href = '/login'; return null; }
                    return res.json();
                })
                .then(function (data) {
                    if (!data) return;
                    const nowLiked = data.liked;
                    likeBtn.dataset.liked = nowLiked;
                    likeBtn.classList.toggle('active', nowLiked);
                    likeBtn.querySelector('span').textContent = nowLiked ? '관심 해제' : '관심 도서';
                })
                .catch(function () {});
        });
    }
});