<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {useRouter} from "vue-router";
import {userApi} from "@/api/UserApi.js";
import {AlertCircle, CheckCircle, Eye, EyeOff, Loader2, Lock, Phone, User} from 'lucide-vue-next';

const router = useRouter();

// form 객체
const name = ref("");
const password = ref("");
const phoneValue = ref("");
const passwordCheck = ref("");

// 에러메시지
const nameError = ref("");
const passwordError = ref("");
const phoneError = ref("");
const passwordCheckError = ref("");

// UI 상태
const isLoading = ref(false);
const showPassword = ref(false);
const showPasswordCheck = ref(false);
const isFormTouched = ref(false);

// 비밀번호 강도 체크
const passwordStrength = computed(() => {
  if (!password.value) return {score: 0, text: '', color: ''};

  let score = 0;
  const checks = {
    length: password.value.length >= 10,
    uppercase: /[A-Z]/.test(password.value),
    lowercase: /[a-z]/.test(password.value),
    number: /\d/.test(password.value),
    special: /[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]/.test(password.value)
  };

  score = Object.values(checks).filter(Boolean).length;

  if (score < 3) return {score, text: '약함', color: '#ef4444', checks};
  if (score < 4) return {score, text: '보통', color: '#f59e0b', checks};
  if (score < 5) return {score, text: '강함', color: '#10b981', checks};
  return {score, text: '매우 강함', color: '#059669', checks};
});

// 실시간 유효성 검사
const nameValid = computed(() => name.value.trim().length >= 2);
const passwordValid = computed(() => isValidPassword(password.value));
const passwordCheckValid = computed(() => passwordCheck.value && passwordCheck.value === password.value);
const phoneValid = computed(() => {
  const onlyNumbers = phoneValue.value.replace(/\D/g, "");
  return onlyNumbers.length >= 10 && onlyNumbers.length <= 11;
});

const isFormValid = computed(() => {
  return nameValid.value && passwordValid.value && passwordCheckValid.value && phoneValid.value;
});

