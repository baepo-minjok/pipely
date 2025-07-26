<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { jobApi } from '@/api/JobApi';

import JobInfo from '@/components/jobs/JobInfo.vue';
import JobBuild from '@/components/jobs/JobBuild.vue';

// 현재 선택된 탭
const selectedTab = ref('detail');

// 탭에 따른 컴포넌트 연결
const selectedTabComponent = computed(() => {
  return selectedTab.value === 'detail' ? JobInfo : JobBuild;
});

// 라우트 및 데이터 초기화
const route = useRoute();
const jobId = route.params.id;

const jobDetail = reactive({
  name: '',
  description: '',
  schedule: '',
  lightScriptDto: {
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

const scriptText = ref('');
const selectedItem = ref(null);

const cicdItems = [
  { label: 'Kubernetes', value: 'k8s', image: '/src/assets/images/k8s.png' },
  { label: 'EC2', value: 'ec2', image: '/src/assets/images/ec2.png' },
];

// API 호출
onMounted(async () => {
  const response = await jobApi.getDetail(jobId);
  const data = response.data;

  if (data.success && data.data) {
    Object.assign(jobDetail, {
      name: data.data.name,
      description: data.data.description,
      schedule: data.data.schedule,
      lightScriptDto: { ...jobDetail.lightScriptDto, ...data.data.lightScriptDto },
    });

    scriptText.value = jobDetail.lightScriptDto.script;

    if (jobDetail.lightScriptDto.isK8sDeploy) {
      selectedItem.value = cicdItems.find((item) => item.value === 'k8s');
    } else if (jobDetail.lightScriptDto.isEc2Deploy) {
      selectedItem.value = cicdItems.find((item) => item.value === 'ec2');
    }
  }
});
</script>

<template>
  <div class="layout">
    <!-- 사이드바 -->
    <aside class="sidebar">
      <ul>
        <li :class="{ active: selectedTab === 'detail' }" @click="selectedTab = 'detail'">상세 정보</li>
        <li :class="{ active: selectedTab === 'build' }" @click="selectedTab = 'build'">빌드</li>
      </ul>
    </aside>

    <!-- 탭 컴포넌트 출력 -->
    <main class="content">
      <component
        :is="selectedTabComponent"
        :jobDetail="jobDetail"
        :selectedItem="selectedItem"
        :scriptText="scriptText"
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
