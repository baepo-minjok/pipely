<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import Dropdown from './Dropdown.vue';

const router = useRouter();
const showMenu = ref(false);

const goToHome = () => {
  router.push('/');
}

const handleMenuClick  = () => {
  showMenu.value = !showMenu.value;
}

// 드롭다운 영역을 참조할 ref
const profileWrapper = ref(null);

// 바깥 클릭 감지 함수
const handleClickOutside = (event) => {
  if (profileWrapper.value && !profileWrapper.value.contains(event.target)) {
    showMenu.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside);
});

</script>

<template>
  <div class="container">
    <img src="/src/assets/images/logo.png" alt="pipely" class="logo_img" @click="goToHome()">
    <div class="profile_wrapper" ref="profileWrapper">
      <img src="/src/assets/icons/profile.svg" alt="profile" class="profile_icon" @click="handleMenuClick()">
      <Dropdown v-if="showMenu" />
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
  height: 90px;
  max-height: 90px;
  background-color: var(--main-color-bg);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 100px;
  padding: 0 170px;
  box-sizing: border-box;
  box-shadow: 0px 2px 10px 0px rgba(0, 0, 0, 0.25);
}

.logo_img {
  width: 130px;
  height: fit-content;
  cursor: pointer;
}

.profile_icon {
  width: 30px;
  height: 30px;
  cursor: pointer;
}

.profile_wrapper {
  position: relative;
}
</style>