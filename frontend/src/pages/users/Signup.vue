<script setup>
import {ref, watch} from 'vue';
import {useRouter} from "vue-router";
import {userApi} from "@/api/UserApi.js";

const router = useRouter();

// form 객체
const name = ref("");
const email = ref("");
const password = ref("");
const phoneValue = ref("");
const passwordCheck = ref("");

// 이메일 관련 메시지
const emailSuccess = ref(false);
const emailSuccessMsg = ref("");

// 에러메시지
const nameError = ref("");
const emailError = ref("");
const passwordError = ref("");
const phoneError = ref("");
const passwordCheckError = ref("");

// 로그인 UI 관리
const isLoading = ref(false);

const handlePress = (e) => {
  let numbersOnly = e.target.value.replace(/\D/g, '');

  numbersOnly = numbersOnly.slice(0, 11);

  if (numbersOnly.length <= 3) {
    phoneValue.value = numbersOnly;
  } else if (numbersOnly.length <= 7) {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d+)/, '$1-$2');
  } else {
    phoneValue.value = numbersOnly.replace(/(\d{3})(\d{4})(\d+)/, '$1-$2-$3');
  }

  e.target.value = phoneValue.value;
};

watch(phoneValue, (newVal, _oldVal) => {
  if (newVal.length === 10) {
    phoneValue.value = newVal.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  } else if (newVal.length === 11) {
    phoneValue.value = newVal.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  }
});

