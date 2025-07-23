<script setup>

import {userApi} from "@/api/UserApi.js";
import {useUserStore} from "@/stores/useUserStore.js"
import {useRouter} from 'vue-router';

const router = useRouter();

const userStore = useUserStore();

const emit = defineEmits(['select']);

const onItemClick = () => {
  emit('select');
};

const onLogoutClick = async () => {

  userStore.reset();

  await userApi.logout();

  router.push('/user/login');
  //emit('select');
};
</script>

<template>
  <div class="dropdown_container">
    <ul class="list">
      <li>
        <router-link class="list_item" to="/job" @click="onItemClick">Job 목록</router-link>
      </li>
      <li>
        <router-link class="list_item" to="/mypage" @click="onItemClick">마이페이지</router-link>
      </li>
      <li>
        <router-link v-if="userStore.isFetched" class="list_item" to="/" @click="onLogoutClick">로그아웃</router-link>
        <router-link v-else class="list_item" to="/user/login">로그인</router-link>
      </li>
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
