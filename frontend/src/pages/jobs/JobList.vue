<script setup>
import { ref, onMounted, watch } from 'vue';
import JobCard from '../../components/jobs/JobCard.vue';
import { useRouter } from 'vue-router';

import { useJobStore } from '../../stores/jobStore.js';

const jobStore = useJobStore()

onMounted(() => {
  jobStore.getJenkinsInfo()

});
const selectedJenkins = ref('');

watch(selectedJenkins, (id) => {
  if (id) jobStore.fetchJobList(id)

})





const router = useRouter();

</script>

<template>
  <div class="container">
    <div class="header">
      <h1>Job 목록</h1>
      <button class="create_job_btn" @click="handleCreateClick">+ 새 Job 생성</button>
      <button v-if="selectedJenkins" class="create_job_btn">+ 새 Job 생성</button>
    </div>



    <div class="jenkins-select-box" style="margin-bottom: 1rem;">
      <label for="jenkins-select">Jenkins 정보 선택:</label>
      <select id="jenkins-select" v-model="selectedJenkins">
        <option value="" disabled>Jenkins 인스턴스 선택</option>
        <option v-for="info in jobStore.jenkinsInfo" :key="info.id" :value="info.id">
          {{ info.name }}
        </option>
      </select>
    </div>
    <div class="job_list">
      <template v-if="selectedJenkins">
        <JobCard v-for="job in jobStore.jobList" :key="job.name" :job="job" @click="router.push(`/job/${job.name}`)" />
      </template>
      <template v-else>
        <div style="text-align: center; color: gray; margin: 30px 0;">
          Jenkins 인스턴스를 먼저 선택하세요.
        </div>
      </template>
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

.header>h1 {
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
