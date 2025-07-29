<script setup>
import { ref, reactive, computed, watch } from 'vue';
import { useRoute } from 'vue-router';

import JobInfo from '@/components/jobs/JobInfo.vue';
import JobBuild from '@/components/jobs/JobBuild.vue';
import { jobApi } from '@/api/JobApi';

const selectedTab = ref('detail');
const selectedTabComponent = computed(() => (selectedTab.value === 'detail' ? JobInfo : JobBuild));

const route = useRoute();
const jobId = ref(route.query.id);

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

const fetchJobDetail = async (id) => {
  if (!id) return;
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
    console.error('❌ jobDetail fetch error:', e);
  }
};

watch(
  () => jobId.value,
  (newId) => fetchJobDetail(newId),
  { immediate: true }
);
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <ul>
        <li :class="{ active: selectedTab === 'detail' }" @click="selectedTab = 'detail'">상세 정보</li>
        <li :class="{ active: selectedTab === 'build' }" @click="selectedTab = 'build'">빌드</li>
      </ul>
    </aside>

    <main class="content">
      <component
        :is="selectedTabComponent"
        :jobDetail="jobDetail"
        :scriptText="scriptText"
        :selectedItem="selectedItem"
        :jobId="jobId"
      />
    </main>
  </div>
</template>
<style scoped>
.layout {
  display: flex;
  width: 80%;
  margin: 70px auto;
}

.sidebar {
  width: 200px;
  padding: 20px;
  border-right: 1px solid #ddd;
}

.sidebar ul {
  list-style: none;
  padding: 0;
}

.sidebar li {
  padding: 18px;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
}

.sidebar li.active {
  background-color: var(--gray100);
  font-weight: bold;
}

.content {
  flex: 1;
  padding: 10px 50px;
}
</style>
