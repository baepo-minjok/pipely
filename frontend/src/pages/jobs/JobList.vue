<script setup>
import {computed, onMounted, onUnmounted, ref, watch} from 'vue';
import JobCard from '@/pages/jobs/JobCard.vue';
import {useRoute, useRouter} from 'vue-router';
import {useJobStore} from '@/stores/useJobStore.js';
import {jobApi} from "@/api/JobApi.js";
import {versionApi} from "@/api/VersionApi.js";
import VersionList from "@/pages/jobs/VersionList.vue";

const jobStore = useJobStore();
const router = useRouter();
const route = useRoute();
const selectedJenkins = ref(route.query.id || '');
const openDropdownJob = ref(null); // 현재 열린 드롭다운 Job ID
const showSnapshotListModal = ref(false);
const snapshotListTargetJobId = ref(null);


const closeSnapshotListModal = () => {
  showSnapshotListModal.value = false;
  snapshotListTargetJobId.value = null;
};

const selected = computed(() =>
    jobStore.jenkinsInfo.find((j) => j.id === selectedJenkins.value)
);

const handleCreateClick = () => {
  if (selected.value) {
    router.push({
      name: 'CreateJob',
      query: {
        id: selected.value.id,
        jenkinsName: selected.value.name,
        jenkinsUri: selected.value.uri,
        connected: selected.value.connected
      },
    });
  }
};

// 드롭다운 관련 핸들러들
const handleJobAction = (job) => {
  console.log('Job action:', job);
  // Job 실행 로직
};

const handleDeleteJob = async (job) => {
  if (!confirm(`정말로 "${job.name}" Job을 삭제하시겠습니까?`)) {
    openDropdownJob.value = null;
    return;
  }

  try {

    await jobApi.deletedJobs(job.pipelineId);

    const originalLength = jobStore.jobList.length;

    jobStore.jobList = jobStore.jobList.filter(j => j.name !== job.name);

    if (jobStore.jobList.length === originalLength) {
      jobStore.jobList = jobStore.jobList.filter(j =>
          j.id !== job.id &&
          j.pipelineId !== job.pipelineId
      );
    }
    openDropdownJob.value = null;

    alert('삭제가 완료되었습니다.');

  } catch (err) {
    console.error('삭제 실패:', err);
    alert('삭제에 실패했습니다.');
    openDropdownJob.value = null;
  }
};

const showSnapshotModal = ref(false);   // 모달 표시 여부
const snapshotTargetJob = ref(null);    // 현재 스냅샷 저장할 Job
const snapshotName = ref("");           // 입력할 스냅샷 이름

const handleSaveSnapshot = (job) => {
  snapshotTargetJob.value = job;
  snapshotName.value = "";
  showSnapshotModal.value = true;
  openDropdownJob.value = null;
};
const confirmSaveSnapshot = async () => {

  isSaving.value = true;

  if (!snapshotName.value.trim()) {
    alert("스냅샷 이름을 입력하세요.");
    return;
  }

  try {
    const success = await versionApi.createSnapshot(
        snapshotTargetJob.value.pipelineId,
        snapshotName.value
    );

    if (success) {
      alert(`스냅샷 "${snapshotName.value}" 생성 성공!`);
      showSnapshotModal.value = false;
      snapshotName.value = '';
      showError.value = false;
    }
  } catch (err) {
    alert("스냅샷 생성 실패");
  } finally {
    isSaving.value = false;
  }
};

const handleViewSnapshots = (job) => {
  console.log('View snapshots for job:', job.pipelineId);
  // 스냅샷 목록 보기 로직
  openDropdownJob.value = null;
  snapshotListTargetJobId.value = job.pipelineId; // job id를 저장
  showSnapshotListModal.value = true;
};

const handleToggleDropdown = (job) => {
  // 같은 Job이면 토글, 다른 Job이면 해당 Job으로 변경
  if (openDropdownJob.value === job.name) {
    openDropdownJob.value = null;
  } else {
    openDropdownJob.value = job.name;
  }
};

// 외부 클릭 시 드롭다운 닫기
const handleOutsideClick = (event) => {
  if (event.target.closest('.more-container') || event.target.closest('.dropdown-menu')) {
    return;
  }
  openDropdownJob.value = null;
};


