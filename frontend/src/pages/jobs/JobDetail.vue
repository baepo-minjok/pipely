<script setup>
import {computed, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import JobInfo from '@/components/jobs/JobInfo.vue';
import JobBuild from '@/components/jobs/JobBuild.vue';
import {useJobStore} from '@/stores/useJobStore';
import {jobApi} from "@/api/JobApi.js";

const route = useRoute();
const jobId = ref(route.query.id);

const selectedTab = ref('detail');
const selectedTabComponent = computed(() => (selectedTab.value === 'detail' ? JobInfo : JobBuild));

const jobStore = useJobStore();

const handleBuildRunClick = async () => {
  selectedTab.value = 'build';
  const requestBody = {
    jobId: jobId.value,
    stageBuilds: [],
  };
  const response = await jobApi.buildJob(requestBody);
  if (response.status === 200) {
    console.log('✅ 수동 실행 요청 성공');
  } else {
    console.error('❌ 수동 실행 요청 실패');
  }
};

watch(
  () => jobId.value,
  (newId) => jobStore.fetchJobDetail(newId),
  {immediate: true}
);
</script>

<template>
  <div class="container">
    <div class="header">
      <h1>Job 상세</h1>
      <div class="breadcrumb">
        <span class="breadcrumb-item">Jenkins</span>
        <span class="breadcrumb-separator">></span>
        <span class="breadcrumb-item">{{ jobStore.jobDetail.name || 'Job' }}</span>
        <span class="breadcrumb-separator">></span>
        <span class="breadcrumb-item current">상세 정보</span>
      </div>
    </div>

    <div class="layout">
      <aside class="sidebar">
        <nav class="nav-menu">
          <button
            :class="['nav-item', { active: selectedTab === 'detail' }]"
            @click="selectedTab = 'detail'"
          >
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14,2 14,8 20,8"/>
              <line x1="16" x2="8" y1="13" y2="13"/>
              <line x1="16" x2="8" y1="17" y2="17"/>
            </svg>
            상세 정보
          </button>
          <button
            :class="['nav-item', { active: selectedTab === 'build' }]"
            @click="selectedTab = 'build'"
          >
            <svg fill="none" height="20" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="20">
              <path d="M12 2L2 7l10 5 10-5-10-5z"/>
              <path d="M2 17l10 5 10-5"/>
              <path d="M2 12l10 5 10-5"/>
            </svg>
            빌드
          </button>
        </nav>
        <div class="manual-run-form">
          <button class="btn btn-primary" @click="handleBuildRunClick">
            <svg fill="none" height="16" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" width="16">
              <polygon points="5,3 19,12 5,21"/>
            </svg>
            실행하기
          </button>
        </div>
      </aside>

      <main class="content">
        <component
          :is="selectedTabComponent"
        />
      </main>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 20px auto;
  padding-left: 24px;
  padding-right: 24px;
  min-height: 100vh;
  background: #f8fafc;
}

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

.layout {
  display: flex;
  gap: 24px;
}

.sidebar {
  margin-top: 20px;
  width: 280px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  height: fit-content;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: transparent;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
  width: 100%;
}

.nav-item:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.nav-item.active {
  background: var(--main-color);
  color: white;
}

.nav-item.active svg {
  color: white;
}

.nav-item svg {
  color: #64748b;
  transition: color 0.2s ease;
}

.content {
  flex: 1;
}

.manual-run-form {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: center;
}

.manual-run-form .btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
  background: var(--main-color);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  width: 100%;
  transition: background 0.2s ease;
}

.manual-run-form .btn:hover {
  background: var(--main-color-hover);
}


@media (max-width: 768px) {
  .container {
    padding: 16px;
  }

  .layout {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .nav-menu {
    flex-direction: row;
    overflow-x: auto;
  }

  .nav-item {
    white-space: nowrap;
    min-width: fit-content;
  }
}
</style>
