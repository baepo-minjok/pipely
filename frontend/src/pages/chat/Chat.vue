<script setup>
import { useAgenticaRpc } from "@/agentica/agentica.js";
import { nextTick, onMounted, ref, watch } from "vue";
import MarkdownIt from "markdown-it";
import { userApi } from "@/api/UserApi.js";

const md = new MarkdownIt();
const { messages, conversate, isConnected, isError, tryConnect } = useAgenticaRpc();
const input = ref("");
const chatHistoryRef = ref(null);
const isLoggedIn = ref(false);

tryConnect();

function renderMarkdown(text) {
  return md.render(text || "");
}

function send() {
  if (!input.value.trim()) return;

  if (!isLoggedIn.value) {
    setTimeout(() => {
      const botMessage = {
        id: Date.now() + "_bot",
        type: "assistantMessage",
        text:
            "죄송합니다. 채팅 서비스를 이용하시려면 먼저 **로그인**해 주세요.\n\n" +
            "로그인 후 다양한 CI/CD 어시스턴트 기능을 사용하실 수 있습니다.",
        created_at: new Date().toISOString(),
      };
      messages.value.push(botMessage);
    }, 500);
    input.value = "";
    return;
  }

  // ✅ 서버에 conversate 요청 (처리중 메시지는 서버에서 내려줌)
  conversate(input.value);
  input.value = "";
}


// Auto scroll to bottom
watch(
    messages,
    () => {
      nextTick(() => {
        if (chatHistoryRef.value) {
          chatHistoryRef.value.scrollTop = chatHistoryRef.value.scrollHeight;
        }
      });
    },
    { deep: true }
);

onMounted(async () => {
  messages.value = [];

  const inputEl = document.querySelector(".chat-input");
  if (inputEl) inputEl.focus();

  isLoggedIn.value = await userApi.isLoggedIn();

  if (isLoggedIn.value) {
    // 연결이 완료된 후 초기 메시지 전송
    const initialMessage = sessionStorage.getItem("initialMessage");
    if (initialMessage) {
      const checkConnection = setInterval(() => {
        if (isConnected.value) {
          conversate(initialMessage);
          sessionStorage.removeItem("initialMessage");
          clearInterval(checkConnection);
        }
      }, 100);
    }
  } else {
    // 로그인하지 않은 경우 환영 메시지 표시
    setTimeout(() => {
      const welcomeMessage = {
        id: "welcome_" + Date.now(),
        type: "assistantMessage",
        text:
            "안녕하세요! 👋\n\nAI 어시스턴트와 대화하시려면 **로그인**이 필요합니다.\n\n" +
            "로그인 후 다음과 같은 기능을 이용하실 수 있습니다:\n- CI/CD 어시스턴트\n- 배포 자동화",
        created_at: new Date().toISOString(),
      };
      messages.value.push(welcomeMessage);
    }, 1000);
  }
});
</script>

<template>
  <div class="chat-container">
    <!-- Header -->
    <div class="chat-header">
      <div class="header-content">
        <div class="bot-avatar">
          <div class="avatar-circle">
            <svg fill="none" height="24" width="24" xmlns="http://www.w3.org/2000/svg">
              <path
                  d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 1H5C3.89 1 3 1.89 3 3V7H9V9H21ZM3 19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V11H3V19Z"
                  fill="currentColor"
              />
            </svg>
          </div>
          <div
              :class="{ connected: isConnected && isLoggedIn, error: isError || !isLoggedIn }"
              class="status-indicator"
          ></div>
        </div>
        <div class="header-info">
          <h3 class="bot-name">AI CI/CD Assistant</h3>
          <p class="bot-status">
            <span v-if="!isLoggedIn" class="status-text error">로그인 필요</span>
            <span v-else-if="isConnected" class="status-text connected">연결됨</span>
            <span v-else-if="isError" class="status-text error">연결 오류</span>
            <span v-else class="status-text connecting">연결 중...</span>
          </p>
        </div>
        <div v-if="!isLoggedIn" class="header-actions">
          <button class="login-btn" @click="$router.push('/user/login')" title="로그인">
            <svg fill="none" height="20" width="20" xmlns="http://www.w3.org/2000/svg">
              <path
                  d="M11 7L9.6 8.4L12.2 11H2V13H12.2L9.6 15.6L11 17L16 12L11 7ZM20 19H12V21H20C21.1 21 22 20.1 22 19V5C22 3.9 21.1 3 20 3H12V5H20V19Z"
                  fill="currentColor"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Chat History -->
    <div ref="chatHistoryRef" class="chat-history-container">
      <div class="chat-history">
        <TransitionGroup name="message" tag="div">
          <div
              v-for="msg in messages"
              :key="msg.id ?? msg.created_at ?? Math.random()"
              :class="['message-wrapper', msg.type === 'userMessage' ? 'user-message' : 'assistant-message']"
          >
            <div class="message-content">
              <div class="avatar">
                <div v-if="msg.type === 'userMessage'" class="user-avatar">
                  <svg fill="none" height="20" width="20" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z"
                        fill="currentColor"
                    />
                  </svg>
                </div>
                <div v-else class="bot-avatar-small">
                  <svg fill="none" height="20" width="20" xmlns="http://www.w3.org/2000/svg">
                    <path
                        d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 1H5C3.89 1 3 1.89 3 3V7H9V9H21ZM3 19C3 20.1 3.9 21 5 21H19C20.1 21 21 20.1 21 19V11H3V19Z"
                        fill="currentColor"
                    />
                  </svg>
                </div>
              </div>
              <div class="message-bubble">
                <div v-if="msg.type === 'userMessage'" class="message-text">
                  {{ msg.contents && msg.contents[0]?.text }}
                </div>
                <div v-else class="message-text" v-html="renderMarkdown(msg.text)"></div>
                <div class="message-time">
                  {{
                    msg.created_at
                        ? new Date(msg.created_at).toLocaleTimeString("ko-KR", {
                          hour: "2-digit",
                          minute: "2-digit",
                        })
                        : ""
                  }}
                </div>
              </div>
            </div>
          </div>
        </TransitionGroup>
      </div>
    </div>

    <!-- Input Area -->
    <div class="chat-input-container">
      <form class="input-form" @submit.prevent="send">
        <div class="input-wrapper">
          <input
              v-model="input"
              :placeholder="isLoggedIn ? '메시지를 입력하세요...' : '로그인 후 채팅을 시작하세요...'"
              autocomplete="off"
              class="chat-input"
              @keyup.enter="send"
          />
          <button :disabled="!input.trim()" class="send-button" type="submit">
            <svg fill="none" height="20" width="20" xmlns="http://www.w3.org/2000/svg">
              <path d="M2.01 21L23 12L2.01 3L2 10L17 12L2 14L2.01 21Z" fill="currentColor" />
            </svg>
          </button>
        </div>
        <div v-if="!isLoggedIn" class="connection-status">
          <span class="login-required-message">
            채팅을 시작하려면
            <button class="login-link" @click="$router.push('/user/login')">로그인</button>
            해주세요
          </span>
        </div>
        <div v-else-if="!isConnected" class="connection-status">
          <span v-if="isError" class="error-message">
            연결에 실패했습니다.
            <button class="retry-btn" @click="tryConnect">다시 시도</button>
          </span>
          <span v-else class="connecting-message">서버에 연결 중...</span>
        </div>
      </form>
    </div>
  </div>
