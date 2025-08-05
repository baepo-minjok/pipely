<script setup>
import {ref} from 'vue';
import {emailApi} from "@/api/EmailApi.js";
import {useRouter} from 'vue-router';

const router = useRouter();
const sendEmail = ref(false);
const email = ref('');
const emailError = ref('');
const isLoading = ref(false);
const isSuccess = ref(false);

const validateEmail = (email) => {
  return /^[\w-.]+@([\w-]+\.)+[\w-]{2,}$/.test(email);
};

const handleNextClick = async () => {
  emailError.value = '';

  if (!email.value.trim()) {
    emailError.value = '이메일을 입력해주세요.';
    return;
  }

  if (!validateEmail(email.value)) {
    emailError.value = '올바른 이메일 주소를 입력해주세요.';
    return;
  }

  isLoading.value = true;

  try {
    await emailApi.sendResetEmail(email.value);
    isSuccess.value = true;
    sendEmail.value = true;
  } catch (error) {
    emailError.value = '이메일 발송에 실패했습니다. 다시 시도해주세요.';
    isSuccess.value = false;
  } finally {
    isLoading.value = false;
  }
};

const goToLogin = () => {
  router.push('/user/login');
};

const goBack = () => {
  sendEmail.value = false;
  email.value = '';
  emailError.value = '';
};
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
        <!-- 이메일 입력 폼 -->
        <div v-if="!sendEmail" class="form-container">
          <div class="header">
            <h1 class="title">비밀번호 찾기</h1>
            <p class="description">
              가입하신 이메일 주소를 입력하시면<br/>
              비밀번호 재설정 링크를 발송해드립니다.
            </p>
          </div>

          <form class="form" @submit.prevent="handleNextClick">
            <div class="input-group">
              <label class="input-label" for="email">이메일 주소</label>
              <div class="input-wrapper">
                <input
                  id="email"
                  v-model="email"
                  :class="['input-field', { 'error': emailError }]"
                  name="email"
                  placeholder="이메일 주소를 입력해주세요"
                  type="email"
                  @input="emailError = ''"
                />
                <div class="input-icon">
                  <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                    <polyline points="22,6 12,13 2,6"/>
                  </svg>
                </div>
              </div>
              <span v-if="emailError" class="field-error">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="15" x2="9" y1="9" y2="15"/>
                  <line x1="9" x2="15" y1="9" y2="15"/>
                </svg>
                {{ emailError }}
              </span>
            </div>

            <button
              :disabled="isLoading || !email.trim()"
              class="submit-btn"
              type="submit"
            >
              <svg v-if="isLoading" class="spinner" fill="none" height="18" stroke="currentColor" stroke-width="2"
                   viewBox="0 0 24 24" width="18">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              <svg v-else fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M5 12h14"/>
                <path d="M12 5l7 7-7 7"/>
              </svg>
              {{ isLoading ? '이메일 발송 중...' : '비밀번호 재설정 링크 발송' }}
            </button>
          </form>

          <!-- 하단 링크 -->
          <div class="footer-links">
            <button class="link-btn" @click="goToLogin">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M19 12H5"/>
                <path d="M12 19l-7-7 7-7"/>
              </svg>
              로그인 화면으로 돌아가기
            </button>
          </div>
        </div>

        <!-- 결과 화면 -->
        <div v-else class="result-container">
          <div class="result-content">
            <div class="result-icon">
              <svg fill="none" height="48" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="48">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                <polyline points="22,6 12,13 2,6"/>
              </svg>
            </div>

            <h2 class="result-title">이메일이 발송되었습니다!</h2>

            <div class="result-description">
              <p class="email-info">
                <strong>{{ email }}</strong>로<br/>
                비밀번호 재설정 링크를 발송했습니다.
              </p>
              <p class="instruction">
                이메일을 확인하고 링크를 클릭하여<br/>
                새로운 비밀번호를 설정해주세요.
              </p>
            </div>

            <!-- 도움말 -->
            <div class="help-section">
              <div class="help-item">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
                  <line x1="12" x2="12.01" y1="17" y2="17"/>
                </svg>
                <span>이메일이 오지 않았나요? 스팸 폴더를 확인해보세요.</span>
              </div>
              <div class="help-item">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                <span>링크는 24시간 동안 유효합니다.</span>
              </div>
            </div>

            <!-- 버튼 그룹 -->
            <div class="button-group">
              <button class="btn btn-secondary" @click="goBack">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M19 12H5"/>
                  <path d="M12 19l-7-7 7-7"/>
                </svg>
                다시 시도
              </button>
              <button class="btn btn-primary" @click="goToLogin">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
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
  margin: 0 0 12px 0;
}

.description {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  line-height: 1.6;
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

/* 입력 래퍼 */
.input-wrapper {
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

/* 입력 아이콘 */
.input-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  pointer-events: none;
}

/* 에러 메시지 */
.field-error {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ef4444;
  font-size: 14px;
  font-weight: 500;
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

/* 하단 링크 */
.footer-links {
  display: flex;
  justify-content: center;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.link-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.link-btn:hover {
  color: #2563eb;
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
  background: #dbeafe;
  color: #2563eb;
  border-radius: 50%;
  margin-bottom: 8px;
}

/* 결과 텍스트 */
.result-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.result-description {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.email-info {
  font-size: 16px;
  color: #374151;
  margin: 0;
  line-height: 1.6;
}

.email-info strong {
  color: #2563eb;
  font-weight: 600;
}

.instruction {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
  line-height: 1.5;
}

/* 도움말 섹션 */
.help-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.help-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
}

.help-item svg {
  color: #94a3b8;
  flex-shrink: 0;
  margin-top: 1px;
}

/* 버튼 그룹 */
.button-group {
  display: flex;
  gap: 12px;
  width: 100%;
}

.btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
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

  .button-group {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }
}
</style>