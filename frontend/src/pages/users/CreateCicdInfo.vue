<script setup>
import {computed, ref} from "vue";
import {useRouter} from 'vue-router';
import {userApi} from "@/api/UserApi.js";
import {useUserStore} from "@/stores/useUserStore.js";
import {jenkinsInfoApi} from "@/api/JenkinsInfoApi.js";

const router = useRouter();
const userStore = useUserStore();

// 로딩 상태
const isLoading = ref(false);
const isTestingConnection = ref(false);
const connectionStatus = ref(null); // null, 'success', 'error'

// 폼 데이터
const formData = ref({
  name: "",
  description: "",
  jenkinsId: "",
  uri: "",
  apiToken: "",
  connected: false,
});

// 폼 에러
const errors = ref({});

// 폼 검증
const validateForm = () => {
  const newErrors = {};

  if (!formData.value.name.trim()) {
    newErrors.name = "정보 닉네임을 입력해주세요.";
  }

  if (!formData.value.uri.trim()) {
    formData.value.uri = formData.value.uri.replace(/\/+$/, "");
    newErrors.uri = "Jenkins URL을 입력해주세요.";
  } else if (!isValidUrl(formData.value.uri)) {
    newErrors.uri = "올바른 URL 형식을 입력해주세요.";
  }

  if (!formData.value.jenkinsId.trim()) {
    newErrors.jenkinsId = "Jenkins ID를 입력해주세요.";
  }

  if (!formData.value.apiToken.trim()) {
    newErrors.apiToken = "API Token을 입력해주세요.";
  }

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

// URL 유효성 검사
const isValidUrl = (string) => {
  try {
    new URL(string);
    return true;
  } catch (_) {
    return false;
  }
};

// 폼이 유효한지 확인
const isFormValid = computed(() => {
  return formData.value.name.trim() &&
    formData.value.uri.trim() &&
    formData.value.jenkinsId.trim() &&
    formData.value.apiToken.trim();
});

// 연결 테스트
const testConnection = async () => {
  if (!formData.value.uri.trim() || !formData.value.jenkinsId.trim() || !formData.value.apiToken.trim()) {
    alert("연결 테스트를 위해 URL, ID, API Token을 모두 입력해주세요.");
    return;
  }

  isTestingConnection.value = true;
  connectionStatus.value = null;

  try {
    // API 호출 (실제 연결 테스트 로직)
    const testData = {
      uri: formData.value.uri,
      jenkinsId: formData.value.jenkinsId,
      apiToken: formData.value.apiToken,
    };

    const response = await jenkinsInfoApi.verify(testData);

    if (response) {
      connectionStatus.value = 'success';
      formData.value.connected = true;
    } else {
      connectionStatus.value = 'error';
      formData.value.connected = false;
    }
  } catch (error) {
    connectionStatus.value = 'error';
    formData.value.connected = false;
  } finally {
    isTestingConnection.value = false;
  }
};

// 폼 제출
const createInfo = async () => {
  if (!validateForm()) {
    return;
  }

  isLoading.value = true;

  try {
    const response = await userApi.createInfo(formData.value);

    if (response.status === 200) {
      await userStore.fetchUserInfo();
      alert("Jenkins 정보가 등록되었습니다!");
      router.push({name: "Mypage"});
    } else {
      alert("Jenkins 정보 등록 실패!\n다시 시도해주세요");
    }
  } catch (error) {
    alert("오류가 발생했습니다. 다시 시도해주세요.");
  } finally {
    isLoading.value = false;
  }
};

// 취소
const handleCancelClick = () => {
  const hasChanges = Object.entries(formData.value).some(([key, value]) => {
    if (typeof value !== 'string') return false;
    return value.trim() !== '';
  });


  if (hasChanges) {
    const confirmed = confirm('작성 중인 내용이 있습니다.\n정말로 취소하시겠습니까?');
    if (!confirmed) return;
  }

  router.back();
};

// 입력 시 에러 제거
const clearError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field];
  }
};
</script>

