<script setup>
import {useRouter} from 'vue-router';
import {onMounted, ref} from 'vue';
import {useUserStore} from "@/stores/useUserStore.js"

const userStore = useUserStore();

const router = useRouter();

const email = ref("");
const name = ref("");
const isVerified = ref(true);

const isLoading = ref(false);

const infoList = ref([]);

const fetchUser = () => {
  const userInfo = userStore.getUserInfo();

  email.value = userInfo.email;
  name.value = userInfo.name;
  isVerified.value = userInfo.isVerified;
  infoList.value = userInfo.infoList;
}

const goToChangePassword = () => {
  router.push({name: 'CheckVerification'});
};

// 로딩될때 유저 정보 세팅
onMounted(async () => {
  isLoading.value = true;  // 로딩 시작

  if (!userStore.isFetched.value) {
    fetchUser();
  } else {
    await userStore.fetchUserInfo();
    fetchUser();
  }

  isLoading.value = false;  // 로딩 종료
});
</script>

<template>
  <div class="container">
    <div class="info_wrapper">
      <h3 class="title">기본 정보</h3>
      <div class="info_box">
        <div class="item_box">
          <p class="item_label">이메일</p>

          <div class="email_text">
            <p>{{ email }}</p>
            <img v-if="isVerified" alt="check_icon" src="/src/assets/icons/check.svg"/>
            <img v-else alt="fail_icon" class="check_icon" src="/src/assets/icons/fail.svg"/>
          </div>
          <button v-if="!isVerified" class="info_btn">인증 메일 발송</button>
        </div>

        <div class="item_box">
          <p class="item_label">이름</p>
          <p>{{ name }}</p>
        </div>

        <div class="item_box">
          <p class="item_label">비밀번호</p>
          <p>••••••••••••</p>
          <button class="info_btn" @click="goToChangePassword">비밀번호 변경</button>
        </div>
      </div>
    </div>
    <div class="cicd_wrapper">
      <h3 class="title">CI/CD 정보</h3>
      <div class="cicd_header">
        <div class="dropdown_container">

        </div>
        <img
            alt="plus_btn"
            class="plus_btn"
            src="/src/assets/icons/plus_circle.svg"
            @click="router.push('/mypage/cicd/create')"
        />
      </div>
      <div class="cicd_card_list">
        <div v-for="info in infoList" class="cicd_card" @click="router.push(`/mypage/cicd/${info.id}`)">
          <p>{{ info.name }}</p>
          <p class="description">{{ info.description }}</p>
          <p>{{ info.uri }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
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

  & > p:nth-last-child(2) {
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

.description {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
