document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('posterFile').addEventListener('change', function() {
        const fileName = this.files[0] ? this.files[0].name : '선택된 파일 없음';
        document.getElementById('fileNameDisplay').value = fileName;
    });
});