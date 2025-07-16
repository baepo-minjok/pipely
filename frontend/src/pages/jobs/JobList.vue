<script setup>
import { ref } from 'vue';
import JobCard from '../../components/jobs/JobCard.vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const jobList = ref([
  {
    idx: 1,
    name: 'CI/CD Demo 01',
    createdBy: '이우진',
    lastExe: '2025-06-11T14:33:00',
    stages: [
      {
        type: 'Build',
        state: 'SUCCESS',
      },
      {
        type: 'Test',
        state: 'SUCCESS',
      },
      {
        type: 'Deploy',
        state: 'SUCCESS',
      },
    ],
    buildState: 'SUCCESS',
  },
  {
    idx: 2,
    name: 'Dev Build Flow',
    createdBy: '서찬영',
    lastExe: '2025-06-10T10:14:00',
    stages: [
      {
        type: 'Build',
        state: 'SUCCESS',
      },
      {
        type: 'Test',
        state: 'FAILED',
      },
      {
        type: 'Deploy',
        state: 'ONPROGRESS',
      },
    ],
    buildState: 'FAILED',
  },
]);
</script>

<template>
  <div class="container">
    <div class="header">
      <h1>Job 목록</h1>
      <button class="create_job_btn">+ 새 Job 생성</button>
    </div>
    <div class="job_list">
      <JobCard v-for="job in jobList" @click="router.push(`/job/${job.idx}`)" />
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 60%;
  margin: 70px auto;
}

.header {
  width: 100%;
  display: flex;
  justify-content: space-between;
}

.header > h1 {
  font-size: 28px;
}

.create_job_btn {
  background-color: var(--main-color);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  padding: 8px 18px;
  box-shadow: 2px 2px 4px 2px var(--gray400);
  transition: 0.3s;
  cursor: pointer;

  &:hover {
    background-color: var(--main-color-hover);
  }
}

.job_list {
  width: 100%;
  margin-top: 40px;
  display: flex;
  flex-direction: column;
  gap: 21px;
}
</style>
