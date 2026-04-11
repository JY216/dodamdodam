document.addEventListener('DOMContentLoaded', function () {

    // ── 상태 변경 버튼 ─────────────────────────────────────────────
    document.querySelectorAll('.js-status-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const id       = btn.dataset.id;
            const title    = btn.dataset.title;
            const status   = btn.dataset.status;
            const page     = btn.dataset.page;
            const isOpen   = status === 'OPEN';
            const next     = isOpen ? 'CLOSED' : 'OPEN';

            document.getElementById('statusModalTitle').textContent =
                isOpen ? '행사 마감' : '행사 재개';
            document.getElementById('statusModalDesc').textContent =
                '"' + title + '" 행사를 ' + (isOpen ? '마감' : '재개') + '하시겠습니까?';
            document.getElementById('statusInput').value    = next;
            document.getElementById('statusPageInput').value = page;
            document.getElementById('statusConfirmBtn').className =
                'modal-btn-confirm ' + (isOpen ? 'danger' : 'confirm');
            document.getElementById('statusForm').action =
                '/admin/events/' + id + '/status';
            document.getElementById('statusModal').classList.add('active');
        });
    });

    // ── 삭제 버튼 ──────────────────────────────────────────────────
    document.querySelectorAll('.js-delete-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const id    = btn.dataset.id;
            const title = btn.dataset.title;
            const page  = btn.dataset.page;

            document.getElementById('deleteModalDesc').textContent =
                '"' + title + '" 행사를 삭제하시겠습니까? 이 작업은 되돌릴 수 없어요.';
            document.getElementById('deletePageInput').value = page;
            document.getElementById('deleteForm').action =
                '/admin/events/' + id + '/delete';
            document.getElementById('deleteModal').classList.add('active');
        });
    });

    // ── 모달 닫기 ──────────────────────────────────────────────────
    document.getElementById('statusCancelBtn').addEventListener('click', closeStatusModal);
    document.getElementById('deleteCancelBtn').addEventListener('click', closeDeleteModal);

    document.getElementById('statusModal').addEventListener('click', function (e) {
        if (e.target === this) closeStatusModal();
    });
    document.getElementById('deleteModal').addEventListener('click', function (e) {
        if (e.target === this) closeDeleteModal();
    });

    function closeStatusModal() {
        document.getElementById('statusModal').classList.remove('active');
    }

    function closeDeleteModal() {
        document.getElementById('deleteModal').classList.remove('active');
    }
});