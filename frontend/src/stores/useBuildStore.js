import {defineStore} from 'pinia';
import {reactive, ref} from "vue";
import {connectWebSocket, disconnectWebSocket} from "@/websocket";
import {jobApi} from "@/api/JobApi.js";

export const useBuildStore = defineStore(
  "buildStore", () => {

    const buildNumber = ref(Number);

    const id = ref("");

    const buildProgress = reactive({
      status: '',
      stages: [],
      log: '',
      progress: 0,
      currentStage: '',
      isBuilding: false
    });

    const isBuilding = ref(false);

    const alertMessage = ref([]);

    function connect() {
      connectWebSocket((msg) => {
          const data = JSON.parse(msg);
          buildProgress.status = data.status;
          buildProgress.stages = data.stages;
          buildProgress.log = data.log;
          buildProgress.progress = data.progress || 0;
          buildProgress.currentStage = data.currentStage || '';
          buildProgress.isBuilding = data.status === 'BUILD_RUNNING';
          isBuilding.value = data.status === 'BUILD_RUNNING';
          console.log(buildProgress);
        },
        (msg) => {
          console.log(msg);
          const data = JSON.parse(msg);
          alertMessage.value.push(data);
          console.log(typeof alertMessage.value[0]); // object 인가?
        });
    }

    function disconnect() {
      disconnectWebSocket();
    }

    async function getBuildInfo(jobId) {
      id.value = jobId;
      await jobApi.viewBuild(jobId, buildNumber.value);
    }

    function gettingStart() {
      isBuilding.value = true;
      buildProgress.isBuilding = true;
      buildProgress.status = 'BUILD_RUNNING';
      buildProgress.progress = 0;
      buildProgress.currentStage = 'Initializing...';
    }

    function removeAlert(index) {
      alertMessage.value.splice(index, 1);
    }

    function clearAllAlerts() {
      alertMessage.value = [];
    }

    return {
      id,
      buildNumber,
      buildProgress,
      isBuilding,
      getBuildInfo,
      connect,
      disconnect,
      gettingStart,
      alertMessage,
      removeAlert,
      clearAllAlerts,
    }
  },
  {
    persist: {
      enabled: true,
      strategies: [
        {
          storage: sessionStorage,
          paths: ["alertMessage"],
        },
      ],
    },
  },
);