</template>


<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* Header */
.chat-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  padding: 16px 24px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bot-avatar {
  position: relative;
}

.avatar-circle {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.status-indicator {
  position: absolute;
  bottom: 2px;
  right: 2px;
  width: 12px;
  height: 12px;
  border: 2px solid white;
  border-radius: 50%;
  background: #9ca3af;
}

.status-indicator.connected {
  background: #4ade80;
  animation: pulse 2s infinite;
}

.status-indicator.error {
  background: #ef4444;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.header-info {
  flex: 1;
}

.bot-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.bot-status {
  margin: 0;
  font-size: 14px;
}

.status-text.connected {
  color: #059669;
}

.status-text.error {
  color: #dc2626;
}

.status-text.connecting {
  color: #d97706;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.login-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

/* Chat History */
.chat-history-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

.chat-history {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 800px;
  margin: 0 auto;
}

.message-wrapper {
  display: flex;
  animation: slideIn 0.3s ease-out;
}

.message-wrapper.user-message {
  justify-content: flex-end;
}

.message-wrapper.assistant-message {
  justify-content: flex-start;
}

.message-content {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 70%;
}

.user-message .message-content {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.user-avatar, .bot-avatar-small {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);
}

.bot-avatar-small {
  background: linear-gradient(135deg, #667eea, #764ba2);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.message-bubble {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 16px 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  position: relative;
  margin: 10px 10px auto;
}

.user-message .message-bubble {
  background: rgba(255, 255, 255, 0.95);
  color: rgba(46, 46, 46, 0.8);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.3);
}

.message-text {
  font-size: 15px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 8px;
  text-align: right;
}

.user-message .message-time {
  color: rgba(46, 46, 46, 0.8);
}

/* Input Area */
.chat-input-container {
  padding: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.input-form {
  max-width: 800px;
  margin: 0 auto;
}

.input-wrapper {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 25px;
  padding: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.chat-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 12px 20px;
  font-size: 15px;
  background: transparent;
  color: #1f2937;
}

.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.chat-input::placeholder {
  color: #9ca3af;
}

.send-button {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.send-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.send-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.connection-status {
  text-align: center;
  margin-top: 12px;
  font-size: 14px;
}

.error-message {
  color: #dc2626;
}

.connecting-message {
  color: #d97706;
}

.login-required-message {
  color: #667eea;
}

.login-link {
  background: none;
  border: none;
  color: #667eea;
  text-decoration: underline;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
}

.login-link:hover {
  color: #4f46e5;
}

.retry-btn {
  background: none;
  border: none;
  color: #667eea;
  text-decoration: underline;
  cursor: pointer;
  font-size: 14px;
}

/* Animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-enter-active {
  transition: all 0.3s ease-out;
}

.message-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.modal-enter-active, .modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from, .modal-leave-to {
  opacity: 0;
  transform: scale(0.9);
}

/* Markdown Styles */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3) {
  margin: 16px 0 8px 0;
  font-weight: 600;
}

.message-text :deep(p) {
  margin: 8px 0;
}

.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 20px;
}

.message-text :deep(code) {
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
  font-size: 13px;
}

.user-message .message-text :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-text :deep(pre) {
  background: rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  padding: 12px;
  overflow-x: auto;
  margin: 8px 0;
}

.user-message .message-text :deep(pre) {
  background: rgba(255, 255, 255, 0.1);
}

/* Responsive Design */
@media (max-width: 768px) {
  .chat-history-container {
    padding: 16px;
  }

  .chat-input-container {
    padding: 16px;
  }

  .message-content {
    max-width: 85%;
  }

  .header-content {
    padding: 0 8px;
  }

  .modal-content {
    margin: 16px;
    padding: 24px;
  }
}

/* Scrollbar Styling */
.chat-history-container::-webkit-scrollbar {
  width: 6px;
}

.chat-history-container::-webkit-scrollbar-track {
  background: transparent;
}

.chat-history-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.chat-history-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.5);
}
</style>
