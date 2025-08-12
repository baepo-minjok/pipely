<script setup>
import {computed, onMounted, reactive, ref, toRefs} from 'vue';
import KubernetesInput from '@/components/jobs/KubernetesInput.vue';
import EC2Input from '@/components/jobs/EC2Input.vue';
import {formatSchedule} from '@/utils/formatSchedule.js';
import {jobApi} from "@/api/JobApi.js";
import {useJobStore} from '@/stores/useJobStore';

const emit = defineEmits(['update:jobDetail']);

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

const jobStore = useJobStore();
const scriptText = computed(() => jobStore.scriptText);
const selectedItem = computed(() => jobStore.selectedItem);

const isEditing = ref(false);
const isLoading = ref(false);
const isManualSelected = ref(false);

// jobStore에서 실시간 빌드 상태 가져오기
const currentJobState = computed(() => {
  return jobStore.getJobBuildState(jobDetail.value.pipelineId);
});

// 현재 빌드 상태 계산 수정
const isJobRunning = computed(() => {
  return currentJobState.value.buildState === 'BUILD_RUNNING' ||
    currentJobState.value.buildState === 'BUILD_WAITING' ||
    currentJobState.value.buildState === 'BUILD_STOPPING';
});

// 폼 유효성 검사
const isFormValid = computed(() => {
  if (!isEditing.value) return true;
  return editableData.name.trim() &&
    editableData.scriptText.trim() &&
    (!editableData.notifications.isDiscordChecked || editableData.notifications.discord.webhookUrl.trim()) &&
    (!editableData.notifications.isSlackChecked || editableData.notifications.slack.webhookUrl.trim());
});

// 편집 가능한 데이터
const editableData = reactive({
  name: '',
  description: '',
  githubUrl: '',
  branch: '',
  trigger: false,
  notifications: {
    isDiscordChecked: false,
    isSlackChecked: false,
    discord: {
      webhookUrl: '',
      checkSuccess: false,
      checkFailure: false,
    },
    slack: {
      webhookUrl: '',
      checkSuccess: false,
      checkFailure: false,
    }
  },
  scriptData: {
    mode: '',
    scriptId: '',
    manualScript: '',
    githubUrl: '',
    branch: 'main',
    isBuildSelected: false,
    isTestSelected: false,
    isDeploySelected: false,
    isK8sDeploy: false,
    isEc2Deploy: false,
    tag: '',
    k8sPath: '',
    deploymentName: '',
    namespace: '',
    appName: '',
    containerName: '',
    imageRepo: '',
    port: '',
    replicas: '',
    ec2DeployPath: '',
    sshKeyPath: '',
    sshPort: '',
    deployTarget: '',
  },
  selectedItem: {
    label: '배포 환경 선택',
    value: 'default',
    image: '/src/assets/icons/default.svg'
  },
  scriptText: '',
  schedule: {
    enabled: false,
    repeatType: 'daily',
    selectedDays: [],
    time: '',
  }
});

// 추가 상태 변수들
const openDropdown = ref(false);
const isScriptGenerating = ref(false);

// CICD 아이템들
const cicdItems = [
  {
    label: 'Kubernetes',
    value: 'k8s',
    image: '/src/assets/images/k8s.png',
  },
  {
    label: 'EC2',
    value: 'ec2',
    image: '/src/assets/images/ec2.png',
  },
];

// 드롭다운 토글
const toggleDropdown = () => {
  openDropdown.value = !openDropdown.value;
};

// 아이템 선택
const selectItem = (item) => {
  editableData.selectedItem = item;
  editableData.scriptData.isK8sDeploy = item.value === 'k8s';
  editableData.scriptData.isEc2Deploy = item.value === 'ec2';
  openDropdown.value = false;
};

// 스크립트 생성
const handleCreateScriptClick = async () => {
  isScriptGenerating.value = true;
  try {
    const response = await jobApi.createScript(editableData.scriptData);
    if (response.status === 200) {
      const data = response.data.data;
      editableData.scriptText = data.script;
      editableData.scriptData.scriptId = data.scriptId;
    } else {
      alert("생성 실패! 다시 시도해주세요");
    }
  } catch (error) {
    alert("스크립트 생성 중 오류가 발생했습니다.");
  } finally {
    isScriptGenerating.value = false;
  }
};

