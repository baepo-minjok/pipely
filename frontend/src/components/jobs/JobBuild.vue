<script setup>
import {onMounted, onUnmounted, ref} from 'vue';
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
import {jobApi} from "@/api/JobApi.js";


const jobStore = useJobStore();
const jobDetail = jobStore.jobDetail;
const jobId = jobDetail.pipelineId;

const streamLogText = ref('');
const buildHistory = ref([]);
const isFetchingHistory = ref(false);
let intervalId = null;

const getBuildStreamLog = async () => {
  const response = await buildApi.getJobBuildStreamLog(jobId);
  if (response.status === 200) {
    streamLogText.value = response.data.log.join('\n');
  }
};

const getBuildAllHistory = async () => {
  const response = await buildApi.getBuildAllHistory(jobId);
  if (response.status === 200) {
    buildHistory.value = [...response.data.data];
  }
  console.log(buildHistory.value);
};

const getBuildLatestHistory = async () => {

  try {
    const buildNumber = await jobApi.getCurrentBuildNumber(jobId);

    const data = {
      jobId: jobId,
      buildNumber: buildNumber,
    };

    const response = await buildApi.getBuildLatestLog(data);
    console.log(response);

    streamLogText.value = response.data?.data.log.join('\n');
  } catch (error) {
    streamLogText.value = "실행로그 로딩에 실패했습니다.."
  }
};

const handleBuildRestartClick = async (excludedStageName) => {
  if (!jobDetail?.stageList) return;
  const includedStages = jobDetail.stageList
    .filter((stage) => stage.stageName !== excludedStageName)
    .map((stage) => stage.stageName);
  const requestBody = {
    jobId: jobId,
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
  await getBuildLatestHistory();
});

onUnmounted(() => {
  if (intervalId) {
    clearInterval(intervalId);
  }
});
</script>

<template>
  <div class="container">
    <!-- 파이프라인 흐름 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <path d="M12 2L2 7l10 5 10-5-10-5z"/>
          <path d="M2 17l10 5 10-5"/>
          <path d="M2 12l10 5 10-5"/>
        </svg>
        파이프라인 흐름
      </h3>
      <div class="pipeline-flow">
        <div
          v-for="(stage, index) in jobDetail.stageList"
          :key="stage.stageName"
          class="pipeline-stage"
        >
          <div
            :class="getStageStatusClass(stage.stageName)"
            class="stage-box"
          >
            <div class="stage-icon">
              <svg v-if="getStageStatusClass(stage.stageName) === 'success'" fill="none" height="16"
                   stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
              <svg v-else-if="getStageStatusClass(stage.stageName) === 'fail'" fill="none" height="16"
                   stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <line x1="18" x2="6" y1="6" y2="18"/>
                <line x1="6" x2="18" y1="6" y2="18"/>
              </svg>
              <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12,6 12,12 16,14"/>
              </svg>
            </div>
            <span class="stage-name">{{ formatStageName(stage.stageName) }}</span>
          </div>
          <div v-if="index < jobDetail.stageList.length - 1" class="stage-connector"></div>
        </div>
      </div>
    </div>

    <!-- Stage 상세 정보 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
          <line x1="8" x2="16" y1="21" y2="21"/>
          <line x1="12" x2="12" y1="17" y2="21"/>
        </svg>
        Job 상세 정보
      </h3>
      <div class="stage-grid">
        <div v-for="stage in jobDetail.stageList" :key="stage.stageName" class="stage-card">
          <div class="stage-card-header">
            <div class="stage-card-title">
              <span>{{ formatStageName(stage.stageName) }}</span>
            </div>
            <div :class="getStageStatusClass(stage.stageName)" class="stage-status">
              <svg v-if="getStageStatusClass(stage.stageName) === 'success'" fill="none" height="20"
                   stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <polyline points="20,6 9,17 4,12"/>
              </svg>
              <svg v-else-if="getStageStatusClass(stage.stageName) === 'fail'" fill="none" height="20"
                   stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <line x1="18" x2="6" y1="6" y2="18"/>
                <line x1="6" x2="18" y1="6" y2="18"/>
              </svg>
              <svg v-else fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <circle cx="12" cy="12" r="10"/>
              </svg>
            </div>
          </div>
          <div class="stage-card-body">
            <div class="stage-info">
              <p class="info-item">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                실행자: admin
              </p>
              <p class="info-item">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                시간: 15:42:01
              </p>
              <p class="info-item">
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
                소요: 30초
              </p>
            </div>
            <button
              v-if="getStageStatusClass(stage.stageName) === 'fail'"
              class="retry-btn"
              @click="handleBuildRestartClick(stage.stageName)"
            >
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <polyline points="23,4 23,10 17,10"/>
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
              </svg>
              재시도
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 실행 로그 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14,2 14,8 20,8"/>
          <line x1="16" x2="8" y1="13" y2="13"/>
          <line x1="16" x2="8" y1="17" y2="17"/>
        </svg>
        최신 실행 로그
      </h3>
      <div class="log-container">
        <pre class="log-content">{{ streamLogText || '로그가 없습니다.' }}</pre>
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
          <tr v-for="(build, index) in buildHistory" :key="index">
            <td>{{ formatDate(build.startedAt) }}</td>
            <td>{{ getUser(build.triggeredBy) }}</td>
            <td>
                <span :class="getStatusClass(build.status)" class="status-badge">
                  {{ getStatusText(build.status) }}
                </span>
            </td>
            <td>{{ build.durationStr }}</td>
            <td>{{ getTriggerText(build.triggeredBy) }}</td>
          </tr>
          <tr v-if="buildHistory.length === 0">
            <td class="no-data" colspan="5">실행 히스토리가 없습니다.</td>
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

