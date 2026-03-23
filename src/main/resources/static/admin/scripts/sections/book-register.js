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
                    width: { min: 1920 },   // ← 해상도 높이기
                    height: { min: 1080 }
                }
            },
            locator: {
                patchSize: "medium",       // ← 인식 영역 크기
                halfSample: true
            },
            numOfWorkers: 2,               // ← 워커 수 늘리기
            frequency: 10,                 // ← 초당 인식 횟수
            decoder: {
                readers: ["ean_reader"],
                multiple: false
            },
            locate: true                   // ← 바코드 위치 자동 탐색
        }, (err) => {
            if (err) {
                alert('카메라를 시작할 수 없습니다. 카메라 권한을 확인해주세요.');
                cameraArea.classList.remove('is-active');
                return;
            }
            Quagga.start();
        });

        // 같은 바코드 연속 인식 방지
        let lastResult = null;
        Quagga.onDetected((result) => {
            const isbn = result.codeResult.code;
            if (isbn === lastResult) return; // 중복 방지
            lastResult = isbn;

            isbnInput.value = isbn;
            stopScan();
            alert(`ISBN 인식 완료: ${isbn}`);
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
            alert('ISBN을 입력해주세요.');
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
                alert('도서 정보를 불러왔어요!');
            } else {
                alert('도서 정보를 찾을 수 없어요. ISBN을 확인해주세요.');
            }
        } catch (e) {
            alert('오류가 발생했습니다.');
        }
    });
});