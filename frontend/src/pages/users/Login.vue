<template>
  <div class="login-container">
    <!-- 왼쪽 로그인 폼 -->
    <div class="login-section">
      <div class="login-wrapper">
        <!-- 로고 -->
        <div class="login-logo">
          <img alt="Pipely" class="logo" src="/src/assets/images/logo.png"/>
        </div>

        <!-- 로그인 폼 -->
        <div class="login-form-container">
          <form class="login-form" @submit.prevent="login">
            <!-- 소셜 로그인 -->
            <div class="social-buttons">
              <button class="social-btn google" type="button" @click="googleLogin">
                <img alt="Google" src="/src/assets/images/google_logo.png"/>
                Google
              </button>
              <button class="social-btn github" type="button" @click="githubLogin">
                <img alt="GitHub" src="/src/assets/images/github_logo.png"/>
                GitHub
              </button>
            </div>

            <!-- 구분선 -->
            <div class="divider">
              <span>또는</span>
            </div>

            <!-- 입력 필드 -->
            <div class="input-fields">
              <div class="input-group">
                <input
                  id="email"
                  v-model="email"
                  :class="['input-field', { error: emailError }]"
                  autocomplete="username"
                  placeholder="이메일 주소"
                  type="email"
                  @input="clearError('email')"
                />
                <span v-if="emailError && errorMessage.includes('이메일')" class="field-error">
                  {{ errorMessage }}
                </span>
              </div>

              <div class="input-group">
                <div class="password-wrapper">
                  <input
                    id="password"
                    v-model="password"
                    :class="['input-field', { error: passwordError }]"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    placeholder="비밀번호"
                    @input="clearError('password')"
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
                <span v-if="passwordError && errorMessage.includes('비밀번호')" class="field-error">
                  {{ errorMessage }}
                </span>
              </div>
            </div>

            <!-- 로그인 버튼 -->
            <button :disabled="isLoading" class="login-btn" type="submit">
              <svg v-if="isLoading" class="spinner" fill="none" height="18" stroke="currentColor" stroke-width="2"
                   viewBox="0 0 24 24" width="18">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              {{ isLoading ? '로그인 중...' : '로그인' }}
            </button>

            <!-- 전체 에러 메시지 -->
            <div v-if="errorMessage && !emailError && !passwordError" class="error-alert">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" x2="9" y1="9" y2="15"/>
                <line x1="9" x2="15" y1="9" y2="15"/>
              </svg>
              {{ errorMessage }}
            </div>
          </form>

          <!-- 하단 링크 -->
          <div class="form-footer">
            <router-link class="footer-link primary" to="/user/signup">
              회원가입
            </router-link>
            <span class="separator">|</span>
            <router-link class="footer-link" to="/user/find/password">
              비밀번호 찾기
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 오른쪽 브랜딩 섹션 -->
    <div class="branding-section">
      <div class="branding-content">
        <!-- 로고 -->
        <div class="brand-logo">
          <img alt="Pipely" class="logo-image" src="/src/assets/images/logo.png"/>
        </div>

        <!-- 메인 메시지 -->
        <div class="brand-message">
          <h2 class="brand-title">AI와 함께하는 스마트한 CI/CD</h2>
        </div>

        <!-- 기능 하이라이트 -->
        <div class="feature-highlights">
          <div class="feature-item">
            <div class="feature-icon">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <path
                  d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
              </svg>
            </div>
            <div class="feature-text">
              <h4>AI 기반 자동화</h4>
              <p>스마트한 빌드 스크립트 생성</p>
            </div>
          </div>

          <div class="feature-item">
            <div class="feature-icon">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <polyline points="22,12 18,12 15,21 9,3 6,12 2,12"/>
              </svg>
            </div>
            <div class="feature-text">
              <h4>실시간 모니터링</h4>
              <p>배포 상태를 한눈에 확인</p>
            </div>
          </div>
        </div>

        <!-- 채팅 데모 -->
        <div class="chat-demo">
          <div class="chat-header">
            <div class="chat-indicator">
              <div class="indicator-dot"></div>
              <span>AI 어시스턴트</span>
            </div>
          </div>
          <div class="chat-messages">
            <div class="message user-message">
              <div class="message-content">빌드 스크립트 작성해주세요</div>
            </div>
            <div class="message ai-message">
              <div class="message-content">요청하신 빌드 스크립트입니다</div>
            </div>
            <div class="message ai-message code-message">
              <div class="message-content">
                <pre class="code-block">
