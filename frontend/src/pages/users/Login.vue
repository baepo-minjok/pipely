<script setup></script>

<template>
  <div class="container">
    <div class="left_wrapper">
      <h1>Sign In</h1>
      <div class="login_box">
        <button class="oauth_btn">
          <img src="/src/assets/images/google_logo.png" alt="google" />
          Google로 로그인
        </button>
        <button class="oauth_btn">
          <img src="/src/assets/images/github_logo.png" alt="github" />
          Github로 로그인
        </button>
        <p>또는</p>
        <input type="text" class="input_box" placeholder="이메일 주소를 입력해주세요." />
        <input type="password" class="input_box" placeholder="비밀번호를 입력해주세요." />
        <button class="login_btn">로그인</button>

        <div class="bottom_box">
          <router-link>회원가입</router-link>
          <div class="col_line"></div>
          <router-link>아이디 찾기</router-link>
          <div class="col_line"></div>
          <router-link>비밀번호 찾기</router-link>
        </div>
      </div>
    </div>
    <div class="right_wrapper"></div>
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
  gap: 46px;
}

.left_wrapper > h1 {
  font-size: 40px;
}

.login_box {
  width: 45%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 44px 42px;
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
  border-radius: 10px;
  background-color: var(--main-color);
  color: white;
  padding: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.login_btn:hover {
  background-color: #4852a8;
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
  background-color: var(--main-color-bg);
  justify-content: center;
  align-items: center;
}
</style>
