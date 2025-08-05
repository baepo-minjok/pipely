<script setup>
import {useRoute, useRouter} from 'vue-router'
import {emailApi} from "@/api/EmailApi.js";
import {ref, watch} from "vue";

const route = useRoute();
const router = useRouter();
const sendApi = ref(false);
const successApi = ref(false);
const isLoading = ref(false);
const password = ref("");
const passwordCheck = ref("");
const passwordError = ref("");
const passwordCheckError = ref("");
const passwordMatchSuccess = ref(false);
const showPassword = ref(false);
const showPasswordCheck = ref(false);

watch([password, passwordCheck], () => {
  passwordError.value = "";
  passwordCheckError.value = "";
  passwordMatchSuccess.value =
    !!password.value &&
    !!passwordCheck.value &&
    password.value === passwordCheck.value &&
    isValidPassword(password.value);
});

function isValidPassword(pw) {
  // 10자 이상, 대문자 1개 이상, 특수문자 1개 이상
  return /^(?=.*[A-Z])(?=.*[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]).{10,}$/.test(pw);
}

const resetPassword = async () => {
  passwordError.value = "";
  passwordCheckError.value = "";
  let valid = true;

  // 비밀번호
  if (!password.value) {
    passwordError.value = "비밀번호를 입력해주세요.";
    valid = false;
  } else if (!isValidPassword(password.value)) {
    passwordError.value = "비밀번호는 10자 이상, 대문자와 특수문자를 각각 1개 이상 포함해야 합니다.";
    valid = false;
  }

  // 비밀번호 확인
  if (!passwordCheck.value) {
    passwordCheckError.value = "비밀번호 확인을 입력해주세요.";
    valid = false;
  } else if (passwordCheck.value !== password.value) {
    passwordCheckError.value = "비밀번호가 일치하지 않습니다.";
    valid = false;
  }

  if (!valid) return;

  isLoading.value = true;

  try {
    const token = route.query.token;
    const data = {
      token: token,
      newPassword: password.value,
    }

    const response = await emailApi.resetPassword(data);
    sendApi.value = true;
    successApi.value = !!response;
  } catch (error) {
    sendApi.value = true;
    successApi.value = false;
  } finally {
    isLoading.value = false;
  }
}

const goToLogin = () => {
  router.push('/user/login');
}
</script>

