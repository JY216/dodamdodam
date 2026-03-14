const chatArea = document.getElementById('chatArea');
const msgInput = document.getElementById('msgInput');

// 엔터키로 전송
msgInput.addEventListener('keydown', e => {
    if (e.key === 'Enter') sendMessage();
});

// 빠른 선택 칩 클릭
function sendChip(text) {
    document.getElementById('quickChips').style.display = 'none';
    appendUserMsg(text);
    fetchBotReply(text);
}

// 전송 버튼 클릭
function sendMessage() {
    const text = msgInput.value.trim();
    if (!text) return;
    msgInput.value = '';
    appendUserMsg(text);
    fetchBotReply(text);
}

// 사용자 메시지 추가
function appendUserMsg(text) {
    const div = document.createElement('div');
    div.className = 'msg user';
    div.innerHTML = `
    <div class="avatar user-av">나</div>
    <div class="bubble user">${escHtml(text)}</div>
  `;
    chatArea.appendChild(div);
    scrollBottom();
}

// 봇 메시지 추가
function appendBotMsg(html) {
    const div = document.createElement('div');
    div.className = 'msg';
    div.innerHTML = `
    <div class="avatar">봇</div>
    <div>${html}</div>
  `;
    chatArea.appendChild(div);
    scrollBottom();
}

// 입력 중 표시
function appendTyping() {
    const div = document.createElement('div');
    div.className = 'msg';
    div.id = 'typingIndicator';
    div.innerHTML = `
    <div class="avatar">봇</div>
    <div class="bubble bot" style="color:#aaa; font-size:0.8125rem;">입력 중...</div>
  `;
    chatArea.appendChild(div);
    scrollBottom();
    return div;
}

// Gemini API 호출 (/api/gemini/chat 경로에 맞게 수정)
async function fetchBotReply(userMsg) {
    const typing = appendTyping();
    try {
        const res = await fetch('/ai/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message: userMsg })
        });
        const data = await res.json();
        typing.remove();
        const formatted = parseMarkdown(data.reply || '응답을 받지 못했어요.');
        appendBotMsg(`<div class="bubble bot">${formatted}</div>`);
    } catch (e) {
        typing.remove();
        appendBotMsg(`<div class="bubble bot">서버에 연결할 수 없어요. 잠시 후 다시 시도해 주세요.</div>`);
    }
}

// 스크롤 맨 아래로
function scrollBottom() {
    chatArea.scrollTop = chatArea.scrollHeight;
}

// XSS 방지용 HTML 이스케이프 (사용자 입력에만 사용)
function escHtml(str) {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;');
}

// Gemini 응답 마크다운 간단 파싱
function parseMarkdown(str) {
    return escHtml(str)
        .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')  // **굵게**
        .replace(/\*(.+?)\*/g, '<em>$1</em>')               // *기울임*
        .replace(/\n/g, '<br>');                             // 줄바꿈
}