/* 파이프라인 흐름 */
.pipeline-flow {
  max-width: 740px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  padding: 16px 0;
  flex-wrap: nowrap;
  overflow-x: auto;
}

.pipeline-stage {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.stage-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  border-radius: 8px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  min-width: 120px;
  max-width: 120px;
  transition: all 0.2s ease;
}

.stage-box.success {
  border-color: #10b981;
  background: #ecfdf5;
  color: #059669;
}

.stage-box.fail {
  border-color: #ef4444;
  background: #fef2f2;
  color: #dc2626;
}

.stage-box.pending {
  border-color: #f59e0b;
  background: #fffbeb;
  color: #d97706;
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

.stage-icon svg {
  color: #059669;
}

.stage-name {
  font-size: 14px;
  font-weight: 500;
  text-align: center;
}

.stage-connector {
  width: 40px;
  height: 2px;
  background: #e2e8f0;
  margin: 0 8px;
}

/* Stage 카드 */
.stage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.stage-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
}

.stage-card:hover {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.stage-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.stage-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.stage-status {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e2e8f0;
  color: #64748b;
}

.stage-status.success {
  background: #10b981;
  color: white;
}

.stage-status.fail {
  background: #ef4444;
  color: white;
}

.stage-status.pending {
  background: #f59e0b;
  color: white;
}

.stage-card-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.stage-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.info-item svg {
  color: #9ca3af;
}

.retry-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.retry-btn:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

/* 로그 */
.log-container {
  background: #1e293b;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
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
  max-height: 400px;
  overflow-y: auto;
}


/* 히스토리 테이블 */
.table-container {
  overflow-x: auto;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

.history-table th {
  background: #f8fafc;
  padding: 12px 16px;
  text-align: left;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e2e8f0;
}

.history-table td {
  padding: 12px 16px;
  font-size: 14px;
  color: #1f2937;
  border-bottom: 1px solid #f1f5f9;
}

.history-table tr:last-child td {
  border-bottom: none;
}

.history-table tr:hover {
  background: #f8fafc;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 4px;
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

.no-data {
  text-align: center;
  color: #9ca3af;
  font-style: italic;
}


@media (max-width: 768px) {
  .container {
    gap: 16px;
  }

  .section {
    padding: 20px 16px;
  }

  .pipeline-flow {
    justify-content: flex-start;
  }

  .stage-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .stage-card-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .retry-btn {
    align-self: flex-end;
  }

  .pipeline-flow {
    flex-wrap: nowrap;
    overflow-x: auto;
  }
}
</style>
