<script setup>
import {ref} from 'vue';
import {useRouter} from 'vue-router';
import {useUserStore} from "@/stores/useUserStore.js";

const userStore = useUserStore();
const router = useRouter();
const sendText = ref('');
const isTransitioning = ref(false);


// 메시지 전송 함수
function sendMessage() {
  if (sendText.value.trim() && !isTransitioning.value) {
    isTransitioning.value = true;

    // 전송할 메시지를 세션 스토리지에 저장 (chat-interface에서 사용)
    sessionStorage.setItem('initialMessage', sendText.value.trim());

    // 애니메이션 후 페이지 이동
    setTimeout(() => {
      router.push('/ai/chat');
    }, 800);
  }
}

// Enter 키 처리
function handleKeyPress(event) {
  if (event.key === 'Enter') {
    sendMessage();
  }
}
</script>

<template>
  <div :class="{ 'transitioning': isTransitioning }" class="page-container">
    <div class="container">
      <div :class="{ 'moving': isTransitioning }" class="chat_box">
        <div v-if="!isTransitioning" class="bot-avatar-large">
          <img alt="thumbnail" src="@/assets/icons/thumbnail.svg"/>
        </div>
        <Transition name="welcome-text">
          <p v-if="!isTransitioning" class="welcome-message">무엇을 도와드릴까요?</p>
        </Transition>


        <form class="message-form" @submit.prevent="sendMessage">
          <div :class="{ 'transforming': isTransitioning }" class="input_box">
            <input
                v-model="sendText"
                :disabled="isTransitioning"
                class="message-input"
                placeholder="오늘 어떤 도움을 드릴까요?"
                type="text"
                @keypress="handleKeyPress"
            />
            <button
                :disabled="!sendText.trim() || isTransitioning"
                class="send_btn"
                type="submit"
            >
              <Transition mode="out-in" name="button-icon">
                <svg
                    v-if="!isTransitioning"
                    key="arrow"
                    fill="none"
                    height="16"
                    viewBox="0 0 24 24"
                    width="16"
                    xmlns="http://www.w3.org/2000/svg"
                >
                  <path d="M2.01 21L23 12L2.01 3L2 10L17 12L2 14L2.01 21Z" fill="currentColor"/>
                </svg>
                <div
                    v-else
                    key="loading"
                    class="loading-spinner"
                ></div>
              </Transition>
            </button>
          </div>
        </form>

        <!-- 전환 중 오버레이 -->
        <Transition name="overlay">
        </Transition>
      </div>
    </div>

    <!-- 배경 애니메이션 -->
    <div :class="{ 'active': isTransitioning }" class="background-animation">
      <div class="wave wave-1"></div>
      <div class="wave wave-2"></div>
      <div class="wave wave-3"></div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  width: 100%;
  height: 100vh;
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.page-container.transitioning {
  animation: pageTransition 0.8s ease-in-out;
}

@keyframes pageTransition {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.02);
  }
  100% {
    transform: scale(1.05);
    opacity: 0.9;
  }
}

.container {
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 2;
}

.chat_box {
  height: 90%;
  margin: 0 20%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.chat_box.moving {
  transform: translateY(20vh) scale(0.95);
  opacity: 0.8;
}

.welcome-message {
  font-size: 32px;
  margin-bottom: 48px;
  color: white;
  text-align: center;
  font-weight: 600;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  letter-spacing: -0.5px;
}

.message-form {
  width: 100%;
  position: relative;
}

.input_box {
  width: 100%;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1),
  0 2px 8px rgba(255, 255, 255, 0.1) inset;
  padding: 16px 20px 16px 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  transition: all 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.input_box::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.8s ease;
}

.input_box.transforming::before {
  left: 100%;
}

.input_box.transforming {
  border-radius: 25px;
  padding: 12px 16px;
  transform: translateY(10px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15),
  0 4px 12px rgba(255, 255, 255, 0.1) inset;
}

.input_box:focus-within {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15),
  0 4px 12px rgba(255, 255, 255, 0.2) inset;
}

.message-input {
  font-size: 16px;
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  color: #1f2937;
  font-weight: 400;
  transition: all 0.3s ease;
}

.message-input::placeholder {
  color: #9ca3af;
  transition: color 0.3s ease;
}

.message-input:focus::placeholder {
  color: #d1d5db;
}

.message-input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.send_btn {
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: white;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
}

.send_btn::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  transition: all 0.3s ease;
  transform: translate(-50%, -50%);
}

.send_btn:hover:not(:disabled)::before {
  width: 100%;
  height: 100%;
}

.send_btn:hover:not(:disabled) {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.5);
}

.send_btn:active:not(:disabled) {
  transform: translateY(-1px) scale(1.02);
}

.send_btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

/* 배경 애니메이션 */
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  opacity: 0;
  transition: opacity 0.8s ease;
}

.background-animation.active {
  opacity: 1;
}

.wave {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: wave 2s ease-in-out infinite;
}

.wave-1 {
  width: 200px;
  height: 200px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.wave-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 0.5s;
}

.wave-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 50%;
  animation-delay: 1s;
}

@keyframes wave {
  0%, 100% {
    transform: scale(1) translateY(0);
    opacity: 0.3;
  }
  50% {
    transform: scale(1.2) translateY(-20px);
    opacity: 0.6;
  }
}

/* 트랜지션 애니메이션 */
.welcome-text-enter-active,
.welcome-text-leave-active {
  transition: all 0.5s ease;
}

.welcome-text-leave-to {
  opacity: 0;
  transform: translateY(-30px) scale(0.9);
}

.button-icon-enter-active,
.button-icon-leave-active {
  transition: all 0.3s ease;
}

.button-icon-enter-from,
.button-icon-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.overlay-enter-active {
  transition: all 0.4s ease;
  transition-delay: 0.2s;
}

.overlay-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.8);
}


/* 반응형 디자인 */
@media (max-width: 1024px) {
  .chat_box {
    margin: 0 10%;
  }

  .welcome-message {
    font-size: 28px;
    margin-bottom: 40px;
  }
}

@media (max-width: 768px) {
  .chat_box {
    margin: 0 5%;
    padding: 0 20px;
  }

  .welcome-message {
    font-size: 24px;
    margin-bottom: 32px;
  }

  .input_box {
    padding: 14px 18px 14px 20px;
  }

  .send_btn {
    width: 40px;
    height: 40px;
  }

  .transition-overlay {
    min-width: 280px;
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .chat_box {
    margin: 0;
    padding: 0 16px;
  }

  .welcome-message {
    font-size: 20px;
    margin-bottom: 24px;
  }

  .message-input {
    font-size: 14px;
  }
}

.bot-avatar-large {
  width: 60px;
  height: 60px;
}

.bot-avatar-large {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(20px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  animation: float 3s ease-in-out infinite;
}

/* 접근성 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 다크 모드 지원 */
@media (prefers-color-scheme: dark) {
  .input_box {
    background: rgba(255, 255, 255, 0.9);
  }

  .transition-overlay {
    background: rgba(255, 255, 255, 0.9);
  }
}
</style>