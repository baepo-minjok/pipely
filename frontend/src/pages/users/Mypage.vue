<script setup>
import Header from '../../components/common/Header.vue';
import { ref } from 'vue';

const openDropdown = ref(false);

const cicdItems = ref([
  {
    label: 'Jenkins',
    image: '/src/assets/images/jenkins.png',
  },
  {
    label: 'GitLab',
    image: 'https://about.gitlab.com/images/press/logo/png/gitlab-icon-rgb.png',
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
    <Header></Header>
    <div class="wrapper">
      <div class="info_wrapper">
        <h3 class="title">기본 정보</h3>
        <div class="info_box">
          <div class="item_box">
            <p class="item_label">이메일</p>

            <div class="email_text">
              <p>test01@example.com</p>
              <img src="/src/assets/icons/check.svg" alt="check_icon" />
            </div>
            <button class="info_btn">인증 메일 발송</button>
          </div>

          <div class="item_box">
            <p class="item_label">이름</p>
            <p>test01</p>
          </div>
          <div class="item_box">
            <p class="item_label">비밀번호</p>
            <p>••••••••••••</p>
            <button class="info_btn">비밀번호 변경</button>
          </div>
        </div>
      </div>
      <div class="cicd_wrapper">
        <h3 class="title">CI/CD 정보</h3>
        <div class="cicd_header">
          <div class="dropdown_container">
            <button @click="toggleDropdown" class="dropdown">
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
          <img src="/src/assets/icons/plus_circle.svg" alt="plus_btn" class="plus_btn" />
        </div>
        <div class="cicd_card_list">
          <div class="cicd_card">
            <p>젠킨스 01</p>
            <p>https://jenkins.io/abcdefghehe</p>
          </div>
          <div class="cicd_card">
            <p>젠킨스 01</p>
            <p>https://jenkins.io/abcdefghehe</p>
          </div>
          <div class="cicd_card">
            <p>젠킨스 01</p>
            <p>https://jenkins.io/abcdefghehe</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  width: 100%;
}

.wrapper {
  width: 60%;
  margin: 70px auto;
}

.title {
  font-size: 22px;
  font-weight: 500;
  color: #4a5464;
  margin-bottom: 18px;
}

/* 기본 정보 */

.info_box {
  background-color: white;
  border-radius: 12px;
  border: 1px solid var(--gray200);
  box-shadow: 2px 2px 4px 0px var(--gray400);
  padding: 25px 35px;
}

.item_box {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 4fr 1fr;
  grid-row-gap: 20px;
  align-items: center;
  padding: 0 10px;
  box-sizing: border-box;
}

.item_box:not(:last-of-type) {
  border-bottom: 1px solid var(--gray200);
  padding-bottom: 22px; /* 아래 여백 */
  margin-bottom: 22px; /* 선과 다음 요소 사이 여백 */
}

.item_label {
  color: var(--gray500);
  white-space: nowrap;
}

.email_text {
  display: flex;
  align-items: center;
  gap: 5px;
}

.info_btn {
  background: none;
  color: var(--main-color);
  border: none;
  white-space: nowrap;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

/* ci/cd 정보 */
.cicd_wrapper {
  margin-top: 76px;
}

.cicd_header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.plus_btn {
  width: 26px;
  cursor: pointer;
}

.cicd_card_list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 19px;
  padding: 28px;
  background-color: var(--gray100);
  margin-top: 10px;
}

.cicd_card {
  border-radius: 12px;
  border: 1px solid var(--gray200);
  background-color: white;
  padding: 32px;
  word-break: break-all;
  cursor: pointer;
  transition: scale 0.3s;

  & > p:last-child {
    margin-top: 17px;
    color: var(--gray500);
    font-size: 14px;
  }

  &:hover {
    scale: 1.05;
  }
}

.cicd_card > p:last-child {
  margin-top: 17px;
  color: var(--gray500);
  font-size: 14px;
}
</style>
