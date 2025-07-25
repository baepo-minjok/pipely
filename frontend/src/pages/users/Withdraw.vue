<script setup>
import {onMounted, ref} from 'vue';
import {userApi} from "@/api/UserApi.js";

const isSuccess = ref(true);

onMounted(async () => {

  const response = await userApi.withdraw();

  if (response) {
    await userApi.logout();
  } else {
    isSuccess.value = false;
  }
});
</script>


<template>
  <div class="container">
    <img alt="logo" src="/src/assets/images/logo.png"/>
    <div v-if="isSuccess" class="send_email_box">
      <img alt="check" src="/src/assets/icons/check.svg"/>
      <p>회원 탈퇴가 완료되었습니다. <br> 10일 이내에는 다시 로그인하면 복구가 가능합니다.</p>
      <router-link class="btn login_btn" to="/user/login">로그인 화면으로</router-link>
    </div>
    <div v-if="!isSuccess" class="send_email_box">
      <img alt="check" src="/src/assets/icons/fail.svg"/>
      <p>회원 탈퇴가 실패했습니다. <br/>다시 시도해주세요.</p>
      <router-link class="btn login_btn" to="/user/login">로그인 화면으로</router-link>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  gap: 30px;
}

.container > img {
  width: 150px;
}

.container > h1 {
  font-size: 25px;
}

.send_email_box {
  width: 32%;
  min-width: 380px;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 40px 35px;
  text-align: center;
  line-height: 150%;
}

.send_email_box > img {
  width: 60px;
  margin-bottom: 10px;
}

.login_btn {
  display: block;
  width: 100%;
  margin-top: 40px;
  text-decoration: none;
  box-sizing: border-box;
}
</style>
