<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useErrorStore } from '@/stores/useErrorStore'

const props = defineProps({
  job: {
    type: Object,
    required: true
  },
  jobDetail: {
    type: Object,
    required: true
  }
})

const errorStore = useErrorStore()
const selectedView = ref('overview') // overview, builds, summary
const selectedBuildNumber = ref(null)

// Computed properties
const jobId = computed(() => props.jobDetail.pipelineId)
const failedBuilds = computed(() => errorStore.getFailedBuildsByJobId(jobId.value))
const buildSummary = computed(() => errorStore.getBuildSummaryByJobId(jobId.value))
const errorStats = computed(() => errorStore.getFailedBuildsStats(jobId.value))
const isLoadingBuilds = computed(() => errorStore.isLoadingFailedBuilds)
const isLoadingSummary = computed(() => errorStore.isLoadingBuildSummary)

// Methods
const fetchErrorData = async () => {
  if (!jobId.value) return

  try {
    await errorStore.fetchFailedBuilds(jobId.value)
  } catch (error) {
    console.error('Failed to fetch error data:', error)
  }
}

const fetchSummary = async (buildNumber = null) => {
  if (!jobId.value) return

  try {
    selectedBuildNumber.value = buildNumber
    await errorStore.fetchBuildSummary(jobId.value, buildNumber)
    selectedView.value = 'summary'
  } catch (error) {
    console.error('Failed to fetch summary:', error)
  }
}

// const formatDate = (dateString) => {
//   if (!dateString) return 'N/A'
//   return new Date(dateString).toLocaleString('ko-KR')
// }

const getStatusColor = (status) => {
  switch (status) {
    case 'FAILURE':
      return 'text-red-600'
    case 'ABORTED':
      return 'text-orange-600'
    case 'UNSTABLE':
      return 'text-yellow-600'
    default:
      return 'text-gray-600'
  }
}

const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'FAILURE':
      return 'badge-error'
    case 'ABORTED':
      return 'badge-warning'
    case 'UNSTABLE':
      return 'badge-unstable'
    default:
      return 'badge-default'
  }
}

// Lifecycle
onMounted(() => {
  fetchErrorData()
})

watch(() => jobId.value, (newJobId) => {
  if (newJobId) {
    fetchErrorData()
  }
})
</script>

<template>
  <div class="error-analysis">
    <!-- 헤더 -->
    <div class="analysis-header">
      <h2 class="analysis-title">에러 분석</h2>
      <div class="view-tabs">
        <button
            :class="['tab-btn', { active: selectedView === 'overview' }]"
            @click="selectedView = 'overview'"
        >
          개요
        </button>
        <button
            :class="['tab-btn', { active: selectedView === 'builds' }]"
            @click="selectedView = 'builds'"
        >
          실패 빌드
        </button>
        <button
            :class="['tab-btn', { active: selectedView === 'summary' }]"
            @click="selectedView = 'summary'"
            :disabled="!buildSummary"
        >
          AI 분석
        </button>
      </div>
    </div>

    <!-- 개요 탭 -->
    <div v-if="selectedView === 'overview'" class="overview-section">
      <div v-if="errorStats" class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon error">
            <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
              <path d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ errorStats.totalFailures }}</div>
            <div class="stat-label">총 실패 빌드</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon warning">
            <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
              <path d="M12 6v6h4l-1 5-5-5H6l6-6z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ errorStats.recentFailures }}</div>
            <div class="stat-label">최근 7일 실패</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon info">
            <svg fill="none" height="24" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="24">
              <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ errorStats.mostCommonError }}</div>
            <div class="stat-label">주요 에러 유형</div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg fill="none" height="48" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24" width="48">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h3 class="empty-title">실패한 빌드가 없습니다</h3>
        <p class="empty-description">이 Job에서 실패한 빌드 기록을 찾을 수 없습니다.</p>
      </div>
    </div>

    <!-- 실패 빌드 탭 -->
    <div v-if="selectedView === 'builds'" class="builds-section">
      <div v-if="isLoadingBuilds" class="loading-state">
        <div class="loading-spinner"></div>
        <p>실패한 빌드를 조회하고 있습니다...</p>
      </div>

      <div v-else-if="failedBuilds.length > 0" class="builds-list">
        <div
            v-for="build in failedBuilds"
            :key="build.buildNumber"
            class="build-card"
        >
          <div class="build-header">
            <div class="build-info">
              <h4 class="build-title">빌드 #{{ build.buildNumber }}</h4>
              <span :class="['build-status', getStatusBadgeClass(build.status)]">
                {{ build.status }}
              </span>
            </div>
            <div class="build-meta">
              <span class="build-date">{{ build.timestamp }}</span>
              <span class="build-duration">{{ build.duration }}ms</span>
            </div>
          </div>

          <div v-if="build.errorMessage" class="build-error">
            <p class="error-message">{{ build.errorMessage }}</p>
          </div>

          <div class="build-actions">
            <button
                class="action-btn secondary"
                @click="fetchSummary(build.buildNumber)"
                :disabled="isLoadingSummary"
            >
              <svg v-if="isLoadingSummary" class="btn-icon animate-spin" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"/>
              </svg>
              <svg v-else class="btn-icon" fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
              </svg>
              AI 분석
            </button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg fill="none" height="48" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24" width="48">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <h3 class="empty-title">실패한 빌드가 없습니다</h3>
        <p class="empty-description">이 Job에서 실패한 빌드 기록을 찾을 수 없습니다.</p>
      </div>
    </div>

    <!-- AI 분석 탭 -->
    <div v-if="selectedView === 'summary'" class="summary-section">
      <div v-if="isLoadingSummary" class="loading-state">
        <div class="loading-spinner"></div>
        <p>AI가 빌드 실패를 분석하고 있습니다...</p>
      </div>

      <div v-else-if="buildSummary" class="summary-content">
        <div class="summary-header">
          <h3 class="summary-title">
            #{{ selectedBuildNumber || '최근' }} 빌드 분석 결과
          </h3>
        </div>

        <div class="summary-body">
          <div v-if="buildSummary.summary" class="summary-section-item">