// 반응형 데이터
const isSaving = ref(false);
const showError = ref(false);

// 메서드
const closeModal = () => {
  if (!isSaving.value) {
    snapshotName.value = '';
    showError.value = false;
    showSnapshotModal.value = false
  }
};


// ESC 키로 모달 닫기
const handleKeydown = (event) => {
  if (event.key === 'Escape' && !isSaving.value) {
    closeModal();
  }
};

watch(selectedJenkins, async (id) => {
  if (id) {
    await jobApi.fetchJobList(id);
  }
  // Jenkins 변경 시 드롭다운 닫기
  openDropdownJob.value = null;
});

const onRollback = (version) => {
  console.log('onRollback:', version);
}

onMounted(async () => {
  await jobApi.getJenkinsInfo();
  if (route.query.id !== undefined) {
    selectedJenkins.value = route.query.id;
    await jobApi.fetchJobList(selectedJenkins.value);
  }
  // 전역 클릭 이벤트 리스너 추가
  document.addEventListener('click', handleOutsideClick);
  document.addEventListener('keydown', handleKeydown);
});

// 컴포넌트 언마운트 시 이벤트 리스너 제거
onUnmounted(() => {
  document.removeEventListener('click', handleOutsideClick);
  document.removeEventListener('keydown', handleKeydown);
});
</script>

