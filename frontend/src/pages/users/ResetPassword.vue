<script setup>
import {useRoute} from 'vue-router'
import {emailApi} from "@/api/EmailApi.js";
import {ref, watch} from "vue";

const route = useRoute();

const sendApi = ref(false);
const successApi = ref(false);
const password = ref("");
const passwordCheck = ref("");

const passwordError = ref("");
const passwordCheckError = ref("");
const passwordMatchSuccess = ref(false);

watch([password, passwordCheck], () => {
  passwordError.value = "";
  passwordCheckError.value = "";

  passwordMatchSuccess.value =
      !!password.value &&
      !!passwordCheck.value &&
      password.value === passwordCheck.value &&
      isValidPassword(password.value);
});

function isValidPassword(pw) {
  // 10자 이상, 대문자 1개 이상, 특수문자 1개 이상
  return /^(?=.*[A-Z])(?=.*[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]).{10,}$/.test(pw);
}

const resetPassword = async () => {

  passwordError.value = "";
  passwordCheckError.value = "";

  let valid = true;

  // 비밀번호
  if (!password.value) {
    passwordError.value = "비밀번호를 입력해주세요.";
    valid = false;
  } else if (!isValidPassword(password.value)) {
    passwordError.value = "비밀번호는 10자 이상, 대문자와 특수문자를 각각 1개 이상 포함해야 합니다.";
    valid = false;
  }

  // 비밀번호 확인
  if (!passwordCheck.value) {
    passwordCheckError.value = "비밀번호 확인을 입력해주세요.";
    valid = false;
  } else if (passwordCheck.value !== password.value) {
    passwordCheckError.value = "비밀번호가 일치하지 않습니다.";
    valid = false;
  }

  if (!valid) return;

  const token = route.query.token;
  const data = {
    token: token,
    newPassword: password.value,
  }

  const response = await emailApi.resetPassword(data);

  sendApi.value = true;

  if (response) {
    successApi.value = true;
  } else {
    successApi.value = false;
  }
}
</script>


<template>
  <div class="container">
    <img alt="logo" src="/src/assets/images/logo.png"/>
    <h1>비밀번호 재설정</h1>
    <form v-if="!sendApi" action="" class="form_box">
      <p>새로운 비밀번호를 입력해주세요.</p>
      <input
          id="password"
          v-model="password"
          :class="['input_box', passwordError ? 'input_box--error' : '']"
          name="password"
          placeholder="새로운 비밀번호를 입력해주세요."
          type="password"
      />
      <p v-if="passwordError" class="input-error">{{ passwordError }}</p>
      <input
          id="password_check"
          v-model="passwordCheck"
          :class="[
              'input_box',passwordCheckError ?
              'input_box--error' : passwordMatchSuccess ?
              'input_box--success' : '']"
          name="password_check"
          placeholder="비밀번호를 다시 입력해주세요."
          type="password"
      />
      <p v-if="passwordCheckError" class="input-error">{{ passwordCheckError }}</p>
      <p v-else-if="passwordMatchSuccess" class="input-success">비밀번호가 일치합니다.</p>
      <button class="btn find_btn" type="button" @click="resetPassword">변경</button>
    </form>
    <div v-if="sendApi">
      <div v-if="successApi" class="send_email_box">
        <img alt="check" src="/src/assets/icons/check.svg"/>
        <p>비밀번호가 재설정되었습니다. <br/>다시 로그인해주세요.</p>
        <router-link class="btn login_btn" to="/user/login">로그인 화면으로</router-link>
      </div>
      <div v-if="!successApi" class="send_email_box">
        <img alt="check" src="/src/assets/icons/fail.svg"/>
        <p>비밀번호 재설정이 실패했습니다. <br/>다시 시도해주세요.</p>
        <router-link class="btn login_btn" to="/user/login">로그인 화면으로</router-link>
      </div>
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

.form_box {
  width: 32%;
  min-width: 380px;
  display: flex;
  flex-direction: column;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 40px 35px;
  gap: 10px;
}

.form_box > p {
  font-size: 16px;
  align-self: center;
  line-height: 140%;
  margin-bottom: 20px;
}

.input_box {
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 12px;
  width: 100%;
  outline: none;
  box-sizing: border-box;
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


.input-error {
  color: #ff5555;
  font-size: 10px !important;
  margin-top: 2px;
  width: 100%;
  text-align: left;
}

.input_box--error {
  border: 1.5px solid #ff7b7b !important;
  background-color: #fff5f5;
  transition: border-color 0.2s;
}

.input-success {
  color: #0f8713;
  font-size: 10px !important;
  margin-top: 2px;
  margin-bottom: 2px;
  width: 100%;
  text-align: left;
}

.input_box--success {
  border: 1.5px solid #0f8713 !important;
  background-color: #e7ffe5;
  transition: border-color 0.2s;
}
</style>
