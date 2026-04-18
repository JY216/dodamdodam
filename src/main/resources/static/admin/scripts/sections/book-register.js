document.addEventListener('DOMContentLoaded', () => {
    const scanBtn = document.getElementById('scanBtn');
    const stopScanBtn = document.getElementById('stopScanBtn');
    const cameraArea = document.getElementById('cameraArea');
    const isbnInput = document.getElementById('isbn');
    const searchBtn = document.getElementById('isbnSearchBtn');

    scanBtn.addEventListener('click', () => {
        cameraArea.classList.add('is-active');

        Quagga.init({
            inputStream: {
                type: "LiveStream",
                target: document.querySelector("#camera"),
                constraints: {
                    facingMode: "environment",
                    width: { min: 640 },
                    height: { min: 480 }
                }
            },
            locator: {
                patchSize: "large",
                halfSample: false
            },
            numOfWorkers: 2,
            frequency: 10,
            decoder: {
                readers: ["ean_reader"],
                multiple: false
            },
            locate: true
        }, (err) => {
            if (err) {
                console.error('Quagga 에러:', err);
                dialog.alert('카메라를 시작할 수 없습니다. 카메라 권한을 확인해주세요.');
                cameraArea.classList.remove('is-active');
                return;
            }
            Quagga.start();
        });

        let lastResult = null;
        Quagga.onDetected((result) => {
            const isbn = result.codeResult.code;
            if (isbn === lastResult) return;
            lastResult = isbn;

            isbnInput.value = isbn;
            stopScan();
            dialog.alert(`ISBN 인식 완료: ${isbn}`);
        });
    });

    stopScanBtn.addEventListener('click', stopScan);

    function stopScan() {
        Quagga.stop();
        cameraArea.classList.remove('is-active');
    }

    searchBtn.addEventListener('click', async () => {
        const isbn = isbnInput.value.trim();
        if (!isbn) {
            dialog.alert('ISBN을 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`/admin/books/search-isbn?isbn=${isbn}`);
            const data = await response.json();

            if (data.result === 'SUCCESS') {
                document.getElementById('title').value = data.title;
                document.getElementById('author').value = data.author;
                document.getElementById('publisher').value = data.publisher;
                document.getElementById('publishDate').value = data.publishDate;
                document.getElementById('coverImage').value = data.coverImage;
                dialog.alert('도서 정보를 불러왔어요!');
            } else {
                dialog.alert('도서 정보를 찾을 수 없어요. ISBN을 확인해주세요.');
            }
        } catch (e) {
            dialog.alert('오류가 발생했습니다.');
        }
    });

    // 등록하기 다이얼로그
    document.querySelector('.register-form').addEventListener('submit', (e) => {
        e.preventDefault();
        dialog.confirm('도서를 등록하시겠어요?', () => {
            document.querySelector('.register-form').submit();
        });
    });
});

window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});