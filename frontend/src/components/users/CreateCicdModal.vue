<script setup>
import { reactive, onMounted, onUnmounted } from 'vue';

const emit = defineEmits(['close']);

const form = reactive({
  name: '',
  url: '',
  token: '',
  id: '',
});

function close() {
  emit('close');
}

function submit() {
  console.log('폼 제출:', { ...form });
  close();
}

function handleKeyDown(e) {
  if (e.key === 'Escape') close();
}

onMounted(() => window.addEventListener('keydown', handleKeyDown));
onUnmounted(() => window.removeEventListener('keydown', handleKeyDown));
</script>

<template>
  <div class="modal-overlay" @click="close">
    <div class="modal-content" @click.stop>
      <div class="modal_header">
        <h2>CI/CD 정보 생성</h2>
        <img src="/src/assets/icons/close.svg" alt="close" @click="close" />
      </div>
      <div class="modal_line"></div>
      <form class="modal-form" @submit.prevent="submit">
        <div>
          <label>젠킨스 정보 이름</label>
          <input v-model="form.name" type="text" placeholder="예: Jenkins01" />
        </div>
        <div>
          <label>URL</label>
          <input v-model="form.url" type="text" placeholder="https://jenkins.io" />
        </div>
        <div>
          <label>Secret Token</label>
          <input v-model="form.token" type="password" placeholder="토큰 입력" />
        </div>
        <div>
          <label>ID</label>
          <input v-model="form.id" type="text" placeh older="예: user01" />
        </div>
        <button type="submit" class="modal-submit">생성하기</button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  min-width: 500px;
  width: 850px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  position: relative;
}

.modal_header {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 22px;
  box-sizing: border-box;

  & > h2 {
    flex-grow: 1;
    font-size: 20px;
    text-align: center;
  }

  & > img {
    justify-self: flex-end;
  }
}

.modal_line {
  width: 100%;
  height: 1px;
  background-color: var(--gray200);
}

/* .modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: transparent;
  border: none;
  cursor: pointer;
  
} */

.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 36px 50px;
}

.modal-form label {
  font-weight: 500;
  margin-bottom: 0.25rem;
}

.modal-form input {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.modal-submit {
  align-self: center;
  padding: 0.5rem 1rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-submit:hover {
  background: #0056b3;
}
</style>