// 요일 목록
const weekdays = [
  {label: '월', value: 'mon'},
  {label: '화', value: 'tue'},
  {label: '수', value: 'wed'},
  {label: '목', value: 'thu'},
  {label: '금', value: 'fri'},
  {label: '토', value: 'sat'},
  {label: '일', value: 'sun'},
];
console.log(editableData.scriptData);
const hasDeploy = computed(() => {
  const dto = jobDetail.value.lightScriptDto;
  return dto?.isK8sDeploy || dto?.isEc2Deploy;
});

// 스케줄 파싱 함수
const parseSchedule = (scheduleString) => {
  if (!scheduleString) return {enabled: false, repeatType: 'daily', selectedDays: [], time: ''};
  const schedule = {enabled: true, repeatType: 'daily', selectedDays: [], time: ''};
  if (scheduleString.includes('매주')) {
    schedule.repeatType = 'weekly';
    const dayMatch = scheduleString.match(/[월화수목금토일]/g);
    if (dayMatch) {
      const dayMap = {'월': 'mon', '화': 'tue', '수': 'wed', '목': 'thu', '금': 'fri', '토': 'sat', '일': 'sun'};
      schedule.selectedDays = dayMatch.map(day => dayMap[day]).filter(Boolean);
    }
  }
  const timeMatch = scheduleString.match(/(\d{1,2})[:시]\s*(\d{1,2})?/);
  if (timeMatch) {
    const hour = parseInt(timeMatch[1]);
    const minute = timeMatch[2] || '00';
    const isAfternoon = scheduleString.includes('오후');
    const adjustedHour = isAfternoon && hour !== 12 ? hour + 12 : (hour === 12 && !isAfternoon ? 0 : hour);
    schedule.time = `${adjustedHour.toString().padStart(2, '0')}:${minute.padStart(2, '0')}`;
  }
  return schedule;
};

// 스케줄 포맷팅 함수
const formatScheduleDisplay = (schedule) => {
  if (!schedule.enabled) return '스케줄이 비활성화됨';
  const repeatText = schedule.repeatType === 'daily' ? '매일' : '매주';
  const daysText = schedule.repeatType === 'weekly'
    ? schedule.selectedDays.map(d => weekdays.find(w => w.value === d)?.label).join(', ') || '요일 미선택'
    : '';
  let timeText = '시간 미설정';
  if (schedule.time) {
    const [hour, minute] = schedule.time.split(':');
    const hourNum = parseInt(hour);
    const period = hourNum >= 12 ? '오후' : '오전';
    const displayHour = hourNum > 12 ? hourNum - 12 : (hourNum === 0 ? 12 : hourNum);
    timeText = `${period} ${displayHour}시 ${minute}분`;
  }
  return `${repeatText} ${daysText} ${timeText}`.trim();
};

// 스케줄 미리보기
const schedulePreview = computed(() => {
  return formatScheduleDisplay(editableData.schedule);
});

// 요일 토글
const toggleDay = (day) => {
  const idx = editableData.schedule.selectedDays.indexOf(day);
  if (idx >= 0) {
    editableData.schedule.selectedDays.splice(idx, 1);
  } else {
    editableData.schedule.selectedDays.push(day);
  }
};

// 변수 초기화
const init = () => {
  editableData.name = jobDetail.value.name || '';
  editableData.description = jobDetail.value.description || '';
  editableData.trigger = jobDetail.value.trigger || false;
  editableData.notifications.isDiscordChecked = !!jobDetail.value.notificationList?.discord;
  editableData.notifications.isSlackChecked = !!jobDetail.value.notificationList?.slack;

  if (jobDetail.value.notificationList?.discord) {
    editableData.notifications.discord = {...jobDetail.value.notificationList.discord};
  }
  if (jobDetail.value.notificationList?.slack) {
    editableData.notifications.slack = {...jobDetail.value.notificationList.slack};
  }

  if (jobDetail.value.lightScriptDto) {
    console.log(jobDetail.value.lightScriptDto);
    Object.assign(editableData.scriptData, jobDetail.value.lightScriptDto);
    editableData.scriptData.isDeploySelected = jobDetail.value.lightScriptDto.isK8sDeploy || jobDetail.value.lightScriptDto.isEc2Deploy;
    isManualSelected.value = editableData.scriptData.mode === 'MANUAL';
  }

  editableData.scriptText = scriptText.value || '';
  editableData.schedule = parseSchedule(jobDetail.value.schedule);


  if (selectedItem.value) {
    editableData.selectedItem = selectedItem.value;
  }
};

