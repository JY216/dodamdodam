window.addEventListener('pageshow', () => {
    const overlay = document.getElementById('dialogOverlay');
    if (overlay) overlay.classList.remove('active');
});

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('posterFile').addEventListener('change', function() {
        const fileName = this.files[0] ? this.files[0].name : '선택된 파일 없음';
        document.getElementById('fileNameDisplay').value = fileName;
    });

    const form = document.querySelector('form[action="/admin/events/register"]');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();

            const title = form.querySelector('input[name="title"]').value.trim();
            const description = form.querySelector('textarea[name="description"]').value.trim();
            const target = form.querySelector('input[name="target"]').value.trim();
            const capacity = form.querySelector('input[name="capacity"]').value.trim();
            const eventStartAt = form.querySelector('input[name="eventStartAt"]').value;
            const eventEndAt = form.querySelector('input[name="eventEndAt"]').value;
            const applyStartAt = form.querySelector('input[name="applyStartAt"]').value;
            const applyEndAt = form.querySelector('input[name="applyEndAt"]').value;

            if (!title) { dialog.alert('행사 제목을 입력해주세요.'); return; }
            if (!description) { dialog.alert('상세 내용을 입력해주세요.'); return; }
            if (!target) { dialog.alert('신청 대상을 입력해주세요.'); return; }
            if (!capacity) { dialog.alert('모집 인원을 입력해주세요.'); return; }
            if (!eventStartAt) { dialog.alert('교육 시작일을 입력해주세요.'); return; }
            if (!eventEndAt) { dialog.alert('교육 종료일을 입력해주세요.'); return; }
            if (!applyStartAt) { dialog.alert('접수 시작일을 입력해주세요.'); return; }
            if (!applyEndAt) { dialog.alert('접수 종료일을 입력해주세요.'); return; }

            dialog.confirm('행사를 등록하시겠어요?', () => {
                form.submit();
            });
        });
    }
});