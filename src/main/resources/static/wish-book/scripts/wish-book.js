document.addEventListener('DOMContentLoaded', () => {
    const directInput = document.getElementById('directInput');
    const wishTitle = document.getElementById('wishTitle');
    const author = document.getElementById('author');
    const publisher = document.getElementById('publisher');
    const publishYear = document.getElementById('publishYear');
    const isbn = document.getElementById('isbn');
    const price = document.getElementById('price');
    const bookSearchBtn = document.getElementById('bookSearchBtn');

    // 검색 결과 드롭다운 생성
    const dropdown = document.createElement('div');
    dropdown.className = 'search-dropdown';
    dropdown.style.display = 'none';
    wishTitle.parentElement.style.position = 'relative';
    wishTitle.parentElement.appendChild(dropdown);

    // 직접 입력 체크박스
    directInput.addEventListener('change', () => {
        const isDirect = directInput.checked;
        [author, publisher, publishYear, isbn, price].forEach(input => {
            input.disabled = isDirect ? false : false;
            input.value = '';
        });
        wishTitle.value = '';
        bookSearchBtn.disabled = isDirect;
        bookSearchBtn.style.opacity = isDirect ? '0.5' : '1';
        dropdown.style.display = 'none';
    });

    // 도서 검색 버튼
    bookSearchBtn.addEventListener('click', async () => {
        const title = wishTitle.value.trim();
        if (!title) {
            alert('희망 도서명을 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`/wish-book/search?keyword=${encodeURIComponent(title)}`);
            const data = await response.json();

            if (data.result === 'SUCCESS' && data.books.length > 0) {
                showDropdown(data.books);
            } else {
                alert('도서 정보를 찾을 수 없어요. 직접 입력해주세요.');
                directInput.checked = true;
            }
        } catch (e) {
            alert('오류가 발생했어요. 직접 입력해주세요.');
        }
    });

    // 드롭다운 표시
    function showDropdown(books) {
        dropdown.innerHTML = '';
        books.forEach(book => {
            const item = document.createElement('div');
            item.className = 'search-dropdown-item';
            item.innerHTML = `
                <span class="dropdown-title">${book.title}</span>
                <span class="dropdown-meta">${book.author} · ${book.publisher}</span>
            `;
            item.addEventListener('click', () => {
                wishTitle.value = book.title;
                author.value = book.author || '';
                publisher.value = book.publisher || '';
                publishYear.value = book.publishYear || '';
                isbn.value = book.isbn || '';
                price.value = book.price || '';
                dropdown.style.display = 'none';
            });
            dropdown.appendChild(item);
        });
        dropdown.style.display = 'block';
    }

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', (e) => {
        if (!dropdown.contains(e.target) && e.target !== bookSearchBtn) {
            dropdown.style.display = 'none';
        }
    });

    // 엔터 키 제출 방지
    document.querySelector('.wish-form').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            // 도서명 입력창에서 엔터 치면 도서 검색 실행
            if (e.target === wishTitle) {
                bookSearchBtn.click();
            }
        }
    });
});