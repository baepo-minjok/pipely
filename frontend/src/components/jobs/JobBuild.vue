<script setup>
import {computed, defineProps, onMounted, onUnmounted, ref, toRefs} from 'vue';
import {buildApi} from '@/api/BuildApi';
import {
  formatDate,
  formatStageName,
  getStageStatusClass,
  getStatusClass,
  getStatusText,
  getTriggerText,
  getUser,
} from '@/utils/formatBuild';
import {useJobStore} from "@/stores/useJobStore.js";

const jobStore = useJobStore();
// const jobDetail = jobStore.jobDetail;
// const jobId = jobDetail.pipelineId;
const buildHistory = ref([]);
const isLogExpanded = ref(false);
let intervalId = null;

const props = defineProps({
  job: {
    type: Object,
    required: true
  },
  jobDetail: {
    type: Object,
    required: true
  }
});

const {job, jobDetail} = toRefs(props);

// jobStore에서 실시간 상태 가져오기
const currentJobState = computed(() => {
  return jobStore.getJobBuildState(jobDetail.value.pipelineId);
});

// 현재 빌드 상태 계산 수정
const currentBuildStatus = computed(() => {
  return currentJobState.value.buildState || 'BUILD_PENDING';
});

// 현재 진행률 계산 수정
const buildProgressPercentage = computed(() => {
  if (jobStore.isBuilding && currentBuildStatus.value === 'BUILD_RUNNING') {
    return Math.min(Math.max(currentJobState.value.buildProgress.progress || 0, 0), 100);
  }
  return 0;
});

// 스테이지별 상태 계산 수정
const getEnhancedStageStatus = (stageName) => {
  const baseStatus = getStageStatusClass(stageName, currentJobState.value.buildProgress.stages);
  // 현재 실행 중인 스테이지 확인
  if (currentJobState.value.buildProgress.currentStage === stageName && jobStore.isBuilding) {
    return 'running';
  }
  // BUILD_WAITING 상태 처리
  if (currentBuildStatus.value === 'BUILD_WAITING') {
    return 'WAITING';
  }
  return baseStatus;
};

const getBuildAllHistory = async () => {
  const response = await buildApi.getBuildAllHistory(jobDetail.value.pipelineId);
  if (response.status === 200) {
    buildHistory.value = [...response.data.data];
  }
};

const handleBuildRestartClick = async (excludedStageName) => {
  if (!jobDetail.value?.stageList) return;
  const includedStages = jobDetail.value.stageList
    .filter((stage) => stage.stageName !== excludedStageName)
    .map((stage) => stage.stageName);
  const requestBody = {
    jobId: jobDetail.value.pipelineId,
    stageBuilds: includedStages,
  };
  const response = await buildApi.triggerBuildStages(requestBody);
  if (response.status === 200) {
    console.log('✅ 특정 스테이지 실행 요청 성공', response.data);
  } else {
    console.error('❌ 특정 스테이지 실행 요청 실패');
  }
};

onMounted(async () => {
  await getBuildAllHistory();
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});
</script>

