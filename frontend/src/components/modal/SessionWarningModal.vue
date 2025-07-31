<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue'

const props = defineProps({
  isVisible: {
    type: Boolean,
    default: false
  },
  remainingTime: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['extend', 'logout', 'close'])

const isExtending = ref(false)
const remainingSeconds = ref(props.remainingTime)
let countdownTimer = null

// 원형 진행률 계산
const circumference = 2 * Math.PI * 54 // r=54
const strokeDashoffset = computed(() => {
  const progress = remainingSeconds.value / 300
  return circumference - (progress * circumference)
})

const startCountdown = () => {
  if (countdownTimer) clearInterval(countdownTimer)

  countdownTimer = setInterval(() => {
    if (remainingSeconds.value > 0) {
      remainingSeconds.value--
    } else {
      clearInterval(countdownTimer)
      handleLogout()
    }
  }, 1000)
}

const stopCountdown = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

// 이벤트 핸들러
const handleExtend = async () => {
  isExtending.value = true
  try {
    await emit('extend')
    stopCountdown()
  } catch (error) {
    console.error('세션 연장 실패:', error)
  } finally {
    isExtending.value = false
  }
}

const handleLogout = () => {
  stopCountdown()
  emit('logout')
}

const handleOverlayClick = () => {
}

// Props 변화 감지
watch(() => props.remainingTime, (newVal) => {
  remainingSeconds.value = newVal
})

watch(() => props.isVisible, (newVal) => {
  if (newVal) {
    remainingSeconds.value = props.remainingTime
    startCountdown()
  } else {
    stopCountdown()
  }
})

onMounted(() => {
  if (props.isVisible) {
    startCountdown()
  }
})

onUnmounted(() => {
  stopCountdown()
})
</script>
<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="isVisible" class="modal-overlay" @click="handleOverlayClick">
        <div class="modal-container" @click.stop>
          <!-- 헤더 -->
          <div class="modal-header">
            <div class="warning-icon">
              <svg fill="none" height="24" viewBox="0 0 24 24" width="24" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M12 9V13M12 17H12.01M21 12C21 16.9706 16.9706 21 12 21C7.02944 21 3 16.9706 3 12C3 7.02944 7.02944 3 12 3C16.9706 3 21 7.02944 21 12Z"
                  stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
              </svg>
            </div>
            <div class="header-content">
              <h2 class="modal-title">세션 만료 경고</h2>
              <p class="modal-subtitle">로그인 세션이 곧 만료됩니다</p>
            </div>
          </div>

          <!-- 바디 -->
          <div class="modal-body">
            <div class="countdown-section">
              <div class="countdown-circle">
                <svg class="countdown-svg" height="120" viewBox="0 0 120 120" width="120">
                  <circle
                    cx="60"
                    cy="60"
                    fill="none"
                    r="54"
                    stroke="#f1f5f9"
                    stroke-width="8"
                  />
                  <circle
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="strokeDashoffset"
                    class="countdown-progress"
                    cx="60"
                    cy="60"
                    fill="none"
                    r="54"
                    stroke="currentColor"
                    stroke-linecap="round"
                    stroke-width="8"
                    transform="rotate(-90 60 60)"
                  />
                </svg>
                <div class="countdown-text">
                  <span class="countdown-number">{{ remainingSeconds }}</span>
                  <span class="countdown-label">초</span>
                </div>
              </div>
            </div>

            <div class="message-section">
              <p class="warning-message">
                <strong>{{ remainingSeconds }}초</strong> 후에 자동으로 로그아웃됩니다.
              </p>
              <p class="info-message">
                계속 사용하시려면 세션을 연장해주세요.
              </p>
            </div>
          </div>

          <!-- 푸터 -->
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="handleLogout">
              <svg fill="none" height="16" viewBox="0 0 24 24" width="16" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" stroke="currentColor"
                      stroke-linecap="round" stroke-linejoin="round" stroke-width="2"/>
              </svg>
              로그아웃
            </button>
            <button :disabled="isExtending" class="btn btn-primary" @click="handleExtend">
              <svg v-if="!isExtending" fill="none" height="16" viewBox="0 0 24 24" width="16"
                   xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M23 12C23 18.0751 18.0751 23 12 23C5.92487 23 1 18.0751 1 12C1 5.92487 5.92487 1 12 1C18.0751 1 23 5.92487 23 12ZM3.00683 12C3.00683 16.9668 7.03321 20.9932 12 20.9932C16.9668 20.9932 20.9932 16.9668 20.9932 12C20.9932 7.03321 16.9668 3.00683 12 3.00683C7.03321 3.00683 3.00683 7.03321 3.00683 12Z"
                  fill="currentColor"/>
                <path
                  d="M12 7C12.5523 7 13 7.44772 13 8V11H16C16.5523 11 17 11.4477 17 12C17 12.5523 16.5523 13 16 13H13V16C13 16.5523 12.5523 17 12 17C11.4477 17 11 16.5523 11 16V13H8C7.44772 13 7 12.5523 7 12C7 11.4477 7.44772 11 8 11H11V8C11 7.44772 11.4477 7 12 7Z"
                  fill="currentColor"/>
              </svg>
              <div v-else class="loading-spinner"></div>
              {{ isExtending ? '연장 중...' : '세션 연장' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-container {
  background: white;
  border-radius: 20px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  max-width: 480px;
  width: 100%;
  overflow: hidden;
  animation: modalPulse 2s infinite;
}

@keyframes modalPulse {
  0%, 100% {
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25), 0 0 0 0 rgba(239, 68, 68, 0.4);
  }
  50% {
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25), 0 0 0 8px rgba(239, 68, 68, 0.1);
  }
}

/* 헤더 */
.modal-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 10px 10px 10px;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.warning-icon {
  width: 48px;
  height: 48px;
  background: #ef4444;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.header-content {
  flex: 1;
}

.modal-title {
  font-size: 20px;
  font-weight: 700;
  color: #dc2626;
  margin: 0 0 4px 0;
}

.modal-subtitle {
  font-size: 14px;
  color: #7f1d1d;
  margin: 0;
}

/* 바디 */
.modal-body {
  padding: 32px 24px;
  text-align: center;
}

.countdown-section {
  margin-bottom: 24px;
}

.countdown-circle {
  position: relative;
  display: inline-block;
  margin-bottom: 20px;
}

.countdown-svg {
  transform: rotate(-90deg);
}

.countdown-progress {
  color: #ef4444;
  transition: stroke-dashoffset 1s ease;
}

.countdown-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.countdown-number {
  font-size: 32px;
  font-weight: 800;
  color: #dc2626;
  line-height: 1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.countdown-label {
  font-size: 14px;
  color: #7f1d1d;
  font-weight: 600;
  margin-top: 4px;
}

.message-section {
  margin-bottom: 8px;
}

.warning-message {
  font-size: 16px;
  color: #1f2937;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.info-message {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

/* 푸터 */
.modal-footer {
  display: flex;
  gap: 12px;
  padding: 0 24px 24px;
  justify-content: flex-end;
}

.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 120px;
  justify-content: center;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #f1f5f9;
  color: #475569;
  transform: translateY(-1px);
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563eb, #1e40af);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.5);
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

/* 모달 애니메이션 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9) translateY(-20px);
}

/* 반응형 */
@media (max-width: 480px) {
  .modal-overlay {
    padding: 16px;
  }

  .modal-container {
    border-radius: 16px;
  }

  .modal-header {
    padding: 20px 20px 0;
  }

  .modal-body {
    padding: 24px 20px;
  }

  .modal-footer {
    padding: 0 20px 20px;
    flex-direction: column;
  }

  .btn {
    min-width: auto;
  }

  .countdown-number {
    font-size: 28px;
  }

  .modal-title {
    font-size: 18px;
  }
}

/* 접근성 */
@media (prefers-reduced-motion: reduce) {
  .modal-container,
  .warning-icon,
  .countdown-progress {
    animation: none !important;
  }

  * {
    transition-duration: 0.01ms !important;
  }
}
</style>