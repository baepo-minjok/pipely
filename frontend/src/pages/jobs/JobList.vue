<script setup>
import {ref, onMounted, watch, computed} from 'vue';
import JobCard from '../../components/jobs/JobCard.vue';
import {useRouter} from 'vue-router';
import {useJobStore} from '../../stores/useJobStore.js';

const jobStore = useJobStore();
const router = useRouter();

const selectedJenkins = ref('');

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


onMounted(() => {
  jobStore.getJenkinsInfo();
});

watch(selectedJenkins, (id) => {
  if (id) jobStore.fetchJobList(id);
  console.log(selected)
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
              class="form-select"
              :class="{ 'placeholder-selected': selectedJenkins === '' }"
          >
            <option value="" disabled>Jenkins 인스턴스를 선택해주세요</option>
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
                @click="router.push(`/job/${job.name}`)"
                class="job-card-item"
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

.job-card-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
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
</style>