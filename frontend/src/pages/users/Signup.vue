<template>
  <div class="signup-container">
    <!-- 왼쪽 회원가입 폼 -->
    <div class="signup-section">
      <div class="signup-wrapper">

        <!-- 회원가입 폼 -->
        <div class="signup-form-container">
          <form class="signup-form" @submit.prevent="signUp">
            <!-- 소셜 회원가입 -->
            <div class="social-buttons">
              <button class="social-btn google" type="button" @click="googleSignUp">
                <img alt="Google" src="/src/assets/images/google_logo.png"/>
                Google
              </button>
              <button class="social-btn github" type="button" @click="githubSignUp">
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
              <!-- 이름 -->
              <div class="input-group">
                <input
                  id="name"
                  v-model="name"
                  :class="['input-field', { error: nameError }]"
                  placeholder="이름을 입력해주세요"
                  type="text"
                  @input="clearError('name')"
                />
                <span v-if="nameError" class="field-error">
                  {{ nameError }}
                </span>
              </div>

              <!-- 이메일 -->
              <div class="input-group">
                <div class="email-wrapper">
                  <input
                    id="email"
                    v-model="email"
                    :class="['input-field', 'email-input', {
                      error: emailError,
                      success: emailSuccess
                    }]"
                    :readonly="emailSuccess"
                    placeholder="이메일 주소를 입력해주세요"
                    type="email"
                    @input="clearError('email')"
                  />
                  <button
                    :disabled="emailSuccess || isCheckingEmail"
                    class="email-check-btn"
                    type="button"
                    @click="checkDuplicate"
                  >
                    {{ isCheckingEmail ? '확인중...' : '중복확인' }}
                  </button>
                </div>
                <span v-if="emailError" class="field-error">
                  {{ emailError }}
                </span>
                <span v-if="emailSuccessMsg" class="field-success">
                  {{ emailSuccessMsg }}
                </span>
              </div>

              <!-- 비밀번호 -->
              <div class="input-group">
                <div class="password-wrapper">
                  <input
                    id="password"
                    v-model="password"
                    :class="['input-field', { error: passwordError }]"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="비밀번호 (10자 이상, 대문자·특수문자 포함)"
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
                <span v-if="passwordError" class="field-error">
                  {{ passwordError }}
                </span>
              </div>

              <!-- 비밀번호 확인 -->
              <div class="input-group">
                <div class="password-wrapper">
                  <input
                    id="passwordCheck"
                    v-model="passwordCheck"
                    :class="['input-field', { error: passwordCheckError }]"
                    :type="showPasswordCheck ? 'text' : 'password'"
                    placeholder="비밀번호를 다시 입력해주세요"
                    @input="clearError('passwordCheck')"
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
              </div>

              <!-- 전화번호 -->
              <div class="input-group">
                <input
                  id="phone"
                  v-model="phoneValue"
                  :class="['input-field', { error: phoneError }]"
                  maxlength="13"
                  placeholder="전화번호를 입력해주세요"
                  type="tel"
                  @input="handlePhoneInput"
                />
                <span v-if="phoneError" class="field-error">
                  {{ phoneError }}
                </span>
              </div>
            </div>

            <!-- 회원가입 버튼 -->
            <button :disabled="isLoading" class="signup-btn" type="submit">
              <svg v-if="isLoading" class="spinner" fill="none" height="18" stroke="currentColor" stroke-width="2"
                   viewBox="0 0 24 24" width="18">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              {{ isLoading ? '회원가입 중...' : '회원가입' }}
            </button>

            <!-- 전체 에러 메시지 -->
            <div v-if="generalError" class="error-alert">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" x2="9" y1="9" y2="15"/>
                <line x1="9" x2="15" y1="9" y2="15"/>
              </svg>
              {{ generalError }}
            </div>
          </form>

          <!-- 하단 링크 -->
          <div class="form-footer">
            <a class="footer-link primary" href="/user/login">
              이미 계정이 있으신가요? 로그인
            </a>
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
          <h2 class="brand-title">처음 오셨군요! 👋</h2>
          <p class="brand-description">
            이제부터 배포는 더 쉽고, 더 똑똑해집니다.<br/>
            당신의 DevOps 여정에 AI가 함께합니다.
          </p>
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
          <div class="feature-item">
            <div class="feature-icon">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div class="feature-text">
              <h4>간편한 배포</h4>
              <p>클릭 한 번으로 배포 완료</p>
            </div>
          </div>
        </div>

        <!-- 시작하기 버튼 -->
        <div class="cta-section">
          <p class="cta-text">지금 바로 시작해보세요!</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, watch} from 'vue';
