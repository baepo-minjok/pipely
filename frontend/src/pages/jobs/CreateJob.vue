<script setup>
import {computed, reactive, ref, watch} from 'vue';
import KubernetesInput from '../../components/jobs/KubernetesInput.vue';
import EC2Input from '../../components/jobs/EC2Input.vue';
import {useRoute, useRouter} from 'vue-router';
import {jobApi} from '@/api/JobApi.js';
import {formatSchedule} from '@/utils/formatSchedule.js';

const route = useRoute();
const router = useRouter();
const isLoading = ref(false);
const isScriptGenerating = ref(false);
const isJobCreating = ref(false);

const jenkinsInfo = {
  id: route.query.id,
  name: route.query.jenkinsName,
  uri: route.query.jenkinsUri,
  connected: route.query.connected,
};

const isDiscordChecked = ref(false);
const isSlackChecked = ref(false);
const openDropdown = ref(false);
const isScheduleSelected = ref(false);
const isManualSelected = ref(false);

const weekdays = [
  {label: '월', value: 'mon'},
  {label: '화', value: 'tue'},
  {label: '수', value: 'wed'},
  {label: '목', value: 'thu'},
  {label: '금', value: 'fri'},
  {label: '토', value: 'sat'},
  {label: '일', value: 'sun'},
];

/**
 * 알림 설정 map 변환
 */
const buildNotificationMap = () => {
  const map = {};
  if (isDiscordChecked.value) {
    map.discord = {
      webhookUrl: notificationData.discordUrl,
      checkSuccess: notificationData.isDiscordSuccess,
      checkFailure: notificationData.isDiscordFailure,
    };
  }
  if (isSlackChecked.value) {
    map.slack = {
      webhookUrl: notificationData.slackUrl,
      checkSuccess: notificationData.isSlackSuccess,
      checkFailure: notificationData.isSlackFailure,
    };
  }
  return map;
};

const toggleDay = (day) => {
  const idx = scheduleData.selectedDays.indexOf(day);
  if (idx >= 0) {
    scheduleData.selectedDays.splice(idx, 1);
  } else {
    scheduleData.selectedDays.push(day);
  }
};

const schedulePreview = computed(() => {
  console.log(formatSchedule(scheduleData));
  if (!isScheduleSelected.value) return '스케줄이 비활성화됨';
  const repeatText = scheduleData.repeatType === 'daily' ? '매일' : '매주';
  const daysText =
    scheduleData.repeatType === 'weekly'
      ? scheduleData.selectedDays.map((d) => weekdays.find((w) => w.value === d)?.label).join(', ') || '요일 미선택'
      : '';
  const timeText = scheduleData.time || '시간 미설정';
  return `${repeatText} ${daysText} ${timeText}`;
});

const notificationData = reactive({
  discordUrl: '',
  slackUrl: '',
  isDiscordSuccess: false,
  isSlackSuccess: false,
  isDiscordFailure: false,
  isSlackFailure: false,
})

const scheduleData = reactive({
  repeatType: 'daily',
  selectedDays: [],
  time: 0
});

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

const selectedItem = ref({
  label: '배포 환경 선택',
  value: 'default',
  image: '/src/assets/icons/default.svg'
});

const jobData = reactive({
  scriptId: '',
  name: '',
  description: '',
  trigger: false,
  schedule: '',
  infoId: jenkinsInfo.id,
  notificationMap: {}
});

const scriptData = reactive({
  mode: '',
  manualScript: '',
  scriptId: '',
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
});

const scriptText = ref('');

const toggleDropdown = () => {
  openDropdown.value = !openDropdown.value;
};

const selectItem = (item) => {
  selectedItem.value = item;
  openDropdown.value = false;
  scriptData.isK8sDeploy = item.value === 'k8s';
  scriptData.isEc2Deploy = item.value === 'ec2';
};

