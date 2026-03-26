document.addEventListener('DOMContentLoaded', () => {
    const bookGrid = document.getElementById('bookGrid');
    const loading = document.getElementById('loading');
    const tabBtns = document.querySelectorAll('.tab-btn');
    const categoryFilter = document.getElementById('categoryFilter');
    const filterSearchBtn = document.querySelector('.filter-search-btn');

    let currentQueryType = 'Bestseller';
    let currentPage = 1;
    const pageSize = 12;

    // 탭 클릭
    tabBtns.forEach(btn => {
        btn.addEventListener('click', () => {
            tabBtns.forEach(b => b.classList.remove('tab-btn--active'));
            btn.classList.add('tab-btn--active');
            switch (btn.dataset.tab) {
                case 'bestseller': currentQueryType = 'Bestseller'; break;
                case 'popular': currentQueryType = 'BlogBest'; break;
            }
            currentPage = 1;
            fetchBooks();
        });
    });

    filterSearchBtn.addEventListener('click', () => {
        currentPage = 1;
        fetchBooks();
    });

    async function fetchBooks() {
        loading.style.display = 'flex';
        bookGrid.innerHTML = '';
        bookGrid.appendChild(loading);

        const categoryId = categoryFilter.value || 0;

        try {
            const response = await fetch(
                `/api/bestseller?queryType=${currentQueryType}&categoryId=${categoryId}&maxResults=${pageSize}&page=${currentPage}`
            );
            const data = await response.json();

            loading.style.display = 'none';

            if (data.result === 'SUCCESS' && data.books.length > 0) {
                data.books.forEach(book => bookGrid.appendChild(createBookCard(book)));
                renderPagination(data.totalCount);
            } else {
                bookGrid.innerHTML = `<div class="empty-result"><p>도서 정보를 불러올 수 없어요.</p></div>`;
            }
        } catch (e) {
            loading.style.display = 'none';
            bookGrid.innerHTML = `<div class="empty-result"><p>오류가 발생했어요.</p></div>`;
        }
    }

    function renderPagination(totalCount) {
        const existing = document.getElementById('pagination');
        if (existing) existing.remove();

        const maxPage = Math.ceil(totalCount / pageSize);
        if (maxPage <= 1) return;

        const pagination = document.createElement('div');
        pagination.id = 'pagination';
        pagination.className = 'pagination';

        const blockSize = 5;
        const currentBlock = Math.ceil(currentPage / blockSize);
        const startPage = (currentBlock - 1) * blockSize + 1;
        const endPage = Math.min(startPage + blockSize - 1, maxPage);

        // 이전
        if (currentPage > 1) {
            pagination.innerHTML += `<div class="page-group">
                <a class="page-link" onclick="goPage(1)">&lt;&lt;</a>
                <a class="page-link" onclick="goPage(${currentPage - 1})">&lt;</a>
            </div>`;
        }

        // 번호
        let nums = '<div class="page-group">';
        for (let i = startPage; i <= endPage; i++) {
            nums += `<a class="page-link ${i === currentPage ? 'page-link--active' : ''}" onclick="goPage(${i})">${i}</a>`;
        }
        nums += '</div>';
        pagination.innerHTML += nums;

        // 다음
        if (currentPage < maxPage) {
            pagination.innerHTML += `<div class="page-group">
                <a class="page-link" onclick="goPage(${currentPage + 1})">&gt;</a>
                <a class="page-link" onclick="goPage(${maxPage})">&gt;&gt;</a>
            </div>`;
        }

        bookGrid.insertAdjacentElement('afterend', pagination);
    }

    window.goPage = function(page) {
        currentPage = page;
        fetchBooks();
    };

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
        card.addEventListener('click', () => window.open(book.link, '_blank'));
        return card;
    }

    fetchBooks();
});