<template>
  <div class="container">
    <!-- 빌드 상태 오버뷰 -->
    <div class="section build-overview">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        빌드 상태
      </h3>

      <!-- 전체 빌드 상태 카드 -->
      <div :class="['build-status-card', currentBuildStatus.toLowerCase()]">
        <div class="build-status-header">
          <div class="status-icon-container">
            <svg v-if="currentBuildStatus === 'BUILD_RUNNING'" class="status-icon animate-spin" fill="none" height="24"
                 stroke="#d97706" stroke-width="2" viewBox="0 0 24 24" width="24">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
            <svg v-else-if="currentBuildStatus === 'BUILD_WAITING'" class="status-icon animate-waiting" fill="none"
                 height="24"
                 stroke="#2563eb" stroke-width="2" viewBox="0 0 24 24" width="24">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12,6 12,12 16,14"/>
            </svg>
            <svg v-else-if="currentBuildStatus === 'BUILD_SUCCESS'" class="status-icon" fill="none" height="24"
                 stroke="#059669" stroke-width="4" viewBox="0 0 24 24" width="24">
              <polyline points="20,6 9,17 4,12"/>
            </svg>
            <svg v-else-if="currentBuildStatus === 'BUILD_FAILURE'" class="status-icon" fill="none" height="24"
                 stroke="#ef4444" stroke-width="2" viewBox="0 0 24 24" width="24">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
            <svg v-else-if="currentBuildStatus === 'BUILD_STOPPING'" class="status-icon animate-pulse" fill="none"
                 height="24" stroke="#c2410c" stroke-width="2" viewBox="0 0 24 24" width="24">
              <circle cx="12" cy="12" r="10"/>
              <rect height="6" rx="1" ry="1" width="6" x="9" y="9"/>
            </svg>
            <svg v-else class="status-icon" fill="none" height="24" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="24">
              <circle cx="12" cy="12" r="10"/>
              <polyline points="12,6 12,12 16,14"/>
            </svg>
          </div>
          <div class="status-info">
            <div class="status-title">
              {{
                currentBuildStatus === 'BUILD_RUNNING' ? '빌드 실행 중' :
                  currentBuildStatus === 'BUILD_WAITING' ? '빌드 대기 중' :
                    currentBuildStatus === 'BUILD_SUCCESS' ? '빌드 성공' :
                      currentBuildStatus === 'BUILD_FAILURE' ? '빌드 실패' :
                        currentBuildStatus === 'BUILD_STOPPING' ? '빌드 중단 중' :
                          currentBuildStatus === 'BUILD_ABORTED' ? '빌드 중단됨' : '대기 중'
              }}
            </div>
            <div class="status-subtitle">
              {{
                currentBuildStatus === 'BUILD_WAITING' ? '빌드 시작을 기다리고 있습니다...' :
                  currentJobState.buildProgress.currentStage || '상태 정보 없음'
              }}
            </div>
          </div>
        </div>

        <!-- 진행률 바 (실행 중일 때만 표시) -->
        <div v-if="jobStore.isBuilding && currentBuildStatus === 'BUILD_RUNNING'"
             class="build-progress">
          <div class="progress-bar">
            <div
              :style="{ width: `${buildProgressPercentage}%` }"
              class="progress-fill"
            ></div>
            <div class="progress-shimmer"></div>
          </div>
          <div class="progress-info">
            <span class="progress-percentage">{{ Math.round(buildProgressPercentage) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 파이프라인 스테이지 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        파이프라인 단계
      </h3>
      <div class="pipeline-container">
        <div class="pipeline-flow">
          <div
            v-for="(stage, index) in jobDetail.stageList"
            :key="stage.stageName"
            class="pipeline-stage"
          >
            <div
              :class="[
                'stage-box',
                getEnhancedStageStatus(stage.stageName),
                { 'current': currentJobState.buildProgress.currentStage === formatStageName(stage.stageName) }
              ]"
            >
              <div class="stage-icon">
                <svg v-if="getEnhancedStageStatus(stage.stageName) === 'SUCCESS'" fill="#059669" height="16"
                     stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/>
                </svg>
                <svg v-else-if="getEnhancedStageStatus(stage.stageName) === 'FAILED'" fill="none" height="16"
                     stroke="#ef4444" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <line x1="18" x2="6" y1="6" y2="18"/>
                  <line x1="6" x2="18" y1="6" y2="18"/>
                </svg>
                <svg v-else-if="getEnhancedStageStatus(stage.stageName) === 'IN_PROGRESS'" class="animate-spin"
                     fill="none"
                     height="16" stroke="#d97706" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M21 12a9 9 0 11-6.219-8.56"/>
                </svg>
                <svg v-else-if="getEnhancedStageStatus(stage.stageName) === 'WAITING'" class="animate-waiting"
                     fill="none" height="16" stroke="#2563eb" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                <svg v-else fill="none" height="16" stroke="#000000" stroke-width="2" viewBox="0 0 24 24"
                     width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
              </div>
              <span class="stage-name">{{ formatStageName(stage.stageName) }}</span>
              <div v-if="getEnhancedStageStatus(stage.stageName) === 'IN_PROGRESS'" class="stage-pulse"></div>
              <div v-if="getEnhancedStageStatus(stage.stageName) === 'WAITING'" class="stage-waiting-pulse"></div>
            </div>
            <div v-if="index < jobDetail.stageList.length - 1"
                 :class="['stage-connector', { 'active': getEnhancedStageStatus(stage.stageName) === 'SUCCESS' }]"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 실행 로그 -->
    <div class="section log-section">
      <div class="log-header">
        <h3 class="section-title">
          <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14,2 14,8 20,8"/>
            <line x1="16" x2="8" y1="13" y2="13"/>
            <line x1="16" x2="8" y1="17" y2="17"/>
          </svg>
          실시간 실행 로그
        </h3>
        <div :class="['log-controls',{'expanded':isLogExpanded}]">
          <button
            class="log-control-btn"
            @click="isLogExpanded = !isLogExpanded"
          >
            <svg v-if="isLogExpanded" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                 width="16">
              <polyline points="4,14 10,14 10,20"/>
              <polyline points="20,10 14,10 14,4"/>
              <polyline points="14,10 21,3"/>
              <polyline points="3,21 10,14"/>
            </svg>
            <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <polyline points="15,3 21,3 21,9"/>
              <polyline points="9,21 3,21 3,15"/>
              <polyline points="21,3 14,10"/>
              <polyline points="3,21 10,14"/>
            </svg>
            {{ isLogExpanded ? '축소' : '확대' }}
          </button>
          <div v-if="jobStore.isBuilding || currentBuildStatus === 'BUILD_WAITING'" class="live-indicator">
            <div class="live-dot"></div>
            <span>{{ currentBuildStatus === 'BUILD_WAITING' ? '대기 중' : '실시간' }}</span>
          </div>
        </div>
      </div>
      <div :class="['log-container', { 'expanded': isLogExpanded }]">
        <pre class="log-content">{{
            currentBuildStatus === 'BUILD_WAITING' ?
              '빌드 시작을 기다리고 있습니다...\n시스템에서 빌드 큐를 처리 중입니다.' :
              currentJobState.buildProgress.log || '로그가 없습니다.'
          }}</pre>
        <div v-if="jobStore.isBuilding || currentBuildStatus === 'BUILD_WAITING'" class="log-loading">
          <div class="loading-dots">
            <div class="dot"></div>
            <div class="dot"></div>
            <div class="dot"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 실행 히스토리 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="#2563eb" height="20" stroke="currentColor" viewBox="0 0 24 24" width="20">
          <path
            d="M13.5,8H12V13L16.28,15.54L17,14.33L13.5,12.25V8M13,3A9,9 0 0,0 4,12H1L4.96,16.03L9,12H6A7,7 0 0,1 13,5A7,7 0 0,1 20,12A7,7 0 0,1 13,19C11.07,19 9.32,18.21 8.06,16.94L6.64,18.36C8.27,20 10.5,21 13,21A9,9 0 0,0 22,12A9,9 0 0,0 13,3"/>
        </svg>
        실행 히스토리
      </h3>
      <div class="table-container">
        <table class="history-table">
          <thead>
          <tr>
            <th>실행 일시</th>
            <th>실행자</th>
            <th>결과</th>
            <th>소요 시간</th>
            <th>트리거</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(build, index) in buildHistory" :key="index" class="history-row">
            <td>{{ formatDate(build.startedAt) }}</td>
            <td>
              <div class="user-info">
                <div class="user-avatar">
                  <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                {{ getUser(build.triggeredBy) }}
              </div>
            </td>
            <td>
                <span :class="['status-badge', getStatusClass(build.status)]">
                  <svg v-if="build.status === 'SUCCESS'" class="status-icon" fill="none" height="12"
                       stroke="#059669" stroke-width="2" viewBox="0 0 24 24" width="12">
                    <polyline points="20,6 9,17 4,12"/>
                  </svg>
                  <svg v-else-if="build.status === 'FAILURE'" class="status-icon" fill="none" height="12"
                       stroke="#dc2626" stroke-width="2" viewBox="0 0 24 24" width="12">
                    <line x1="18" x2="6" y1="6" y2="18"/>
                    <line x1="6" x2="18" y1="6" y2="18"/>
                  </svg>
                  <svg v-else class="status-icon" fill="none" height="12" stroke="#1F1F1F" stroke-width="2"
                       viewBox="0 0 24 24" width="12">
                    <circle cx="12" cy="12" r="10"/>
                    <polyline points="12,6 12,12 16,14"/>
                  </svg>
                  {{ getStatusText(build.status) }}
                </span>
            </td>
            <td>
              <div class="duration-info">
                <svg class="duration-icon" fill="none" height="14" stroke="currentColor" stroke-width="2"
                     viewBox="0 0 24 24" width="14">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                {{ build.durationStr }}
              </div>
            </td>
            <td>
              <div class="trigger-info">
                {{ getTriggerText(build.triggeredBy) }}
              </div>
            </td>
          </tr>
          <tr v-if="buildHistory.length === 0">
            <td class="no-data" colspan="5">
              <div class="empty-state">
                <svg fill="none" height="48" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24" width="48">
                  <path
                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                </svg>
                <p>실행 히스토리가 없습니다.</p>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
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

/* 빌드 상태 오버뷰 */
.build-overview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
}

