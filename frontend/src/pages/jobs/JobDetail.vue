<script setup>
import {computed, onMounted, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import JobInfo from '@/components/jobs/JobInfo.vue';
import JobBuild from '@/components/jobs/JobBuild.vue';
import ErrorAnalysis from "@/pages/jobs/ErrorAnalysis.vue";
import {useJobStore} from '@/stores/useJobStore';

const route = useRoute();
const jobId = ref(route.query.id);
const selectedTab = ref('detail');
const selectedTabComponent = computed(() => {
  switch (selectedTab.value) {
    case 'detail':
      return JobInfo;
    case 'build':
      return JobBuild;
    case 'errors':
      return ErrorAnalysis;
    default:
      return JobInfo;
  }
});
const jobStore = useJobStore();

// 다음으로 변경
const currentJob = computed(() => {
  return jobStore.getJobBuildState(jobId.value);
});

const jobDetail = computed(() => {
  return jobStore.jobDetails[jobId.value] || {};
});

const handleBuildRunClick = async () => {
  selectedTab.value = 'build';
  try {
    await jobStore.getBuildInfo(jobId.value, true);
  } catch (error) {
    console.error('Build error:', error);
  }
};

const handleStopBuild = async () => {
  try {
    await jobStore.stopBuild(jobId.value);
  } catch (error) {
    console.error('Stop build error:', error);
  }
};

// 진행률 계산 수정
const progressPercentage = computed(() => {
  if (jobStore.isBuilding && currentJob.value.buildProgress.status === 'BUILD_RUNNING') {
    return Math.min(Math.max(currentJob.value.buildProgress.progress || 0, 0), 100);
  }
  return 0;
});

// 버튼 텍스트와 클래스 함수들을 computed로 변경
const buttonText = computed(() => {
  switch (currentJob.value.buildProgress.status) {
    case 'BUILD_RUNNING':
      return '실행 중';
    case 'BUILD_STOPPING':
      return '중단 중';
    case 'BUILD_WAITING':
      return '빌드 준비 중..';
    case 'BUILD_SUCCESS':
      return '재실행';
    case 'BUILD_FAILURE':
      return '재시도';
    case 'BUILD_ABORTED':
      return '실행하기';
    default:
      return '실행하기';
  }
});

const buttonClass = computed(() => {
  switch (currentJob.value.buildProgress.status) {
    case 'BUILD_RUNNING':
      return 'btn-running';
    case 'BUILD_STOPPING':
      return 'btn-stopping';
    case 'BUILD_WAITING':
      return 'btn-waiting';
    case 'BUILD_SUCCESS':
      return 'btn-success';
    case 'BUILD_FAILURE':
      return 'btn-retry';
    case 'BUILD_ABORTED':
      return 'btn-primary';
    default:
      return 'btn-primary';
  }
});

watch(
  () => jobId.value,
  (newId) => jobStore.fetchJobDetail(newId),
  {immediate: true}
);

onMounted(async () => {
  await jobStore.getBuildInfo(jobId.value, false);
});
</script>

<template>
  <div class="page-container">
    <div class="content-wrapper">
      <div class="header">
        <div class="header-content">
          <h1 class="page-title">Job 상세</h1>
          <div class="breadcrumb">
            <span class="breadcrumb-item">Jenkins</span>
            <svg class="breadcrumb-separator" fill="none" height="14" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="14">
              <polyline points="9,18 15,12 9,6"/>
            </svg>
            <span class="breadcrumb-item">{{ jobDetail.name || 'Job' }}</span>
            <svg class="breadcrumb-separator" fill="none" height="14" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="14">
              <polyline points="9,18 15,12 9,6"/>
            </svg>
            <span class="breadcrumb-item current">상세 정보</span>
          </div>
        </div>
      </div>

      <div class="layout">
        <aside class="sidebar">
          <nav class="nav-menu">
            <button
              :class="['nav-item', { active: selectedTab === 'detail' }]"
              @click="selectedTab = 'detail'"
            >
              <svg class="nav-icon" fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                   width="20">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14,2 14,8 20,8"/>
                <line x1="16" x2="8" y1="13" y2="13"/>
                <line x1="16" x2="8" y1="17" y2="17"/>
              </svg>
              <span>상세 정보</span>
            </button>
            <button
              :class="['nav-item', { active: selectedTab === 'build' }]"
              @click="selectedTab = 'build'"
            >
              <svg class="nav-icon" fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                   width="20">
                <path d="M12 2L2 7l10 5 10-5z"/>
                <path d="M2 17l10 5 10-5"/>
                <path d="M2 12l10 5 10-5"/>
              </svg>
              <span>빌드</span>
            </button>
            <button
                :class="['nav-item', { active: selectedTab === 'errors' }]"
                @click="selectedTab = 'errors'"
            >
              <svg class="nav-icon" fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                   width="20">
                <path d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"/>
              </svg>
              <span>에러 분석</span>
            </button>
          </nav>

          <div class="action-section">
            <button
              :class="['action-btn', buttonClass, { 'has-progress': jobStore.isBuilding }]"
              :disabled="currentJob.buildProgress.status === 'BUILD_STOPPING' || currentJob.buildProgress.status === 'BUILD_WAITING'"
              :style="jobStore.isBuilding ? { '--progress': `${progressPercentage}%` } : {}"
              @click="currentJob.buildProgress.status === 'BUILD_RUNNING' ? handleStopBuild() : handleBuildRunClick()"
            >
              <!-- 진행률 배경 -->
              <div v-if="jobStore.buildProgress.isBuilding" class="btn-progress-bg">
                <div :style="{ width: `${progressPercentage}%` }" class="btn-progress-fill"></div>
                <div class="btn-wave-effect"></div>
              </div>

              <!-- 버튼 내용 -->
              <div class="btn-content">
                <svg v-if="currentJob.buildProgress.status === 'BUILD_RUNNING'" class="btn-icon animate-pulse"
                     fill="none"
                     height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <rect height="10" rx="1" ry="1" width="4" x="6" y="7"/>
                  <rect height="10" rx="1" ry="1" width="4" x="14" y="7"/>
                </svg>
                <svg v-else-if="currentJob.buildProgress.status === 'BUILD_WAITING'"
                     class="btn-icon animate-waiting"
                     fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                <svg v-else-if="jobStore.buildProgress.status === 'BUILD_SUCCESS'" class="btn-icon"
                     fill="currentColor"
                     height="16" viewBox="0 0 24 24" width="16">
                  <path
                    d="M12,5V1L7,6L12,11V7A6,6 0 0,1 18,13A6,6 0 0,1 12,19A6,6 0 0,1 6,13H4A8,8 0 0,0 12,21A8,8 0 0,0 20,13A8,8 0 0,0 12,5Z"/>
                </svg>
                <svg v-else-if="jobStore.buildProgress.status === 'BUILD_FAILURE'" class="btn-icon"
                     fill="currentColor"
                     height="16" viewBox="0 0 24 24" width="16">
                  <path
                    d="M12,5V1L7,6L12,11V7A6,6 0 0,1 18,13A6,6 0 0,1 12,19A6,6 0 0,1 6,13H4A8,8 0 0,0 12,21A8,8 0 0,0 20,13A8,8 0 0,0 12,5Z"/>
                </svg>
                <svg v-else class="btn-icon" fill="none" height="16" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24" width="16">
                  <polygon points="5,3 19,12 5,21"/>
                </svg>
                <span class="btn-text">{{ buttonText }}</span>
                <span v-if="jobStore.buildProgress.isBuilding" class="progress-percentage">{{
                    Math.round(progressPercentage)
                  }}%</span>
              </div>
            </button>
          </div>
        </aside>

        <main class="content">
          <component
            :is="selectedTabComponent"
            :job="currentJob"
            :job-detail="jobDetail"
          />
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 24px;
}