<template>
  <div class="page-container">
    <div class="content-wrapper">
      <!-- 로고 -->
      <div class="logo-container">
        <img alt="Pipely" class="logo" src="/src/assets/images/logo.png"/>
      </div>

      <!-- 메인 컨텐츠 -->
      <div class="main-content">
        <!-- 비밀번호 재설정 폼 -->
        <div v-if="!sendApi" class="form-container">
          <div class="header">
            <h1 class="title">비밀번호 재설정</h1>
            <p class="description">새로운 비밀번호를 입력해주세요.</p>
          </div>

          <form class="form" @submit.prevent="resetPassword">
            <!-- 새 비밀번호 -->
            <div class="input-group">
              <label class="input-label" for="password">새 비밀번호</label>
              <div class="password-wrapper">
                <input
                  id="password"
                  v-model="password"
                  :class="['input-field', { 'error': passwordError }]"
                  :type="showPassword ? 'text' : 'password'"
                  name="password"
                  placeholder="새로운 비밀번호를 입력해주세요"
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
              <div v-else class="password-requirements">
                <p class="requirements-title">비밀번호 요구사항:</p>
                <ul class="requirements-list">
                  <li :class="{ 'valid': password.length >= 10 }">
                    <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                      <path d="M5 13l4 4L19 7"/>
                    </svg>
                    10자 이상
                  </li>
                  <li :class="{ 'valid': /[A-Z]/.test(password) }">
                    <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                      <path d="M5 13l4 4L19 7"/>
                    </svg>
                    대문자 1개 이상
                  </li>
                  <li :class="{ 'valid': /[!@#$%^&*()_\-+=\[\]{};':\\|,.<>\/?]/.test(password) }">
                    <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                      <path d="M5 13l4 4L19 7"/>
                    </svg>
                    특수문자 1개 이상
                  </li>
                </ul>
              </div>
            </div>

            <!-- 비밀번호 확인 -->
            <div class="input-group">
              <label class="input-label" for="password_check">비밀번호 확인</label>
              <div class="password-wrapper">
                <input
                  id="password_check"
                  v-model="passwordCheck"
                  :class="['input-field', {
                    'error': passwordCheckError,
                    'success': passwordMatchSuccess
                  }]"
                  :type="showPasswordCheck ? 'text' : 'password'"
                  name="password_check"
                  placeholder="비밀번호를 다시 입력해주세요"
                  @input="passwordCheckError = ''"
                />
                <button
                  class="password-toggle"
                  type="button"
                  @click="showPasswordCheck = !showPasswordCheck"
                >
                  <svg v-if="showPasswordCheck" fill="none" height="18" stroke="currentColor" stroke-width="2"
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
              <span v-if="passwordCheckError" class="field-error">
                {{ passwordCheckError }}
              </span>
              <span v-else-if="passwordMatchSuccess" class="field-success">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M5 13l4 4L19 7"/>
                </svg>
                비밀번호가 일치합니다.
              </span>
            </div>

            <!-- 변경 버튼 -->
            <button
              :disabled="isLoading || !passwordMatchSuccess"
              class="submit-btn"
              type="submit"
            >
              <svg v-if="isLoading" class="spinner" fill="none" height="18" stroke="currentColor" stroke-width="2"
                   viewBox="0 0 24 24" width="18">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              <svg v-else fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M5 13l4 4L19 7"/>
              </svg>
              {{ isLoading ? '변경 중...' : '비밀번호 변경' }}
            </button>
          </form>
        </div>

        <!-- 결과 화면 -->
        <div v-else class="result-container">
          <!-- 성공 -->
          <div v-if="successApi" class="result-content success">
            <div class="result-icon success">
              <svg fill="none" height="48" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="48">
                <path d="M5 13l4 4L19 7"/>
              </svg>
            </div>
            <h2 class="result-title">비밀번호 변경 완료!</h2>
            <p class="result-description">
              비밀번호가 성공적으로 변경되었습니다.<br/>
              새로운 비밀번호로 로그인해주세요.
            </p>
            <button class="result-btn" @click="goToLogin">
              <svg fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                <polyline points="10,17 15,12 10,7"/>
                <line x1="15" x2="3" y1="12" y2="12"/>
              </svg>
              로그인 화면으로
            </button>
          </div>

          <!-- 실패 -->
          <div v-else class="result-content error">
            <div class="result-icon error">
              <svg fill="none" height="48" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="48">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" x2="9" y1="9" y2="15"/>
                <line x1="9" x2="15" y1="9" y2="15"/>
              </svg>
            </div>
            <h2 class="result-title">변경 실패</h2>
            <p class="result-description">
              비밀번호 변경에 실패했습니다.<br/>
              토큰이 만료되었거나 잘못된 요청입니다.
            </p>
            <button class="result-btn" @click="goToLogin">
              <svg fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                <polyline points="10,17 15,12 10,7"/>
                <line x1="15" x2="3" y1="12" y2="12"/>
              </svg>
              로그인 화면으로
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 페이지 컨테이너 */
.page-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.content-wrapper {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

/* 로고 */
.logo-container {
  display: flex;
  justify-content: center;
  padding: 32px 32px 0 32px;
}

.logo {
  height: 48px;
  width: auto;
}

/* 메인 컨텐츠 */
.main-content {
  padding: 32px;
}

/* 폼 컨테이너 */
.form-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* 헤더 */
.header {
  text-align: center;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.description {
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

.input-field.success {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
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

/* 에러/성공 메시지 */
.field-error {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ef4444;
  font-size: 14px;
  font-weight: 500;
}

.field-success {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #10b981;
  font-size: 14px;
  font-weight: 500;
}

/* 비밀번호 요구사항 */
.password-requirements {
  margin-top: 8px;
}

.requirements-title {
  font-size: 12px;
  color: #6b7280;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.requirements-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.requirements-list li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.requirements-list li.valid {
  color: #10b981;
}

.requirements-list li svg {
  width: 12px;
  height: 12px;
  opacity: 0.5;
}

.requirements-list li.valid svg {
  opacity: 1;
}

/* 제출 버튼 */
.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 14px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 결과 컨테이너 */
.result-container {
  display: flex;
  justify-content: center;
}

.result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 24px;
  padding: 24px;
}

/* 결과 아이콘 */
.result-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  margin-bottom: 8px;
}

.result-icon.success {
  background: #dcfce7;
  color: #16a34a;
}

.result-icon.error {
  background: #fef2f2;
  color: #dc2626;
}

/* 결과 텍스트 */
.result-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.result-description {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  line-height: 1.6;
}

/* 결과 버튼 */
.result-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 32px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
  margin-top: 16px;
}

.result-btn:hover {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
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
  .page-container {
    padding: 16px;
  }

  .content-wrapper {
    max-width: none;
  }

  .logo-container {
    padding: 24px 24px 0 24px;
  }

  .main-content {
    padding: 24px;
  }

  .title {
    font-size: 24px;
  }

  .form-container {
    gap: 24px;
  }

  .form {
    gap: 20px;
  }
}
</style>