<template>
  <div class="container">
    <!-- 헤더 -->
    <div class="header">
      <div>
        <h1>Job 목록</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item">Pipely</span>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-item current">Job 목록</span>
        </div>
      </div>
      <button
          v-if="selectedJenkins"
          class="btn btn-primary create-job-btn"
          @click="handleCreateClick"
      >
        <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
          <line x1="12" x2="12" y1="5" y2="19"/>
          <line x1="5" x2="19" y1="12" y2="12"/>
        </svg>
        새 Job 생성
      </button>
    </div>

    <div v-if="showSnapshotModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <!-- 헤더 -->
        <div class="modal-header">
          <div class="modal-title-section">
            <div class="modal-icon">
              <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="10,6 10,10 14,14"/>
              </svg>
            </div>
            <h3 class="modal-title">스냅샷 저장</h3>
          </div>
          <button class="modal-close-btn" @click="closeModal">
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
          </button>
        </div>

        <!-- 컨텐츠 -->
        <div class="modal-content">
          <div class="modal-description">
            <div class="description-icon">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 6v6l4 2"/>
              </svg>
            </div>
            <div class="description-text">
              <p class="description-main">현재 작업 중인 버전이 새로운 스냅샷으로 저장됩니다.</p>
              <p class="description-sub">이후 언제든 해당 시점으로 복원할 수 있습니다.</p>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label" for="snapshotName">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              스냅샷 이름
            </label>
            <input
                id="snapshotName"
                v-model="snapshotName"
                :class="['form-input', { error: !snapshotName.trim() && showError }]"
                placeholder="예: 기능 개발 완료, 버그 수정 전 등..."
                type="text"
                @input="showError = false"
                @keyup.enter="confirmSaveSnapshot"
            />
            <span v-if="!snapshotName.trim() && showError" class="error-message">
            스냅샷 이름을 입력해주세요.
          </span>
            <span class="help-text">
            나중에 쉽게 찾을 수 있도록 의미있는 이름을 입력하세요.
          </span>
          </div>
        </div>

        <!-- 액션 -->
        <div class="modal-actions">
          <button
              :disabled="isSaving"
              class="btn btn-primary"
              @click="confirmSaveSnapshot"
          >
            <svg v-if="isSaving" class="animate-spin" fill="none" height="16" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="16">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
            <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
              <polyline points="17,21 17,13 7,13 7,21"/>
              <polyline points="7,3 7,8 15,8"/>
            </svg>
            {{ isSaving ? '저장 중...' : '스냅샷 저장' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Jenkins 선택 섹션 -->
    <div class="section jenkins-select-section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
          <line x1="8" x2="16" y1="21" y2="21"/>
          <line x1="12" x2="12" y1="17" y2="21"/>
        </svg>
        Jenkins 인스턴스 선택
      </h3>

      <div class="form-group">
        <label class="form-label" for="jenkins-select">Jenkins 정보</label>
        <div class="custom-select-wrapper">
          <select
              id="jenkins-select"
              v-model="selectedJenkins"
              :class="{ 'placeholder-selected': selectedJenkins === '' }"
              class="form-select"
          >
            <option :value="''" disabled>Jenkins 인스턴스를 선택해주세요</option>
            <option v-for="info in jobStore.jenkinsInfo" :key="info.id" :value="info.id">
              {{ info.name }}
            </option>
          </select>
          <svg class="select-arrow" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
               width="16">
            <polyline points="6,9 12,15 18,9"/>
          </svg>
        </div>
      </div>

      <!-- 선택된 Jenkins 정보 표시 -->
      <div v-if="selected" class="jenkins-info-card">
        <div class="info-item">
          <span class="info-label">이름</span>
          <span class="info-value">{{ selected.name }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">주소</span>
          <span class="info-value">{{ selected.uri }}</span>
        </div>
        <div class="connection-status">
          <div :class="['status-dot', { connected: selected.connected }]"></div>
          <span :class="['status-text', { connected: selected.connected }]">
                {{ selected.connected ? '연결됨' : '연결 실패' }}
          </span>
        </div>
      </div>
    </div>

    <!-- Job 목록 섹션 -->
    <div class="section job-list-section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14,2 14,8 20,8"/>
          <line x1="16" x2="8" y1="13" y2="13"/>
          <line x1="16" x2="8" y1="17" y2="17"/>
          <polyline points="10,9 9,9 8,9"/>
        </svg>
        Job 목록
        <span v-if="selectedJenkins && jobStore.jobList.length > 0" class="job-count">
          ({{ jobStore.jobList.length }}개)
        </span>
      </h3>

      <div class="job-list-content">
        <template v-if="selectedJenkins">
          <div v-if="jobStore.jobList.length === 0" class="empty-state">
            <svg class="empty-icon" fill="none" height="64" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24"
                 width="64">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="16" x2="8" y1="13" y2="13"/>
              <line x1="16" x2="8" y1="17" y2="17"/>
              <polyline points="10,9 9,9 8,9"/>
            </svg>
            <h4 class="empty-title">Job이 없습니다</h4>
            <p class="empty-description">새로운 Job을 생성해보세요!</p>
            <button class="btn btn-primary" @click="handleCreateClick">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <line x1="12" x2="12" y1="5" y2="19"/>
                <line x1="5" x2="19" y1="12" y2="12"/>
              </svg>
              첫 번째 Job 생성하기
            </button>
          </div>

          <div v-else class="job-grid">
            <JobCard
                v-for="job in jobStore.jobList"
                :key="job.name"
                :job="job"
                :open-dropdown-job="openDropdownJob"
                class="job-card-item"
                @action="handleJobAction"
                @click="() => router.push(`/job/${job.name}`)"
                @delete="handleDeleteJob"
                @saveSnapshot="handleSaveSnapshot"
                @toggleDropdown="handleToggleDropdown"
                @viewSnapshots="handleViewSnapshots"
            />


          </div>
        </template>

        <template v-else>
          <div class="select-prompt">
            <svg class="prompt-icon" fill="none" height="48" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24"
                 width="48">
              <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
              <line x1="8" x2="16" y1="21" y2="11"/>
              <line x1="12" x2="12" y1="17" y2="17"/>
            </svg>
            <h4 class="prompt-title">Jenkins 인스턴스를 선택하세요</h4>
            <p class="prompt-description">Job 목록을 확인하려면 먼저 Jenkins 인스턴스를 선택해주세요.</p>
          </div>
        </template>
      </div>
    </div>
  </div>
  <div v-if="showSnapshotListModal" class="modal-overlay" @click="closeSnapshotListModal">

    <div class="modal" @click.stop>
      <VersionList
          :jobId="snapshotListTargetJobId"
          @onRollback="onRollback"
      />
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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
  color: #64748b;
}

.breadcrumb-item.current {
  color: #2563eb;
  font-weight: 500;
}

.breadcrumb-separator {
  color: #cbd5e1;
}

/* 섹션 */
.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  margin-bottom: 24px;
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

.job-count {
  font-size: 16px;
  color: #64748b;
  font-weight: 400;
}

/* 폼 요소 */
.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.custom-select-wrapper {
  position: relative;
}

.form-select {
  width: 100%;
  padding: 12px 40px 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  background: white;
  cursor: pointer;
  transition: all 0.2s ease;
  appearance: none;
  box-sizing: border-box;
}

.form-select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.form-select.placeholder-selected {
  color: #9ca3af;
}

.select-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #6b7280;
}

/* Jenkins 정보 카드 */
.jenkins-info-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
  margin-top: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-item:last-of-type {
  margin-bottom: 16px;
}

.info-label {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  min-width: 60px;
  margin-right: 16px;
}

.info-value {
  color: #1e293b;
  font-weight: 500;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
}

.status-text {
  color: #dc2626;
  font-weight: 500;
  font-size: 14px;
}

.status-dot.connected {
  background: #10b981;
}

.status-text.connected {
  color: #059669;
}

/* Job 목록 */
.job-list-content {
  min-height: 200px;
}

.job-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.job-card-item {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
}

/* 빈 상태 */
.empty-state, .select-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon, .prompt-icon {
  color: #9ca3af;
  margin-bottom: 16px;
}

.empty-title, .prompt-title {
  font-size: 20px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-description, .prompt-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0 0 24px 0;
  line-height: 1.5;
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
  text-decoration: none;
}

.btn-primary {
  background: var(--main-color, #2563eb);
  color: white;
}

.btn-primary:hover {
  background: var(--main-color-hover, #1d4ed8);
  transform: translateY(-1px);
}

.create-job-btn {
  white-space: nowrap;
}

/* 반응형 */
@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header h1 {
    font-size: 24px;
  }

  .section {
    padding: 20px 16px;
  }

  .job-grid {
    grid-template-columns: 1fr;
  }

  .create-job-btn {
    width: 100%;
    justify-content: center;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 모달 */
.modal {
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
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
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 24px;
  padding-bottom: 20px;
}

.modal-title-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #dbeafe;
  border-radius: 10px;
  color: #2563eb;
}

.modal-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.modal-close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-close-btn:hover {
  background: #e2e8f0;
  color: #374151;
  transform: scale(1.05);
}

/* 모달 컨텐츠 */
.modal-content {
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.modal-description {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
}

.description-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #dbeafe;
  border-radius: 8px;
  color: #2563eb;
  flex-shrink: 0;
  margin-top: 2px;
}

.description-text {
  flex: 1;
}

.description-main {
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.description-sub {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* 폼 그룹 */
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
}

.form-input::placeholder {
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

/* 모달 액션 */
.modal-actions {
  padding: 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
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
@media (max-width: 640px) {
  .modal-overlay {
    padding: 16px;
  }

  .modal {
    border-radius: 12px;
  }

  .modal-header {
    padding: 20px 20px 0 20px;
    margin-bottom: 20px;
    padding-bottom: 16px;
  }

  .modal-title-section {
    gap: 10px;
  }

  .modal-icon {
    width: 36px;
    height: 36px;
  }

  .modal-title {
    font-size: 18px;
  }

  .modal-content {
    padding: 0 20px;
    gap: 20px;
  }

  .modal-description {
    padding: 16px;
    gap: 12px;
  }

  .description-icon {
    width: 28px;
    height: 28px;
  }

  .description-main {
    font-size: 15px;
  }

  .description-sub {
    font-size: 13px;
  }

  .modal-actions {
    padding: 20px;
  }

  .btn {
    width: 100%;
    padding: 14px 24px;
  }
}

/* 다크 모드 지원 (선택사항) */
@media (prefers-color-scheme: dark) {
  .modal {
    background: #1e293b;
    color: #f1f5f9;
  }

  .modal-header {
    border-bottom-color: #334155;
  }

  .modal-title {
    color: #f1f5f9;
  }

  .modal-close-btn {
    background: #334155;
    color: #94a3b8;
  }

  .modal-close-btn:hover {
    background: #475569;
    color: #e2e8f0;
  }

  .modal-description {
    background: #0f172a;
    border-color: #334155;
  }

  .description-main {
    color: #f1f5f9;
  }

  .description-sub {
    color: #94a3b8;
  }

  .form-input {
    background: #0f172a;
    border-color: #334155;
    color: #f1f5f9;
  }

  .form-input:focus {
    border-color: #3b82f6;
  }

  .modal-actions {
    border-top-color: #334155;
  }
}

</style>