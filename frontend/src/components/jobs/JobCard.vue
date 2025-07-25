<script setup>
import {  toRefs } from 'vue';
import { formatDateTime } from '@/utils/formatDateTime.js';


const props = defineProps({
  job: {
    type: Object,
    required: true
  }

})
const { job } = toRefs(props)

// const job = ({
//   idx: 1,
//   name: 'CI/CD Demo 01',
//   createdBy: '이우진',
//   lastExe: '2025-06-11T14:33:00',
//   stages: [
//     {
//       type: 'Build',
//       state: 'SUCCESS',
//     },
//     {
//       type: 'Test',
//       state: 'SUCCESS',
//     },
//     {
//       type: 'Deploy',
//       state: 'SUCCESS',
//     },
//   ],
//   buildState: 'SUCCESS',
// });



</script>

<template>
  <div class="card_container">
    <div class="header">
      <h3>{{ job.name }}</h3>
      <div class="state_box" :class="{ success: job.buildState === 'SUCCESS' }">
        <img v-if="job.buildState === 'SUCCESS'" src="/src/assets/icons/check.svg" alt="success" />
        성공
      </div>
    </div>

    <div class="text_box">
      <p>실행자 : {{ job.createdBy }}</p>
      <p>·</p>
      <p>마지막 실행 : {{ formatDateTime(job.lastExe) }}</p>
    </div>

    <div class="description_box">
      <p>설명 : {{ job.description || '설명이 없습니다.' }}</p>
    </div>
    <button class="start_btn" :class="{ restart: job.buildState === 'SUCCESS' }">
      {{ job.buildState === 'SUCCESS' ? '재실행' : job.buildState === 'FAILED' ? '재시도' : '지금 실행' }}
    </button>
  </div>
</template>

<style scoped>
.card_container {
  width: 100%;
  min-width: 360px;
  background-color: white;
  padding: 27px;
  border-radius: 10px;
  border: 1px solid var(--gray200);
  box-sizing: border-box;
  transition: scale 0.3s;
  cursor: pointer;

  &:hover {
    scale: 1.03;
  }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header>h3 {
  font-size: 20px;
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

.text_box {
  display: flex;
  gap: 10px;
  color: var(--gray600);
  margin-top: 5px;
  font-size: 14px;
}

.stage_box {
  display: flex;
  align-items: center;
  gap: 15px;
  margin: 12px 0;
  font-size: 14px;

  &>div {
    display: flex;
    align-items: center;
    gap: 5px;
  }
}

.start_btn {
  border-radius: 6px;
  border: none;
  padding: 9px 18px;
  font-size: 16px;
  transition: all 0.3s;
  cursor: pointer;

  &.restart {
    background-color: var(--gray200);
    color: var(--gray700);
  }

  &.restart:hover {
    background-color: var(--gray300);
  }
}
</style>