<template>
  <div class="container">
    <!-- 헤더 -->
    <div class="header">
      <h1>새 Jenkins 서버 추가</h1>
      <div class="breadcrumb">
        <span class="breadcrumb-item">홈</span>
        <span class="breadcrumb-separator">></span>
        <button class="breadcrumb-item" @click="router.push({name: 'Mypage'})">
          마이페이지
        </button>
        <span class="breadcrumb-separator">></span>
        <span class="breadcrumb-item">CI/CD 정보</span>
        <span class="breadcrumb-separator">></span>
        <span class="breadcrumb-item current">새 서버 추가</span>
      </div>
    </div>

    <!-- 메인 컨텐츠 -->
    <div class="content">
      <div class="form-section">
        <div class="section-header">
          <h3 class="section-title">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
              <line x1="8" x2="16" y1="21" y2="21"/>
              <line x1="12" x2="12" y1="17" y2="21"/>
            </svg>
            Jenkins 서버 정보
          </h3>
          <p class="section-description">
            새로운 Jenkins 서버를 추가하여 CI/CD 파이프라인을 구성하세요.
          </p>
        </div>

        <form class="form" @submit.prevent="createInfo">
          <!-- 기본 정보 -->
          <div class="form-group">
            <label class="form-label required" for="name">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              정보 닉네임
            </label>
            <input
              id="name"
              v-model="formData.name"
              :class="['form-input', { error: errors.name }]"
              placeholder="예: 개발서버 Jenkins"
              type="text"
              @input="clearError('name')"
            />
            <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
            <span class="help-text">Jenkins 서버를 구분할 수 있는 이름을 입력하세요.</span>
          </div>

          <div class="form-group">
            <label class="form-label" for="description">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14,2 14,8 20,8"/>
                <line x1="16" x2="8" y1="13" y2="13"/>
                <line x1="16" x2="8" y1="17" y2="17"/>
                <polyline points="10,9 9,9 8,9"/>
              </svg>
              설명
            </label>
            <textarea
              id="description"
              v-model="formData.description"
              class="form-textarea"
              placeholder="Jenkins 서버에 대한 설명을 입력하세요. (선택사항)"
              @input="clearError('description')"
            ></textarea>
            <span class="help-text">이 Jenkins 서버의 용도나 특징을 간단히 설명해주세요.</span>
          </div>

          <!-- 연결 정보 -->
          <div class="connection-section">
            <h4 class="subsection-title">
              <svg fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
              연결 정보
            </h4>

            <div class="form-group">
              <label class="form-label required" for="uri">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
                Jenkins URL
              </label>
              <input
                id="uri"
                v-model="formData.uri"
                :class="['form-input', { error: errors.uri }]"
                placeholder="https://jenkins.example.com"
                type="url"
                @input="clearError('uri')"
              />
              <span v-if="errors.uri" class="error-message">{{ errors.uri }}</span>
              <span class="help-text">Jenkins 서버의 전체 URL을 입력하세요.</span>
            </div>

            <div class="form-group">
              <label class="form-label required" for="jenkinsId">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                Jenkins 사용자 ID
              </label>
              <input
                id="jenkinsId"
                v-model="formData.jenkinsId"
                :class="['form-input', { error: errors.jenkinsId }]"
                placeholder="jenkins_user"
                type="text"
                @input="clearError('jenkinsId')"
              />
              <span v-if="errors.jenkinsId" class="error-message">{{ errors.jenkinsId }}</span>
              <span class="help-text">Jenkins에 로그인할 때 사용하는 사용자 ID입니다.</span>
            </div>

            <div class="form-group">
              <label class="form-label required" for="apiToken">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <rect height="11" rx="2" ry="2" width="18" x="3" y="11"/>
                  <circle cx="12" cy="16" r="1"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                API Token
              </label>
              <input
                id="apiToken"
                v-model="formData.apiToken"
                :class="['form-input', { error: errors.apiToken }]"
                placeholder="11abcdef1234567890abcdef1234567890"
                type="password"
                @input="clearError('apiToken')"
              />
              <span v-if="errors.apiToken" class="error-message">{{ errors.apiToken }}</span>
              <div class="help-text">
                Jenkins 사용자 설정에서 생성한 API Token을 입력하세요.
              </div>
            </div>

            <!-- 연결 테스트 -->
            <div class="connection-test">
              <button
                :disabled="isTestingConnection || !formData.uri || !formData.jenkinsId || !formData.apiToken"
                class="test-connection-btn"
                type="button"
                @click="testConnection"
              >
                <svg v-if="isTestingConnection" class="animate-spin" fill="none" height="16" stroke="currentColor"
                     stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="16">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22,4 12,14.01 9,11.01"/>
                </svg>
                {{ isTestingConnection ? '연결 확인 중...' : '연결 테스트' }}
              </button>

              <div v-if="connectionStatus" :class="connectionStatus" class="connection-result">
                <div class="result-icon">
                  <svg v-if="connectionStatus === 'success'" fill="none" height="16" stroke="currentColor"
                       stroke-width="2"
                       viewBox="0 0 24 24" width="16">
                    <polyline points="20,6 9,17 4,12"/>
                  </svg>
                  <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                       width="16">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" x2="9" y1="9" y2="15"/>
                    <line x1="9" x2="15" y1="9" y2="15"/>
                  </svg>
                </div>
                <span class="result-text">
                  {{
                    connectionStatus === 'success' ? 'Jenkins 서버에 성공적으로 연결되었습니다!' : 'Jenkins 서버 연결에 실패했습니다. 정보를 확인해주세요.'
                  }}
                </span>
              </div>
            </div>
          </div>

          <!-- 액션 버튼 -->
          <div class="form-actions">
            <button
              :disabled="isLoading"
              class="btn btn-secondary"
              type="button"
              @click="handleCancelClick"
            >
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <line x1="18" x2="6" y1="6" y2="18"/>
                <line x1="6" x2="18" y1="6" y2="18"/>
              </svg>
              취소
            </button>
            <button
              :disabled="isLoading || !isFormValid"
              class="btn btn-primary"
              type="submit"
            >
              <svg v-if="isLoading" class="animate-spin" fill="none" height="16" stroke="currentColor" stroke-width="2"
                   viewBox="0 0 24 24" width="16">
                <path d="M21 12a9 9 0 11-6.219-8.56"/>
              </svg>
              <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17,21 17,13 7,13 7,21"/>
                <polyline points="7,3 7,8 15,8"/>
              </svg>
              {{ isLoading ? '생성 중...' : 'Jenkins 서버 추가' }}
            </button>
          </div>
        </form>
      </div>

      <!-- 도움말 섹션 -->
      <div class="help-section">
        <h3 class="help-title">
          <svg fill="currentColor" height="20" viewBox="0 0 24 24" width="20"
               xmlns="http://www.w3.org/2000/svg"><title>help-circle-outline</title>
            <path
              d="M11,18H13V16H11V18M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,6A4,4 0 0,0 8,10H10A2,2 0 0,1 12,8A2,2 0 0,1 14,10C14,12 11,11.75 11,15H13C13,12.75 16,12.5 16,10A4,4 0 0,0 12,6Z"/>
          </svg>
          도움말
        </h3>

        <div class="help-content">
          <div class="help-item">
            <h4>API Token 생성 방법</h4>
            <ol>
              <li>Jenkins에 로그인 후 우상단 사용자명 클릭</li>
              <li>'Configure' 또는 '설정' 메뉴 선택</li>
              <li>'API Token' 섹션에서 'Add new Token' 클릭</li>
              <li>토큰 이름 입력 후 'Generate' 클릭</li>
              <li>생성된 토큰을 복사하여 위 필드에 입력</li>
            </ol>
          </div>

          <div class="help-item">
            <h4>연결 문제 해결</h4>
            <ul>
              <li>Jenkins URL이 올바른지 확인하세요</li>
              <li>네트워크 방화벽 설정을 확인하세요</li>
              <li>Jenkins 서버가 실행 중인지 확인하세요</li>
              <li>API Token이 유효한지 확인하세요</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 1000px;
  margin: 20px auto 0;
  padding: 24px;
  min-height: 100vh;
  background: #f8fafc;
}