// 편집 모드 시작
const startEditing = () => {
  isEditing.value = true;
};

// 편집 취소
const cancelEditing = () => {
  isEditing.value = false;
  init(); // 편집 데이터 초기화
};

// 저장 (진행률 표시 포함)
const saveWithProgress = async () => {
  isLoading.value = true;
  const jobData = {
    pipelineId: jobDetail.value.pipelineId,
    scriptId: editableData.scriptData.scriptId,
    name: editableData.name,
    description: editableData.description,
    trigger: editableData.trigger,
    notificationMap: buildNotificationMap(),
    schedule: editableData.schedule.enabled ? formatSchedule(editableData.schedule) : ''
  };

  const data = {
    job: jobData,
    script: editableData.scriptData,
  }

  try {
    const response = await jobApi.updateJob(data);
    Object.assign(jobDetail.value, {...jobDetail.value, ...jobData});
    await jobStore.fetchJobDetail(jobDetail.value.pipelineId);
    init();
    isEditing.value = false;
    alert('설정이 성공적으로 저장되었습니다.');
  } catch (error) {
    alert('저장 중 오류가 발생했습니다. 다시 시도해주세요.');
    console.error('Save error:', error);
  } finally {
    isLoading.value = false;
  }
};

const toggleManual = () => {
  if (isManualSelected.value) {
    editableData.scriptData.mode = 'GENERATED';
  } else {
    editableData.scriptData.mode = 'MANUAL';
  }
  console.log(isManualSelected.value);
}

const buildNotificationMap = () => {
  const map = {};
  if (editableData.notifications.isDiscordChecked) {
    map.discord = {
      webhookUrl: editableData.notifications.discord.webhookUrl,
      checkSuccess: editableData.notifications.discord.checkSuccess,
      checkFailure: editableData.notifications.discord.checkFailure,
    };
  }
  if (editableData.notifications.isSlackChecked) {
    map.slack = {
      webhookUrl: editableData.notifications.slack.webhookUrl,
      checkSuccess: editableData.notifications.slack.checkSuccess,
      checkFailure: editableData.notifications.slack.checkFailure,
    };
  }
  console.log(map);
  return map;
};

onMounted(() => {
  init();
});
</script>

