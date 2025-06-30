<script setup>
import { ref, watch } from 'vue';

const phoneValue = ref('');

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
</script>

<template>
  <div class="container">
    <div class="left_wrapper">
      <img src="/src/assets/images/logo.png" alt="logo">
      <h3>처음 오셨군요! 👋</h3>
      <p>이제부터 배포는 더 쉽고, 더 똑똑해집니다. <br />
        당신의 DevOps 여정에 AI가 함께합니다.</p>
      <router-link to="login" class="btn login_btn">로그인</router-link>
    </div>

    <div class="right_wrapper">
      <h1>Sign Up</h1>
      <form class="signup_box">
        <div class="email_box">
          <input type="email" id="email" name="email" class="input_box" placeholder="이메일 주소를 입력해주세요." />
          <button class="btn">중복 확인</button>
        </div>
        <input type="password" id="password" name="password" class="input_box" placeholder="비밀번호를 입력해주세요." />
        <input type="password" id="password_check" name="password_check" class="input_box"
          placeholder="비밀번호를 확인해주세요." />
        <input type="tel" id="phone" name="phone" class="input_box" maxlength="13" placeholder="010-1234-5678" :value="phoneValue"
          @input="handlePress" required>
        <button class="btn signup_btn">회원가입</button>
        <p>또는</p>
        <button class="oauth_btn">
          <img src="/src/assets/images/google_logo.png" alt="google" />
          Google로 로그인
        </button>
        <button class="oauth_btn">
          <img src="/src/assets/images/github_logo.png" alt="github" />
          Github로 로그인
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

.left_wrapper>img {
  width: 200px;
  margin: 20px 0;
}

.left_wrapper>h3 {
  font-weight: 700;
}

.left_wrapper>p {
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

.right_wrapper>h1 {
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

.signup_box>p {
  font-size: 14px;
  margin: 6px 0;
}

.email_box {
  width: 100%;
  display: flex;
  gap: 5px;
}

.email_box>button {
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

.oauth_btn>img {
  width: 16px;
  height: 16px;
}
</style>