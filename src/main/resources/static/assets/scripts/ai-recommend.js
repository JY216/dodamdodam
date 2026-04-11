document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('ai-recommend-container');
    if (!container) return;

    // 로딩 상태 표시
    container.innerHTML = `
        <div class="ai-loading">
            <div class="ai-loading-spinner"></div>
            <p>도담 매니저가 추천 도서를 준비하고 있어요...</p>
        </div>
    `;

    fetch('/ai/recommend')
        .then(res => res.text())
        .then(text => {
            let books;
            try {
                books = JSON.parse(text);
            } catch (e) {
                throw new Error('파싱 실패');
            }

            if (!Array.isArray(books) || books.length === 0) {
                throw new Error('빈 응답');
            }

            const isLoggedIn = container.dataset.loggedIn === 'true';
            const hasHistory = container.dataset.hasHistory === 'true';

            let headerText = '도담 매니저의 추천 도서';
            let subText    = '다양한 장르에서 골라봤어요';

            if (isLoggedIn && hasHistory) {
                headerText = '회원님을 위한 맞춤 추천';
                subText    = '대출 기록을 분석해 추천해드려요';
            }

            container.innerHTML = `
                <div class="ai-recommend-header">
                    <div class="ai-badge">AI 추천</div>
                    <h2 class="ai-recommend-title">${headerText}</h2>
                    <p class="ai-recommend-sub">${subText}</p>
                </div>
                <div class="ai-book-list">
                    ${books.map(book => `
                        <div class="ai-book-card">
                            <div class="ai-book-cover">
                                ${book.coverUrl
                                ? `<img src="${book.coverUrl}" alt="${book.title}" style="width:100%;height:100%;object-fit:cover;border-radius:0.25rem;" />`
                                : `<span>${book.title.charAt(0)}</span>`
                                }
                            </div>
                            <div class="ai-book-info">
                                <p class="ai-book-title">${book.title}</p>
                                <p class="ai-book-author">${book.author}</p>
                                <p class="ai-book-reason">${book.reason}</p>
                            </div>
                        </div>
                    `).join('')}
            `;
        })
        .catch(() => {
            container.innerHTML = `
                <div class="ai-error">
                    <p>추천 도서를 불러오지 못했어요. 잠시 후 다시 시도해주세요.</p>
                </div>
            `;
        });
});