document.addEventListener('DOMContentLoaded', () => {
    const directInput = document.getElementById('directInput');
    const wishTitle = document.getElementById('wishTitle');
    const author = document.getElementById('author');
    const publisher = document.getElementById('publisher');
    const publishYear = document.getElementById('publishYear');
    const isbn = document.getElementById('isbn');
    const price = document.getElementById('price');
    const bookSearchBtn = document.getElementById('bookSearchBtn');

    const dropdown = document.createElement('div');
    dropdown.className = 'search-dropdown';
    dropdown.style.display = 'none';
    wishTitle.parentElement.style.position = 'relative';
    wishTitle.parentElement.appendChild(dropdown);

    directInput.addEventListener('change', () => {
        const isDirect = directInput.checked;
        [author, publisher, publishYear, isbn, price].forEach(input => {
            input.value = '';
        });
        wishTitle.value = '';
        bookSearchBtn.disabled = isDirect;
        bookSearchBtn.style.opacity = isDirect ? '0.5' : '1';
        dropdown.style.display = 'none';
    });

    bookSearchBtn.addEventListener('click', async () => {
        const title = wishTitle.value.trim();
        if (!title) {
            dialog.alert('희망 도서명을 입력해주세요.');
            return;
        }
        try {
            const response = await fetch(`/wish-book/search?keyword=${encodeURIComponent(title)}`);
            const data = await response.json();
            if (data.result === 'SUCCESS' && data.books.length > 0) {
                showDropdown(data.books);
            } else {
                dialog.alert('도서 정보를 찾을 수 없어요. 직접 입력해주세요.', () => {
                    directInput.checked = true;
                });
            }
        } catch (e) {
            dialog.alert('오류가 발생했어요. 직접 입력해주세요.', () => {
                directInput.checked = true;
            });
        }
    });

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

    document.addEventListener('click', (e) => {
        if (!dropdown.contains(e.target) && e.target !== bookSearchBtn) {
            dropdown.style.display = 'none';
        }
    });

    document.querySelector('.wish-form').addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            if (e.target === wishTitle) bookSearchBtn.click();
        }
    });

    document.querySelector('.wish-form').addEventListener('submit', (e) => {
        const title = wishTitle.value.trim();
        if (!title) {
            e.preventDefault();
            dialog.alert('희망 도서명을 입력해주세요.');
        }
    });

    const flash = document.getElementById('flashData');
    if (flash) {
        const success = flash.dataset.success;
        const error = flash.dataset.error;
        if (success) dialog.alert(success);
        else if (error) dialog.alert(error);
    }
});