import {userApi} from "@/api/UserApi.js";

// Form 데이터
const name = ref("");
const email = ref("");
const password = ref("");
const passwordCheck = ref("");
const phoneValue = ref("");

// UI 상태
const showPassword = ref(false);
const showPasswordCheck = ref(false);
const isLoading = ref(false);
const isCheckingEmail = ref(false);

// 이메일 관련
const emailSuccess = ref(false);
const emailSuccessMsg = ref("");

// 에러 메시지
const nameError = ref("");
const emailError = ref("");
const passwordError = ref("");
const passwordCheckError = ref("");
const phoneError = ref("");
const generalError = ref("");

// 전화번호 포맷팅
const handlePhoneInput = (e) => {
  let numbersOnly = e.target.value.replace(/\D/g, '');
  numbersOnly = numbersOnly.slice(0, 11);

  if (numbersOnly.length <= 3) {
    phoneValue.value = numbersOnly;
  } else if (numbersOnly.length <= 7) {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d+)/, '$1-$2');
  } else {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
  }

  e.target.value = phoneValue.value;
};

// 전화번호 자동 포맷팅
watch(phoneValue, (newVal) => {
  if (newVal.length === 10) {
    phoneValue.value = newVal.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  } else if (newVal.length === 11) {
    phoneValue.value = newVal.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  }
});

// 유효성 검사 함수들
const isValidEmail = (email) => {
  return /^[\w-.]+@([\w-]+\.)+[\w-]{2,}$/.test(email);
};

const isValidPassword = (pw) => {
  return /^(?=.*[A-Z])(?=.*[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]).{10,}$/.test(pw);
};

// 에러 클리어
const clearError = (field) => {
  switch (field) {
    case 'name':
      nameError.value = "";
      break;
    case 'email':
      emailError.value = "";
      emailSuccess.value = false;
      emailSuccessMsg.value = "";
      break;
    case 'password':
      passwordError.value = "";
      break;
    case 'passwordCheck':
      passwordCheckError.value = "";
      break;
    case 'phone':
      phoneError.value = "";
      break;
  }
  generalError.value = "";
};

// 이메일 중복 확인
const checkDuplicate = async () => {
  emailError.value = "";
  emailSuccessMsg.value = "";

  if (!email.value) {
    emailError.value = "이메일을 입력해주세요.";
    return;
  }

  if (!isValidEmail(email.value)) {
    emailError.value = "올바른 이메일 주소를 입력해주세요.";
    return;
  }

  isCheckingEmail.value = true;

  try {
    console.log("rer");
    const isAvailable = await userApi.checkDuplicate(email.value);
    console.log(isAvailable);
    if (isAvailable) {
      emailSuccess.value = true;
      emailSuccessMsg.value = "사용가능한 이메일입니다.";
    } else {
      emailError.value = "이미 사용중인 이메일입니다.";
    }
  } catch (error) {
    emailError.value = "이미 사용중인 이메일입니다.";
  } finally {
    isCheckingEmail.value = false;
  }
};

