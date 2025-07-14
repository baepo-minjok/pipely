<script setup>
import { reactive } from 'vue';

const job = reactive({
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
});
</script>

<template>
  <div class="container">
    <div class="header">
      <h3>{{ job.name }}</h3>
      <div class="state_box" :class="{ success: job.buildState === 'SUCCESS' }">
        <img v-if="job.buildState === 'SUCCESS'" src="/src/assets/icons/check.svg" alt="success" />
        성공
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
  background-color: white;
  padding: 27px;
  border-radius: 10px;
  border: 1px solid var(--gray200);
}

.header {
  display: flex;
  justify-content: space-between;
}

.state_box {
  display: flex;
  align-items: center;
  border-radius: 6px;
  padding: 5px 9px;
}

.state_box.success {
  background-color: var(--green-bg);
  color: var(--green-text);
}
</style>
