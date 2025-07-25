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
const isLoading = ref(true); // 로딩 상태 추가
const jenkinsInfoId = route.params.id;
const data = reactive({});
const originalData = reactive({});

onMounted(async () => {
  await fetchJenkinsInfo();
});

const enabledEdit = () => {
  isEdit.value = true;
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
};

const saveEdit = async () => {
  const isOk = confirm("저장하시겠습니까?");
  if (!isOk) {
    Object.assign(data, originalData);
    isEdit.value = false;
    return;
  }
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
  } else {
    Object.assign(data, originalData);
  }
  isEdit.value = false;
}

async function deleteInfo() {
  const isOk = confirm("Jenkins 정보를 삭제하시겠습니까?");
  if (!isOk) {
    return;
  }
  try {
    const response = await jenkinsInfoApi.delete(jenkinsInfoId);
    if (response) {
      await userStore.fetchUserInfo();
      alert("삭제가 완료 되었습니다.");
    } else {
      alert("오류가 발생했습니다. 다시 시도해주세요")
    }
  } catch (e) {
    alert("오류가 발생했습니다. 다시 시도해주세요")
  } finally {
    router.push({name: "Mypage"});
  }
}

async function jenkinsURITest() {
  try {
    data.connected = await jenkinsInfoApi.verify(data.id);
  } catch (e) {
    data.connected = false;
  } finally {
    await fetchApi();
    data.apiToken = "";
  }
}

const fetchApi = async () => {
  const response = await jenkinsInfoApi.getDetail(jenkinsInfoId);
  const responseData = response.data.data;
  Object.assign(data, responseData);
  Object.assign(originalData, data);
  data.apiToken = "";
}

const fetchJenkinsInfo = async () => {
  isLoading.value = true; // 로딩 시작
  try {
    await fetchApi();
  } catch (e) {
    router.back();
  } finally {
    isLoading.value = false; // 로딩 완료
  }
}
</script>

<template>
  <div class="container">
    <!-- 스켈레톤 UI -->
    <div v-if="isLoading" class="skeleton-container">
      <!-- Header Skeleton -->
      <div class="skeleton-header">
        <div class="skeleton-title"></div>
        <div class="skeleton-buttons">
          <div class="skeleton-button"></div>
          <div class="skeleton-button"></div>
        </div>
      </div>

      <!-- Info Box Skeleton -->
      <div class="skeleton-info-box">
        <div v-for="i in 4" :key="i" class="skeleton-row">
          <div class="skeleton-label"></div>
          <div class="skeleton-value"></div>
        </div>
      </div>
    </div>

    <!-- 실제 컨텐츠 -->
    <div v-else>
      <!-- Header -->
      <div class="header">
        <input
            v-model="data.name"
            :class="{ editing: isEdit }"
            :readonly="!isEdit"
            class="name"
            placeholder="Jenkins 이름을 입력하세요"
            type="text"
        />
        <div class="btn_box">
          <template v-if="!isEdit">
            <button class="edit_btn" @click="enabledEdit">
              <svg fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
              </svg>
            </button>
            <button class="delete_btn" @click="deleteInfo">
              <svg fill="none" height="18" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="18">
                <polyline points="3,6 5,6 21,6"/>
                <path d="m19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6m3,0V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2"/>
              </svg>
            </button>
          </template>
          <template v-else>
            <button class="save_btn" @click="saveEdit">저장</button>
            <button class="cancel_btn" @click="disableEdit">취소</button>
          </template>
        </div>
      </div>

      <!-- Info Box -->
      <div class="info_box">
        <!-- Description -->
        <div class="row">
          <div class="label">설명</div>
          <div class="value">
            <textarea
                v-if="isEdit"
                v-model="data.description"
                class="input_text description editing"
                placeholder="Jenkins 서버에 대한 설명을 입력하세요"
            />
            <div v-else class="description readonly">
              {{ data.description || '설명이 없습니다.' }}
            </div>
          </div>
        </div>

        <!-- URI -->
        <div class="row">
          <div class="label">URI</div>
          <div class="value with_actions">
            <input
                v-model="data.uri"
                :class="{ editing: isEdit }"
                :readonly="!isEdit"
                class="input_text"
                placeholder="https://jenkins.example.com"
                type="text"
            />
            <div v-if="!isEdit" class="connection_status">
              <div :class="['status_dot', { connected: data.connected }]"></div>
              <span :class="['status_text', { connected: data.connected }]">
                {{ data.connected ? '연결됨' : '연결 실패' }}
              </span>
              <button class="test_btn" @click="jenkinsURITest">연결 확인</button>
            </div>
          </div>
        </div>

        <!-- Jenkins ID -->
        <div class="row">
          <div class="label">Jenkins ID</div>
          <input
              v-model="data.jenkinsId"
              :class="{ editing: isEdit }"
              :readonly="!isEdit"
              class="input_text"
              placeholder="Jenkins 사용자 ID를 입력하세요"
              type="text"
          />
        </div>

        <!-- API Token -->
        <div class="row">
          <div class="label">API Token</div>
          <div class="value">
            <div v-if="!isEdit" class="token_display">••••••••••••••</div>
            <div v-else-if="isTokenEdit" class="token_edit">
              <input
                  v-model="data.apiToken"
                  class="input_text editing token_input"
                  placeholder="새로운 API Token을 입력하세요"
                  type="text"
              />
              <button class="token_cancel_btn" @click="disabledEditToken">취소</button>
            </div>
            <button v-else-if="isEdit && !isTokenEdit" class="token_edit_btn" @click="enabledEditToken">
              API Token 수정
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 800px;
  margin: 60px auto;
  padding: 0 20px;
}