<template>
  <div class="container">
    <!-- 실행 상태 배너 (Job이 실행 중일 때만 표시) -->
    <div v-if="isJobRunning" class="running-banner">
      <div class="banner-content">
        <div class="banner-icon">
          <svg v-if="currentJobState.buildState === 'BUILD_RUNNING'" class="animate-spin" fill="none" height="20"
               stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <path d="M21 12a9 9 0 11-6.219-8.56"/>
          </svg>
          <svg v-else-if="currentJobState.buildState === 'BUILD_WAITING'" class="animate-waiting" fill="none"
               height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12,6 12,12 16,14"/>
          </svg>
          <svg v-else-if="currentJobState.buildState === 'BUILD_STOPPING'" class="animate-pulse" fill="none" height="20"
               stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <circle cx="12" cy="12" r="10"/>
            <rect height="6" rx="1" ry="1" width="6" x="9" y="9"/>
          </svg>
        </div>
        <div class="banner-text">
          <div class="banner-title">
            {{
              currentJobState.buildState === 'BUILD_RUNNING' ? 'Job 실행 중' :
                currentJobState.buildState === 'BUILD_WAITING' ? 'Job 대기 중' :
                  currentJobState.buildState === 'BUILD_STOPPING' ? 'Job 중단 중' : 'Job 실행 중'
            }}
          </div>
          <div class="banner-subtitle">
            {{
              currentJobState.buildState === 'BUILD_WAITING' ? '빌드 시작을 기다리고 있습니다. 편집이 제한됩니다.' :
                '현재 이 Job이 실행 중입니다. 편집이 제한될 수 있습니다.'
            }}
          </div>
        </div>
      </div>
    </div>

    <!-- Job 기본 정보 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14,2 14,8 20,8"/>
            <line x1="16" x2="8" y1="13" y2="13"/>
            <line x1="16" x2="8" y1="17" y2="17"/>
          </svg>
          Job 기본 정보
        </h3>
      </div>
      <div class="form-grid">
        <div class="form-group">
          <label class="form-label">
            Job 이름
            <span class="required">*</span>
          </label>
          <div class="input-wrapper">
            <input
              v-if="isEditing"
              v-model="editableData.name"
              :class="['form-input', { 'error': !editableData.name.trim() }]"
              placeholder="Job 이름을 입력해주세요"
            />
            <input
              v-else
              :value="jobDetail.name"
              class="form-input readonly"
              readonly
            />
            <div v-if="!isEditing && jobDetail.name" class="input-icon">
              <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                <path d="M9 12l2 2 4-4"/>
                <circle cx="12" cy="12" r="10"/>
              </svg>
            </div>
          </div>
        </div>
        <div class="form-group full-width">
          <label class="form-label">설명</label>
          <div class="input-wrapper">
            <textarea
              v-if="isEditing"
              v-model="editableData.description"
              class="form-textarea"
              placeholder="Job에 대한 설명을 입력해주세요"
              rows="3"
            />
            <textarea
              v-else
              :value="jobDetail.description || '설명이 없습니다.'"
              class="form-textarea readonly"
              readonly
              rows="3"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 트리거 설정 -->
    <div class="section">
      <div class="section-header">
        <h3 class="section-title">
          <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
          </svg>
          트리거 설정
        </h3>
        <div class="section-badge">
          <div :class="['status-dot', { 'active': jobDetail.trigger }]"></div>
          {{ jobDetail.trigger ? '활성화' : '비활성화' }}
        </div>
      </div>
      <div class="toggle-group">
        <label class="enhanced-toggle">
          <input
            v-if="isEditing"
            v-model="editableData.trigger"
            type="checkbox"
          />
          <input
            v-else
            :checked="jobDetail.trigger"
            disabled
            type="checkbox"
          />
          <span class="toggle-slider"></span>
          <div class="toggle-content">
            <img alt="GitHub" class="toggle-icon" src="/src/assets/icons/github.svg"/>
            <div class="toggle-info">
              <div class="toggle-title">Github Webhook</div>
              <div class="toggle-description">코드 푸시 시 자동으로 빌드를 실행합니다</div>
            </div>
          </div>
        </label>
      </div>
    </div>

    <!-- 알림 설정 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
             stroke-width="2" viewBox="0 0 24 24" width="20" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
        </svg>
        알림 설정
      </h3>
      <div class="checkbox-group">
        <label class="toggle-switch">
          <input
            v-if="isEditing"
            v-model="editableData.notifications.isDiscordChecked"
            type="checkbox"
          />
          <input
            v-else
            :checked="editableData.notifications.isDiscordChecked"
            disabled
            type="checkbox"
          />
          <span class="slider"></span>
          <span class="toggle-label">Discord</span>
        </label>
        <div
          v-if="(isEditing && editableData.notifications.isDiscordChecked) || (!isEditing && editableData.notifications.isDiscordChecked)"
          class="nested-input">
          <label class="toggle-switch">
            <input
              v-if="isEditing"
              v-model="editableData.notifications.discord.checkSuccess"
              type="checkbox"
            />
            <input
              v-else
              :checked="editableData.notifications.discord.checkSuccess"
              disabled
              type="checkbox"
            />
            <span class="slider"></span>
            <span class="toggle-label">성공시 알림</span>
          </label>
          <label class="toggle-switch">
            <input
              v-if="isEditing"
              v-model="editableData.notifications.discord.checkFailure"
              type="checkbox"
            />
            <input
              v-else
              :checked="editableData.notifications.discord.checkFailure"
              disabled
              type="checkbox"
            />
            <span class="slider"></span>
            <span class="toggle-label">실패시 알림</span>
          </label>
          <input
            v-if="isEditing"
            v-model="editableData.notifications.discord.webhookUrl"
            class="form-input"
            placeholder="Discord Webhook 링크를 입력해주세요."
            type="text"
          />
          <input
            v-else
            :value="editableData.notifications.discord.webhookUrl"
            class="form-input"
            disabled
            placeholder="Discord Webhook URL이 설정되지 않았습니다"
          />
        </div>
      </div>
      <div class="checkbox-group">
        <label class="toggle-switch">
          <input
            v-if="isEditing"
            v-model="editableData.notifications.isSlackChecked"
            type="checkbox"
          />
          <input
            v-else
            :checked="editableData.notifications.isSlackChecked"
            disabled
            type="checkbox"
          />
          <span class="slider"></span>
          <span class="toggle-label">Slack</span>
        </label>
        <div
          v-if="(isEditing && editableData.notifications.isSlackChecked) || (!isEditing && editableData.notifications.isSlackChecked)"
          class="nested-input">
          <label class="toggle-switch">
            <input
              v-if="isEditing"
              v-model="editableData.notifications.slack.checkSuccess"
              type="checkbox"
            />
            <input
              v-else
              :checked="editableData.notifications.slack.checkSuccess"
              disabled
              type="checkbox"
            />
            <span class="slider"></span>
            <span class="toggle-label">성공시 알림</span>
          </label>
          <label class="toggle-switch">
            <input
              v-if="isEditing"
              v-model="editableData.notifications.slack.checkFailure"
              type="checkbox"
            />
            <input
              v-else
              :checked="editableData.notifications.slack.checkFailure"
              disabled
              type="checkbox"
            />
            <span class="slider"></span>
            <span class="toggle-label">실패시 알림</span>
          </label>
          <input
            v-if="isEditing"
            v-model="editableData.notifications.slack.webhookUrl"
            class="form-input"
            placeholder="Slack Webhook 링크를 입력해주세요."
            type="text"
          />
          <input
            v-else
            :value="editableData.notifications.slack.webhookUrl"
            class="form-input"
            disabled
            placeholder="Slack Webhook URL이 설정되지 않았습니다"
          />
        </div>
      </div>
    </div>

    <!-- 스케줄 설정 -->
    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12,6 12,12 16,14"/>
        </svg>
        스케줄 설정
        <label v-if="isEditing" class="toggle-switch">
          <input v-model="editableData.schedule.enabled" type="checkbox"/>
          <span class="slider"></span>
        </label>
      </h3>
      <div v-if="!isEditing" class="form-group">
        <label class="form-label">현재 스케줄</label>
        <div class="schedule-display">
          <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12,6 12,12 16,14"/>
          </svg>
          <span>{{ jobDetail.schedule || '스케줄이 설정되지 않았습니다' }}</span>
        </div>
      </div>
      <div v-else-if="editableData.schedule.enabled" class="schedule-config">
        <!-- 반복 유형 -->
        <div class="form-row">
          <label class="form-label">반복</label>
          <select v-model="editableData.schedule.repeatType" class="form-select">
            <option value="daily">매일</option>
            <option value="weekly">매주</option>
          </select>
        </div>
        <!-- 요일 선택 (매주일 때만 표시) -->
        <div v-if="editableData.schedule.repeatType === 'weekly'" class="form-row">
          <label class="form-label">요일</label>
          <div class="weekday-buttons">
            <button
              v-for="day in weekdays"
              :key="day.value"
              :class="['weekday-btn', { active: editableData.schedule.selectedDays.includes(day.value) }]"
              type="button"
              @click="toggleDay(day.value)"
            >
              {{ day.label }}
            </button>
          </div>
        </div>
        <!-- 시간 선택 -->
        <div class="form-row">
          <label class="form-label">시간</label>
          <input v-model="editableData.schedule.time" class="form-input" type="time"/>
        </div>
        <!-- 미리보기 -->
        <div class="schedule-preview">
          <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12,6 12,12 16,14"/>
          </svg>
          <span>현재 설정: {{ schedulePreview }}</span>
        </div>
      </div>
      <div v-else-if="isEditing" class="schedule-disabled">
        <p class="disabled-text">스케줄이 비활성화되어 있습니다. 위의 토글을 활성화하여 스케줄을 설정하세요.</p>
      </div>
    </div>

    <div class="section">
      <h3 class="section-title">
        <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
          <polyline points="16,18 22,12 16,6"/>
          <polyline points="8,6 2,12 8,18"/>
        </svg>
        스크립트
        <label class="toggle-switch">
          <input id="webhook_check" v-model="isManualSelected" :disabled="!isEditing" type="checkbox"
                 @click="toggleManual"/>
          <span class="slider"></span>
          <span class="toggle-label"></span>
        </label>
      </h3>
      <div class="script-config">
        <div class="form-group">
          <label class="form-label" for="github_url">Github 주소</label>
          <input
            v-if="isEditing"
            id="github_url"
            v-model="editableData.scriptData.githubUrl"
            class="form-input"
            placeholder="Github 프로젝트 주소를 입력해주세요."
            type="text"
          />
          <input
            v-else
            :value="jobDetail.lightScriptDto?.githubUrl"
            class="form-input"
            disabled
            placeholder="Github URL이 설정되지 않았습니다"
          />
        </div>
        <div v-if="!isManualSelected" class="form-group">
          <label class="form-label" for="branch">Git Branch</label>
          <input
            v-if="isEditing"
            id="branch"
            v-model="editableData.scriptData.branch"
            class="form-input"
            type="text"
          />
          <input
            v-else
            :value="jobDetail.lightScriptDto?.branch"
            class="form-input"
            disabled
          />
        </div>
        <div v-if="!isManualSelected" class="stage-selection">
          <label class="form-label">스테이지 선택</label>
          <div class="stage-options">
            <label class="toggle-switch">
              <input
                v-if="isEditing"
                v-model="editableData.scriptData.isBuildSelected"
                type="checkbox"
              />
              <input
                v-else
                :checked="editableData.scriptData.isBuildSelected"
                disabled
                type="checkbox"
              />
              <span class="slider"></span>
              <span class="toggle-label">Build</span>
            </label>
            <label class="toggle-switch">
              <input
                v-if="isEditing"
                v-model="editableData.scriptData.isTestSelected"
                type="checkbox"
              />
              <input
                v-else
                :checked="editableData.scriptData.isTestSelected"
                disabled
                type="checkbox"
              />
              <span class="slider"></span>
              <span class="toggle-label">Test</span>
            </label>
            <label class="toggle-switch">
              <input
                v-if="isEditing"
                v-model="editableData.scriptData.isDeploySelected"
                type="checkbox"
              />
              <input
                v-else
                :checked="hasDeploy"
                disabled
                type="checkbox"
              />
              <span class="slider"></span>
              <span class="toggle-label">Deploy</span>
            </label>
          </div>
          <div v-if="(isEditing && editableData.scriptData.isDeploySelected) || (!isEditing && hasDeploy)"
               class="deploy-config">
            <div class="dropdown-container">
              <button v-if="isEditing" class="custom-dropdown" @click="toggleDropdown">
                <div class="dropdown-content">
                  <img :src="editableData.selectedItem.image" alt="icon" class="dropdown-icon"/>
                  <span>{{ editableData.selectedItem.label }}</span>
                </div>
                <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
                  <polyline points="6,9 12,15 18,9"/>
                </svg>
              </button>
              <div v-else class="custom-dropdown disabled">
                <div class="dropdown-content">
                  <img v-if="selectedItem?.image" :src="selectedItem?.image" alt="icon" class="dropdown-icon"/>
                  <span>{{ selectedItem?.label || '배포 환경' }}</span>
                </div>
              </div>
              <div v-if="isEditing && openDropdown" class="dropdown-menu">
                <div
                  v-for="(item, index) in cicdItems"
                  :key="index"
                  class="dropdown-item"
                  @click="selectItem(item)"
                >
                  <img :src="item.image" alt="icon" class="dropdown-icon"/>
                  <span>{{ item.label }}</span>
                </div>
              </div>
            </div>
            <KubernetesInput
              v-if="(isEditing && editableData.scriptData.isK8sDeploy) || (!isEditing && jobDetail.lightScriptDto?.isK8sDeploy)"
              :form="isEditing ? editableData.scriptData : jobDetail.lightScriptDto"
              :readonly="!isEditing"
            />
            <EC2Input
              v-if="(isEditing && editableData.scriptData.isEc2Deploy) || (!isEditing && jobDetail.lightScriptDto?.isEc2Deploy)"
              :form="isEditing ? editableData.scriptData : jobDetail.lightScriptDto"
              :readonly="!isEditing"
            />
          </div>
        </div>
        <div v-if="isEditing && isManualSelected" class="script-editor">
          <label class="form-label">스크립트</label>
          <textarea
            id="script"
            v-model="editableData.scriptData.manualScript"
            class="script-textarea"
            placeholder="스크립트가 여기에 생성됩니다..."
            spellcheck="false"
          ></textarea>
        </div>
        <div
          v-else-if="!isEditing" class="script-editor">
          <label class="form-label">스크립트</label>
          <textarea
            :value="scriptText"
            class="script-textarea"
            disabled
            placeholder="스크립트가 생성되지 않았습니다..."
            spellcheck="false"
          ></textarea>
        </div>
      </div>
    </div>

    <!-- 편집 컨트롤 -->
    <div class="edit-controls">
      <div class="edit-actions">
        <button
          v-if="!isEditing"
          :disabled="isJobRunning"
          class="btn btn-secondary"
          @click="startEditing"
        >
          <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
          </svg>
          편집
        </button>
        <template v-else>
          <button
            :disabled="isLoading || !isFormValid"
            class="btn btn-primary"
            @click="saveWithProgress"
          >
            <svg v-if="isLoading" class="animate-spin" fill="none" height="16" stroke="currentColor" stroke-width="2"
                 viewBox="0 0 24 24" width="16">
              <path d="M21 12a9 9 0 11-6.219-8.56"/>
            </svg>
            <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <polyline points="20,6 9,17 4,12"/>
            </svg>
            {{ isLoading ? '저장 중...' : '저장' }}
          </button>
          <button
            :disabled="isLoading"
            class="btn btn-secondary"
            @click="cancelEditing"
          >
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <line x1="18" x2="6" y1="6" y2="18"/>
              <line x1="6" x2="18" y1="6" y2="18"/>
            </svg>
            취소
          </button>
        </template>
      </div>
      <!-- 유효성 검사 메시지 -->
      <div v-if="isEditing && !isFormValid" class="validation-message">
        <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
          <circle cx="12" cy="12" r="10"/>
          <line x1="12" x2="12" y1="8" y2="12"/>
          <line x1="12" x2="12.01" y1="16" y2="16"/>
        </svg>
        필수 항목을 모두 입력해주세요.
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