// 전화번호 포맷팅
const handlePress = (e) => {
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

watch(phoneValue, (newVal) => {
  const numbersOnly = newVal.replace(/\D/g, '');
  if (numbersOnly.length === 10) {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  } else if (numbersOnly.length === 11) {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  }
});

// 폼 터치 감지
watch([name, password, passwordCheck, phoneValue], () => {
  if (!isFormTouched.value) {
    isFormTouched.value = true;
  }
});

function isValidPassword(pw) {
  return /^(?=.*[A-Z])(?=.*[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]).{10,}$/.test(pw);
}

const signUp = async () => {
  // 에러 초기화
  nameError.value = "";
  passwordError.value = "";
  phoneError.value = "";
  passwordCheckError.value = "";

  let valid = true;

  // 이름 검증
  if (!name.value.trim()) {
    nameError.value = "이름을 입력해주세요.";
    valid = false;
  } else if (name.value.trim().length < 2) {
    nameError.value = "이름은 2자 이상 입력해주세요.";
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

  try {
    const signUpRequest = {
      name: name.value.trim(),
      password: password.value,
      phoneNumber: phoneValue.value,
    };

    const response = await userApi.oAuthSignup(signUpRequest);

    if (response.status === 200) {
      // 성공 메시지와 함께 리다이렉트
      if (window.opener && !window.opener.closed) {
        window.opener.postMessage({type: 'SIGNUP_SUCCESS'}, '*');
        window.close();
      } else {
        router.push({
          name: "Login",
          query: {message: 'signup_success'}
        });
      }
    } else {
      throw new Error('회원가입에 실패했습니다.');
    }
  } catch (error) {
    console.error('Signup error:', error);
    alert(error.message || "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  if (window.opener && !window.opener.closed) {
    window.close();
  } else {
    router.push({name: "Login"});
  }
};

onMounted(() => {
  // URL 파라미터로 자동 회원가입 모드인지 확인
  const urlParams = new URLSearchParams(window.location.search);
  const autoSignup = urlParams.get('auto');

  if (!autoSignup) {
    const isOk = confirm("계정이 없습니다. 회원가입하시겠습니까?");
    if (!isOk) {
      handleCancel();
    }
  }
});
</script>

<template>
  <div class="signup-container">
    <div class="signup-wrapper">
      <div class="signup-header">
        <img alt="logo" class="logo" src="/src/assets/images/logo.png"/>
        <h1 class="title">회원가입</h1>
        <p class="subtitle">새로운 계정을 만들어 시작해보세요</p>
      </div>
      <form class="signup-form" @submit.prevent="signUp">
        <div class="input-group">
          <label class="input-label" for="name">이름</label>
          <div class="input-wrapper">
            <div class="input-icon">
              <User :size="20"/>
            </div>
            <input
              id="name"
              v-model="name"
              :class="[
                'form-input',
                {
                  'error': nameError && isFormTouched,
                  'success': nameValid && isFormTouched && name
                }
              ]"
              name="name"
              placeholder="이름을 입력해주세요"
              type="text"
            />
            <div v-if="nameValid && isFormTouched && name" class="success-icon">
              <CheckCircle :size="20"/>
            </div>
          </div>
          <p v-if="nameError && isFormTouched" class="error-message">
            <AlertCircle :size="16"/>
            {{ nameError }}
          </p>
        </div>

        <div class="input-group">
          <label class="input-label" for="password">비밀번호</label>
          <div class="input-wrapper">
            <div class="input-icon">
              <Lock :size="20"/>
            </div>
            <input
              id="password"
              v-model="password"
              :class="[
                'form-input',
                {
                  'error': passwordError && isFormTouched,
                  'success': passwordValid && isFormTouched && password
                }
              ]"
              :type="showPassword ? 'text' : 'password'"
              name="password"
              placeholder="비밀번호를 입력해주세요"
            />
            <button
              class="password-toggle"
              type="button"
              @click="showPassword = !showPassword"
            >
              <Eye v-if="!showPassword" :size="20"/>
              <EyeOff v-else :size="20"/>
            </button>
          </div>

          <div v-if="password && isFormTouched" class="password-strength">
            <div class="strength-bar">
              <div
                :style="{
                  width: `${(passwordStrength.score / 5) * 100}%`,
                  backgroundColor: passwordStrength.color
                }"
                class="strength-fill"
              ></div>
            </div>
            <div class="strength-info">
              <span :style="{ color: passwordStrength.color }">
                {{ passwordStrength.text }}
              </span>
              <div class="strength-requirements">
                <div class="requirement-list">
                  <div :class="['requirement', { met: passwordStrength.checks?.length }]">
                    <CheckCircle v-if="passwordStrength.checks?.length" :size="12"/>
                    <div v-else class="requirement-dot"></div>
                    10자 이상
                  </div>
                  <div :class="['requirement', { met: passwordStrength.checks?.uppercase }]">
                    <CheckCircle v-if="passwordStrength.checks?.uppercase" :size="12"/>
                    <div v-else class="requirement-dot"></div>
                    대문자 포함
                  </div>
                  <div :class="['requirement', { met: passwordStrength.checks?.special }]">
                    <CheckCircle v-if="passwordStrength.checks?.special" :size="12"/>
                    <div v-else class="requirement-dot"></div>
                    특수문자 포함
                  </div>
                </div>
              </div>
            </div>
          </div>

          <p v-if="passwordError && isFormTouched" class="error-message">
            <AlertCircle :size="16"/>
            {{ passwordError }}
          </p>
        </div>

        <div class="input-group">
          <label class="input-label" for="password_check">비밀번호 확인</label>
          <div class="input-wrapper">
            <div class="input-icon">
              <Lock :size="20"/>
            </div>
            <input
              id="password_check"
              v-model="passwordCheck"
              :class="[
                'form-input',
                {
                  'error': passwordCheckError && isFormTouched,
                  'success': passwordCheckValid && isFormTouched && passwordCheck
                }
              ]"
              :type="showPasswordCheck ? 'text' : 'password'"
              name="password_check"
              placeholder="비밀번호를 다시 입력해주세요"
            />
            <button
              class="password-toggle"
              type="button"
              @click="showPasswordCheck = !showPasswordCheck"
            >
              <Eye v-if="!showPasswordCheck" :size="20"/>
              <EyeOff v-else :size="20"/>
            </button>
            <div v-if="passwordCheckValid && isFormTouched && passwordCheck" class="success-icon">
              <CheckCircle :size="20"/>
            </div>
          </div>
          <p v-if="passwordCheckError && isFormTouched" class="error-message">
            <AlertCircle :size="16"/>
            {{ passwordCheckError }}
          </p>
        </div>

        <div class="input-group">
          <label class="input-label" for="phone">전화번호</label>
          <div class="input-wrapper">
            <div class="input-icon">
              <Phone :size="20"/>
            </div>
            <input
              id="phone"
              v-model="phoneValue"
              :class="[
                'form-input',
                {
                  'error': phoneError && isFormTouched,
                  'success': phoneValid && isFormTouched && phoneValue
                }
              ]"
              maxlength="13"
              name="phone"
              placeholder="010-1234-5678"
              type="tel"
              @input="handlePress"
            />
            <div v-if="phoneValid && isFormTouched && phoneValue" class="success-icon">
              <CheckCircle :size="20"/>
            </div>
          </div>
          <p v-if="phoneError && isFormTouched" class="error-message">
            <AlertCircle :size="16"/>
            {{ phoneError }}
          </p>
        </div>

        <div class="button-group">
          <button
            :class="['btn', 'btn-primary', { loading: isLoading }]"
            :disabled="isLoading || !isFormValid"
            type="submit"
          >
            <Loader2 v-if="isLoading" :size="20" class="animate-spin"/>
            <span>{{ isLoading ? '회원가입 중...' : '회원가입' }}</span>
          </button>

          <button
            :disabled="isLoading"
            class="btn btn-secondary"
            type="button"
            @click="handleCancel"
          >
            취소
          </button>
        </div>
      </form>

      <div class="signup-footer">
        <p>이미 계정이 있으신가요?
          <router-link class="login-link" to="/user/login">로그인</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.signup-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.signup-wrapper {
  width: 100%;
  max-width: 480px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.signup-header {
  text-align: center;
  padding: 40px 40px 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.logo {
  width: 80px;
  height: auto;
  margin-bottom: 20px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 16px;
  color: #64748b;
  margin: 0;
}

.signup-form {
  padding: 40px;
}

.input-group {
  margin-bottom: 24px;
}

.input-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  z-index: 2;
  color: #9ca3af;
  pointer-events: none;
}

.form-input {
  width: 100%;
  padding: 16px 16px 16px 48px;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  font-size: 16px;
  color: #1f2937;
  background: #ffffff;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-input.error {
  border-color: #ef4444;
  background: #fef2f2;
}

.form-input.success {
  border-color: #10b981;
  background: #f0fdf4;
  padding-right: 48px;
}

.form-input::placeholder {
  color: #9ca3af;
}

.password-toggle {
  position: absolute;
  right: 16px;
  z-index: 2;
  background: none;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s ease;
}

.password-toggle:hover {
  color: #6b7280;
}

.success-icon {
  position: absolute;
  right: 16px;
  z-index: 2;
  color: #10b981;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #ef4444;
  font-size: 14px;
  margin-top: 8px;
  margin-bottom: 0;
}

.password-strength {
  margin-top: 12px;
}

.strength-bar {
  width: 100%;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 8px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 12px;
}

.strength-requirements {
  text-align: right;
}

.requirement-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.requirement {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  transition: color 0.2s ease;
}

.requirement.met {
  color: #10b981;
}

.requirement-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid #d1d5db;
  flex-shrink: 0;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 16px 24px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  box-shadow: 0 4px 6px -1px rgba(59, 130, 246, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 8px -1px rgba(59, 130, 246, 0.4);
}

.btn-primary:disabled {
  background: #9ca3af;
  box-shadow: none;
}

.btn-secondary {
  background: #f8fafc;
  color: #64748b;
  border: 2px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #f1f5f9;
  border-color: #cbd5e1;
  transform: translateY(-1px);
}

.signup-footer {
  text-align: center;
  padding: 20px 40px 40px;
  background: #f8fafc;
  border-top: 1px solid #e2e8f0;
}

.signup-footer p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.login-link {
  color: #3b82f6;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s ease;
}

.login-link:hover {
  color: #2563eb;
  text-decoration: underline;
}

.animate-spin {
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

/* 반응형 디자인 */
@media (max-width: 640px) {
  .signup-container {
    padding: 10px;
  }

  .signup-wrapper {
    max-width: 100%;
  }

  .signup-header {
    padding: 30px 20px 15px;
  }

  .title {
    font-size: 24px;
  }

  .subtitle {
    font-size: 14px;
  }

  .signup-form {
    padding: 30px 20px;
  }

  .form-input {
    padding: 14px 14px 14px 44px;
    font-size: 16px; /* iOS에서 줌 방지 */
  }

  .input-icon {
    left: 14px;
  }

  .password-toggle,
  .success-icon {
    right: 14px;
  }

  .signup-footer {
    padding: 15px 20px 30px;
  }

  .requirement-list {
    align-items: flex-end;
  }

  .strength-info {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
}

/* 접근성 개선 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 다크 모드 지원 */
@media (prefers-color-scheme: dark) {
  .signup-wrapper {
    background: #1f2937;
    color: #f9fafb;
  }

  .signup-header {
    background: linear-gradient(135deg, #374151 0%, #4b5563 100%);
  }

  .title {
    color: #f9fafb;
  }

  .subtitle {
    color: #d1d5db;
  }

  .input-label {
    color: #e5e7eb;
  }

  .form-input {
    background: #374151;
    border-color: #4b5563;
    color: #f9fafb;
  }

  .form-input:focus {
    border-color: #60a5fa;
  }

  .signup-footer {
    background: #374151;
    border-color: #4b5563;
  }
}
</style>