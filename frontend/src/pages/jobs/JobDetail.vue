<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';

import JobInfo from '@/components/jobs/JobInfo.vue';
import JobBuild from '@/components/jobs/JobBuild.vue';

const selectedTab = ref('detail');
const selectedTabComponent = computed(() => (selectedTab.value === 'detail' ? JobInfo : JobBuild));
const route = useRoute();
const jobId = route.query.id;
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
      <component :is="selectedTabComponent" :jobId="jobId" />
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