const handleCreateScriptClick = async () => {
  isScriptGenerating.value = true;
  try {
    const response = await jobApi.createScript(scriptData);
    console.log(response);
    if (response.status === 200) {
      console.log(response.data);
      const data = response.data.data;
      scriptText.value = data.script;
      scriptData.scriptId = data.scriptId;
      jobData.scriptId = data.scriptId;
    } else {
      alert("생성 실패! 다시 시도해주세요");
    }
  } catch (error) {
    alert("오류가 발생했습니다. 다시 시도해주세요");
  } finally {
    isScriptGenerating.value = false;
  }
};

const handleCreateJobClick = async () => {
  isJobCreating.value = true;
  try {
    /*const validateData = {
      infoId: jenkinsInfo.id,
      script: scriptText.value,
    };
    const isValid = await jobApi.validateScript(validateData);
    if (!isValid) {
      const isOk = confirm("스크립트가 올바르지 않습니다!\n계속 진행하시겠습니까?");
      if (!isOk) {
        return;
      }
    }*/
    await createJob();
  } catch (error) {
    alert("오류가 발생했습니다. 다시 시도해주세요");
  } finally {
    isJobCreating.value = false;
  }
};

const createJob = async () => {
  jobData.schedule = formatSchedule(scheduleData);
  jobData.notificationMap = buildNotificationMap();
  if (isManualSelected.value) {
    scriptData.mode = 'MANUAL';
    scriptData.manualScript = scriptText.value;
  } else {
    scriptData.mode = 'GENERATED';
  }
  const data = {
    job: jobData,
    script: scriptData
  }
  const response = await jobApi.createJob(data);
  if (response.status === 200) {
    alert('Job이 생성되었습니다!');
  } else {
    alert('오류로 인해 생성이 실패했습니다!');
  }
  router.replace({name: 'JobList'});
};

const handleCancelClick = () => {
  const confirmed = confirm('Job 생성을 취소하시겠습니까?\n현재 작성한 모든 내용이 삭제됩니다.');
  if (confirmed) {
    router.back();
  }
};

watch(scriptText, (newVal) => {
  console.log('watch - scriptText changed:', newVal);
});
</script>