pipeline {
  agent any
  stages {
    stage('Build') {
      steps {
        sh './gradlew build'
      }
    }
  }
}</pre>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import {ref} from "vue";
import {userApi} from "@/api/UserApi.js";
import {useRouter} from "vue-router";
import {useUserStore} from "@/stores/useUserStore.js";
import {emailApi} from "@/api/EmailApi.js";

const router = useRouter();
const userStore = useUserStore();

const email = ref("");
const password = ref("");
const showPassword = ref(false);
const emailError = ref(false);
const passwordError = ref(false);
const errorMessage = ref("");
const isLoading = ref(false);

const isValidEmail = (email) => {
  return /^[\w-.]+@([\w-]+\.)+[\w-]{2,}$/.test(email);
};

const clearError = (field) => {
  if (field === 'email') {
    emailError.value = false;
  } else if (field === 'password') {
    passwordError.value = false;
  }

  if (!emailError.value && !passwordError.value) {
    errorMessage.value = "";
  }
};

const reset = () => {
  emailError.value = false;
  passwordError.value = false;
  errorMessage.value = "";
};

const reactivation = async (data) => {
  email.value = "";
  password.value = "";
  reset();
  const isOk = confirm("탈퇴한 유저입니다.\n계정을 복구하시겠습니까?");
  if (!isOk) {
    return;
  }
  const response = await userApi.reactivation(data);
  if (response) {
    alert("계정이 복구되었습니다🎉!\n다시 로그인해주세요!");
  } else {
    alert("오류가 발생했습니다.\n다시 시도해주세요.");
  }
};

const dormant = async (email) => {
  email.value = "";
  password.value = "";
  reset();
  const isOk = confirm("휴면 처리된 유저입니다.\n계정을 복구하시겠습니까?");
  if (!isOk) {
    return;
  }
  const res = await emailApi.sendDormantEmail(email);
  if (res) {
    alert("재활성화 이메일이 발송되었습니다!");
  } else {
    alert("오류가 발생했습니다.\n다시 시도해주세요");
  }
};

const login = async () => {
  reset();
  isLoading.value = true;
  let valid = true;

  // 이메일 검증
  if (!email.value) {
    emailError.value = true;
    errorMessage.value = "이메일을 입력해주세요.";
    valid = false;
  } else if (!isValidEmail(email.value)) {
    emailError.value = true;
    errorMessage.value = "올바른 이메일 주소를 입력해주세요.";
    valid = false;
  }

  // 비밀번호 검증
  if (!password.value) {
    passwordError.value = true;
    if (!errorMessage.value) errorMessage.value = "비밀번호를 입력해주세요.";
    valid = false;
  }

  if (!valid) {
    isLoading.value = false;
    return;
  }

  const loginRequest = {
    email: email.value,
    password: password.value,
  };

  try {
    const response = await userApi.login(loginRequest);

    if (response.status === 200) {
      await userStore.fetchUserInfo();
      router.push({name: "Main"});
    } else if (response.status === 401) {
      if (response.code === "USER_WITHDRAWN_401") {
        await reactivation(loginRequest);
      } else if (response.code === "USER_DORMANT_401") {
        await dormant(loginRequest.email);
      } else {
        errorMessage.value = response.message;
      }
    } else {
      errorMessage.value = "로그인에 실패했습니다.";
    }
  } catch (error) {
    errorMessage.value = "로그인에 실패했습니다.";
  } finally {
    isLoading.value = false;
  }
};

const googleLogin = () => {
  window.location.href = "https://www.pipely.kro.kr/oauth2/authorization/google";
};

const githubLogin = () => {
  window.location.href = "https://www.pipely.kro.kr/oauth2/authorization/github";
};
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
}

/* 왼쪽 로그인 섹션 */
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: white;
}

.login-content {
  width: 100%;
  max-width: 400px;
}

/* 헤더 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  background: #dbeafe;
  border-radius: 16px;
  color: #2563eb;
  margin-bottom: 16px;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.login-subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* 폼 */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 소셜 로그인 */
.social-login-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.social-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  transform: translateY(-1px);
}

.social-icon {
  width: 20px;
  height: 20px;
}

/* 구분선 */
.divider {
  position: relative;
  text-align: center;
  margin: 8px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #e2e8f0;
}

.divider-text {
  background: white;
  color: #64748b;
  font-size: 14px;
  padding: 0 16px;
  position: relative;
}

/* 폼 필드 */
.form-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-label svg {
  color: #6b7280;
}

