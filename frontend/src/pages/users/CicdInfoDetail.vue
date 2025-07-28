<script setup>
import {onMounted, reactive, ref} from 'vue';
import {useRoute, useRouter} from "vue-router";
import {useUserStore} from "@/stores/useUserStore.js"
import {jenkinsInfoApi} from "@/api/JenkinsInfoApi.js";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const isEdit = ref(false);
const isTokenEdit = ref(false);
const isLoading = ref(true);
const isSaving = ref(false);
const isDeleting = ref(false);
const isTesting = ref(false);

const jenkinsInfoId = route.params.id;
const data = reactive({});
const originalData = reactive({});

// 폼 검증 에러
const errors = ref({});

const fetchApi = async () => {
  const response = await jenkinsInfoApi.getDetail(jenkinsInfoId);
  await userStore.fetchUserInfo();
  const responseData = response.data.data;
  Object.assign(data, responseData);
  Object.assign(originalData, data);
  data.apiToken = "";
}

const fetchJenkinsInfo = async () => {
  isLoading.value = true;
  try {
    await fetchApi();
  } catch (e) {
    alert("Jenkins 정보를 불러오는데 실패했습니다.");
    router.back();
  } finally {
    isLoading.value = false;
  }
}

onMounted(async () => {
  await fetchJenkinsInfo();
});

const validateForm = () => {
  const newErrors = {};

  if (!data.name?.trim()) {
    newErrors.name = "Jenkins 이름을 입력해주세요.";
  }

  if (!data.uri?.trim()) {
    newErrors.uri = "Jenkins URL을 입력해주세요.";
  } else if (!isValidUrl(data.uri)) {
    newErrors.uri = "올바른 URL 형식을 입력해주세요.";
  }

  if (!data.jenkinsId?.trim()) {
    newErrors.jenkinsId = "Jenkins ID를 입력해주세요.";
  }

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const isValidUrl = (string) => {
  try {
    new URL(string);
    return true;
  } catch (_) {
    return false;
  }
};

const clearError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field];
  }
};

const enabledEdit = () => {
  isEdit.value = true;
  errors.value = {};
};

const enabledEditToken = () => {
  isTokenEdit.value = true;
}

const disabledEditToken = () => {
  isTokenEdit.value = false;
  data.apiToken = "";
}

const disableEdit = () => {
  Object.assign(data, originalData);
  isEdit.value = false;
  disabledEditToken();
  errors.value = {};
};

const saveEdit = async () => {
  if (!validateForm()) {
    return;
  }

  const isOk = confirm("변경사항을 저장하시겠습니까?");
  if (!isOk) {
    return;
  }

  isSaving.value = true;

  try {
    const payload = {
      infoId: data.id,
      name: data.name,
      description: data.description,
      jenkinsId: data.jenkinsId,
      uri: data.uri,
      apiToken: data.apiToken
    }

    const response = await jenkinsInfoApi.update(payload);

    if (response) {
      await fetchJenkinsInfo();
      alert("변경사항이 저장되었습니다.");
    } else {
      Object.assign(data, originalData);
      alert("저장에 실패했습니다. 다시 시도해주세요.");
    }
  } catch (error) {
    Object.assign(data, originalData);
    alert("오류가 발생했습니다. 다시 시도해주세요.");
  } finally {
    isSaving.value = false;
    isEdit.value = false;
    disabledEditToken();
  }
}

async function deleteInfo() {
  const isOk = confirm(
      "정말로 이 Jenkins 정보를 삭제하시겠습니까?\n\n" +
      "삭제된 정보는 복구할 수 없으며, 관련된 모든 Job도 함께 삭제됩니다."
  );
  if (!isOk) {
    return;
  }

  isDeleting.value = true;

  try {
    const response = await jenkinsInfoApi.delete(jenkinsInfoId);
    if (response) {
      await userStore.fetchUserInfo();
      alert("Jenkins 정보가 삭제되었습니다.");
      router.push({name: "Mypage"});
    } else {
      alert("삭제에 실패했습니다. 다시 시도해주세요.");
    }
  } catch (e) {
    alert("오류가 발생했습니다. 다시 시도해주세요.");
  } finally {
    isDeleting.value = false;
  }
}

async function jenkinsURITest() {
  isTesting.value = true;

  try {
    data.connected = await jenkinsInfoApi.verify({infoId: data.id});
    if (data.connected) {
      alert("Jenkins 서버에 성공적으로 연결되었습니다!");
    } else {
      alert("Jenkins 서버 연결에 실패했습니다. 설정을 확인해주세요.");
    }
  } catch (e) {
    data.connected = false;
    alert("연결 테스트 중 오류가 발생했습니다.");
  } finally {
    await fetchApi();
    data.apiToken = "";
    isTesting.value = false;
  }
}

