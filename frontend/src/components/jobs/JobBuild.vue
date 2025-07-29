<script setup>
import { onMounted, ref } from 'vue';
import { buildApi } from '@/api/BuildApi';
import {
  getStatusText,
  getStatusClass,
  getUser,
  getTriggerText,
  formatDate,
  formatStageName,
  getStageStatusClass,
} from '@/utils/formatBuild';

const props = defineProps({
  jobDetail: Object,
  scriptText: String,
  selectedItem: Object,
  jobId: String,
});

const streamLogText = ref('');
const buildHistory = ref([]);

const getBuildStreamLog = async () => {
  const response = await buildApi.getJobBuildStreamLog(props.jobId);

  if (response.status === 200) {
    streamLogText.value = response.data.log.join('\n');
  }
};

const getBuildHistoryAll = async () => {
  const response = await buildApi.getBuildHistoryAll(props.jobId);

  if (response.status === 200) {
    buildHistory.value = response.data.data;
  }
};

const handleBuildRestartClick = async (excludedStageName) => {
  if (!props.jobDetail?.stageList) return;

  const includedStages = props.jobDetail.stageList
    .filter((stage) => stage.stageName !== excludedStageName)
    .map((stage) => stage.stageName);

  const requestBody = {
    jobId: props.jobId,
    stageBuilds: includedStages,
  };

  const response = await buildApi.triggerBuildStages(requestBody);

  if (response.status === 200) {
    console.log('✅ 특정 스테이지 실행 요청 성공', response.data);
  } else {
    console.error('❌ 특정 스테이지 실행 요청 실패');
  }
};

const handleBuildRunClick = () => {
  // 수동 실행
};

onMounted(async () => {
  await getBuildStreamLog();
  await getBuildHistoryAll();
});
</script>

<template>
  <section class="container">
    <!-- 파이프라인 흐름 -->
    <div class="pipeline_flow">
      <h3 class="section_title">파이프라인 흐름</h3>
      <div class="stage_flow">
        <div
          v-for="stage in jobDetail.stageList"
          :key="stage.stageName"
          class="stage_flow_box"
          :class="getStageStatusClass(stage.stageName)"
        >
          <span>{{ formatStageName(stage.stageName) }}</span>
        </div>
      </div>
    </div>

    <!-- Stage 상세 정보 -->
    <div class="stage_info">
      <h3 class="section_title">Job 상세 정보</h3>
      <div class="stage_box">
        <div class="stage_card" v-for="stage in jobDetail.stageList" :key="stage.stageName">
          <div class="stage_header">
            <p>Job: {{ formatStageName(stage.stageName) }}</p>
            <img
              v-if="getStageStatusClass(stage.stageName) === 'success'"
              src="/src/assets/icons/check.svg"
              alt="success"
            />
            <img
              v-else-if="getStageStatusClass(stage.stageName) === 'fail'"
              src="/src/assets/icons/fail.svg"
              alt="fail"
            />
          </div>
          <div class="stage_body">
            <div>
              <p>실행자: admin</p>
              <p>시간: 15:42:01</p>
              <p>소요: 30초</p>
            </div>
            <button
              v-if="getStageStatusClass(stage.stageName) === 'fail'"
              class="restart_btn"
              @click="handleBuildRestartClick(stage.stageName)"
            >
              재시도
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 실행 로그 -->
    <div class="log_section">
      <h3 class="section_title">실행 로그</h3>
      <pre class="log_box"
        >{{ streamLogText }}
      </pre>
    </div>

    <!-- 실행 히스토리 -->
    <div class="history_section">
      <h3 class="section_title">실행 히스토리</h3>
      <table class="history_table">
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
            <td :class="getStatusClass(build.status)">
              {{ getStatusText(build.status) }}
            </td>
            <td>{{ build.durationStr }}</td>
            <td>{{ getTriggerText(build.triggeredBy) }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 수동 실행 -->
    <div class="manual_run" v-if="jobDetail && jobDetail.lightScriptDto">
      <h3 class="section_title">수동 실행</h3>
      <div class="run_box">
        <p>Git 브랜치</p>
        <input class="input" :value="jobDetail.lightScriptDto.branch" placeholder="번치 선택" />
        <p>버전 태그</p>
        <input class="input" placeholder="v2.3.1" />
        <button class="btn run_btn" @click="handleBuildRunClick">수동 실행하기</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  gap: 50px;
  padding: 30px;
}

.section_title {
  font-weight: bold;
  margin-bottom: 14px;
}

.stage_flow {
  display: flex;
  gap: 12px;
}

.stage_flow_box {
  padding: 20px 15px;
  border-radius: 8px;
  background: #f4f4f4;
  border: 1px solid #ddd;
  min-width: 100px;
  text-align: center;
}

.stage_flow_box.success {
  border-color: var(--green-font);
  color: var(--green-font);
}

.stage_flow_box.fail {
  border-color: var(--red-font);
  color: var(--red-font);
}

.stage_info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stage_card {
  padding: 16px 20px;
  border: 1px solid #ddd;
  border-radius: 10px;
  width: 200px;
  line-height: 140%;
  box-shadow: 0 4px 4px rgba(0, 0, 0, 0.15);
}

.stage_header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;

  & > p {
    font-weight: bold;
  }
}

.stage_body {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.restart_btn {
  padding: 7px 14px;
  background-color: #ef4444;
  color: white;
  border: none;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background-color: var(--red-font);
  }
}

.stage_box {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 5px;
}

.log_box {
  background-color: #0f172a;
  color: #22c55e;
  padding: 16px;
  border-radius: 6px;
  font-family: monospace;
}

.history_table {
  width: 100%;
  border-collapse: collapse;
}

.history_table th,
.history_table td {
  border: 1px solid #ddd;
  padding: 8px 12px;
  text-align: center;
}

.history_table .success {
  color: green;
}

.history_table .fail {
  color: red;
}

.notify-box label {
  display: inline-block;
  margin-right: 20px;
}

.script-box {
  width: 100%;
  height: 300px;
  padding: 14px;
  border: 1px solid #ddd;
  background: #f9f9f9;
  border-radius: 8px;
  font-family: monospace;
  white-space: pre-wrap;
}

.run_box {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  border: 1px solid var(--gray200);
  padding: 25px 28px;

  & > p {
    font-size: 14px;
    margin-bottom: 10px;
  }

  & > .input {
    padding: 10px 14px;
    border-radius: 8px;
    background-color: #f3f4f6;
    border: none;
    margin-bottom: 20px;
  }
}

.run_btn {
  width: 124px;
  border-radius: 6px;
  padding: 8px 10px;
}
</style>