.form-input {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  background: white;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-input.error {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
  background: #fef2f2;
}

.form-input::placeholder {
  color: #9ca3af;
}

/* 비밀번호 입력 */
.password-input-wrapper {
  position: relative;
}

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

/* 에러 텍스트 */
.error-text {
  color: #dc2626;
  font-size: 14px;
  font-weight: 500;
}

/* 로그인 버튼 */
.login-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-button:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 에러 배너 */
.error-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
}

/* 하단 링크 */
.login-footer {
  margin-top: 24px;
  text-align: center;
}

.footer-links {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.footer-link {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: #2563eb;
}

.footer-link.primary {
  color: #2563eb;
}

.link-divider {
  width: 1px;
  height: 16px;
  background: #e2e8f0;
}

/* 오른쪽 브랜딩 섹션 */
.branding-section {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: white;
}

.branding-content {
  max-width: 500px;
  text-align: center;
}

/* 브랜드 로고 */
.brand-logo {
  margin-bottom: 32px;
}

.logo-image {
  height: 60px;
  width: auto;
  filter: brightness(0) invert(1);
}

/* 브랜드 메시지 */
.brand-message {
  margin-bottom: 48px;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0 0 16px 0;
  line-height: 1.2;
}

.brand-description {
  font-size: 18px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.6;
}

/* 기능 하이라이트 */
.feature-highlights {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 48px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.feature-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  flex-shrink: 0;
}

.feature-text h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.feature-text p {
  font-size: 14px;
  opacity: 0.8;
  margin: 0;
}

/* 채팅 데모 */
.chat-demo {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.chat-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.chat-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  display: flex;
}

.user-message {
  justify-content: flex-end;
}

.ai-message {
  justify-content: flex-start;
}

.message-content {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.4;
}

.user-message .message-content {
  background: rgba(255, 255, 255, 0.2);
}

.ai-message .message-content {
  background: rgba(255, 255, 255, 0.9);
  color: #374151;
}

.code-block {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.4;
  margin: 0;
  text-align: left;
  white-space: pre-wrap;
  color: #1f2937;
}

/* 애니메이션 */
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 반응형 */
@media (max-width: 1024px) {
  .branding-section {
    display: none;
  }

  .login-section {
    flex: none;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .login-section {
    padding: 16px;
  }

  .login-content {
    max-width: 100%;
  }

  .login-title {
    font-size: 28px;
  }

  .footer-links {
    flex-direction: column;
    gap: 12px;
  }

  .link-divider {
    display: none;
  }
}

.login-container {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
}

/* 왼쪽 로그인 섹션 */
.login-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.login-wrapper {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 로고 */
.login-logo {
  margin-bottom: 32px;
}

.logo {
  height: 48px;
  width: auto;
}

/* 제목 섹션 */
.login-title-section {
  text-align: center;
  margin-bottom: 40px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

/* 폼 컨테이너 */
.login-form-container {
  width: 100%;
  background: #fafafa;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #e5e5e5;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 소셜 버튼 */
.social-buttons {
  display: flex;
  gap: 12px;
}

.social-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  color: #333;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.social-btn:hover {
  border-color: #bbb;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.social-btn img {
  width: 18px;
  height: 18px;
}

/* 구분선 */
.divider {
  position: relative;
  text-align: center;
  margin: 8px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #ddd;
}

.divider span {
  background: #fafafa;
  color: #888;
  font-size: 14px;
  padding: 0 16px;
  position: relative;
}

/* 입력 필드 */
.input-fields {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-field {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  color: #333;
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
  color: #999;
}

/* 비밀번호 래퍼 */
.password-wrapper {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #888;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #555;
}

/* 필드 에러 */
.field-error {
  color: #ef4444;
  font-size: 13px;
  font-weight: 500;
}

/* 로그인 버튼 */
.login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  background: var(--main-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.login-btn:hover:not(:disabled) {
  background: var(--main-color-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 에러 알림 */
.error-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
}

/* 폼 하단 */
.form-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e5e5;
}

.footer-link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: #2563eb;
}

.footer-link.primary {
  color: #2563eb;
}

.separator {
  color: #ccc;
  font-size: 14px;
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
@media (max-width: 1024px) {
  .branding-section {
    display: none;
  }

  .login-section {
    flex: none;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .login-section {
    padding: 24px 16px;
  }

  .login-form-container {
    padding: 24px 20px;
  }

  .social-buttons {
    flex-direction: column;
  }

  .form-footer {
    flex-direction: column;
    gap: 8px;
  }

  .separator {
    display: none;
  }
}
</style>