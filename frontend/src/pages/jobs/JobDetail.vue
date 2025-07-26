<script setup>
import { reactive, ref, onMounted } from 'vue';
import KubernetesInput from '../../components/jobs/KubernetesInput.vue';
import EC2Input from '../../components/jobs/EC2Input.vue';

const route = useRoute();
const jobId = route.params.id;

onMounted(async () => {
  const response = await jobApi.getJobDetail(jobId);
  console.log('Job Detail:', response.data);
});

// mock 데이터 – 실제로는 API로 받아야 함
const jobDetail = {
  name: 'sample-job',
  description: '테스트 Job',
  schedule: '매일 오후 12시 30분',
  lightScriptDto: {
    scriptId: '0c6fd9ad-991c-4e62-abe7-723b4be4a57e',
    githubUrl: 'https://github.com/org/repo.git',
    branch: 'main',
    isBuildSelected: true,
    isTestSelected: false,
    isK8sDeploy: true,
    isEc2Deploy: false,
    tag: 'latest',
    sshKeyPath: '~/.ssh/id_rsa',
    sshPort: '22',
    deployTarget: 'ubuntu@1.2.3.4',
    k8sPath: '/home/ubuntu/app/deploy.yaml',
    deploymentName: 'my-app-deployment',
    namespace: 'default',
    appName: 'my-app',
    containerName: 'my-container',
    imageRepo: 'ghcr.io/org/project',
    port: '8080',
    replicas: '2',
    script: '#!/bin/bash\necho Hello World',
  },
};

const cicdItems = [
  { label: 'Kubernetes', value: 'k8s', image: '/src/assets/images/k8s.png' },
  { label: 'EC2', value: 'ec2', image: '/src/assets/images/ec2.png' },
];

const selectedItem = ref(jobDetail.lightScriptDto.isK8sDeploy ? cicdItems[0] : cicdItems[1]);

const scriptData = reactive({ ...jobDetail.lightScriptDto });
const scriptText = ref(jobDetail.lightScriptDto.script);
</script>

<template>
  <div class="container">
    <h1>Job 상세 정보</h1>
    <div class="body">
      <!-- Job 기본 정보 -->
      <div class="info_box">
        <h3 class="sub_title">Job 기본 정보</h3>
        <input class="input" disabled :value="jobDetail.name" />
        <textarea class="textarea" disabled>{{ jobDetail.description }}</textarea>
      </div>

      <!-- 연동 정보 -->
      <div class="link_box">
        <h3 class="sub_title">연동 설정</h3>
        <div class="checkbox_input">
          <span>Github</span>
          <input class="input" disabled :value="scriptData.githubUrl" />
        </div>
      </div>

      <!-- 스케줄 -->
      <div class="script_box">
        <h3 class="sub_title">스케줄</h3>
        <input class="input" disabled :value="jobDetail.schedule" />
      </div>

      <!-- 스크립트 -->
      <div class="script_box">
        <h3 class="sub_title">스크립트</h3>
        <input class="input" disabled :value="scriptData.githubUrl" />
        <input class="input" disabled :value="scriptData.branch" />

        <div class="stage_group">
          <label><input type="checkbox" checked disabled /> Build</label>
          <label><input type="checkbox" :checked="scriptData.isTestSelected" disabled /> Test</label>
          <label
            ><input type="checkbox" :checked="scriptData.isK8sDeploy || scriptData.isEc2Deploy" disabled />
            Deploy</label
          >
        </div>

        <div v-if="scriptData.isK8sDeploy || scriptData.isEc2Deploy" class="deploy_section">
          <div class="dropdown_container">
            <button class="dropdown">
              <div>
                <img :src="selectedItem.image" class="dropdown_img" />
                <span class="dropdown_label">{{ selectedItem.label }}</span>
              </div>
            </button>
          </div>

          <KubernetesInput v-if="scriptData.isK8sDeploy" :form="scriptData" readonly />
          <EC2Input v-if="scriptData.isEc2Deploy" :form="scriptData" readonly />
        </div>

        <textarea class="script" readonly>{{ scriptText }}</textarea>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 동일한 스타일 복붙 */
.container {
  width: 60%;
  margin: 70px auto;
}

.container > h1 {
  font-size: 28px;
  margin-bottom: 6px;
}

.body > div {
  display: flex;
  flex-direction: column;
  padding: 34px 22px;
  border-bottom: 1px solid var(--gray200);
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
}

.textarea {
  background-color: #f9f9f9;
  border: 1px solid var(--gray200);
  border-radius: 8px;
  padding: 15px 17px;
  font-size: 16px;
  resize: none;
  height: 100px;
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
