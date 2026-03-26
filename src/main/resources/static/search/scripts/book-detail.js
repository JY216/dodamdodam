document.addEventListener('DOMContentLoaded', () => {

    // 도서 목록 -> 상세 페이지 이동
    // .book-list 가 존재하는 페이지(검색 결과)에서만 실행
    const bookList = document.querySelector('.book-list');
    if (bookList) {
        bookList.addEventListener('click', (e) => {

            if (e.target.closest('.book-action')) return;

            const item = e.target.closest('.book-item');
            if (!item) return;

            // th:each 로 렌더링된 bookId 값을 data 속성으로 읽어옴
            const bookId = item.dataset.bookId;
            if (bookId) {
                window.location.href = `/books/${bookId}`;
            }
        });
    }

    // 뒤로가기
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', () => history.back());
    }

    // 하트(찜) 토글
    const heartBtn = document.getElementById('heartBtn');
    if (heartBtn) {
        heartBtn.addEventListener('click', () => {
            const svg = heartBtn.querySelector('svg');
            heartBtn.classList.toggle('on');
            if (heartBtn.classList.contains('on')) {
                svg.setAttribute('stroke', '#e74c3c');
            } else {
                svg.setAttribute('stroke', '#aaa');
            }
        });
    }

});