.build-overview .section-title {
  color: white;
}

.build-overview .section-title svg {
  color: rgba(255, 255, 255, 0.8);
}

.build-status-card {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.build-status-card.build_waiting {
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(147, 197, 253, 0.3);
}

.build-status-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.status-icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.status-icon {
  color: white;
}

.status-info {
  flex: 1;
}

.status-title {
  font-size: 18px;
  font-weight: 600;
  color: white;
  margin-bottom: 4px;
}

.status-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

/* 빌드 진행률 */
.build-progress {
  margin-top: 16px;
}

.progress-bar {
  position: relative;
  height: 8px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffffff, #f0f9ff);
  border-radius: 4px;
  transition: width 0.5s ease;
}

.progress-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  animation: shimmer 2s infinite;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.progress-percentage {
  font-weight: 600;
  color: white;
}

/* 파이프라인 */
.pipeline-container {
  background: #f8fafc;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #f1f5f9;
}

.pipeline-flow {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  overflow-x: auto;
  padding: 16px 0;
}

.pipeline-stage {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.stage-box {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-radius: 12px;
  background: white;
  border: 2px solid #e2e8f0;
  min-width: 120px;
  transition: all 0.3s ease;
}

.stage-box.current {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

.stage-box.SUCCESS {
  border-color: #10b981;
  background: #ecfdf5;
  color: #059669;
}

.stage-box.FAILED {
  border-color: #ef4444;
  background: #fef2f2;
  color: #dc2626;
}

.stage-box.IN_PROGRESS {
  border-color: #f59e0b;
  background: #fffbeb;
  color: #d97706;
}

.stage-box.WAITING {
  border-color: #3b82f6;
  background: #eff6ff;
  color: #2563eb;
}

.stage-box.PENDING {
  border-color: #e2e8f0;
  background: #f8fafc;
  color: #64748b;
}

.stage-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: currentColor;
  color: white;
}

.stage-name {
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}

.stage-pulse {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 14px;
}

.stage-waiting-pulse {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 14px;
}

.stage-connector {
  width: 40px;
  height: 3px;
  background: #e2e8f0;
  margin: 0 8px;
  border-radius: 2px;
  transition: background 0.3s ease;
}

.stage-connector.active {
  background: #10b981;
}

/* 로그 섹션 */
.log-section {
  min-height: 400px;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.log-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.log-control-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.log-controls.expanded {
  position: fixed;
  top: 30px;
  right: 50px;
  z-index: 1001;
}

.log-control-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.live-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #dc2626;
  font-weight: 500;
}

.live-indicator:has(.live-dot) {
  color: #2563eb;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #dc2626;
  border-radius: 50%;
  animation: pulse-dot 1s infinite;
}

.log-container {
  position: relative;
  background: #1e293b;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.log-container.expanded {
  position: fixed;
  top: 20px;
  left: 20px;
  right: 20px;
  bottom: 20px;
  z-index: 1000;
  border-radius: 12px;
}

.log-content {
  color: #22c55e;
  padding: 20px;
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-wrap: break-word;
  word-break: break-all;
  max-width: 100%;
  height: 400px;
  overflow-y: auto;
  transition: height 0.3s ease;
}

.log-container.expanded .log-content {
  height: calc(100vh - 120px);
}

.log-loading {
  position: absolute;
  bottom: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots .dot {
  width: 6px;
  height: 6px;
  background: #22c55e;
  border-radius: 50%;
  animation: loading-bounce 1.4s infinite ease-in-out both;
}

.loading-dots .dot:nth-child(1) {
  animation-delay: -0.32s;
}

.loading-dots .dot:nth-child(2) {
  animation-delay: -0.16s;
}

/* 히스토리 테이블 */
.table-container {
  overflow: hidden;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  overflow-y: auto;
  max-height: 500px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.history-table th {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #f8fafc;
  padding: 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
}

.history-table tbody td {
  overflow-y: auto;
  max-height: 500px;
}

.history-table td {
  padding: 16px;
  font-size: 14px;
  color: #1f2937;
  border-bottom: 1px solid #f1f5f9;
}

.history-row {
  transition: background-color 0.2s ease;
}

.history-row:hover {
  background: #f8fafc;
}

.history-row:last-child td {
  border-bottom: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #f1f5f9;
  border-radius: 50%;
  color: #64748b;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
}

.status-badge.success {
  background: #dcfce7;
  color: #166534;
}

.status-badge.fail {
  background: #fef2f2;
  color: #991b1b;
}

.status-badge.pending {
  background: #fef3c7;
  color: #92400e;
}

.status-badge .status-icon {
  width: 12px;
  height: 12px;
}

.duration-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
}

.duration-icon {
  color: #9ca3af;
}

.trigger-info {
  font-size: 13px;
  color: #64748b;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  color: #9ca3af;
}

.empty-state svg {
  color: #d1d5db;
}

.empty-state p {
  margin: 0;
  font-style: italic;
}

.no-data {
  text-align: center;
}

/* 애니메이션 */
@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes loading-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@keyframes waiting-stage-pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.02);
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
@media (max-width: 768px) {
  .container {
    gap: 16px;
  }

  .section {
    padding: 20px 16px;
  }

  .pipeline-flow {
    justify-content: flex-start;
    overflow-x: auto;
  }

  .stage-box {
    min-width: 100px;
    padding: 12px 16px;
  }

  .log-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .log-controls {
    justify-content: space-between;
  }

  .log-container.expanded {
    top: 10px;
    left: 10px;
    right: 10px;
    bottom: 10px;
  }

  .history-table th,
  .history-table td {
    padding: 12px 8px;
    font-size: 13px;
  }

  .build-status-header {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .progress-info {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }
}
</style>