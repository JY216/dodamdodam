// 배너 슬라이더 (무한 루프)
const $bannerSlider = document.getElementById('bannerSlider');
const $slides = Array.from($bannerSlider.querySelectorAll('.banner-slide'));
const totalSlides = $slides.length;
let currentBanner = 0;
let isTransitioning = false;

// 첫 번째 슬라이드 복제본을 맨 뒤에 추가
const $firstClone = $slides[0].cloneNode(true);
$bannerSlider.appendChild($firstClone);

const showBanner = (index, animate = true) => {
    if (!animate) {
        $bannerSlider.style.transition = 'none';
    } else {
        $bannerSlider.style.transition = 'transform 0.6s ease';
    }
    $bannerSlider.style.transform = `translateX(-${index * 100}%)`;
    currentBanner = index;
};

// 트랜지션 끝났을 때 복제본에서 진짜 첫 번째로 순간이동
$bannerSlider.addEventListener('transitionend', () => {
    if (currentBanner === totalSlides) {
        showBanner(0, false);
    }
    isTransitioning = false;
});

// 자동 전환 (4초)
setInterval(() => {
    if (isTransitioning) return;
    isTransitioning = true;
    showBanner(currentBanner + 1);
}, 4000);

// 문화행사 슬라이더
const $programsSlider = document.getElementById('programsSlider');
const $programsTrack = document.getElementById('programsTrack');
const $prevBtn = document.querySelector('.programs-btn--prev');
const $nextBtn = document.querySelector('.programs-btn--next');

if ($programsSlider && $programsTrack && $prevBtn && $nextBtn) {
    const $cards = Array.from($programsTrack.querySelectorAll('.program-card'));

    if ($cards.length > 0) {
        let currentProgram = 0;
        const visibleCount = 3;
        const maxIndex = Math.max(0, $cards.length - visibleCount);

        const showProgram = (index) => {
            currentProgram = Math.max(0, Math.min(index, maxIndex));
            const cardWidth = $cards[0].offsetWidth + 20;
            $programsTrack.style.transform = `translateX(-${currentProgram * cardWidth}px)`;

            $prevBtn.style.opacity = currentProgram === 0 ? '0.3' : '1';
            $nextBtn.style.opacity = currentProgram >= maxIndex ? '0.3' : '1';
        };

        $prevBtn.addEventListener('click', () => showProgram(currentProgram - 1));
        $nextBtn.addEventListener('click', () => showProgram(currentProgram + 1));

        showProgram(0);
    }
}