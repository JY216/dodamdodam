document.addEventListener('DOMContentLoaded', () => {
    const bookGrid = document.getElementById('bookGrid');
    const loading = document.getElementById('loading');
    const tabBtns = document.querySelectorAll('.tab-btn');
    const categoryFilter = document.getElementById('categoryFilter');
    const filterSearchBtn = document.querySelector('.filter-search-btn');

    let currentQueryType = 'Bestseller';

    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            tabBtns.forEach(b => b.classList.remove('tab-btn--active'));
            btn.classList.add('tab-btn--active');

            switch (btn.dataset.tab) {
                case 'bestseller':
                    currentQueryType = 'Bestseller';
                    break;
                case 'popular':
                    currentQueryType = 'BlogBest';
                    break;
            }
            fetchBooks();
        });
    });

    filterSearchBtn.addEventListener('click', fetchBooks);

    async function fetchBooks() {
        loading.style.display = 'flex';
        bookGrid.innerHTML = '';
        bookGrid.appendChild(loading);

        const categoryId = categoryFilter.value || 0;

        try {
            const response = await fetch(
                `/api/bestseller?queryType=${currentQueryType}&categoryId=${categoryId}&maxResults=20`
            );
            const data = await response.json();

            loading.style.display = 'none';

            if (data.result === 'SUCCESS' && data.books.length > 0) {
                data.books.forEach(book => {
                    bookGrid.appendChild(createBookCard(book));
                });
            } else {
                bookGrid.innerHTML = `
                    <div class="empty-result">
                        <p>도서 정보를 불러올 수 없어요.</p>
                    </div>`;
            }
        } catch (e) {
            loading.style.display = 'none';
            bookGrid.innerHTML = `
                <div class="empty-result">
                    <p>오류가 발생했어요. 잠시 후 다시 시도해주세요.</p>
                </div>`;
        }
    }

    function createBookCard(book) {
        const card = document.createElement('div');
        card.className = 'book-card';
        card.innerHTML = `
            <div class="book-card-cover">
                <img src="${book.cover}" alt="${book.title}" onerror="this.style.display='none'">
            </div>
            <div class="book-card-body">
                <p class="book-card-rank">${book.rank}위</p>
                <h3 class="book-card-title">${book.title}</h3>
                <p class="book-card-author">${book.author}</p>
                <p class="book-card-publisher">${book.publisher}</p>
            </div>
        `;
        card.addEventListener('click', () => {
            window.open(book.link, '_blank');
        });
        return card;
    }

    fetchBooks();
});