<template>
  <div class="container">
    <!-- 스켈레톤 UI -->
    <div v-if="isLoading" class="skeleton-container">
      <div class="skeleton-header">
        <div class="skeleton-title"></div>
      </div>
      <div class="skeleton-content">
        <div v-for="i in 5" :key="i" class="skeleton-section">
          <div class="skeleton-section-title"></div>
          <div class="skeleton-section-content">
            <div v-for="j in 3" :key="j" class="skeleton-input"></div>
          </div>
        </div>
      </div>
      <div class="skeleton-buttons">
        <div class="skeleton-button"></div>
        <div class="skeleton-button"></div>
      </div>
    </div>

    <!-- 실제 컨텐츠 -->
    <div v-else>
      <div class="header">
        <h1>Job 생성</h1>
        <div class="breadcrumb">
          <span class="breadcrumb-item">Jenkins</span>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-item">{{ jenkinsInfo.name }}</span>
          <span class="breadcrumb-separator">></span>
          <span class="breadcrumb-item current">Job 생성</span>
        </div>
      </div>

      <div class="content">
        <!-- 메인 컨텐츠 -->
        <div class="main-content">
          <!-- 서버 정보 -->
          <div class="section jenkins-info">
            <h3 class="section-title">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <rect height="14" rx="2" ry="2" width="20" x="2" y="3"/>
                <line x1="8" x2="16" y1="21" y2="21"/>
                <line x1="12" x2="12" y1="17" y2="21"/>
              </svg>
              서버 정보
            </h3>
            <div class="jenkins-info-card" @click="router.push(`/mypage/cicd/${jenkinsInfo.id}`)">
              <div class="info-item">
                <span class="info-label">이름</span>
                <span class="info-value">{{ jenkinsInfo.name }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">주소</span>
                <span class="info-value">{{ jenkinsInfo.uri }}</span>
              </div>
              <div class="connection_status">
                <div :class="['status_dot', { connected: jenkinsInfo.connected === 'true' }]"></div>
                <span :class="['status_text', { connected: jenkinsInfo.connected === 'true' }]">
                  {{ jenkinsInfo.connected ? '연결됨' : '연결 실패' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Job 기본 정보 -->
          <div class="section">
            <h3 class="section-title">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14,2 14,8 20,8"/>
                <line x1="16" x2="8" y1="13" y2="13"/>
                <line x1="16" x2="8" y1="17" y2="17"/>
                <polyline points="10,9 9,9 8,9"/>
              </svg>
              Job 기본 정보
            </h3>
            <div class="form-group">
              <label class="form-label" for="name">Job 이름</label>
              <input
                id="name"
                v-model="jobData.name"
                class="form-input"
                placeholder="Job 이름을 입력해주세요."
                type="text"
              />
            </div>
            <div class="form-group">
              <label class="form-label" for="description">설명</label>
              <textarea
                id="description"
                v-model="jobData.description"
                class="form-textarea"
                placeholder="Job에 대한 설명을 입력해주세요."
              ></textarea>
            </div>
          </div>

          <!-- 연동 설정 -->
          <div class="section">
            <h3 class="section-title">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
                <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
              </svg>
              트리거 설정
            </h3>
            <div class="checkbox-group">
              <label class="toggle-switch">
                <input id="webhook_check" v-model="jobData.trigger" type="checkbox"/>
                <span class="slider"></span>
                <img alt="icon" class="dropdown-icon" src="/src/assets/icons/github.svg"/>
                <span class="toggle-label">Github</span>
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
                <input id="webhook_check" v-model="isDiscordChecked" type="checkbox"/>
                <span class="slider"></span>
                <span class="toggle-label">Discord</span>
              </label>
              <div v-if="isDiscordChecked" class="nested-input">
                <label class="toggle-switch">
                  <input id="webhook_check" v-model="notificationData.isDiscordSuccess" type="checkbox"/>
                  <span class="slider"></span>
                  <span class="toggle-label">성공시 알림</span>
                </label>
                <label class="toggle-switch">
                  <input id="webhook_check" v-model="notificationData.isDiscordFailure" type="checkbox"/>
                  <span class="slider"></span>
                  <span class="toggle-label">실패시 알림</span>
                </label>
                <input
                  v-model="notificationData.discordUrl"
                  class="form-input"
                  placeholder="Discord Webhook 링크를 입력해주세요."
                  type="text"
                />
              </div>
            </div>
            <div class="checkbox-group">
              <label class="toggle-switch">
                <input id="webhook_check" v-model="isSlackChecked" type="checkbox"/>
                <span class="slider"></span>
                <span class="toggle-label">Slack</span>
              </label>
              <div v-if="isSlackChecked" class="nested-input">
                <label class="toggle-switch">
                  <input id="webhook_check" v-model="notificationData.isSlackSuccess" type="checkbox"/>
                  <span class="slider"></span>
                  <span class="toggle-label">성공시 알림</span>
                </label>
                <label class="toggle-switch">
                  <input id="webhook_check" v-model="notificationData.isSlackFailure" type="checkbox"/>
                  <span class="slider"></span>
                  <span class="toggle-label">실패시 알림</span>
                </label>
                <input
                  v-model="notificationData.slackUrl"
                  class="form-input"
                  placeholder="Slack Webhook 링크를 입력해주세요."
                  type="text"
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
              <label class="toggle-switch">
                <input id="webhook_check" v-model="isScheduleSelected" type="checkbox"/>
                <span class="slider"></span>
                <span class="toggle-label"></span>
              </label>
            </h3>
            <div class="checkbox-group">
              <div v-if="isScheduleSelected" class="schedule-card">
                <!-- 반복 유형 -->
                <div class="form-row">
                  <label class="form-label">반복</label>
                  <select v-model="scheduleData.repeatType" class="form-select">
                    <option value="daily">매일</option>
                    <option value="weekly">매주</option>
                  </select>
                </div>
                <!-- 요일 선택 (매주일 때만 표시) -->
                <div v-if="scheduleData.repeatType === 'weekly'" class="form-row">
                  <label class="form-label">요일</label>
                  <div class="weekday-buttons">
                    <button
                      v-for="day in weekdays"
                      :key="day.value"
                      :class="['weekday-btn', { active: scheduleData.selectedDays.includes(day.value) }]"
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
                  <input v-model="scheduleData.time" class="form-input" type="time"/>
                </div>
                <!-- 미리보기 -->
                <div class="schedule-preview">
                  <span>🕒 현재 설정: {{ schedulePreview }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 스크립트 -->
          <div class="section">
            <h3 class="section-title">
              <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
                <polyline points="16,18 22,12 16,6"/>
                <polyline points="8,6 2,12 8,18"/>
              </svg>
              스크립트

              <label class="toggle-switch">
                <input id="webhook_check" v-model="isManualSelected" type="checkbox"/>
                <span class="slider"></span>
                <span class="toggle-label"></span>
              </label>
            </h3>

            <div v-if="!isManualSelected" class="script-config">
              <div class="form-group">
                <label class="form-label" for="github_url">Github 주소</label>
                <input
                  id="github_url"
                  v-model="scriptData.githubUrl"
                  class="form-input"
                  placeholder="Github 프로젝트 주소를 입력해주세요."
                  type="text"
                />
              </div>
              <div class="form-group">
                <label class="form-label" for="branch">Git Branch</label>
                <input
                  id="branch"
                  v-model="scriptData.branch"
                  class="form-input"
                  type="text"
                />
              </div>
              <div class="stage-selection">
                <label class="form-label">스테이지 선택</label>
                <div class="stage-options">
                  <label class="toggle-switch">
                    <input id="webhook_check" v-model="scriptData.isBuildSelected" type="checkbox"/>
                    <span class="slider"></span>
                    <span class="toggle-label">Build</span>
                  </label>
                  <label class="toggle-switch">
                    <input id="webhook_check" v-model="scriptData.isTestSelected" type="checkbox"/>
                    <span class="slider"></span>
                    <span class="toggle-label">Test</span>
                  </label>
                  <label class="toggle-switch">
                    <input id="webhook_check" v-model="scriptData.isDeploySelected" type="checkbox"/>
                    <span class="slider"></span>
                    <span class="toggle-label">Deploy</span>
                  </label>
                </div>
                <div v-if="scriptData.isDeploySelected" class="deploy-config">
                  <div class="dropdown-container">
                    <button class="custom-dropdown" @click="toggleDropdown">
                      <div class="dropdown-content">
                        <img :src="selectedItem.image" alt="icon" class="dropdown-icon"/>
                        <span>{{ selectedItem.label }}</span>
                      </div>
                      <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"
                           width="16">
                        <polyline points="6,9 12,15 18,9"/>
                      </svg>
                    </button>
                    <div v-if="openDropdown" class="dropdown-menu">
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
                  <KubernetesInput v-if="scriptData.isK8sDeploy" :form="scriptData"/>
                  <EC2Input v-if="scriptData.isEc2Deploy" :form="scriptData"/>
                </div>
              </div>
            </div>
            <div v-else class="script-config">
              <div class="form-group">
                <label class="form-label" for="github_url">Github 주소</label>
                <input
                  id="github_url"
                  v-model="scriptData.githubUrl"
                  class="form-input"
                  placeholder="Github 프로젝트 주소를 입력해주세요."
                  type="text"
                />
              </div>
              <div class="script-editor">
                <label class="form-label">생성된 스크립트</label>
                <textarea
                  id="script"
                  v-model="scriptText"
                  class="script-textarea"
                  placeholder="스크립트를 입력해주세요"
                  spellcheck="false"
                ></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- 도움말 섹션 -->
        <div class="help-section">
          <h3 class="help-title">
            <svg fill="currentColor" height="20" viewBox="0 0 24 24" width="20"
                 xmlns="http://www.w3.org/2000/svg">
              <path
                d="M11,18H13V16H11V18M12,2A10,10 0 0,0 2,12A10,10 0 0,0 12,22A10,10 0 0,0 22,12A10,10 0 0,0 12,2M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20M12,6A4,4 0 0,0 8,10H10A2,2 0 0,1 12,8A2,2 0 0,1 14,10C14,12 11,11.75 11,15H13C13,12.75 16,12.5 16,10A4,4 0 0,0 12,6Z"/>
            </svg>
            도움말
          </h3>
          <div class="help-content">
            <div class="help-item">
              <h4>Jenkins Job이란?</h4>
              <p>Jenkins Job은 빌드, 테스트, 배포 등의 작업을 자동화하는 단위입니다. 소스 코드 변경 시 자동으로 실행되거나 스케줄에 따라 정기적으로 실행할 수 있습니다.</p>
            </div>

            <div class="help-item">
              <h4>스크립트 생성 가이드</h4>
              <ol>
                <li>GitHub 저장소 URL을 정확히 입력하세요</li>
                <li>사용할 브랜치를 선택하세요 (기본: main)</li>
                <li>필요한 스테이지를 선택하세요:
                  <ul>
                    <li><strong>Build:</strong> 소스 코드 컴파일 및 빌드</li>
                    <li><strong>Test:</strong> 단위 테스트 및 통합 테스트</li>
                    <li><strong>Deploy:</strong> 애플리케이션 배포</li>
                  </ul>
                </li>
                <li>배포 환경(Kubernetes/EC2)을 선택하고 설정하세요</li>
              </ol>
            </div>

            <div class="help-item">
              <h4>알림 설정 방법</h4>
              <ul>
                <li><strong>Discord:</strong> 서버 설정 → 연동 → 웹후크에서 URL 복사</li>
                <li><strong>Slack:</strong> 앱 → Incoming Webhooks → 웹후크 URL 생성</li>
                <li>성공/실패 시 알림을 각각 설정할 수 있습니다</li>
              </ul>
            </div>

            <div class="help-item">
              <h4>스케줄 설정</h4>
              <ul>
                <li><strong>매일:</strong> 지정한 시간에 매일 실행</li>
                <li><strong>매주:</strong> 선택한 요일의 지정 시간에 실행</li>
                <li>GitHub 트리거와 함께 사용 가능합니다</li>
              </ul>
            </div>

            <div class="help-item">
              <h4>문제 해결</h4>
              <ul>
                <li>스크립트 생성 실패 시 GitHub URL과 브랜치를 확인하세요</li>
                <li>배포 설정이 올바른지 확인하세요</li>
                <li>Jenkins 서버 연결 상태를 확인하세요</li>
                <li>필수 필드가 모두 입력되었는지 확인하세요</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- 액션 버튼 -->
      <div class="actions">
        <button class="btn btn-secondary" @click="handleCancelClick">
          <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
            <line x1="18" x2="6" y1="6" y2="18"/>
            <line x1="6" x2="18" y1="6" y2="18"/>
          </svg>
          취소
        </button>
        <button
          :disabled="isJobCreating || !jobData.name || !scriptData.githubUrl"
          class="btn btn-primary"
          @click="handleCreateJobClick"
        >
          <svg v-if="isJobCreating" class="animate-spin" fill="none" height="16" stroke="currentColor" stroke-width="2"
               viewBox="0 0 24 24" width="16">
            <path d="M21 12a9 9 0 11-6.219-8.56"/>
          </svg>
          <svg v-else fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
            <polyline points="17,21 17,13 7,13 7,21"/>
            <polyline points="7,3 7,8 15,8"/>
          </svg>
          {{ isJobCreating ? '생성 중...' : 'Job 생성' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
  background: var(--main-color);
}

.toggle-switch input:checked + .slider::before {
  transform: translateX(20px);
}

.toggle-label {
  font-size: 16px;
  color: #1f2937;
  font-weight: 500;
}

.container {
  max-width: 1200px;
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

.skeleton-title {
  height: 36px;
  width: 300px;
  background: #e2e8f0;
  border-radius: 8px;
  margin-bottom: 12px;
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  margin-bottom: 32px;
}

.skeleton-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.skeleton-section-title {
  height: 24px;
  width: 200px;
  background: #e2e8f0;
  border-radius: 6px;
  margin-bottom: 16px;
}

.skeleton-section-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.skeleton-input {
  height: 48px;
  background: #f1f5f9;
  border-radius: 8px;
}

.skeleton-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.skeleton-button {
  height: 44px;
  width: 120px;
  background: #e2e8f0;
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

/* 컨텐츠 */
.content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
  margin-bottom: 32px;
}

.main-content {
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

/* Jenkins 정보 카드 */
.jenkins-info-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 20px;
  position: relative;
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

.connection_status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status_dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
}

.status_text {
  color: #dc2626;
  font-weight: 500;
}

.status_dot.connected {
  background: #10b981;
}

.status_text.connected {
  color: #059669;
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

.form-input::placeholder {
  color: #9ca3af;
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

.form-textarea::placeholder {
  color: #9ca3af;
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

/* 체크박스 */
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

/* 스케줄 컨트롤 */
.schedule-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  margin-top: 10px;
}

.schedule-card .form-label {
  margin-top: 10px;
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
  margin-top: 12px;
  padding: 8px 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

/* 스테이지 선택 */
.stage-selection {
  margin-bottom: 20px;
}

.stage-options {
  display: flex;
  gap: 20px;
  margin-bottom: 16px;
}

.deploy-config {
  margin-left: 26px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

/* 커스텀 드롭다운 */
.dropdown-container {
  position: relative;
  margin-bottom: 16px;
}

.custom-dropdown {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 200px;
  padding: 12px 16px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s ease;
}

.custom-dropdown:hover {
  border-color: #2563eb;
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

/* 스크립트 생성 버튼 */
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

/* 스크립트 에디터 */
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

/* 도움말 섹션 */
.help-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  height: fit-content;
  position: sticky;
  top: 24px;
}

.help-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.help-title svg {
  color: #2563eb;
}

.help-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.help-item h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 8px 0;
}

.help-item p {
  margin: 0 0 8px 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

.help-item ol,
.help-item ul {
  margin: 0;
  padding-left: 20px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
}

.help-item li {
  margin-bottom: 4px;
}

.help-item ul ul {
  margin-top: 4px;
  padding-left: 16px;
}

.help-item strong {
  color: #374151;
  font-weight: 600;
}

/* 액션 버튼 */
.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

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
  min-width: 120px;
  justify-content: center;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
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

.btn-primary {
  background: var(--main-color);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: var(--main-color-hover);
  transform: translateY(-1px);
}

/* 반응형 */
@media (max-width: 1024px) {
  .content {
    grid-template-columns: 1fr;
  }

  .help-section {
    position: static;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .header h1 {
    font-size: 24px;
  }

  .section {
    padding: 20px 16px;
  }

  .stage-options {
    flex-direction: column;
    gap: 12px;
  }

  .actions {
    flex-direction: column-reverse;
  }

  .btn {
    width: 100%;
  }

  .custom-dropdown {
    width: 100%;
  }

  .help-section {
    padding: 20px 16px;
  }
}
</style>