function isValidPassword(pw) {
  // 10자 이상, 대문자 1개 이상, 특수문자 1개 이상
  return /^(?=.*[A-Z])(?=.*[!@#$%^&*()_\-+=\[\]{};':"\\|,.<>\/?]).{10,}$/.test(pw);
}

const checkEmail = () => {
  if (!email.value) {
    emailError.value = "이메일을 입력해주세요.";
    return false;
  } else if (!/^[\w-.]+@([\w-]+\.)+[\w-]{2,}$/.test(email.value)) {
    emailError.value = "올바른 이메일 주소를 입력해주세요.";
    return false;
  }
  return true;
}

const signUp = async () => {
  // 에러 초기화
  nameError.value = "";
  emailError.value = "";
  passwordError.value = "";
  phoneError.value = "";
  passwordCheckError.value = "";
  emailSuccessMsg.value = "";

  let valid = true;

  // 이름
  if (!name.value.trim()) {
    nameError.value = "이름을 입력해주세요.";
    valid = false;
  }

  if (!emailSuccess.value) {
    emailError.value = "이메일 중복확인을 해주세요.";
    valid = false;
  }

  // 이메일
  valid = checkEmail();

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

  // 전화번호
  const onlyNumberPhone = phoneValue.value.replace(/\D/g, "");
  if (!onlyNumberPhone) {
    phoneError.value = "전화번호를 입력해주세요.";
    valid = false;
  } else if (onlyNumberPhone.length < 10 || onlyNumberPhone.length > 11) {
    phoneError.value = "전화번호를 정확히 입력해주세요.";
    valid = false;
  }

  if (!valid) return;

  isLoading.value = true;

  const signUpRequest = {
    name: name.value,
    email: email.value,
    password: password.value,
    phoneNumber: phoneValue.value,
  };

  const response = await userApi.signup(signUpRequest);

  if (response.status === 200) {
    alert("회원가입 성공!\n인증 이메일이 발송되었습니다!");
    router.push({name: "Login"});
  } else {
    alert("회원가입 실패!");
    name.value = "";
    phoneValue.value = "";
    password.value = "";
    email.value = "";
    passwordCheck.value = "";
  }
  isLoading.value = false;
  emailSuccess.value = false;
};

const checkDuplicate = async () => {
  emailSuccessMsg.value = "";
  emailError.value = "";

  let valid = true;

  // 이메일
  valid = checkEmail();

  if (!valid) return;

  const response = await userApi.checkDuplicate(email.value);

  if (response.status === 200) {
    emailSuccess.value = true;
    emailSuccessMsg.value = "사용가능한 이메일입니다.";
  } else {
    emailError.value = "사용할 수 없는 이메일입니다.";
  }
};

const googleSignUp = () => {
  const popup = window.open(
      "https://www.pipely.kro.kr/oauth2/authorization/google",
      "_blank",
      "width=500,height=600"
  );
  if (!popup || popup.closed || typeof popup.closed === "undefined") {
    alert(
        "팝업이 차단되었습니다.\n브라우저 설정에서 팝업 차단을 해제해 주세요!"
    );
  }
};

const githubSignUp = () => {
  window.open(
      "https://www.pipely.kro.kr/oauth2/authorization/github",
      "_blank",
      "width=500,height=600"
  );
  if (!popup || popup.closed || typeof popup.closed === "undefined") {
    alert(
        "팝업이 차단되었습니다.\n브라우저 설정에서 팝업 차단을 해제해 주세요!"
    );
  }
};

</script>

<template>
  <div class="container">
    <div class="left_wrapper">
      <img alt="logo" src="/src/assets/images/logo.png"/>
      <h3>처음 오셨군요! 👋</h3>
      <p>
        이제부터 배포는 더 쉽고, 더 똑똑해집니다. <br/>
        당신의 DevOps 여정에 AI가 함께합니다.
      </p>
      <router-link class="btn login_btn" to="/user/login">로그인</router-link>
    </div>

    <div class="right_wrapper">
      <h1>Sign Up</h1>
      <form class="signup_box" @submit.prevent="signUp">
        <input id="name" v-model="name"
               :class="['input_box', nameError ? 'input_box--error' : '']" name="name" placeholder="이름을 입력해주세요."/>
        <p v-if="nameError" class="input-error">{{ nameError }}</p>

        <div class="email_box">
          <input id="email" v-model="email"
                 :class="['input_box', emailError ? 'input_box--error' : '', emailSuccess ? 'input_box--success' : '']"
                 :readonly="emailSuccess" name="email"
                 placeholder="이메일 주소를 입력해주세요."
                 type="email"/>
          <button :disabled="emailSuccess" class="btn" type="button" @click="checkDuplicate">중복 확인</button>
        </div>
        <p v-if="emailError" class="input-error">{{ emailError }}</p>
        <p v-if="emailSuccessMsg" class="input-success">{{ emailSuccessMsg }}</p>

        <input id="password" v-model="password"
               :class="['input_box', passwordError ? 'input_box--error' : '']" name="password"
               placeholder="비밀번호를 입력해주세요.(10자리 이상, 대문자 1개, 특수문자 1개 포함)"
               type="password"/>
        <p v-if="passwordError" class="input-error">{{ passwordError }}</p>

        <input
            id="password_check"
            v-model="passwordCheck"
            :class="['input_box', passwordCheckError ? 'input_box--error' : '']"
            name="password_check"
            placeholder="비밀번호를 다시 입력해주세요."
            type="password"
        />
        <p v-if="passwordCheckError" class="input-error">{{ passwordCheckError }}</p>

        <input
            id="phone"
            v-model="phoneValue"
            :class="['input_box', phoneError ? 'input_box--error' : '']"
            maxlength="13"
            name="phone"
            placeholder="010-1234-5678"
            type="tel"
            @input="handlePress"
        />
        <p v-if="phoneError" class="input-error">{{ phoneError }}</p>

        <button :disabled="isLoading" class="btn signup_btn" type="submit">
          {{ isLoading ? '회원가입중..' : '회원가입' }}
        </button>

        <p>또는</p>
        <button class="oauth_btn" type="button" @click="googleSignUp">
          <img alt="google" src="/src/assets/images/google_logo.png"/>
          Google로 회원가입
        </button>
        <button class="oauth_btn" type="button" @click="githubSignUp">
          <img alt="github" src="/src/assets/images/github_logo.png"/>
          Github로 회원가입
        </button>
      </form>
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
  background-color: var(--main-color-bg);
  gap: 20px;
}

.input-error {
  color: #ff5555;
  font-size: 10px;
  margin-top: 2px;
  margin-bottom: 2px;
  width: 100%;
  text-align: left;
}

.input-success {
  color: #0f8713;
  font-size: 10px;
  margin-top: 2px;
  margin-bottom: 2px;
  width: 100%;
  text-align: left;
}

.btn[disabled] {
  color: #bbb;
  cursor: not-allowed;
}

.input_box--error {
  border: 1.5px solid #ff7b7b !important;
  background-color: #fff5f5;
  transition: border-color 0.2s;
}

.input_box--success {
  border: 1.5px solid #0f8713 !important;
  background-color: #e7ffe5;
  transition: border-color 0.2s;
}

.left_wrapper > img {
  width: 200px;
  margin: 20px 0;
}

.left_wrapper > h3 {
  font-weight: 700;
}

.left_wrapper > p {
  line-height: 140%;
  margin-top: 10px;
  text-align: center;
}

.login_btn {
  width: 160px;
  margin-top: 30px;
  text-align: center;
  text-decoration: none;
  font-size: 14px;
  padding: 12px;
}

.login_btn:hover {
  background-color: var(--main-color-hover);
}

/* 오른쪽  */
.right_wrapper {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 30px;
}

.right_wrapper > h1 {
  font-size: 30px;
}

.signup_box {
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

.signup_box > p {
  font-size: 14px;
  margin: 6px 0;
}

.email_box {
  width: 100%;
  display: flex;
  gap: 5px;
}

.email_box > button {
  white-space: nowrap;
  padding: 10px 12px;
  font-size: 12px;
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

.signup_btn {
  width: 100%;
}

.oauth_btn {
  font-size: 14px;
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
</style>
