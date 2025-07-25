<script setup>
import {ref} from "vue";
import {userApi} from "@/api/UserApi.js";
import {useRouter} from "vue-router";
import {useUserStore} from "@/stores/useUserStore.js";
import {emailApi} from "@/api/EmailApi.js";

const router = useRouter();

const userStore = useUserStore();

const email = ref("");
const password = ref("");
const emailError = ref(false);
const passwordError = ref(false);
const errorMessage = ref("");

function isValidEmail(email) {
  return /^[\w-.]+@([\w-]+\.)+[\w-]{2,}$/.test(email);
}

const reset = () => {
  emailError.value = false;
  passwordError.value = false;
  errorMessage.value = "";
}

const reactivation = async (data) => {

  email.value = "";
  password.value = "";
  reset();

  const isOk = confirm("탈퇴한 유저입니다.\n 계정을 복구하시겠습니까?");

  if (!isOk) {
    return;
  }

  const response = await userApi.reactivation(data);
  if (response) {
    alert("계정이 복구되었습니다🎉!\n 다시 로그인해주세요!");
  } else {
    alert("오류가 발생했습니다.\n 다시 시도해주세요.");
  }
}

const dormant = async (email) => {

  email.value = "";
  password.value = "";
  reset();

  const isOk = confirm("휴면 처리된 유저입니다.\n 계정을 복구하시겠습니까?");

  if (!isOk) {
    return;
  }
  const res = await emailApi.sendDormantEmail(email);
  if (res) {
    alert("재활성화 이메일이 발송되었습니다!");
  } else {
    alert("오류가 발생했습니다.\n 다시 시도해주세요");
  }
}

const login = async () => {
  reset();

  let valid = true;

  // 이메일 입력 여부 검사
  if (!email.value) {
    emailError.value = true;
    errorMessage.value = "이메일을 입력해주세요.";
    valid = false;
  } else if (!isValidEmail(email.value)) {
    emailError.value = true;
    errorMessage.value = "올바른 이메일 주소를 입력해주세요.";
    valid = false;
  } else {
    emailError.value = false;
  }

  // 비밀번호 입력 여부 검사
  if (!password.value) {
    passwordError.value = true;
    if (!errorMessage.value) errorMessage.value = "비밀번호를 입력해주세요.";
    valid = false;
  } else {
    passwordError.value = false;
  }

  if (!valid) {
    return;
  } else {
    errorMessage.value = "";
  }

  const loginRequest = {
    email: email.value,
    password: password.value,
  };

  const response = await userApi.login(loginRequest);
  try {
    if (response.status === 200) { // 로그인 성공;

      await userStore.fetchUserInfo();

      // 메인으로
      router.push({name: "Main"});

    } else if (response.status === 401) {
      if (response.code === "USER_WITHDRAWN_401") {
        await reactivation(loginRequest);
      } else if (response.code === "USER_DORMANT_401") {
        await dormant(loginRequest.email);
      } else {
        errorMessage.value = response.message;
      }
    } else {
      errorMessage.value = "로그인에 실패했습니다.";
    }
  } catch (error) {
    errorMessage.value = "로그인에 실패했습니다.";
  }
};
const googleLogin = () => {
  window.location.href = "https://www.pipely.kro.kr/oauth2/authorization/google";
};

const githubLogin = () => {
  window.location.href = "https://www.pipely.kro.kr/oauth2/authorization/github";
}

</script>

<template>
  <div class="container">
    <div class="left_wrapper">
      <h1>Sign In</h1>
      <form class="login_box" @submit.prevent="login">
        <button class="oauth_btn" type="button" @click="googleLogin">
          <img alt="google" src="/src/assets/images/google_logo.png"/>
          Google로 로그인
        </button>
        <button class="oauth_btn" type="button" @click="githubLogin">
          <img alt="github" src="/src/assets/images/github_logo.png"/>
          Github로 로그인
        </button>
        <p>또는</p>
        <input
            v-model="email"
            :class="['input_box', emailError ? 'input_box--error' : '']"
            autocomplete="username"
            placeholder="이메일 주소를 입력해주세요."
            type="text"
        />
        <input
            v-model="password"
            :class="['input_box', passwordError ? 'input_box--error' : '']"
            autocomplete="current-password"
            placeholder="비밀번호를 입력해주세요."
            type="password"
        />
        <button class="btn login_btn" type="submit">로그인</button>
        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
        <div class="bottom_box">
          <router-link to="/user/signup">회원가입</router-link>
          <div class="col_line"></div>
          <router-link to="/user/find/password">비밀번호 찾기</router-link>
        </div>
      </form>

    </div>
    <div class="right_wrapper">
      <img alt="logo" src="/src/assets/images/logo.png"/>
      <p class="text">
        이제 복잡한 배포는 그만! <br/>
        AI가 함께 하는 간편한 CI/CD를 경험해보세요.
      </p>

      <div class="chat_box">
        <div class="send_box">빌드 스크립트 작성해주세요.</div>
        <div class="rec_box">요청하신 빌드 스크립트입니다.</div>
        <div class="rec_box">
          <pre>
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/your-repo/project.git'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }
    }
    ...
}
    </pre
    >
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex;
  width: 100%;
  height: 100%;
}

/* 왼쪽 */
.left_wrapper {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 40px;
}

.left_wrapper > h1 {
  font-size: 35px;
}

.login_box {
  width: 45%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 35px;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  gap: 10px;
}

.login_box > p {
  font-size: 14px;
  margin: 6px 0;
}

.oauth_btn {
  font-size: 14.5px;
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 10px;
  width: 100%;
  background-color: white;
  cursor: pointer;
  display: flex;
  justify-content: center;
  gap: 5px;
  align-items: center;
  transition: all 0.3s;
}

.oauth_btn:hover {
  background-color: var(--main-color-bg);
}

.oauth_btn > img {
  width: 16px;
  height: 16px;
}

.input_box {
  border-radius: 10px;
  border: 1px solid var(--gray300);
  padding: 12px;
  width: 100%;
  outline: none;
  box-sizing: border-box;
}

.input_box::placeholder {
  color: var(--gray400);
}

.login_btn {
  width: 100%;
}

.bottom_box {
  display: flex;
  gap: 12px;
  margin-top: 16px;
}

.col_line {
  width: 1px;
  height: 100%;
  background-color: var(--gray300);
}

.bottom_box > a {
  font-size: 12px;
  text-decoration: none;
  color: var(--gray600);
  white-space: nowrap;
}

/* 오른쪽  */
.right_wrapper {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: var(--main-color-bg);
}

.right_wrapper > img {
  width: 200px;
  margin: 20px 0;
}

.right_wrapper > .text {
  text-align: center;
  line-height: 160%;
  margin-bottom: 40px;
}

.chat_box div {
  border-radius: 10px;
  padding: 11px 16px;
  width: fit-content;
  font-size: 14px;
  margin-bottom: 15px;
  max-width: 400px;
}

.chat_box pre {
  font-family: Consolas, 'Courier New', monospace;
  font-size: 11px;
}

.send_box {
  background-color: var(--chat-send);
  justify-self: right;
}

.rec_box {
  background-color: white;
}

.input_box--error {
  border: 1.5px solid #ff7b7b !important;
  background-color: #fff5f5;
}

.error-message {
  color: #ff5555;
  font-size: 13px;
  margin-top: 5px;
  margin-bottom: 0;
  width: 100%;
  text-align: left;
}
</style>
