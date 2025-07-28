<script setup>
import { reactive, ref, watch } from 'vue';
import { jobApi } from '@/api/JobApi';
import KubernetesInput from '@/components/jobs/KubernetesInput.vue';
import EC2Input from '@/components/jobs/EC2Input.vue';

const props = defineProps({
  jobId: String,
});

const jobDetail = reactive({
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

const selectedItem = ref(null);
const scriptText = ref('');

const cicdItems = [
  { label: 'Kubernetes', value: 'k8s', image: '/src/assets/images/k8s.png' },
  { label: 'EC2', value: 'ec2', image: '/src/assets/images/ec2.png' },
];

watch(
  () => props.jobId,
  async (id) => {
    if (!id) return;

    try {
      const response = await jobApi.getJobDetail(id);
      const data = response.data;

      if (data.success && data.data) {
        Object.assign(jobDetail, {
          pipelineId: data.data.pipelineId,
          name: data.data.name,
          description: data.data.description,
          schedule: data.data.schedule,
          lastExe: data.data.lastExe,
          stageList: data.data.stageList || [],
          notificationList: data.data.notificationList || {},
          pipelineVersionList: data.data.pipelineVersionList || [],
          lightScriptDto: {
            ...jobDetail.lightScriptDto,
            ...data.data.lightScriptDto,
          },
        });

        scriptText.value = jobDetail.lightScriptDto.script;

        if (jobDetail.lightScriptDto.isK8sDeploy) {
          selectedItem.value = cicdItems.find((item) => item.value === 'k8s');
        } else if (jobDetail.lightScriptDto.isEc2Deploy) {
          selectedItem.value = cicdItems.find((item) => item.value === 'ec2');
        }
      }
    } catch (error) {
      console.error('❌ Job Detail fetch error:', error);
    }
  },
  { immediate: true }
);
</script>

<template>
  <div class="body">
    <!-- Job 기본 정보 -->
    <div class="info_box">
      <h3 class="sub_title">Job 기본 정보</h3>
      <input class="input" disabled v-model="jobDetail.name" />
      <textarea class="textarea" disabled v-model="jobDetail.description" />
    </div>

    <!-- 연동 정보 -->
    <div class="link_box">
      <h3 class="sub_title">연동 설정</h3>
      <div class="checkbox_input">
        <span>Github</span>
        <input class="input" disabled v-model="jobDetail.lightScriptDto.githubUrl" />
      </div>
    </div>

    <!-- 스케줄 -->
    <div class="script_box">
      <h3 class="sub_title">스케줄</h3>
      <input class="input" disabled v-model="jobDetail.schedule" />
    </div>

    <!-- 스크립트 -->
    <div class="script_box">
      <h3 class="sub_title">스크립트</h3>
      <input class="input" disabled v-model="jobDetail.lightScriptDto.githubUrl" />
      <input class="input" disabled v-model="jobDetail.lightScriptDto.branch" />

      <div class="stage_group">
        <label> <input type="checkbox" :checked="jobDetail.lightScriptDto.isBuildSelected" disabled /> Build </label>
        <label> <input type="checkbox" :checked="jobDetail.lightScriptDto.isTestSelected" disabled /> Test </label>
        <label>
          <input
            type="checkbox"
            :checked="jobDetail.lightScriptDto.isK8sDeploy || jobDetail.lightScriptDto.isEc2Deploy"
            disabled
          />
          Deploy
        </label>
      </div>

      <!-- Deploy 정보 -->
      <div v-if="jobDetail.lightScriptDto.isK8sDeploy || jobDetail.lightScriptDto.isEc2Deploy" class="deploy_section">
        <div class="dropdown_container">
          <button class="dropdown">
            <div>
              <img :src="selectedItem?.image" class="dropdown_img" />
              <span class="dropdown_label">{{ selectedItem?.label }}</span>
            </div>
          </button>
        </div>

        <!-- Kubernetes 또는 EC2 컴포넌트 -->
        <KubernetesInput v-if="jobDetail.lightScriptDto.isK8sDeploy" :form="jobDetail.lightScriptDto" readonly />
        <EC2Input v-if="jobDetail.lightScriptDto.isEc2Deploy" :form="jobDetail.lightScriptDto" readonly />
      </div>

      <!-- 쉘 스크립트 -->
      <textarea class="script" readonly v-model="scriptText" />
    </div>
  </div>
</template>

<style scoped>
/* 동일한 스타일 복붙 */
.body > div {
  display: flex;
  flex-direction: column;
  border-bottom: 1px solid var(--gray200);
  padding: 34px 22px;

  &:last-child {
    border-bottom: none;
  }
}

.info_box,
.link_box,
.script_box {
  gap: 20px;
}

.input {
  width: 100%;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px solid var(--gray200);
  padding: 13px 16px;
  font-size: 16px;
  color: var(--gray900);
}

.textarea {
  background-color: #f9f9f9;
  border: 1px solid var(--gray200);
  border-radius: 8px;
  padding: 15px 17px;
  font-size: 16px;
  resize: none;
  height: 100px;
  color: var(--gray900);
}

.checkbox_input {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.deploy_section {
  border-left: 1px solid var(--gray300);
  margin-left: 10px;
  padding: 8px 0 8px 20px;
}

.stage_group label {
  display: inline-flex;
  gap: 6px;
  margin-right: 14px;
}

.dropdown_container {
  margin-bottom: 14px;
}

.dropdown {
  width: 180px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border: 1px solid var(--gray200);
  padding: 8px 12px;
  font-size: 16px;
  cursor: default;
}

.dropdown_img {
  width: 20px;
  height: 20px;
  margin-right: 6px;
}

.script {
  background-color: #f3f4f6;
  resize: none;
  height: 222px;
  border: none;
  outline: none;
  padding: 20px;
  font-family: monospace;
  font-size: 14px;
}
</style>
