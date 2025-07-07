<script setup>
import { ref } from 'vue';

const openDropdown = ref(false);
const cicdItems = ref([
  {
    label: 'Jenkins',
    image: '/src/assets/images/jenkins.png',
  },
]);

const selectedItem = ref(cicdItems.value[0]);

function toggleDropdown() {
  openDropdown.value = !openDropdown.value;
}

function selectItem(item) {
  selectedItem.value = item;
  openDropdown.value = false;
}
</script>

<template>
  <div class="container">
    <h1>CI/CD 정보 생성</h1>
    <div class="line"></div>
    <div class="dropdown_container">
      <button type="button" @click="toggleDropdown" class="dropdown">
        <div>
          <img :src="selectedItem.image" alt="icon" class="dropdown_img" />
          <span class="dropdown_label">{{ selectedItem.label }}</span>
        </div>
        <img src="/src/assets/icons/down_arrow.svg" alt="down_icon" class="dropdown_arrow" />
      </button>

      <div v-if="openDropdown" class="dropdown_menu">
        <ul>
          <li v-for="(item, index) in cicdItems" :key="index" @click="selectItem(item)" class="dropdown_item">
            <img :src="item.image" alt="icon" class="dropdown_img" />
            <span>{{ item.label }}</span>
          </li>
        </ul>
      </div>
    </div>
    <form action="">
      <div class="input_box">
        <label for="name">정보 닉네임</label>
        <input type="text" placeholder="정보 닉네임을 입력해주세요." class="input" />
      </div>
    </form>
  </div>
</template>

<style scoped>
.container {
  width: 60%;
  margin: 70px auto;
}

.container > h1 {
  font-size: 22px;
}

.line {
  width: 100%;
  height: 1px;
  background-color: var(--gray200);
  margin: 18px 0;
}

/* 드롭다운 */
.dropdown_container {
  position: relative;
  display: inline-block;
}

.dropdown {
  width: 180px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  border: 1px solid var(--gray200);
  padding: 8px 12px;
  cursor: pointer;
  font-size: 16px;

  & > div {
    display: flex;
    align-items: center;
  }
}

.dropdown_img {
  width: 20px;
  height: 20px;
  margin-right: 6px;
}

.dropdown_label {
  white-space: nowrap;
}

.dropdown_arrow {
  width: 16px;
  height: 16px;
}

.dropdown_menu {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 100%;
  background: white;
  border: 1px solid var(--gray200);
  margin-top: 2px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  z-index: 10;
  box-sizing: border-box;
}

.dropdown_item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;

  &:hover {
    background-color: var(--gray100);
  }
}

.dropdown_item:hover {
  background: #f0f0f0;
}

form {
  display: flex;
  flex-direction: column;
  gap: 46px;
  margin: 35px 0 64px;
}

label {
  display: block;
  margin-bottom: 11px;
}

.input {
  width: 100%;
  border: 1px solid var(--gray200);
  padding: 14px 16px;
  font-size: 16px;

  &::placeholder {
    color: var(--gray400);
  }
}
</style>