/* 헤더 */
.header {
  margin-bottom: 32px;
}

.header h1 {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #64748b;
  text-decoration: none;
  background: none;
  border: none;
  padding: 4px 0;
  transition: color 0.2s ease;
}

.breadcrumb-item.current {
  color: #2563eb;
  font-weight: 500;
}

.breadcrumb-separator {
  color: #cbd5e1;
}

/* 컨텐츠 */
.content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
}

/* 폼 섹션 */
.form-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.section-header {
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.section-title svg {
  color: #2563eb;
}

.section-description {
  color: #64748b;
  font-size: 16px;
  margin: 0;
  line-height: 1.5;
}

/* 폼 */
.form {
  display: flex;
  flex-direction: column;
  gap: 24px;
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

.form-label.required::after {
  content: '*';
  color: #dc2626;
  margin-left: 4px;
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
}

.form-input::placeholder {
  color: #9ca3af;
}

.form-textarea {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  background: white;
  min-height: 100px;
  resize: vertical;
  font-family: inherit;
  line-height: 1.5;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-textarea::placeholder {
  color: #9ca3af;
}

.error-message {
  color: #dc2626;
  font-size: 14px;
  font-weight: 500;
}

.help-text {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.4;
}

.help-text {
  margin-bottom: 10px;
}

.help-link {
  color: #2563eb;
  text-decoration: none;
  font-weight: 500;
}

.help-link:hover {
  text-decoration: underline;
}

/* 연결 섹션 */
.connection-section {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 24px;
  margin-top: 8px;
}

.subsection-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
}

.subsection-title svg {
  color: #2563eb;
}

/* 연결 테스트 */
.connection-test {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.test-connection-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f1f5f9;
  color: #374151;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.test-connection-btn:hover:not(:disabled) {
  background: #e2e8f0;
  border-color: #9ca3af;
}

.test-connection-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.connection-result {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
}

.connection-result.success {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.connection-result.error {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.result-icon svg {
  width: 16px;
  height: 16px;
}

/* 버튼 */
.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 140px;
  justify-content: center;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-primary {
  background: var(--main-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--main-color-hover);
}

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
  margin-top: 8px;
}

/* 도움말 섹션 */
.help-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  height: fit-content;
}

.help-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.help-title svg {
  color: #2563eb;
}

.help-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.help-item h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.help-item ol,
.help-item ul {
  margin: 0;
  padding-left: 20px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

.help-item li {
  margin-bottom: 4px;
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

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 반응형 */
@media (max-width: 1024px) {
  .content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .header h1 {
    font-size: 24px;
  }

  .form-section {
    padding: 24px 20px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }
}
</style>