const goToJobList = () => {
  router.push({
    name: 'JobList',
    query: {
      id: data.id
    }
  });
};
</script>

<template>
  <div class="container">
    <!-- 스켈레톤 UI -->
    <div v-if="isLoading" class="skeleton-container">
      <!-- Header Skeleton -->
      <div class="skeleton-header">
        <div class="skeleton-breadcrumb"></div>
        <div class="skeleton-title-section">
          <div class="skeleton-title"></div>
          <div class="skeleton-buttons">
            <div class="skeleton-button"></div>
            <div class="skeleton-button"></div>
          </div>
        </div>
      </div>

      <!-- Content Skeleton -->
      <div class="skeleton-content">
        <div class="skeleton-info-section">
          <div class="skeleton-section-title"></div>
          <div class="skeleton-info-box">
            <div v-for="i in 4" :key="i" class="skeleton-row">
              <div class="skeleton-label"></div>
              <div class="skeleton-value"></div>
            </div>
          </div>
        </div>

        <div class="skeleton-actions-section">
          <div class="skeleton-section-title"></div>
          <div class="skeleton-action-card">
            <div class="skeleton-action-content"></div>
            <div class="skeleton-action-button"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 실제 컨텐츠 -->
    <div v-else>
      <!-- 헤더 -->
      <div class="header">
        <div class="breadcrumb">
          <button class="breadcrumb-item" @click="router.push({name: 'Mypage'})">
            마이페이지
          </button>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-item">CI/CD 정보</span>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-item current">{{ data.name || 'Jenkins 서버' }}</span>
        </div>

        <div class="title-section">
          <div class="title-content">
            <input
                v-model="data.name"
                :class="['server-name', { editing: isEdit, error: errors.name }]"
                :readonly="!isEdit"
                placeholder="Jenkins 이름을 입력하세요"
                type="text"
                @input="clearError('name')"
            />
            <div :class="{ connected: data.connected }" class="server-status">
              <div class="status-dot"></div>
              <span class="status-text">{{ data.connected ? '연결됨' : '연결 실패' }}</span>
            </div>
          </div>

          <div class="header-actions">
            <template v-if="!isEdit">
              <button class="btn btn-outline" @click="enabledEdit">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                  <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                </svg>
                편집
              </button>
              <button :disabled="isDeleting" class="btn btn-danger-outline" @click="deleteInfo">
                <svg v-if="isDeleting" class="animate-spin" fill="none" height="16" stroke="currentColor"
                     stroke-width="2"
                     viewBox="0 0 24 24" width="16">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="16">
                  <polyline points="3,6 5,6 21,6"/>
                  <path d="m19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"/>
                </svg>
                {{ isDeleting ? '삭제 중...' : '삭제' }}
              </button>
            </template>
            <template v-else>
              <button :disabled="isSaving" class="btn btn-primary" @click="saveEdit">
                <svg v-if="isSaving" class="animate-spin" fill="none" height="16" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24" width="16">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="16">
                  <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                  <polyline points="17,21 17,13 7,13 7,21"/>
                  <polyline points="7,3 7,8 15,8"/>
                </svg>
                {{ isSaving ? '저장 중...' : '저장' }}
              </button>
              <button :disabled="isSaving" class="btn btn-secondary" @click="disableEdit">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <line x1="18" x2="6" y1="6" y2="18"/>
                  <line x1="6" x2="18" y1="6" y2="18"/>
                </svg>
                취소
              </button>
            </template>
          </div>
        </div>

        <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
      </div>

      <!-- 메인 컨텐츠 -->
      <div class="content">
        <!-- 서버 정보 섹션 -->
        <div class="section">
          <h3 class="section-title">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
              <line x1="8" x2="16" y1="21" y2="21"/>
              <line x1="12" x2="12" y1="17" y2="21"/>
            </svg>
            서버 정보
          </h3>

          <div class="info-grid">
            <!-- 설명 -->
            <div class="info-item">
              <label class="info-label">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14,2 14,8 20,8"/>
                  <line x1="16" x2="8" y1="13" y2="13"/>
                  <line x1="16" x2="8" y1="17" y2="17"/>
                  <polyline points="10,9 9,9 8,9"/>
                </svg>
                설명
              </label>
              <div class="info-content">
                <textarea
                    v-if="isEdit"
                    v-model="data.description"
                    class="form-textarea"
                    placeholder="Jenkins 서버에 대한 설명을 입력하세요"
                />
                <div v-else class="info-display">
                  {{ data.description || '설명이 없습니다.' }}
                </div>
              </div>
            </div>

            <!-- URI -->
            <div class="info-item">
              <label class="info-label">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                  <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
                </svg>
                Jenkins URL
              </label>
              <div class="info-content">
                <input
                    v-model="data.uri"
                    :class="['form-input', { editing: isEdit, error: errors.uri }]"
                    :readonly="!isEdit"
                    placeholder="https://jenkins.example.com"
                    type="text"
                    @input="clearError('uri')"
                />
                <span v-if="errors.uri" class="error-message">{{ errors.uri }}</span>
              </div>
            </div>

            <!-- Jenkins ID -->
            <div class="info-item">
              <label class="info-label">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                Jenkins 사용자 ID
              </label>
              <div class="info-content">
                <input
                    v-model="data.jenkinsId"
                    :class="['form-input', { editing: isEdit, error: errors.jenkinsId }]"
                    :readonly="!isEdit"
                    placeholder="Jenkins 사용자 ID를 입력하세요"
                    type="text"
                    @input="clearError('jenkinsId')"
                />
                <span v-if="errors.jenkinsId" class="error-message">{{ errors.jenkinsId }}</span>
              </div>
            </div>

            <!-- API Token -->
            <div class="info-item">
              <label class="info-label">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <rect height="11" rx="2" ry="2" width="18" x="3" y="11"/>
                  <circle cx="12" cy="16" r="1"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                API Token
              </label>
              <div class="info-content">
                <div v-if="!isEdit" class="token-display">
                  <span class="token-masked">••••••••••••••••••••</span>
                </div>
                <div v-else-if="isTokenEdit" class="token-edit">
                  <input
                      v-model="data.apiToken"
                      class="form-input token-input"
                      placeholder="새로운 API Token을 입력하세요"
                      type="password"
                  />
                  <button class="btn btn-secondary btn-sm" @click="disabledEditToken">취소</button>
                </div>
                <button v-else-if="isEdit && !isTokenEdit" class="btn btn-outline btn-sm" @click="enabledEditToken">
                  <svg fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="14">
                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                    <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                  </svg>
                  API Token 수정
                </button>
              </div>
            </div>

            <div v-if="!isEdit" class="connection-test">
              <button :disabled="isTesting" class="test-btn" @click="jenkinsURITest">
                <svg v-if="isTesting" class="animate-spin" fill="none" height="14" stroke="currentColor"
                     stroke-width="2"
                     viewBox="0 0 24 24" width="14">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else fill="none" height="14" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                     width="14">
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                  <polyline points="22,4 12,14.01 9,11.01"/>
                </svg>
                {{ isTesting ? '확인 중...' : '연결 확인' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 액션 섹션 -->
        <div class="section">
          <h3 class="section-title">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20"
                 xmlns="http://www.w3.org/2000/svg"><title>cog</title>
              <path
                  d="M12,15.5A3.5,3.5 0 0,1 8.5,12A3.5,3.5 0 0,1 12,8.5A3.5,3.5 0 0,1 15.5,12A3.5,3.5 0 0,1 12,15.5M19.43,12.97C19.47,12.65 19.5,12.33 19.5,12C19.5,11.67 19.47,11.34 19.43,11L21.54,9.37C21.73,9.22 21.78,8.95 21.66,8.73L19.66,5.27C19.54,5.05 19.27,4.96 19.05,5.05L16.56,6.05C16.04,5.66 15.5,5.32 14.87,5.07L14.5,2.42C14.46,2.18 14.25,2 14,2H10C9.75,2 9.54,2.18 9.5,2.42L9.13,5.07C8.5,5.32 7.96,5.66 7.44,6.05L4.95,5.05C4.73,4.96 4.46,5.05 4.34,5.27L2.34,8.73C2.21,8.95 2.27,9.22 2.46,9.37L4.57,11C4.53,11.34 4.5,11.67 4.5,12C4.5,12.33 4.53,12.65 4.57,12.97L2.46,14.63C2.27,14.78 2.21,15.05 2.34,15.27L4.34,18.73C4.46,18.95 4.73,19.03 4.95,18.95L7.44,17.94C7.96,18.34 8.5,18.68 9.13,18.93L9.5,21.58C9.54,21.82 9.75,22 10,22H14C14.25,22 14.46,21.82 14.5,21.58L14.87,18.93C15.5,18.67 16.04,18.34 16.56,17.94L19.05,18.95C19.27,19.03 19.54,18.95 19.66,18.73L21.66,15.27C21.78,15.05 21.73,14.78 21.54,14.63L19.43,12.97Z"/>
            </svg>
            작업 관리
          </h3>

          <div class="action-card">
            <div class="action-content">
              <h4 class="action-title">Job 관리</h4>
              <p class="action-description">
                이 Jenkins 서버에서 실행되는 CI/CD Job들을 관리하고 모니터링할 수 있습니다.
              </p>
            </div>
            <button class="btn btn-primary" @click="goToJobList">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M5 12h14"/>
                <path d="M12 5l7 7-7 7"/>
              </svg>
              Job 목록 보기
            </button>
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

/* 스켈레톤 UI */
.skeleton-container {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-header {
  margin-bottom: 32px;
}

.skeleton-breadcrumb {
  height: 20px;
  width: 300px;
  background: #e2e8f0;
  border-radius: 4px;
  margin-bottom: 16px;
}

.skeleton-title-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.skeleton-title {
  height: 32px;
  width: 250px;
  background: #e2e8f0;
  border-radius: 8px;
}

.skeleton-buttons {
  display: flex;
  gap: 12px;
}

.skeleton-button {
  height: 40px;
  width: 80px;
  background: #f1f5f9;
  border-radius: 8px;
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.skeleton-info-section,
.skeleton-actions-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.skeleton-section-title {
  height: 24px;
  width: 150px;
  background: #e2e8f0;
  border-radius: 6px;
  margin-bottom: 20px;
}

.skeleton-info-box {
  display: grid;
  gap: 24px;
}

.skeleton-row {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
  align-items: start;
}

.skeleton-label {
  height: 20px;
  width: 120px;
  background: #f1f5f9;
  border-radius: 4px;
}

.skeleton-value {
  height: 48px;
  background: #f1f5f9;
  border-radius: 8px;
}

.skeleton-action-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.skeleton-action-content {
  height: 60px;
  width: 300px;
  background: #f1f5f9;
  border-radius: 6px;
}

.skeleton-action-button {
  height: 40px;
  width: 120px;
  background: #f1f5f9;
  border-radius: 8px;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

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

/* 헤더 */
.header {
  margin-bottom: 32px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 14px;
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

.title-section {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.title-content {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.server-name {
  flex: 1;
  background: none;
  border: none;
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  outline: none;
  padding: 8px 0;
}

.server-name.editing {
  border-bottom: 2px solid #2563eb;
  padding: 8px 12px;
  border-radius: 8px 8px 0 0;
  background: #f8fafc;
}

.server-name.error {
  border-bottom-color: #dc2626;
}

.server-name::placeholder {
  color: #9ca3af;
}

.server-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  background: #fee2e2;
  color: #dc2626;
}

.server-status.connected {
  background: #dcfce7;
  color: #16a34a;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.header-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.error-message {
  color: #dc2626;
  font-size: 14px;
  font-weight: 500;
  margin-top: 8px;
}

/* 컨텐츠 */
.content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 20px 0;
}

.section-title svg {
  color: #2563eb;
}

/* 정보 그리드 */
.info-grid {
  display: grid;
  gap: 24px;
}

.info-item {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
  align-items: start;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  padding-top: 12px;
}

.info-label svg {
  color: #94a3b8;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-input {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  background: #f9fafb;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-input.editing {
  background: white;
}

.form-input.error {
  border-color: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.1);
}

.form-input:read-only {
  cursor: default;
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

.info-display {
  background: #f9fafb;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
  color: #1f2937;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
  min-height: 48px;
}

/* 연결 테스트 */
.connection-test {
  margin-top: 8px;
  justify-self: right;
  align-items: center;
}

.test-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--main-color);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.test-btn:hover:not(:disabled) {
  background: var(--main-color-hover);
  transform: translateY(-1px);
}

.test-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

/* 토큰 */
.token-display {
  background: #f9fafb;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 12px 16px;
}

.token-masked {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  color: #6b7280;
  font-size: 14px;
}

.token-edit {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.token-input {
  flex: 1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
}

/* 액션 카드 */
.action-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.action-content {
  flex: 1;
}

.action-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.action-description {
  color: #64748b;
  font-size: 14px;
  margin: 0;
  line-height: 1.5;
}

/* 버튼 */
.btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
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

.btn-outline {
  background: transparent;
  color: var(--main-color);
  border: 1px solid var(--main-color);
}

.btn-outline:hover:not(:disabled) {
  background: var(--main-color-hover);
  color: white;
}

.btn-danger-outline {
  background: transparent;
  color: #dc2626;
  border: 1px solid #dc2626;
}

.btn-danger-outline:hover:not(:disabled) {
  background: #dc2626;
  color: white;
}

/* 반응형 */
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .title-section {
    padding: 20px 16px;
  }

  .title-content {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .server-name {
    font-size: 24px;
    text-align: center;
  }

  .header-actions {
    justify-content: center;
  }

  .info-item {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .info-label {
    padding-top: 0;
  }

  .action-card {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .token-edit {
    flex-direction: column;
  }

  .skeleton-title-section {
    flex-direction: column;
    gap: 16px;
  }

  .skeleton-buttons {
    justify-content: center;
  }

  .skeleton-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}
</style>