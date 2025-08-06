import {defineStore} from 'pinia';
import {computed, ref} from 'vue';
import {jobApi} from '@/api/JobApi';
import {connectWebSocket, disconnectWebSocket} from "@/websocket/index.js";

export const useJobStore = defineStore('job', () => {
  // 1. Job 개별 기본 상태
  const initialBuildProgress = () => ({
    status: '',
    stages: [],
    log: '',
    progress: 0,
    currentStage: '',
    isBuilding: false
  });

  const initialJobDetail = () => ({
    pipelineId: '',
    name: '',
    description: '',
    schedule: '',
    lastExe: '',
    stageList: [],
    notificationList: {},
    pipelineVersionList: [],
    lightScriptDto: {
      scriptId: '',
      githubUrl: '',
      branch: '',
      isBuildSelected: false,
      isTestSelected: false,
      isK8sDeploy: false,
      isEc2Deploy: false,
      tag: '',
      sshKeyPath: '',
      sshPort: '',
      deployTarget: '',
      k8sPath: '',
      deploymentName: '',
      namespace: '',
      appName: '',
      containerName: '',
      imageRepo: '',
      port: '',
      replicas: '',
      script: '',
    },
    buildProgress: initialBuildProgress(),
    buildState: '',
  });

  // 2. Store 상태 정의
  const jobDetails = ref({});
  const selectedItem = ref(null);
  const scriptText = ref('');
  const jobList = ref([]);
  const jenkinsInfo = ref([]);
  const cicdItems = [
    {label: 'Kubernetes', value: 'k8s', image: '/src/assets/images/k8s.png'},
    {label: 'EC2', value: 'ec2', image: '/src/assets/images/ec2.png'},
  ];

  const id = ref('');
  const isBuilding = ref(false);
  const alertMessage = ref([]);
  const buildQueue = ref([]);
  let buildQueueTimer = null;

  // 3. Computed - 현재 선택된 job의 빌드 상태
  const currentJobDetail = computed(() => {
    return id.value ? jobDetails.value[id.value] : null;
  });

  const buildProgress = computed(() => {
    return currentJobDetail.value?.buildProgress || initialBuildProgress();
  });

  const buildNumber = ref(null);

  // 4. 빌드 상태 동기화 함수
  const syncBuildState = (jobId, buildData) => {
    // jobDetails 업데이트
    if (jobDetails.value[jobId]) {
      if (!jobDetails.value[jobId].buildProgress) {
        jobDetails.value[jobId].buildProgress = initialBuildProgress();
      }
      Object.assign(jobDetails.value[jobId].buildProgress, {
        status: buildData.status,
        stages: buildData.stages || [],
        log: buildData.log || '',
        progress: buildData.progress || 0,
        currentStage: buildData.currentStage || '',
        isBuilding: buildData.status === 'BUILD_RUNNING' || buildData.status === 'BUILD_WAITING'
      });
      jobDetails.value[jobId].buildState = buildData.status;
    }

    // jobList 업데이트
    const jobInList = jobList.value.find(j => j.pipelineId === jobId);
    if (jobInList) {
      jobInList.buildState = buildData.status;
      jobInList.progress = buildData.progress || 0;
      if (!jobInList.buildProgress) {
        jobInList.buildProgress = initialBuildProgress();
      }
      Object.assign(jobInList.buildProgress, {
        status: buildData.status,
        stages: buildData.stages || [],
        log: buildData.log || '',
        progress: buildData.progress || 0,
        currentStage: buildData.currentStage || '',
        isBuilding: buildData.status === 'BUILD_RUNNING' || buildData.status === 'BUILD_WAITING'
      });
    }
  };

  // 5. 함수 정의
  const resetJobDetail = () => {
    jobDetails.value = {};
    selectedItem.value = null;
    scriptText.value = '';
  };

  const fetchJobDetail = async (jobId) => {
    if (!jobId) return;

    try {
      const response = await jobApi.getJobDetail(jobId);
      console.log("fetchJobDetail", response);
      const data = response.data;
      if (data.success && data.data) {
        console.log("fetch...");
        const detail = {
          ...initialJobDetail(),
          ...data.data,
          lightScriptDto: {
            ...initialJobDetail().lightScriptDto,
            ...data.data.lightScriptDto,
          },
        };

        if (!detail.buildProgress) detail.buildProgress = initialBuildProgress();

        // 기존 빌드 상태가 있다면 유지
        const existingJob = jobDetails.value[jobId];
        if (existingJob && existingJob.buildProgress) {
          detail.buildProgress = existingJob.buildProgress;
          detail.buildState = existingJob.buildState;
        }
        scriptText.value = detail.lightScriptDto.script;

        jobDetails.value[jobId] = detail;
        id.value = jobId;
        console.log(detail);
        return detail;
      }
    } catch (e) {
      console.error('jobDetail fetch error:', e);
    }
  };

  async function fetchJobList(infoId) {
    const response = await jobApi.fetchJobList(infoId);

    jobList.value = response.map(job => {
      // 기존 빌드 상태가 있다면 유지
      const existingJob = jobDetails.value[job.pipelineId];
      const buildState = existingJob?.buildState || job.buildState || '';
      const buildProgress = existingJob?.buildProgress || initialBuildProgress();

      const jobData = {
        ...job,
        buildState,
        progress: buildProgress.progress || 0,
        buildProgress: {...buildProgress}
      };

      // jobDetails에도 동기화
      if (!jobDetails.value[job.pipelineId]) {
        jobDetails.value[job.pipelineId] = {
          ...initialJobDetail(),
          ...job,
          buildState,
          buildProgress: {...buildProgress},
        };
      }

      return jobData;
    });

  }

  function connect() {
    connectWebSocket((msg) => {
      const data = JSON.parse(msg);

      if (data.id) {
        syncBuildState(data.id, {
          status: data.status,
          stages: data.stages || [],
          log: data.log || '',
          progress: data.progress || 0,
          currentStage: data.currentStage || ''
        });

      }

      // 빌드 종료 감지
      if (data.id && data.status !== 'BUILD_RUNNING' && data.status !== 'BUILD_WAITING') {
        onBuildFinished(data.id);
      }
    }, (msg) => {
      const data = JSON.parse(msg);
      alertMessage.value.push(data);
    });
  }

  function disconnect() {
    disconnectWebSocket();
  }

  // 빌드 시작 함수
  async function getBuildInfo(jobId, run = true) {

    if (isBuilding.value) {
      if (!run) {
        return;
      }

      // 이미 다른 빌드가 실행 중이면 큐에 추가
      if (!buildQueue.value.some(j => j.jobId === jobId)) {
        buildQueue.value.push({jobId: jobId, buildNumber: null});

        // 대기 상태로 설정
        syncBuildState(jobId, {
          status: 'BUILD_WAITING',
          progress: 0,
          currentStage: '다른 빌드 완료 대기 중...',
          stages: [],
          log: '다른 빌드가 실행 중입니다. 잠시만 기다려주세요...'
        });
      }
    } else {
      await runBuild(jobId, run);
    }
  }

  async function runBuild(jobId, run) {

    isBuilding.value = run;
    id.value = jobId;
    let buildNum;

    if (run) {
      // 빌드 시작 전 대기 상태로 설정
      syncBuildState(jobId, {
        status: 'BUILD_WAITING',
        progress: 0,
        currentStage: '빌드 시작 준비 중...',
        stages: [],
        log: '빌드를 시작합니다...'
      });

      try {
        const response = await jobApi.buildJob({
          stageBuilds: [],
          jobId: jobId
        });

        buildNum = response.data?.data;
        buildNumber.value = buildNum;

        // 빌드큐에도 buildNumber 저장
        const target = buildQueue.value.find(j => j.jobId === jobId && j.buildNumber == null);
        if (target) {
          target.buildNumber = buildNum;
        } else {
          buildQueue.value.push({jobId, buildNumber: buildNum});
        }
      } catch (error) {
        syncBuildState(jobId, {
          status: 'BUILD_FAILURE',
          progress: 0,
          currentStage: '빌드 시작 실패',
          stages: [],
          log: '빌드 시작에 실패했습니다.'
        });
        isBuilding.value = false;
        return;
      }
    } else {
      buildNum = await jobApi.getCurrentBuildNumber(jobId);
      buildNumber.value = buildNum;
    }

    await jobApi.viewBuild(jobId, buildNum);
  }

  // 빌드 종료 처리
  function onBuildFinished(jobId) {

    const job = jobList.value.find(j => j.pipelineId === jobId);
    if (job && job.buildProgress) {
      job.buildProgress.isBuilding = false;
    }

    const jobDetail = jobDetails.value[jobId];
    if (jobDetail && jobDetail.buildProgress) {
      jobDetail.buildProgress.isBuilding = false;
    }

    // 현재 실행 중인 빌드가 끝났다면
    if (id.value === jobId) {
      isBuilding.value = false;
    }

    // 빌드큐에서 완료된 작업 제거
    buildQueue.value = buildQueue.value.filter(item => item.jobId !== jobId);

    // 다음 빌드 실행
    if (buildQueue.value.length > 0) {
      buildQueueTimer = setTimeout(async () => {
        const next = buildQueue.value.shift();
        if (next && next.jobId) {
          await runBuild(next.jobId, true);
        }
      }, 2000);
    }
  }

  function gettingStart(jobId) {

    syncBuildState(jobId, {
      status: 'BUILD_RUNNING',
      progress: 0,
      currentStage: 'Initializing...',
      stages: [],
      log: '빌드를 초기화하고 있습니다...'
    });

    isBuilding.value = true;
    id.value = jobId;
  }

  // 빌드 중단
  async function stopBuild(jobId) {

    syncBuildState(jobId, {
      status: 'BUILD_STOPPING',
      currentStage: '빌드 중단 중...',
      stages: [],
      log: '빌드를 중단하고 있습니다...'
    });

    try {
      await jobApi.stopBuild(jobId, buildNumber.value);

      setTimeout(() => {
        syncBuildState(jobId, {
          status: 'BUILD_ABORTED',
          currentStage: '빌드 중단됨',
          isBuilding: false,
          stages: [],
          log: '빌드가 중단되었습니다.'
        });

        if (id.value === jobId) {
          isBuilding.value = false;
        }
      }, 2000);
    } catch (error) {
      console.error('Build stop failed:', error);
      // 중단 실패 시 다시 실행 중으로 되돌림
      const currentJob = jobDetails.value[jobId];
      if (currentJob) {
        syncBuildState(jobId, {
          status: 'BUILD_RUNNING',
          currentStage: currentJob.buildProgress.currentStage,
          stages: currentJob.buildProgress.stages,
          log: currentJob.buildProgress.log + '\n빌드 중단에 실패했습니다.'
        });
      }
    }
  }

  function removeAlert(index) {
    alertMessage.value.splice(index, 1);
  }

  function clearAllAlerts() {
    alertMessage.value = [];
  }

  function findJobById(jobId) {
    return jobList.value.find(job => job.pipelineId === jobId);
  }

  // 특정 job의 빌드 상태 가져오기
  function getJobBuildState(jobId) {
    const job = jobDetails.value[jobId] || jobList.value.find(j => j.pipelineId === jobId);
    return job ? {
      buildState: job.buildState || '',
      buildProgress: job.buildProgress || initialBuildProgress(),
      progress: job.progress || 0
    } : {
      buildState: '',
      buildProgress: initialBuildProgress(),
      progress: 0
    };
  }

  return {
    // 상태
    jobDetails,
    scriptText,
    selectedItem,
    cicdItems,
    jobList,
    jenkinsInfo,
    id,
    isBuilding,
    alertMessage,
    buildQueue,
    buildNumber,

    // Computed
    currentJobDetail,
    buildProgress,

    // 함수
    fetchJobDetail,
    resetJobDetail,
    fetchJobList,
    getBuildInfo,
    connect,
    disconnect,
    gettingStart,
    stopBuild,
    removeAlert,
    clearAllAlerts,
    findJobById,
    getJobBuildState,
    syncBuildState
  };
}, {
  persist: {
    enabled: true,
    strategies: [
      {
        storage: sessionStorage,
        paths: ["jobDetails", "jobList"],
      },
    ],
  },
});