.edit-controls {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

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
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #1d4ed8;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
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

.animate-waiting {
  animation: animate-waiting 2s ease-in-out infinite;
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

.toggle-switch {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.toggle-switch input {
  display: none;
}

.slider {
  position: relative;
  width: 40px;
  height: 20px;
  background: #d1d5db;
  border-radius: 20px;
  transition: background 0.3s;
}

.slider::before {
  content: "";
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.toggle-switch input:checked + .slider {
  background: #2563eb;
}

.toggle-switch input:checked + .slider::before {
  transform: translateX(20px);
}

.form-group {
  margin-bottom: 20px;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
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

.form-input:disabled {
  background: #f9fafb;
  color: #6b7280;
  cursor: not-allowed;
}

.form-textarea {
  width: 100%;
  min-height: 100px;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  background: white;
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

.form-textarea:disabled {
  background: #f9fafb;
  color: #6b7280;
  cursor: not-allowed;
}

.form-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  color: #1f2937;
  cursor: pointer;
  transition: all 0.2s ease;
}

.form-select:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.integration-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.integration-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  color: #374151;
}

.integration-header svg {
  color: #6b7280;
}

.integration-label {
  font-size: 14px;
}

.schedule-display {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  color: #374151;
}

.schedule-display svg {
  color: #2563eb;
}

.schedule-config {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 10px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.weekday-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.weekday-btn {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.weekday-btn:hover {
  background: #f1f5f9;
}

.weekday-btn.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.schedule-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.schedule-preview svg {
  color: #2563eb;
}

.schedule-disabled {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 10px;
}

.disabled-text {
  color: #6b7280;
  font-style: italic;
  margin: 0;
}

.stage-options {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.stage-checkbox {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: default;
  font-size: 16px;
  color: #374151;
}

.stage-checkbox input {
  display: none;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #d1d5db;
  border-radius: 4px;
  position: relative;
  background: #f9fafb;
}

.stage-checkbox input:checked + .checkmark {
  background: #2563eb;
  border-color: #2563eb;
}

.stage-checkbox input:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 6px;
  top: 2px;
  width: 6px;
  height: 10px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.stage-label {
  font-weight: 500;
}

.deploy-config {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.deploy-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 12px;
}

.deploy-header svg {
  color: #6b7280;
}

.dropdown-container {
  margin-bottom: 16px;
  position: relative;
}

.custom-dropdown {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 200px;
  padding: 12px 16px;
  background: #f9fafb;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #6b7280;
}

.custom-dropdown.disabled {
  cursor: not-allowed;
}

.dropdown-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dropdown-icon {
  width: 20px;
  height: 20px;
}

.script-editor {
  margin-top: 20px;
}

.script-textarea {
  width: 100%;
  min-height: 300px;
  padding: 20px;
  background: #1e293b;
  color: #e2e8f0;
  border: 1px solid #334155;
  border-radius: 8px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  box-sizing: border-box;
}

.script-textarea:focus {
  outline: none;
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.script-textarea::placeholder {
  color: #64748b;
}

@media (max-width: 768px) {
  .container {
    gap: 16px;
  }

  .section {
    padding: 20px 16px;
  }

  .stage-options {
    flex-direction: column;
    gap: 12px;
  }

  .custom-dropdown {
    width: 100%;
  }

  .edit-actions {
    flex-direction: column;
    width: 100%;
  }

  .btn {
    width: 100%;
    justify-content: center;
  }
}

.checkbox-group {
  margin-bottom: 20px;
}

.nested-input {
  margin-left: 26px;
  margin-bottom: 12px;
  margin-top: 10px;
}

.nested-input .toggle-switch {
  margin-bottom: 12px;
}

.dropdown-icon {
  width: 20px;
  height: 20px;
}

.script-config {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.generate-script-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--main-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 20px;
}

.generate-script-btn:hover:not(:disabled) {
  background: var(--main-color-hover);
  transform: translateY(-1px);
}

.generate-script-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  z-index: 10;
  margin-top: 4px;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.dropdown-item:hover {
  background: #f1f5f9;
}

.dropdown-item:first-child {
  border-radius: 8px 8px 0 0;
}

.dropdown-item:last-child {
  border-radius: 0 0 8px 8px;
}

.running-banner {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
}

.running-banner.waiting {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.running-banner.stopping {
  background: linear-gradient(135deg, #ea580c, #c2410c);
  box-shadow: 0 4px 12px rgba(234, 88, 12, 0.3);
}

.banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.banner-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.banner-text {
  flex: 1;
}

.banner-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 4px;
}

.banner-subtitle {
  font-size: 14px;
  opacity: 0.9;
}

/* 섹션 헤더 개선 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #f1f5f9;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 500;
  color: #64748b;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  transition: background 0.2s ease;
}

.status-dot.active {
  background: #10b981;
}

/* 폼 그리드 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

/* 향상된 입력 필드 */
.input-wrapper {
  position: relative;
}

.form-input.readonly,
.form-textarea.readonly {
  background: #f8fafc;
  border-color: #e2e8f0;
  color: #64748b;
  cursor: default;
}

.form-input.error {
  border-color: #ef4444;
  background: #fef2f2;
}

.input-icon {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #10b981;
}

.required {
  color: #ef4444;
  margin-left: 4px;
}

/* 향상된 토글 */
.enhanced-toggle {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.enhanced-toggle:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.enhanced-toggle input {
  display: none;
}

.toggle-slider {
  position: relative;
  width: 48px;
  height: 24px;
  background: #cbd5e1;
  border-radius: 24px;
  transition: background 0.3s ease;
  flex-shrink: 0;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  transition: transform 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.enhanced-toggle input:checked + .toggle-slider {
  background: #2563eb;
}

.enhanced-toggle input:checked + .toggle-slider::before {
  transform: translateX(24px);
}

.toggle-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.toggle-icon {
  width: 24px;
  height: 24px;
  flex-shrink: 0;
}

.toggle-info {
  flex: 1;
}

.toggle-title {
  font-size: 16px;
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 4px;
}

.toggle-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.4;
}

/* 유효성 검사 메시지 */
.validation-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fef2f2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #dc2626;
  font-size: 14px;
  margin-top: 16px;
}

/* 향상된 버튼 스타일 */
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.btn.btn-primary:disabled {
  background: #9ca3af;
}

/* 반응형 개선 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .section-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .enhanced-toggle {
    padding: 16px;
  }

  .toggle-content {
    gap: 8px;
  }

  .banner-content {
    gap: 12px;
  }

  .banner-icon {
    width: 32px;
    height: 32px;
  }
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
</style>