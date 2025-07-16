<script setup>
const emit = defineEmits(['select']);

const onItemClick = () => {
  emit('select');
};
</script>

<template>
  <div class="dropdown_container">
    <ul class="list">
      <li><router-link to="/job" class="list_item" @click="onItemClick">Job 목록</router-link></li>
      <li><router-link to="/mypage" class="list_item" @click="onItemClick">마이페이지</router-link></li>
      <li><router-link to="/" class="list_item" @click="onItemClick">로그아웃</router-link></li>
    </ul>
  </div>
</template>

<style scoped>
.dropdown_container {
  position: absolute;
  top: 100%;
  right: 0;
  z-index: 100;
  min-width: 110px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 2px 2px 4px 2px var(--gray400);
  padding: 21px 24px;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.list_item {
  list-style: none;
  cursor: pointer;
  text-decoration: none;
  color: var(--gray900);

  &:hover {
    font-weight: 700;
  }
}
</style>