<!--            <h4 class="section-title"></h4>-->
            <div class="section-content">
              <p>{{ buildSummary.summary }}</p>
            </div>
          </div>

          <div v-if="buildSummary.solution" class="summary-section-item">
            <h4 class="section-title">해결 방안</h4>
            <div class="section-content">
              <p>{{ buildSummary.solution }}</p>
            </div>
          </div>

          <div v-if="buildSummary.recommendations && buildSummary.recommendations.length > 0" class="summary-section-item">
            <h4 class="section-title">추천 사항</h4>
            <div class="section-content">
              <ul class="recommendation-list">
                <li v-for="(rec, index) in buildSummary.recommendations" :key="index">
                  {{ rec }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <div class="empty-icon">
          <svg fill="none" height="48" stroke="currentColor" stroke-width="1" viewBox="0 0 24 24" width="48">
            <path d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
          </svg>
        </div>
        <h3 class="empty-title">분석 결과가 없습니다</h3>
        <p class="empty-description">실패한 빌드를 선택하여 AI 분석을 요청해주세요.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.error-analysis {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

/* 헤더 */
.analysis-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
}

.analysis-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.view-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 8px;
}

.tab-btn {
  padding: 8px 16px;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-btn:hover:not(:disabled) {
  color: #1e293b;
  background: rgba(255, 255, 255, 0.5);
}

.tab-btn.active {
  background: white;
  color: #2563eb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.tab-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 통계 그리드 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.stat-icon.error {
  background: #fef2f2;
  color: #dc2626;
}

.stat-icon.warning {
  background: #fffbeb;
  color: #f59e0b;
}

.stat-icon.info {
  background: #eff6ff;
  color: #2563eb;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

/* 빌드 목록 */
.builds-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.build-card {
  padding: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fafafa;
}

.build-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.build-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.build-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.build-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-error {
  background: #fef2f2;
  color: #dc2626;
}

.badge-warning {
  background: #fffbeb;
  color: #f59e0b;
}

.badge-unstable {
  background: #fefce8;
  color: #ca8a04;
}

.badge-default {
  background: #f1f5f9;
  color: #64748b;
}

.build-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: #64748b;
}

.build-error {
  margin-bottom: 16px;
  padding: 12px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 6px;
}

.error-message {
  font-size: 14px;
  color: #dc2626;
  margin: 0;
  font-family: monospace;
}

.build-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #d1d5db;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn.secondary {
  color: #2563eb;
  border-color: #2563eb;
}

.action-btn.secondary:hover:not(:disabled) {
  background: #eff6ff;
}

.btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

/* 요약 섹션 */
.summary-content {
  max-width: none;
}

.summary-header {
  margin-bottom: 24px;
}

.summary-title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.summary-body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.summary-section-item {
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px 0;
}

.section-content {
  color: #374151;
  line-height: 1.6;
}

.section-content p {
  margin: 0;
}

.recommendation-list {
  margin: 0;
  padding-left: 20px;
}

.recommendation-list li {
  margin-bottom: 8px;
}

/* 공통 상태 */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e2e8f0;
  border-top: 3px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.empty-icon {
  color: #9ca3af;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
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
@media (max-width: 768px) {
  .error-analysis {
    padding: 16px;
  }

  .analysis-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }

  .view-tabs {
    justify-content: center;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .build-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .build-meta {
    justify-content: space-between;
  }
}

/* 이 컴포넌트 <style>에 추가 */
.section-content,
.summary-body,
.build-error {
  white-space: pre-line;   /* \n을 줄바꿈으로 렌더 */
  word-break: break-word;
}

</style>