// 회원가입
const signUp = async () => {
  // 에러 초기화
  nameError.value = "";
  emailError.value = "";
  passwordError.value = "";
  passwordCheckError.value = "";
  phoneError.value = "";
  generalError.value = "";

  let valid = true;

  // 이름 검증
  if (!name.value.trim()) {
    nameError.value = "이름을 입력해주세요.";
    valid = false;
  }

  // 이메일 검증
  if (!emailSuccess.value) {
    emailError.value = "이메일 중복확인을 해주세요.";
    valid = false;
  }

  // 비밀번호 검증
  if (!password.value) {
    passwordError.value = "비밀번호를 입력해주세요.";
    valid = false;
  } else if (!isValidPassword(password.value)) {
    passwordError.value = "비밀번호는 10자 이상, 대문자와 특수문자를 각각 1개 이상 포함해야 합니다.";
    valid = false;
  }

  // 비밀번호 확인 검증
  if (!passwordCheck.value) {
    passwordCheckError.value = "비밀번호 확인을 입력해주세요.";
    valid = false;
  } else if (passwordCheck.value !== password.value) {
    passwordCheckError.value = "비밀번호가 일치하지 않습니다.";
    valid = false;
  }

  // 전화번호 검증
  const onlyNumberPhone = phoneValue.value.replace(/\D/g, "");
  if (!onlyNumberPhone) {
    phoneError.value = "전화번호를 입력해주세요.";
    valid = false;
  } else if (onlyNumberPhone.length < 10 || onlyNumberPhone.length > 11) {
    phoneError.value = "전화번호를 정확히 입력해주세요.";
    valid = false;
  }

  if (!valid) return;

  isLoading.value = true;

  const signUpRequest = {
    name: name.value,
    email: email.value,
    password: password.value,
    phoneNumber: phoneValue.value,
  };

  try {
    await userApi.signup(signUpRequest);

    alert("회원가입 성공!\n인증 이메일이 발송되었습니다!");

    name.value = "";
    email.value = "";
    password.value = "";
    passwordCheck.value = "";
    phoneValue.value = "";
    emailSuccess.value = false;
    emailSuccessMsg.value = "";

  } catch (error) {
    generalError.value = "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.";
  } finally {
    isLoading.value = false;
  }
};

// 소셜 회원가입
const googleSignUp = () => {
  window.location.href = "https://www.pipely.kro.kr/oauth2/authorization/google";
};

const githubSignUp = () => {
  window.location.href = "https://www.pipely.kro.kr/oauth2/authorization/github";
};
</script>

<style scoped>
.signup-container {
  display: flex;
  min-height: 100vh;
  background: #f8fafc;
}

/* 왼쪽 회원가입 섹션 */
.signup-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
}

.signup-wrapper {
  width: 100%;
  max-width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 로고 */
.signup-logo {
  margin-bottom: 32px;
}

.logo {
  height: 48px;
  width: auto;
}

/* 폼 컨테이너 */
.signup-form-container {
  width: 100%;
  background: #fafafa;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #e5e5e5;
}

.signup-form {
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

.input-field.success {
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.input-field::placeholder {
  color: #999;
}

/* 이메일 래퍼 */
.email-wrapper {
  display: flex;
  gap: 8px;
}

.email-input {
  flex: 1;
}

.email-check-btn {
  padding: 14px 16px;
  background: var(--main-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.email-check-btn:hover:not(:disabled) {
  background: var(--main-color-hover);
}

.email-check-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

/* 에러/성공 메시지 */
.field-error {
  color: #ef4444;
  font-size: 13px;
  font-weight: 500;
}

.field-success {
  color: #10b981;
  font-size: 13px;
  font-weight: 500;
}

/* 회원가입 버튼 */
.signup-btn {
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

.signup-btn:hover:not(:disabled) {
  background: var(--main-color-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.signup-btn:disabled {
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
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e5e5;
}

.footer-link {
  color: var(--main-color);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s ease;
}

.footer-link:hover {
  color: var(--main-color-hover);
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

/* CTA 섹션 */
.cta-section {
  text-align: center;
}

.cta-text {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
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

  .signup-section {
    flex: none;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .signup-section {
    padding: 24px 16px;
  }

  .signup-form-container {
    padding: 24px 20px;
  }

  .social-buttons {
    flex-direction: column;
  }

  .email-wrapper {
    flex-direction: column;
  }

  .email-check-btn {
    align-self: stretch;
  }
}
</style>