/* 스켈레톤 UI 스타일 */
.skeleton-container {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.skeleton-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.skeleton-title {
  height: 32px;
  width: 300px;
  background: #f3f4f6;
  border-radius: 8px;
}

.skeleton-buttons {
  display: flex;
  gap: 12px;
}

.skeleton-button {
  height: 36px;
  width: 36px;
  background: #f3f4f6;
  border-radius: 8px;
}

.skeleton-info-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 32px;
}

.skeleton-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 24px;
  margin-bottom: 32px;
  align-items: start;
}

.skeleton-row:last-child {
  margin-bottom: 0;
}

.skeleton-label {
  height: 20px;
  width: 80px;
  background: #f3f4f6;
  border-radius: 4px;
  margin-top: 12px;
}

.skeleton-value {
  height: 48px;
  width: 60%;
  background: #f3f4f6;
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

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.name {
  flex: 1;
  background: none;
  border: none;
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  outline: none;
  margin-right: 20px;
}

.name.editing {
  border-bottom: 2px solid #2B2D65;
  padding-bottom: 8px;
  padding-left: 8px;
  padding-right: 8px;
  border-radius: 4px 4px 0 0;
}

.name::placeholder {
  color: #9ca3af;
}

.btn_box {
  display: flex;
  gap: 12px;
}

.edit_btn, .delete_btn {
  align-items: center;
  gap: 6px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.edit_btn {
  background: #f3f4f6;
  color: #4b5563;
}

.edit_btn:hover {
  background: #e5e7eb;
  transform: translateY(-1px);
}

.delete_btn {
  background: #fef2f2;
  color: #dc2626;
}

.delete_btn:hover {
  background: #fee2e2;
  transform: translateY(-1px);
}

.cancel_btn, .save_btn {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel_btn {
  background: #f3f4f6;
  color: #6b7280;
}

.cancel_btn:hover {
  background: #e5e7eb;
}

.save_btn {
  background: #2B2D65;
  color: white;
}

.save_btn:hover {
  background: rgba(43, 45, 101, 0.82);
  transform: translateY(-1px);
}

.info_box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
  padding: 32px;
}

.row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 24px;
  margin-bottom: 32px;
  align-items: start;
}

.row:last-child {
  margin-bottom: 0;
}

.label {
  color: #6b7280;
  font-weight: 600;
  font-size: 14px;
  padding-top: 12px;
}

.value {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.value.with_actions {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.input_text {
  width: 60%;
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 16px;
  color: #1f2937;
  outline: none;
  transition: all 0.2s ease;
}

.input_text:focus {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input_text.editing {
  background: white;
}

.input_text::placeholder {
  color: #9ca3af;
}

.description {
  width: 90% !important;
  min-height: 80px;
  resize: vertical;
  font-family: inherit;
  line-height: 1.6;
}

.description.readonly {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
  color: #1f2937;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  min-height: 60px;
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.test_btn {
  background: #2B2D65;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.test_btn:hover {
  background: rgba(43, 45, 101, 0.82);
  transform: translateY(-1px);
}

.status_indicator {
  display: flex;
  align-items: center;
}

.status_icon.success {
  color: #10b981;
}

.status_icon.error {
  color: #ef4444;
}

.connection_status {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-top: 8px;
}

.status_dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
}

.status_dot.connected {
  background: #10b981;
}

.status_text {
  color: #dc2626;
  font-weight: 500;
}

.status_text.connected {
  color: #059669;
}

.token_display {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  color: #6b7280;
  font-size: 14px;
}

.token_edit {
  display: flex;
  align-items: center;
  gap: 12px;
}

.token_input {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
}

.token_cancel_btn {
  background: #f3f4f6;
  color: #6b7280;
  border: none;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.token_cancel_btn:hover {
  background: #e5e7eb;
}

.token_edit_btn {
  background: #2B2D65;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.token_edit_btn:hover {
  background: rgba(43, 45, 101, 0.82);
  transform: translateY(-1px);
}

/* 반응형 */
@media (max-width: 768px) {
  .container {
    margin: 20px auto;
    padding: 0 16px;
  }

  .header, .skeleton-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .name {
    margin-right: 0;
    text-align: center;
  }

  .btn_box, .skeleton-buttons {
    justify-content: center;
  }

  .info_box, .skeleton-info-box {
    padding: 24px 20px;
  }

  .row, .skeleton-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }

  .label, .skeleton-label {
    padding-top: 0;
  }

  .skeleton-title {
    width: 100%;
  }

  .skeleton-value {
    width: 100%;
  }

  .value.with_actions {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .actions {
    justify-content: space-between;
  }

  .token_edit {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>