.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
}

/* 헤더 */
.header {
  margin-bottom: 32px;
}

.header-content {
  margin-bottom: 16px;
}

.page-title {
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

/* 빌드 상태 배너 */
.build-status-banner {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.status-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f1f5f9;
}

.status-icon {
  color: #2563eb;
}

.status-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-text {
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #2563eb;
  min-width: 40px;
}

/* 레이아웃 */
.layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

/* 사이드바 */
.sidebar {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  height: fit-content;
  position: sticky;
  top: 24px;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.nav-item.active {
  background: var(--main-color);
  color: white;
}

.nav-item.active .nav-icon {
  color: white;
}

.nav-icon {
  color: #64748b;
  transition: color 0.2s ease;
}

/* 액션 섹션 */
.action-section {
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.action-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 14px 20px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  min-height: 48px;
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 진행률 배경 */
.btn-progress-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 8px;
  overflow: hidden;
}

.btn-progress-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.1) 50%, rgba(255, 255, 255, 0.2) 100%);
  transition: width 0.5s ease;
}

.btn-wave-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.1) 25%, rgba(255, 255, 255, 0.2) 50%, rgba(255, 255, 255, 0.1) 75%, transparent 100%);
  animation: wave 2s ease-in-out infinite;
  transform: translateX(-100%);
}

.btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 8px;
  color: inherit;
}

.btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.btn-text {
  font-weight: 600;
}

.progress-percentage {
  font-size: 12px;
  font-weight: 700;
  margin-left: 4px;
  opacity: 0.9;
}

/* 버튼 상태별 스타일 */
.btn-primary {
  background: #2563eb;
  color: white;
  box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(37, 99, 235, 0.3);
}

.btn-running {
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  box-shadow: 0 2px 8px rgba(245, 158, 11, 0.3);
}

.btn-running:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
}

.btn-stopping {
  background: linear-gradient(135deg, #ea580c, #c2410c);
  color: white;
  box-shadow: 0 2px 8px rgba(234, 88, 12, 0.3);
  animation: stopping-pulse 1s ease-in-out infinite;
}

.btn-waiting {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
  position: relative;
  overflow: hidden;
}

.btn-waiting::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
  rgba(147, 197, 253, 0.3),
  rgba(59, 130, 246, 0.3)
  );
  border-radius: 8px;
  animation: waiting-pulse 2s ease-in-out infinite;
}

.btn-waiting::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  animation: waiting-shimmer 2.5s infinite;
}

.btn-success {
  background: #10b981;
  color: white;
  box-shadow: 0 2px 4px rgba(16, 185, 129, 0.2);
}

.btn-success:hover:not(:disabled) {
  background: #059669;
  transform: translateY(-1px);
}

.btn-retry {
  background: #dc2626;
  color: white;
  box-shadow: 0 2px 4px rgba(220, 38, 38, 0.2);
}

.btn-retry:hover:not(:disabled) {
  background: #b91c1c;
  transform: translateY(-1px);
}

/* 메인 컨텐츠 */
.content {
  min-height: 600px;
}

/* 애니메이션 */
@keyframes wave {
  0% {
    transform: translateX(-100%) skewX(-15deg);
  }
  50% {
    transform: translateX(0%) skewX(-15deg);
  }
  100% {
    transform: translateX(100%) skewX(-15deg);
  }
}

@keyframes stopping-pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(234, 88, 12, 0.3);
  }
  50% {
    box-shadow: 0 4px 16px rgba(234, 88, 12, 0.6);
  }
}

@keyframes waiting-pulse {
  0%, 100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes waiting-shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes animate-waiting {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.05);
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

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animate-waiting {
  animation: animate-waiting 2s ease-in-out infinite;
}

/* 반응형 */
@media (max-width: 1024px) {
  .layout {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .sidebar {
    position: static;
  }

  .nav-menu {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }

  .page-title {
    font-size: 24px;
  }

  .sidebar {
    padding: 20px 16px;
  }

  .nav-menu {
    grid-template-columns: 1fr 1fr;
  }

  .build-status-banner {
    padding: 12px 16px;
  }

  .status-content {
    gap: 12px;
  }

  .status-indicator {
    width: 32px;
    height: 32px;
  }
}
</style>