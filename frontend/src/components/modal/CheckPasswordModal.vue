<script setup>
import {useRouter} from 'vue-router'
import {userApi} from "@/api/UserApi.js";
import {ref} from "vue";
import {useUserStore} from "@/stores/useUserStore.js";

const userStore = useUserStore();
const emit = defineEmits(["close"]);
const props = defineProps({
  destination: {
    type: String,
    required: true,
  },
});

const router = useRouter();
const email = ref("");
const password = ref("");
const passwordError = ref("");
const isLoading = ref(false);
const showPassword = ref(false);

const handleCheck = async () => {
  if (!password.value.trim()) {
    passwordError.value = "비밀번호를 입력해주세요.";
    return;
  }

  passwordError.value = "";
  isLoading.value = true;

  const userInfo = userStore.getUserInfo();
  email.value = userInfo.email;

  const data = {
    email: email.value,
    password: password.value,
  }

  try {
    const response = await userApi.login(data);
    if (response.status === 200) {
      const tokenResponse = await userApi.getToken(userInfo.email);
      const data = tokenResponse.data.data;
      if (tokenResponse.status === 200) {
        userStore.reset();
        if (props.destination === 'reset') {
          await userApi.logout();
          router.push({name: 'ResetPassword', query: {token: data}});
        } else if (props.destination === 'withdraw') {
          router.push({name: 'Withdraw'});
        } else {
          router.push({name: 'Main'});
        }
      } else {
        alert("오류가 발생했습니다\n 다시 시도해주세요");
        router.push("/");
      }
    } else {
      passwordError.value = "비밀번호가 틀렸습니다.";
    }
  } catch (err) {
    alert("오류가 발생했습니다\n 다시 시도해주세요");
    router.push("/");
  } finally {
    isLoading.value = false;
  }
}

const handleClose = () => {
  emit('close');
}

const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) {
    handleClose();
  }
}
</script>

<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="modal">
      <!-- 헤더 -->
      <div class="modal-header">
        <h2 class="modal-title">비밀번호 확인</h2>
        <button class="close-button" type="button" @click="handleClose">
          <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
            <line x1="18" x2="6" y1="6" y2="18"/>
            <line x1="6" x2="18" y1="6" y2="18"/>
          </svg>
        </button>
      </div>

      <!-- 컨텐츠 -->
      <div class="modal-content">

        <!-- 설명 -->
        <div class="description">
          <p class="description-text">
            보안을 위해 현재 비밀번호를 입력해주세요.
          </p>
        </div>

        <!-- 폼 -->
        <form class="form" @submit.prevent="handleCheck">
          <div class="input-group">
            <label class="input-label" for="password">현재 비밀번호</label>
            <div class="password-wrapper">
              <input
                id="password"
                v-model="password"
                :class="['input-field', { 'error': passwordError }]"
                :type="showPassword ? 'text' : 'password'"
                name="password"
                placeholder="현재 비밀번호를 입력해주세요"
                @input="passwordError = ''"
              />
              <button
                class="password-toggle"
                type="button"
                @click="showPassword = !showPassword"
              >
                <svg v-if="showPassword" fill="none" height="18" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24" width="18">
                  <path
                    d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" x2="23" y1="1" y2="23"/>
                </svg>
                <svg v-else fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="18">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
              </button>
            </div>
            <span v-if="passwordError" class="field-error">
              {{ passwordError }}
            </span>
          </div>

          <!-- 버튼 -->
          <div class="button-group">
            <button
              :disabled="isLoading"
              class="btn btn-secondary"
              type="button"
              @click="handleClose"
            >
              취소
            </button>
            <button
              :disabled="isLoading || !password.trim()"
              class="btn btn-primary"
              type="submit"
            >
              <svg v-if="isLoading" class="spinner" fill="none" height="18" stroke="currentColor" stroke-width="2"
                   viewBox="0 0 24 24" width="18">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              <svg v-else fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M5 13l4 4L19 7"/>
              </svg>
              {{ isLoading ? '확인 중...' : '확인' }}
            </button>
          </div>
        </form>

        <!-- 도움말 -->
        <div class="help-section">
          <div class="help-item">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" x2="12.01" y1="17" y2="17"/>
            </svg>
            <span>비밀번호를 잊으셨나요? <a class="help-link" href="/user/find/password">비밀번호 재설정</a></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

/* 모달 */
.modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 모달 헤더 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 0 24px;
}

.modal-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.close-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: #f1f5f9;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.close-button:hover {
  background: #e2e8f0;
  color: #334155;
}

/* 모달 컨텐츠 */
.modal-content {
  padding: 24px;
}

/* 로고 */
.logo-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.logo {
  height: 48px;
  width: auto;
}

/* 설명 */
.description {
  text-align: center;
  margin-bottom: 32px;
}

.description-text {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* 폼 */
.form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 입력 그룹 */
.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 비밀번호 래퍼 */
.password-wrapper {
  position: relative;
}

.input-field {
  width: 100%;
  padding: 14px 48px 14px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  background: white;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.input-field:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.input-field.error {
  border-color: #ef4444;
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.1);
}

.input-field::placeholder {
  color: #9ca3af;
}

/* 비밀번호 토글 */
.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #374151;
}

/* 에러 메시지 */
.field-error {
  color: #ef4444;
  font-size: 14px;
  font-weight: 500;
}

/* 버튼 그룹 */
.button-group {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 20px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 48px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #f1f5f9;
  transform: translateY(-1px);
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

/* 도움말 섹션 */
.help-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.help-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
  justify-content: center;
}

.help-item svg {
  color: #9ca3af;
  flex-shrink: 0;
}

.help-link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.help-link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

/* 스피너 애니메이션 */
.spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 반응형 */
@media (max-width: 640px) {
  .modal {
    margin: 16px;
    max-width: none;
  }

  .modal-header {
    padding: 20px 20px 0 20px;
  }

  .modal-title {
    font-size: 20px;
  }

  .modal-content {
    padding: 20px;
  }

  .button-group {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }
}
</style>