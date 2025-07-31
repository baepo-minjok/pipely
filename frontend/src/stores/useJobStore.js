import {defineStore} from 'pinia';
import {reactive, ref} from 'vue';
import {jobApi} from '@/api/JobApi';

export const useJobStore = defineStore('job', () => {
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
    });

    const jobDetail = reactive(initialJobDetail());
    const selectedItem = ref(null);
    const scriptText = ref('');

    const jobList = ref([]);
    const jenkinsInfo = ref([]);

    const cicdItems = [
      {label: 'Kubernetes', value: 'k8s', image: '/src/assets/images/k8s.png'},
      {label: 'EC2', value: 'ec2', image: '/src/assets/images/ec2.png'},
    ];
    const resetJobDetail = () => {
      Object.assign(jobDetail, initialJobDetail());
      selectedItem.value = null;
      scriptText.value = '';
    };
    const fetchJobDetail = async (id) => {
      if (!id) return;

      if (jobDetail.pipelineId === id) {
        return;
      }

      try {
        const response = await jobApi.getJobDetail(id);
        const data = response.data;
        if (data.success && data.data) {
          Object.assign(jobDetail, {
            ...jobDetail,
            ...data.data,
            lightScriptDto: {
              ...jobDetail.lightScriptDto,
              ...data.data.lightScriptDto,
            },
          });

          scriptText.value = data.data.lightScriptDto?.script ?? '';

          if (data.data.lightScriptDto?.isK8sDeploy) {
            selectedItem.value = cicdItems.find((item) => item.value === 'k8s');
          } else if (data.data.lightScriptDto?.isEc2Deploy) {
            selectedItem.value = cicdItems.find((item) => item.value === 'ec2');
          }
        }
      } catch (e) {
        console.error(' jobDetail fetch error:', e);
      }
    };

    return {
      jobDetail,
      scriptText,
      selectedItem,
      cicdItems,
      fetchJobDetail,
      jobList,
      jenkinsInfo,
      resetJobDetail,
    };
  },
  {
    persist: {
      enabled: true,
      strategies: [
        {
          storage: sessionStorage,
          paths: ["jobDetail"],
        },
      ],
    },
  },
);
